<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/** v6.8 · merchant-editable message templates, one row per event. */
class SmsTemplate extends Model
{
    protected $table = 'sms_templates';

    protected $fillable = ['key', 'title', 'body', 'enabled'];

    protected $casts = ['enabled' => 'boolean'];
}
