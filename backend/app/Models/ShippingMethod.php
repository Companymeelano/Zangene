<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ShippingMethod extends Model
{
    protected $fillable = ['name', 'slug', 'base_cost', 'estimated_delivery', 'is_active'];

    protected $casts = [
        'base_cost' => 'decimal:2',
        'is_active' => 'boolean',
    ];
}
