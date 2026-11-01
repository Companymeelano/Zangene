<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/** v6.8 · an auditable record of every message the maison has tried to send. */
class SmsMessage extends Model
{
    protected $table = 'sms_messages';

    protected $fillable = ['mobile', 'template_key', 'body', 'status', 'attempts', 'error', 'sent_at'];

    protected $casts = [
        'attempts' => 'integer',
        'sent_at' => 'datetime',
    ];
}
