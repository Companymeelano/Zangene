<?php

namespace App\Http\Controllers\Customer;

use App\Http\Controllers\Controller;
use App\Models\Coupon;
use App\Models\CouponUsage;
use App\Models\Customer;
use App\Models\Inventory;
use App\Models\PaymentGateway;
use App\Models\ProductVariant;
use App\Models\ShippingMethod;
use App\Models\StockMovement;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class CheckoutController extends Controller
{
    public function processCheckout(Request $request)
    {
        $validated = $request->validate([
            'items' => ['required', 'array', 'min:1', 'max:50'],
            'items.*.variant_id' => ['required', 'integer', 'distinct', 'exists:product_variants,id'],
            'items.*.quantity' => ['required', 'integer', 'min:1', 'max:50'],
            'address_id' => ['required', 'integer', 'exists:customer_addresses,id'],
            'shipping_method_id' => ['required', 'integer', 'exists:shipping_methods,id'],
            'payment_gateway_id' => ['nullable', 'integer', 'exists:payment_gateways,id'],
            'payment_type' => ['nullable', 'in:online,installment'],
            'coupon_code' => ['nullable', 'string', 'max:50'],
            'use_gold_wallet' => ['nullable', 'boolean'],
            'is_vip_gift_box' => ['nullable', 'boolean'],
            'scented_tissue' => ['nullable', 'string', 'max:60'],
            'gift_card_message' => ['nullable', 'string', 'max:400'],
            'complimentary_samples' => ['nullable', 'array', 'max:2'],
            'cold_chain_packaging' => ['nullable', 'boolean'],
            'notes' => ['nullable', 'string', 'max:500'],
        ]);

        $user = $request->user();
        $customer = Customer::firstOrCreate(['user_id' => $user->id]);

        // Prevent IDOR: Verify address belongs strictly to authenticated customer
        $address = $customer->addresses()->where('id', $validated['address_id'])->first();
        if (!$address) {
            return response()->json(['error' => 'آدرس انتخاب شده متعلق به حساب کاربری شما نیست.'], 403);
        }

        $shippingMethod = ShippingMethod::where('id', $validated['shipping_method_id'])
            ->where('is_active', true)
            ->firstOrFail();

        $paymentType = $validated['payment_type'] ?? 'online';
        $gateway = null;
        if ($paymentType === 'online') {
            if (empty($validated['payment_gateway_id'])) {
                return response()->json(['error' => 'لطفا درگاه پرداخت را انتخاب کنید.'], 422);
            }
            $gateway = PaymentGateway::where('id', $validated['payment_gateway_id'])
                ->where('is_active', true)
                ->firstOrFail();
        }

        try {
            $result = DB::transaction(function () use ($validated, $customer, $address, $shippingMethod, $gateway, $paymentType) {
                $subtotal = 0.0;
                $itemsData = [];
                $lockedInventories = [];

                // Sort variant IDs deterministically to prevent database deadlocks on concurrent checkouts
                $sortedItems = collect($validated['items'])->sortBy('variant_id')->values();

                foreach ($sortedItems as $item) {
                    $variant = ProductVariant::with('product')->findOrFail($item['variant_id']);
                    if (!$variant->is_active || !$variant->product || !$variant->product->published) {
                        throw ValidationException::withMessages([
                            'items' => ["محصول «{$variant->product?->name_fa}» در حال حاضر غیرفعال است."],
                        ]);
                    }

                    // Pessimistic row-level lock prevents overselling race conditions
                    $inventory = Inventory::where('product_variant_id', $variant->id)
                        ->lockForUpdate()
                        ->first();

                    if (!$inventory || $inventory->quantity < $item['quantity']) {
                        throw ValidationException::withMessages([
                            'items' => ["موجودی کافی برای «{$variant->product->name_fa}» وجود ندارد."],
                        ]);
                    }

                    $unitPrice = $variant->effectivePrice();
                    $subtotal += $unitPrice * $item['quantity'];

                    $itemsData[] = [
                        'product_variant_id' => $variant->id,
                        'quantity' => $item['quantity'],
                        'price_at_purchase' => $unitPrice,
                    ];

                    $lockedInventories[] = [$inventory, $item['quantity']];
                }

                // Server-side Coupon Evaluation with row lock
                $discountAmount = 0.0;
                $coupon = null;
                if (!empty($validated['coupon_code'])) {
                    $coupon = Coupon::where('code', strtoupper(trim($validated['coupon_code'])))
                        ->lockForUpdate()
                        ->first();

                    if (!$coupon) {
                        throw ValidationException::withMessages(['coupon_code' => ['کد تخفیف یافت نشد.']]);
                    }

                    $customerUses = CouponUsage::where('coupon_id', $coupon->id)
                        ->where('customer_id', $customer->id)
                        ->count();

                    if ($coupon->usage_per_customer !== null && $customerUses >= $coupon->usage_per_customer) {
                        throw ValidationException::withMessages(['coupon_code' => ['سقف استفاده شما از این کد تخفیف تکمیل شده است.']]);
                    }

                    $discountAmount = $coupon->calculateDiscount($subtotal);
                    if ($discountAmount <= 0) {
                        throw ValidationException::withMessages(['coupon_code' => ['شرایط استفاده از کد تخفیف (حداقل خرید یا تاریخ اعتبار) برقرار نیست.']]);
                    }
                }

                // Apply ASIYE Gold Cashback Wallet balance if requested
                $afterCoupon = max(0.0, $subtotal - $discountAmount);
                $walletDeduction = 0.0;
                $lockedCustomer = Customer::where('id', $customer->id)->lockForUpdate()->first();
                if (!empty($validated['use_gold_wallet']) && $lockedCustomer && $lockedCustomer->wallet_balance > 0) {
                    $walletDeduction = min((float) $lockedCustomer->wallet_balance, $afterCoupon);
                }
                $afterDiscounts = max(0.0, $afterCoupon - $walletDeduction);

                // Calculate Shipping Cost (Free above luxury threshold)
                $freeThreshold = (float) env('FREE_SHIPPING_THRESHOLD_TOMAN', 3000000);
                $shippingCost = $afterDiscounts >= $freeThreshold
                    ? 0.0
                    : (float) $shippingMethod->base_cost;

                $totalAmount = max(0, $afterDiscounts + $shippingCost);
                $cashbackRate = ($lockedCustomer?->tier === 'black_diamond') ? 0.07 : (($lockedCustomer?->tier === 'gold') ? 0.05 : 0.03);
                $cashbackEarned = round($totalAmount * $cashbackRate);
                $orderNumber = 'AS-' . now()->format('Ymd') . '-' . strtoupper(substr(bin2hex(random_bytes(3)), 0, 6));

                $order = $customer->orders()->create([
                    'order_number' => $orderNumber,
                    'address_id' => $address->id,
                    'shipping_method_id' => $shippingMethod->id,
                    'payment_gateway_id' => $gateway?->id,
                    'coupon_id' => $coupon?->id,
                    'subtotal_amount' => $subtotal,
                    'shipping_cost' => $shippingCost,
                    'discount_amount' => $discountAmount,
                    'wallet_deduction' => $walletDeduction,
                    'total_amount' => $totalAmount,
                    'cashback_earned' => $cashbackEarned,
                    'status' => 'pending_payment',
                    'payment_type' => $paymentType,
                    'is_vip_gift_box' => $validated['is_vip_gift_box'] ?? true,
                    'scented_tissue' => $validated['scented_tissue'] ?? 'damask_rose_oud',
                    'gift_card_message' => $validated['gift_card_message'] ?? null,
                    'complimentary_samples' => !empty($validated['complimentary_samples'])
                        ? json_encode($validated['complimentary_samples'], JSON_UNESCAPED_UNICODE)
                        : null,
                    'cold_chain_packaging' => $validated['cold_chain_packaging'] ?? true,
                    'notes' => $validated['notes'] ?? null,
                ]);

                // Update Gold Cashback Wallet Ledger & Mint Digital Authenticity Certificate
                if ($lockedCustomer) {
                    $newBalance = ((float) $lockedCustomer->wallet_balance - $walletDeduction) + $cashbackEarned;
                    $lockedCustomer->update(['wallet_balance' => $newBalance]);
                    if ($walletDeduction > 0) {
                        DB::table('wallet_transactions')->insert([
                            'customer_id' => $lockedCustomer->id,
                            'order_id' => $order->id,
                            'amount' => -$walletDeduction,
                            'type' => 'order_redemption',
                            'description' => "کسر کیف پول طلایی بابت سفارش {$order->order_number}",
                            'balance_after' => (float) $lockedCustomer->wallet_balance + $walletDeduction - $walletDeduction,
                            'created_at' => now(),
                            'updated_at' => now(),
                        ]);
                    }
                    if ($cashbackEarned > 0) {
                        DB::table('wallet_transactions')->insert([
                            'customer_id' => $lockedCustomer->id,
                            'order_id' => $order->id,
                            'amount' => $cashbackEarned,
                            'type' => 'cashback_reward',
                            'description' => "پاداش کش‌بک طلایی بابت سفارش {$order->order_number}",
                            'balance_after' => $newBalance,
                            'created_at' => now(),
                            'updated_at' => now(),
                        ]);
                    }
                }

                $certificateSerial = 'AS-CERT-' . strtoupper(substr(bin2hex(random_bytes(4)), 0, 8));
                $firstVariantId = $itemsData[0]['product_variant_id'] ?? null;
                $firstProductId = $firstVariantId ? ProductVariant::where('id', $firstVariantId)->value('product_id') : null;
                if ($firstProductId) {
                    DB::table('authenticity_certificates')->insert([
                        'serial_code' => $certificateSerial,
                        'ttac_uid' => 'TTAC-' . random_int(1000000000, 9999999999),
                        'product_id' => $firstProductId,
                        'order_id' => $order->id,
                        'batch_code' => 'LOT-IMP-' . now()->format('ym'),
                        'manufactured_at' => now()->subMonths(2)->toDateString(),
                        'expires_at' => now()->addMonths(28)->toDateString(),
                        'origin_atelier' => 'Paris / ASIYE Certified Cold-Chain Vault',
                        'storage_temp_celsius' => '18.0°C Controlled',
                        'is_authentic' => true,
                        'created_at' => now(),
                        'updated_at' => now(),
                    ]);
                }

                foreach ($itemsData as $data) {
                    $order->items()->create($data);
                }

                // Decrement locked inventory & record stock movement audit trail
                foreach ($lockedInventories as [$inventory, $qty]) {
                    $inventory->decrement('quantity', $qty);
                    StockMovement::create([
                        'inventory_id' => $inventory->id,
                        'quantity' => -$qty,
                        'type' => 'out',
                        'reason' => "ثبت سفارش شماره {$order->order_number}",
                        'reference_order_id' => $order->id,
                    ]);
                }

                if ($coupon) {
                    $coupon->increment('used_count');
                    CouponUsage::create([
                        'coupon_id' => $coupon->id,
                        'order_id' => $order->id,
                        'customer_id' => $customer->id,
                    ]);
                }

                $order->histories()->create([
                    'from_status' => null,
                    'status' => 'pending_payment',
                    'notes' => 'سفارش توسط مشتری ثبت شد و در انتظار پرداخت است.',
                ]);

                $paymentAuthority = null;
                if ($gateway) {
                    $paymentAuthority = 'A' . strtoupper(bin2hex(random_bytes(12)));
                    $order->payments()->create([
                        'gateway_id' => $gateway->id,
                        'authority' => $paymentAuthority,
                        'amount' => $totalAmount,
                        'status' => 'pending',
                    ]);
                }

                // Clear active server-side cart
                $customer->cart?->items()->delete();

                return [$order, $paymentAuthority, $certificateSerial];
            });

            [$order, $authority, $certificateSerial] = $result;

            // v6.8 · the two messages every order earns
            try {
                $customerName = $order->customer?->user?->name ?: 'مشتری گرامی';
                $vars = [
                    'name' => $customerName,
                    'order' => $order->order_number,
                    'total' => number_format((float) $order->total_amount) . ' تومان',
                ];
                if ($order->customer?->user?->sms_optin !== false) {
                    \App\Services\Sms\SmsService::send('order_placed', $order->customer?->user?->mobile, $vars);
                }
                \App\Services\Sms\SmsService::send(
                    'owner_alert',
                    config('services.sms.owner_mobile'),
                    $vars
                );
            } catch (\Throwable $ignored) {
                // a messaging hiccup must never break a paid checkout
            }

            return response()->json([
                'order_id' => $order->id,
                'order_number' => $order->order_number,
                'total_amount' => $order->total_amount,
                'shipping_cost' => $order->shipping_cost,
                'discount_amount' => $order->discount_amount,
                'wallet_deduction' => $order->wallet_deduction,
                'cashback_earned' => $order->cashback_earned,
                'authenticity_certificate' => $certificateSerial,
                'payment_authority' => $authority,
                'message' => 'سفارش تشریفاتی شما با گواهی اصالت و پاداش کش‌بک طلایی ثبت شد.',
            ], 201);
        } catch (ValidationException $ve) {
            return response()->json(['errors' => $ve->errors()], 422);
        } catch (\Throwable $e) {
            report($e);
            return response()->json(['error' => 'خطا در پردازش سفارش. لطفا مجددا تلاش کنید.'], 500);
        }
    }
}
