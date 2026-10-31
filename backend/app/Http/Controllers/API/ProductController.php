<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Brand;
use App\Models\Category;
use App\Models\Product;
use Illuminate\Http\Request;

class ProductController extends Controller
{
    private const ALLOWED_SORT_COLUMNS = [
        'created_at',
        'sale_price',
        'original_price',
        'discount_percent',
        'name_fa',
    ];

    public function index(Request $request)
    {
        $query = Product::with(['brand', 'category', 'variants.inventory', 'images'])
            ->where('published', true);

        if ($request->filled('category')) {
            $query->where('category_id', (int) $request->input('category'));
        }

        if ($request->filled('brand')) {
            $query->where('brand_id', (int) $request->input('brand'));
        }

        if ($request->filled('min_price')) {
            $query->where('sale_price', '>=', (float) $request->input('min_price'));
        }

        if ($request->filled('max_price')) {
            $query->where('sale_price', '<=', (float) $request->input('max_price'));
        }

        if ($request->boolean('in_stock')) {
            $query->whereHas('variants.inventory', fn ($q) => $q->where('quantity', '>', 0));
        }

        if ($request->filled('search')) {
            // Escape LIKE wildcards to prevent wildcard DoS / unexpected matching
            $escaped = addcslashes(trim((string) $request->input('search')), '%_\\');
            $query->where(function ($sub) use ($escaped) {
                $sub->where('name_fa', 'like', "%{$escaped}%")
                    ->orWhere('name_en', 'like', "%{$escaped}%")
                    ->orWhere('sku', 'like', "%{$escaped}%")
                    ->orWhere('barcode', 'like', "%{$escaped}%");
            });
        }

        // Strict whitelist to prevent SQL Injection in ORDER BY
        $sortBy = in_array($request->input('sort_by'), self::ALLOWED_SORT_COLUMNS, true)
            ? $request->input('sort_by')
            : 'created_at';
        $direction = strtolower((string) $request->input('direction', 'desc')) === 'asc'
            ? 'asc'
            : 'desc';

        $perPage = min(max((int) $request->input('per_page', 15), 1), 60);

        return response()->json($query->orderBy($sortBy, $direction)->paginate($perPage));
    }

    public function show($id)
    {
        $product = Product::with(['brand', 'category', 'variants.inventory', 'images', 'reviews.customer.user'])
            ->where('published', true)
            ->findOrFail($id);

        return response()->json($product);
    }

    public function catalogMeta()
    {
        return response()->json([
            'categories' => Category::where('is_active', true)->orderBy('order')->get(),
            'brands' => Brand::where('is_active', true)->orderBy('name_fa')->get(),
        ]);
    }
}
