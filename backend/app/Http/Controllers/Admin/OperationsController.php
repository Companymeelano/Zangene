<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Coupon;
use App\Models\Customer;
use App\Models\Order;
use Illuminate\Http\Request;

/**
 * v6.5 — the merchant's operational surface: CRM, coupon control, order search and CSV export.
 * Mirrors the Android management tools so both clients stay in sync.
 */
class OperationsController extends Controller
{
    /** GET /v1/admin/customers — CRM rows aggregated from real orders. */
    public function customers()
    {
        $rows = Customer::with('user:id,name,phone')
            ->withCount('orders')
            ->withSum('orders as total_spend', 'total_amount')
            ->orderByDesc('total_spend')
            ->limit(50)
            ->get()
            ->map(fn ($c) => [
                'id' => $c->id,
                'name' => $c->user?->name,
                'mobile' => $c->user?->mobile,
                'orders_count' => $c->orders_count,
                'total_spend' => (int) $c->total_spend,
                'tier' => $c->tier,
                'wallet_balance' => (int) $c->wallet_balance,
            ]);

        return response()->json(['count' => $rows->count(), 'customers' => $rows]);
    }

    /** GET /v1/admin/coupons — every coupon with its live state. */
    public function coupons()
    {
        return response()->json(Coupon::orderByDesc('id')->get());
    }

    /** POST /v1/admin/coupons/{id}/toggle — switch a campaign on or off. */
    public function toggleCoupon($id)
    {
        $coupon = Coupon::findOrFail($id);
        $coupon->is_active = !$coupon->is_active;
        $coupon->save();

        return response()->json([
            'message' => $coupon->is_active ? 'کوپن فعال شد.' : 'کوپن خاموش شد.',
            'coupon' => $coupon,
        ]);
    }

    /** DELETE /v1/admin/coupons/{id} */
    public function destroyCoupon($id)
    {
        Coupon::findOrFail($id)->delete();

        return response()->json(['message' => 'کوپن حذف شد.']);
    }

    /** GET /v1/admin/orders/search?q=&status= — number, name or mobile. */
    public function searchOrders(Request $request)
    {
        $validated = $request->validate([
            'q' => ['nullable', 'string', 'max:80'],
            'status' => ['nullable', 'string', 'max:40'],
        ]);

        $query = Order::with('items')->latest('id');

        if (!empty($validated['status']) && $validated['status'] !== 'all') {
            $query->where('status', $validated['status']);
        }

        if (!empty($validated['q'])) {
            $like = '%' . $validated['q'] . '%';
            $query->where(function ($sub) use ($like) {
                $sub->where('order_number', 'like', $like)
                    ->orWhereHas('customer.user', function ($u) use ($like) {
                        $u->where('name', 'like', $like)->orWhere('mobile', 'like', $like);
                    });
            });
        }

        return response()->json(['orders' => $query->limit(60)->get()]);
    }

    /** GET /v1/admin/export/orders.csv — the accounting hand-off. */
    public function exportOrders()
    {
        $rows = Order::with('customer.user')->orderByDesc('id')
            ->get(['id', 'order_number', 'customer_id', 'total_amount', 'status', 'created_at']);

        $csv = "order_number,customer,mobile,total,status,created_at\n";
        foreach ($rows as $row) {
            $csv .= implode(',', [
                $this->csvCell($row->order_number),
                $this->csvCell($row->customer?->user?->name),
                $this->csvCell($row->customer?->user?->mobile),
                (int) $row->total_amount,
                $this->csvCell($row->status),
                $row->created_at,
            ]) . "\n";
        }

        return response($csv, 200, [
            'Content-Type' => 'text/csv; charset=UTF-8',
            'Content-Disposition' => 'attachment; filename="asiye-orders.csv"',
        ]);
    }

    private function csvCell($value): string
    {
        $value = (string) $value;
        if (str_contains($value, ',') || str_contains($value, '"')) {
            return '"' . str_replace('"', '""', $value) . '"';
        }

        return $value;
    }
}
