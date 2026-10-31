<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Inventory;
use App\Models\StockMovement;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class InventoryController extends Controller
{
    public function index()
    {
        $inventories = Inventory::with(['variant.product.brand'])
            ->orderBy('quantity')
            ->paginate(30);

        return response()->json($inventories);
    }

    public function adjust(Request $request)
    {
        $validated = $request->validate([
            'product_variant_id' => ['required', 'integer', 'exists:product_variants,id'],
            'type' => ['required', 'in:in,out,adjustment'],
            'quantity' => ['required', 'integer', 'min:1', 'max:100000'],
            'reason' => ['required', 'string', 'min:3', 'max:255'],
        ]);

        return DB::transaction(function () use ($validated, $request) {
            $inventory = Inventory::where('product_variant_id', $validated['product_variant_id'])
                ->lockForUpdate()
                ->firstOrFail();

            $delta = 0;
            if ($validated['type'] === 'in') {
                $delta = $validated['quantity'];
                $inventory->increment('quantity', $delta);
            } elseif ($validated['type'] === 'out') {
                if ($inventory->quantity < $validated['quantity']) {
                    return response()->json(['error' => 'موجودی فعلی انبار کمتر از مقدار خروجی درخواستی است.'], 422);
                }
                $delta = -$validated['quantity'];
                $inventory->decrement('quantity', $validated['quantity']);
            } else {
                // Direct stocktake adjustment
                $delta = $validated['quantity'] - $inventory->quantity;
                $inventory->update(['quantity' => $validated['quantity']]);
            }

            $movement = StockMovement::create([
                'inventory_id' => $inventory->id,
                'quantity' => $delta,
                'type' => $validated['type'],
                'reason' => $validated['reason'],
                'admin_id' => $request->user()?->id,
            ]);

            return response()->json([
                'inventory' => $inventory->fresh('variant.product'),
                'movement' => $movement,
                'message' => 'موجودی انبار با موفقیت بروزرسانی و در لاگ گردش کالا ثبت شد.',
            ]);
        });
    }

    public function movements()
    {
        return response()->json(
            StockMovement::with(['inventory.variant.product', 'admin'])
                ->latest()
                ->limit(100)
                ->get()
        );
    }
}
