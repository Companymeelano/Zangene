<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\OrderItem;
use App\Models\Product;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class ProductManagementController extends Controller
{
    public function index()
    {
        $products = Product::with(['brand', 'category', 'variants.inventory', 'images'])
            ->latest()
            ->paginate(20);

        return response()->json($products);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'name_fa' => ['required', 'string', 'max:255'],
            'name_en' => ['required', 'string', 'max:255'],
            'brand_id' => ['required', 'integer', 'exists:brands,id'],
            'category_id' => ['required', 'integer', 'exists:categories,id'],
            'description' => ['required', 'string'],
            'introduction' => ['nullable', 'string'],
            'features' => ['nullable', 'string'],
            'usage_method' => ['nullable', 'string'],
            'ingredients' => ['nullable', 'string'],
            'country' => ['required', 'string', 'max:80'],
            'volume_weight' => ['nullable', 'string', 'max:60'],
            'skin_type' => ['nullable', 'string', 'max:100'],
            'hair_type' => ['nullable', 'string', 'max:100'],
            'original_price' => ['required', 'numeric', 'min:0'],
            'sale_price' => ['required', 'numeric', 'min:0', 'lte:original_price'],
            'sku' => ['required', 'string', 'max:64', 'unique:products,sku'],
            'barcode' => ['nullable', 'string', 'max:64', 'unique:products,barcode'],
            'published' => ['boolean'],
            'initial_stock' => ['nullable', 'integer', 'min:0'],
            'image_path' => ['nullable', 'string', 'max:500'],
        ]);

        return DB::transaction(function () use ($validated, $request) {
            $orig = (float) $validated['original_price'];
            $sale = (float) $validated['sale_price'];
            $validated['discount_percent'] = $orig > 0 ? (int) round((($orig - $sale) / $orig) * 100) : 0;
            $validated['slug'] = Str::slug($validated['name_en']) . '-' . strtolower(Str::random(5));

            $initialStock = (int) ($validated['initial_stock'] ?? 0);
            $imagePath = $validated['image_path'] ?? '/images/product_serum_luxury.png';
            unset($validated['initial_stock'], $validated['image_path']);

            $product = Product::create($validated);

            $variant = $product->variants()->create([
                'size' => $product->volume_weight ?? 'استاندارد',
                'price_offset' => 0,
                'is_active' => true,
            ]);

            $inventory = $variant->inventory()->create([
                'quantity' => $initialStock,
                'low_stock_threshold' => 5,
            ]);

            if ($initialStock > 0) {
                $inventory->movements()->create([
                    'quantity' => $initialStock,
                    'type' => 'in',
                    'reason' => 'موجودی اولیه هنگام ایجاد محصول',
                    'admin_id' => $request->user()?->id,
                ]);
            }

            $product->images()->create([
                'path' => $imagePath,
                'is_primary' => true,
                'order' => 1,
            ]);

            return response()->json($product->load(['brand', 'category', 'variants.inventory', 'images']), 201);
        });
    }

    public function update(Request $request, $id)
    {
        $product = Product::findOrFail($id);

        $validated = $request->validate([
            'name_fa' => ['sometimes', 'required', 'string', 'max:255'],
            'name_en' => ['sometimes', 'required', 'string', 'max:255'],
            'brand_id' => ['sometimes', 'required', 'integer', 'exists:brands,id'],
            'category_id' => ['sometimes', 'required', 'integer', 'exists:categories,id'],
            'description' => ['sometimes', 'required', 'string'],
            'country' => ['sometimes', 'required', 'string', 'max:80'],
            'volume_weight' => ['nullable', 'string', 'max:60'],
            'original_price' => ['sometimes', 'required', 'numeric', 'min:0'],
            'sale_price' => ['sometimes', 'required', 'numeric', 'min:0'],
            'sku' => ['sometimes', 'required', 'string', 'max:64', 'unique:products,sku,' . $product->id],
            'barcode' => ['nullable', 'string', 'max:64', 'unique:products,barcode,' . $product->id],
            'published' => ['sometimes', 'boolean'],
        ]);

        $orig = (float) ($validated['original_price'] ?? $product->original_price);
        $sale = (float) ($validated['sale_price'] ?? $product->sale_price);

        if ($sale > $orig) {
            return response()->json(['error' => 'قیمت فروش نمی‌تواند بیشتر از قیمت اصلی باشد.'], 422);
        }

        $validated['discount_percent'] = $orig > 0 ? (int) round((($orig - $sale) / $orig) * 100) : 0;

        $product->update($validated);

        return response()->json($product->load(['brand', 'category', 'variants.inventory', 'images']));
    }

    public function destroy($id)
    {
        $product = Product::with('variants')->findOrFail($id);
        $variantIds = $product->variants->pluck('id');

        $hasActiveOrders = OrderItem::whereIn('product_variant_id', $variantIds)
            ->whereHas('order', fn ($q) => $q->whereIn('status', ['pending_payment', 'paid', 'confirmed', 'preparing', 'ready_to_ship', 'shipped']))
            ->exists();

        if ($hasActiveOrders) {
            return response()->json([
                'error' => 'این محصول دارای سفارش فعال در جریان است. به جای حذف، وضعیت انتشار آن را غیرفعال کنید.',
            ], 422);
        }

        // Uses SoftDeletes so historical financial invoices remain intact
        $product->update(['published' => false]);
        $product->delete();

        return response()->json(['message' => 'محصول با حفظ سوابق مالی به صورت امن آرشیو شد.']);
    }
}
