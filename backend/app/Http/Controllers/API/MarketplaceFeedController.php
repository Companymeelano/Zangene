<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Product;

/**
 * Standardized High-Speed Price & Availability Feeds for Iranian Comparison Engines
 * (Torob.com & Emalls.ir Official Spec)
 */
class MarketplaceFeedController extends Controller
{
    public function torob()
    {
        $products = Product::with(['variants.inventory', 'images', 'category'])
            ->where('published', true)
            ->latest()
            ->paginate(100);

        $items = $products->getCollection()->map(function (Product $p) {
            $inStock = $p->total_inventory > 0;
            return [
                'product_id' => (string) $p->id,
                'page_url' => url('/products/' . $p->slug),
                'price' => (int) round((float) $p->sale_price),
                'old_price' => (int) round((float) $p->original_price),
                'availability' => $inStock ? 'instock' : 'outofstock',
                'title' => $p->name_fa,
                'subtitle' => $p->name_en,
                'category_name' => $p->category?->name_fa ?? 'زیبایی و مراقبت پوست',
                'image_link' => $p->primary_image ? url($p->primary_image) : null,
                'guarantee' => 'ضمانت اصالت ۱۰۰٪ سامانه TTAC + زنجیره سرد ASIYE',
            ];
        });

        return response()->json([
            'count' => $products->total(),
            'max_pages' => $products->lastPage(),
            'products' => $items,
        ]);
    }

    public function emalls()
    {
        $products = Product::with(['variants.inventory', 'images', 'category'])
            ->where('published', true)
            ->get()
            ->map(function (Product $p) {
                return [
                    'id' => $p->sku,
                    'title' => $p->name_fa,
                    'price' => (int) round((float) $p->sale_price),
                    'is_available' => $p->total_inventory > 0,
                    'url' => url('/products/' . $p->slug),
                    'image' => $p->primary_image ? url($p->primary_image) : null,
                ];
            });

        return response()->json(['success' => true, 'items' => $products]);
    }
}
