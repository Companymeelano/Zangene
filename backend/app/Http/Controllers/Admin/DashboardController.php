<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Customer;
use App\Models\InstallmentOrder;
use App\Models\Inventory;
use App\Models\Order;
use Illuminate\Support\Facades\DB;

class DashboardController extends Controller
{
    public function stats()
    {
        $paidStatuses = ['paid', 'confirmed', 'preparing', 'ready_to_ship', 'shipped', 'delivered'];

        $totalSalesToday = (float) Order::whereDate('created_at', today())
            ->whereIn('status', $paidStatuses)
            ->sum('total_amount');

        $totalSalesMonth = (float) Order::whereYear('created_at', now()->year)
            ->whereMonth('created_at', now()->month)
            ->whereIn('status', $paidStatuses)
            ->sum('total_amount');

        $newOrdersCount = Order::whereIn('status', ['pending_payment', 'paid', 'confirmed'])->count();

        $lowInventoryCount = Inventory::whereColumn('quantity', '<=', 'low_stock_threshold')->count();

        $newCustomersCount = Customer::where('created_at', '>=', now()->subDays(30))->count();

        $installmentOrdersCount = InstallmentOrder::where('status', 'approved')->count();

        $recentOrders = Order::with(['customer.user'])
            ->latest()
            ->limit(10)
            ->get();

        $dailyChart = Order::select(
            DB::raw('DATE(created_at) as day'),
            DB::raw('SUM(total_amount) as revenue'),
            DB::raw('COUNT(*) as orders_count')
        )
            ->where('created_at', '>=', now()->subDays(7))
            ->whereIn('status', $paidStatuses)
            ->groupBy(DB::raw('DATE(created_at)'))
            ->orderBy('day')
            ->get();

        return response()->json([
            'total_sales_today' => (int) round($totalSalesToday),
            'total_sales_month' => (int) round($totalSalesMonth),
            'new_orders_count' => $newOrdersCount,
            'low_inventory_count' => $lowInventoryCount,
            'new_customers_count' => $newCustomersCount,
            'installment_orders_count' => $installmentOrdersCount,
            'recent_orders' => $recentOrders,
            'daily_chart' => $dailyChart,
        ]);
    }

    public function orders()
    {
        return response()->json(
            Order::with(['customer.user', 'address', 'items.variant.product', 'shipments', 'installmentOrders.provider'])
                ->latest()
                ->paginate(20)
        );
    }
}
