<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

class Product extends Model
{
    use HasFactory, SoftDeletes;

    protected $fillable = [
        'brand_id',
        'category_id',
        'name_fa',
        'name_en',
        'slug',
        'description',
        'introduction',
        'features',
        'usage_method',
        'ingredients',
        'country',
        'volume_weight',
        'skin_type',
        'hair_type',
        'sku',
        'barcode',
        'ttac_code',
        'cold_chain_required',
        'replenishment_days',
        'olfactory_notes',
        'original_price',
        'sale_price',
        'discount_percent',
        'published',
    ];

    protected $casts = [
        'original_price' => 'decimal:2',
        'sale_price' => 'decimal:2',
        'discount_percent' => 'integer',
        'cold_chain_required' => 'boolean',
        'replenishment_days' => 'integer',
        'published' => 'boolean',
    ];

    protected $appends = ['total_inventory', 'primary_image'];

    public function brand()
    {
        return $this->belongsTo(Brand::class);
    }

    public function category()
    {
        return $this->belongsTo(Category::class);
    }

    public function variants()
    {
        return $this->hasMany(ProductVariant::class);
    }

    public function images()
    {
        return $this->hasMany(ProductImage::class)->orderBy('order');
    }

    public function reviews()
    {
        return $this->hasMany(Review::class)->where('is_approved', true);
    }

    public function getTotalInventoryAttribute(): int
    {
        if (!$this->relationLoaded('variants')) {
            return (int) Inventory::whereIn('product_variant_id', $this->variants()->pluck('id'))->sum('quantity');
        }
        return (int) $this->variants->sum(fn ($v) => $v->inventory?->quantity ?? 0);
    }

    public function getPrimaryImageAttribute(): ?string
    {
        if ($this->relationLoaded('images') && $this->images->isNotEmpty()) {
            return $this->images->first()->path;
        }
        return $this->images()->value('path');
    }
}
