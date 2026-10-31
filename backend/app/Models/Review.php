<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Review extends Model
{
    protected $fillable = ['product_id', 'customer_id', 'author_name', 'rating', 'comment', 'is_approved'];

    protected $casts = [
        'rating' => 'integer',
        'is_approved' => 'boolean',
    ];

    protected $appends = ['author_display'];

    public function product()
    {
        return $this->belongsTo(Product::class);
    }

    public function customer()
    {
        return $this->belongsTo(Customer::class);
    }

    /** Display name resolved from the linked customer, falling back to the guest signature. */
    public function getAuthorDisplayAttribute(): string
    {
        if ($this->author_name) {
            return $this->author_name;
        }

        $name = $this->customer?->user?->name;

        return $name ?: 'مشتری اسیه';
    }

    /** Only approved reviews are ever exposed to the storefront. */
    public function scopeApproved($query)
    {
        return $query->where('is_approved', true);
    }
}
