<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ProductVariant extends Model
{
    protected $fillable = [
        'product_id',
        'sku_suffix',
        'color',
        'color_hex',
        'size',
        'price_offset',
        'is_active',
    ];

    protected $casts = [
        'price_offset' => 'decimal:2',
        'is_active' => 'boolean',
    ];

    public function product()
    {
        return $this->belongsTo(Product::class);
    }

    public function inventory()
    {
        return $this->hasOne(Inventory::class);
    }

    public function effectivePrice(): float
    {
        return (float) $this->product->sale_price + (float) $this->price_offset;
    }
}
