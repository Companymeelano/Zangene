<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Coupon extends Model
{
    protected $fillable = [
        'code',
        'discount_amount',
        'discount_percent',
        'min_purchase',
        'max_discount',
        'start_at',
        'end_at',
        'usage_limit',
        'used_count',
        'usage_per_customer',
        'is_active',
    ];

    protected $casts = [
        'discount_amount' => 'decimal:2',
        'discount_percent' => 'integer',
        'min_purchase' => 'decimal:2',
        'max_discount' => 'decimal:2',
        'start_at' => 'datetime',
        'end_at' => 'datetime',
        'usage_limit' => 'integer',
        'used_count' => 'integer',
        'usage_per_customer' => 'integer',
        'is_active' => 'boolean',
    ];

    public function usages()
    {
        return $this->hasMany(CouponUsage::class);
    }

    public function calculateDiscount(float $subtotal): float
    {
        if (!$this->is_active || now()->lt($this->start_at) || now()->gt($this->end_at)) {
            return 0.0;
        }
        if ($subtotal < (float) $this->min_purchase) {
            return 0.0;
        }
        if ($this->usage_limit !== null && $this->used_count >= $this->usage_limit) {
            return 0.0;
        }

        $discount = 0.0;
        if ($this->discount_percent) {
            $discount = round($subtotal * ($this->discount_percent / 100), 2);
            if ($this->max_discount !== null) {
                $discount = min($discount, (float) $this->max_discount);
            }
        } elseif ($this->discount_amount) {
            $discount = (float) $this->discount_amount;
        }

        return min($discount, $subtotal);
    }
}
