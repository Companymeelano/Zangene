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

                // Calculate Shipping Cost (Free above luxury threshold)
                $freeThreshold = (float) env('FREE_SHIPPING_THRESHOLD_TOMAN', 3000000);
                $shippingCost = ($subtotal - $discountAmount) >= $freeThreshold
                    ? 0.0
                    : (float) $shippingMethod->base_cost;

                $totalAmount = max(0, ($subtotal - $discountAmount) + $shippingCost);
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
                    'total_amount' => $totalAmount,
                    'status' => 'pending_payment',
                    'payment_type' => $paymentType,
                    'notes' => $validated['notes'] ?? null,
                ]);

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

                return [$order, $paymentAuthority];
            });

            [$order, $authority] = $result;

            return response()->json([
                'order_id' => $order->id,
                'order_number' => $order->order_number,
                'total_amount' => $order->total_amount,
                'shipping_cost' => $order->shipping_cost,
                'discount_amount' => $order->discount_amount,
                'payment_authority' => $authority,
                'message' => 'سفارش لوکس شما با موفقیت ثبت شد. در حال انتقال به درگاه پرداخت امن...',
            ], 201);
        } catch (ValidationException $ve) {
            return response()->json(['errors' => $ve->errors()], 422);
        } catch (\Throwable $e) {
            report($e);
            return response()->json(['error' => 'خطا در پردازش سفارش. لطفا مجددا تلاش کنید.'], 500);
        }
    }
}
