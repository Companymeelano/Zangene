<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Cart;
use App\Models\Coupon;
use App\Models\Customer;
use App\Models\ProductVariant;
use Illuminate\Http\Request;

class CartController extends Controller
{
    private function resolveCart(Request $request): Cart
    {
        $user = $request->user();
        $customer = Customer::firstOrCreate(['user_id' => $user->id]);
        return Cart::firstOrCreate(['customer_id' => $customer->id]);
    }

    public function index(Request $request)
    {
        $cart = $this->resolveCart($request)->load(['items.variant.product.images', 'items.variant.inventory', 'coupon']);
        return response()->json($this->formatCart($cart));
    }

    public function add(Request $request)
    {
        $validated = $request->validate([
            'variant_id' => ['required', 'integer', 'exists:product_variants,id'],
            'quantity' => ['required', 'integer', 'min:1', 'max:50'],
        ]);

        $variant = ProductVariant::with(['product', 'inventory'])->findOrFail($validated['variant_id']);
        if (!$variant->is_active || !$variant->product?->published) {
            return response()->json(['error' => 'این محصول در حال حاضر قابل خرید نیست.'], 422);
        }

        $available = $variant->inventory?->quantity ?? 0;
        $cart = $this->resolveCart($request);
        $existing = $cart->items()->where('product_variant_id', $variant->id)->first();
        $desiredQty = ($existing?->quantity ?? 0) + $validated['quantity'];

        if ($desiredQty > $available) {
            return response()->json(['error' => "حداکثر موجودی قابل سفارش {$available} عدد است."], 422);
        }

        if ($existing) {
            $existing->update(['quantity' => $desiredQty]);
        } else {
            $cart->items()->create([
                'product_variant_id' => $variant->id,
                'quantity' => $validated['quantity'],
            ]);
        }

        return response()->json($this->formatCart($cart->fresh(['items.variant.product.images', 'items.variant.inventory', 'coupon'])));
    }

    public function remove(Request $request, $variantId)
    {
        $cart = $this->resolveCart($request);
        $cart->items()->where('product_variant_id', (int) $variantId)->delete();
        return response()->json($this->formatCart($cart->fresh(['items.variant.product.images', 'items.variant.inventory', 'coupon'])));
    }

    public function applyCoupon(Request $request)
    {
        $validated = $request->validate([
            'code' => ['required', 'string', 'max:50'],
        ]);

        $coupon = Coupon::where('code', strtoupper(trim($validated['code'])))->first();
        if (!$coupon || !$coupon->is_active) {
            return response()->json(['error' => 'کد تخفیف معتبر نیست.'], 422);
        }

        $cart = $this->resolveCart($request);
        $cart->update(['coupon_id' => $coupon->id]);

        return response()->json($this->formatCart($cart->fresh(['items.variant.product.images', 'items.variant.inventory', 'coupon'])));
    }

    private function formatCart(Cart $cart): array
    {
        $subtotal = 0.0;
        foreach ($cart->items as $item) {
            $subtotal += $item->variant->effectivePrice() * $item->quantity;
        }
        $discount = $cart->coupon ? $cart->coupon->calculateDiscount($subtotal) : 0.0;

        return [
            'cart_id' => $cart->id,
            'items' => $cart->items,
            'subtotal' => $subtotal,
            'discount' => $discount,
            'payable_before_shipping' => max(0, $subtotal - $discount),
            'coupon' => $cart->coupon?->code,
        ];
    }
}
