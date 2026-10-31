<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class PaymentLog extends Model
{
    protected $fillable = ['payment_id', 'provider', 'event', 'payload', 'ip_address'];

    protected $casts = [
        'payload' => 'array',
    ];
}
