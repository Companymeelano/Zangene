<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Customer extends Model
{
    protected $fillable = [
        'user_id',
        'birth_date',
        'loyalty_points',
        'wallet_balance',
        'lifetime_spend',
        'tier',
    ];

    protected $casts = [
        'loyalty_points' => 'decimal:2',
        'wallet_balance' => 'decimal:2',
        'lifetime_spend' => 'decimal:2',
    ];

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function addresses()
    {
        return $this->hasMany(CustomerAddress::class);
    }

    public function orders()
    {
        return $this->hasMany(Order::class);
    }

    public function cart()
    {
        return $this->hasOne(Cart::class);
    }

    public function wishlists()
    {
        return $this->hasMany(Wishlist::class);
    }
}
