<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class InstallmentOrder extends Model
{
    protected $fillable = [
        'order_id',
        'provider_id',
        'installments_count',
        'request_id',
        'transaction_id',
        'amount',
        'per_installment_amount',
        'status',
    ];

    protected $casts = [
        'installments_count' => 'integer',
        'amount' => 'decimal:2',
        'per_installment_amount' => 'decimal:2',
    ];

    public function order()
    {
        return $this->belongsTo(Order::class);
    }

    public function provider()
    {
        return $this->belongsTo(InstallmentProvider::class, 'provider_id');
    }
}
