<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Inventory;
use App\Models\Order;
use App\Models\Shipment;
use App\Models\StockMovement;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

class OrderStatusController extends Controller
{
    private const RESTORE_STOCK_STATUSES = ['cancelled', 'returned'];

    public function updateStatus(Request $request, $orderId)
    {
        $validated = $request->validate([
            'status' => ['required', Rule::in(Order::VALID_STATUSES)],
            'tracking_code' => ['nullable', 'string', 'max:80'],
            'notes' => ['nullable', 'string', 'max:500'],
        ]);

        // SECURITY FIX: Always derive admin_id from authenticated token, never from request body!
        $adminId = $request->user()?->id;

        return DB::transaction(function () use ($orderId, $validated, $adminId) {
            $order = Order::with('items')->lockForUpdate()->findOrFail($orderId);
            $previousStatus = $order->status;
            $newStatus = $validated['status'];

            if ($previousStatus === $newStatus) {
                return response()->json($order->load(['histories', 'shipments']));
            }

            // Prevent reviving already cancelled/refunded orders without proper inventory deduction
            if (in_array($previousStatus, ['cancelled', 'refunded'], true) && !in_array($newStatus, ['cancelled', 'refunded'], true)) {
                return response()->json([
                    'error' => 'امکان تغییر وضعیت سفارش لغوشده یا مستردشده به وضعیت فعال وجود ندارد.',
                ], 422);
            }

            // Restore inventory atomically if transitioning into cancelled/returned for the first time
            if (
                !in_array($previousStatus, self::RESTORE_STOCK_STATUSES, true) &&
                in_array($newStatus, self::RESTORE_STOCK_STATUSES, true)
            ) {
                foreach ($order->items as $item) {
                    $inventory = Inventory::where('product_variant_id', $item->product_variant_id)
                        ->lockForUpdate()
                        ->first();
                    if ($inventory) {
                        $inventory->increment('quantity', $item->quantity);
                        StockMovement::create([
                            'inventory_id' => $inventory->id,
                            'quantity' => $item->quantity,
                            'type' => 'return_restore',
                            'reason' => "بازگشت موجودی بابت تغییر وضعیت سفارش {$order->order_number} به {$newStatus}",
                            'reference_order_id' => $order->id,
                            'admin_id' => $adminId,
                        ]);
                    }
                }
            }

            $order->update(['status' => $newStatus]);

            $order->histories()->create([
                'from_status' => $previousStatus,
                'status' => $newStatus,
                'admin_id' => $adminId,
                'notes' => $validated['notes'] ?? null,
            ]);

            if (!empty($validated['tracking_code']) || in_array($newStatus, ['shipped', 'delivered'], true)) {
                Shipment::updateOrCreate(
                    ['order_id' => $order->id],
                    [
                        'shipping_method_id' => $order->shipping_method_id,
                        'tracking_code' => $validated['tracking_code'] ?? null,
                        'status' => $newStatus === 'delivered' ? 'delivered' : 'in_transit',
                        'shipped_at' => $newStatus === 'shipped' ? now() : null,
                        'delivered_at' => $newStatus === 'delivered' ? now() : null,
                    ]
                );
            }

            // v6.8 · tell the customer, automatically
            try {
                $map = [
                    'paid' => 'order_paid',
                    'shipped' => 'order_shipped',
                    'delivered' => 'order_delivered',
                ];
                if (isset($map[$newStatus]) && $previousStatus !== $newStatus) {
                    \App\Services\Sms\SmsService::send($map[$newStatus], $order->customer?->user?->mobile, [
                        'name' => $order->customer?->user?->name ?: 'مشتری گرامی',
                        'order' => $order->order_number,
                        'tracking' => $validated['tracking_code'] ?? optional($order->shipments()->first())->tracking_code ?? '',
                    ]);
                }
            } catch (\Throwable $ignored) {
            }

            return response()->json($order->load(['items.variant.product', 'histories', 'shipments']));
        });
    }

    public function track(Request $request, $orderId)
    {
        $customer = $request->user()?->customer;
        if (!$customer) {
            return response()->json(['error' => 'حساب مشتری یافت نشد.'], 404);
        }

        $order = Order::with([
            'items.variant.product.images',
            'address',
            'shippingMethod',
            'shipments',
            'payments',
            'installmentOrders.provider',
            'histories',
        ])
            ->where('customer_id', $customer->id)
            ->findOrFail($orderId);

        return response()->json($order);
    }
}
