<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class InstallmentProvider extends Model
{
    protected $fillable = ['name', 'slug', 'max_installments', 'is_active'];

    protected $casts = [
        'max_installments' => 'integer',
        'is_active' => 'boolean',
    ];
}
