<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/** v6.9 · a card with a balance: bought by one person, spent by another. */
class GiftCard extends Model
{
    protected $fillable = [
        'code', 'amount', 'remaining', 'buyer_name', 'buyer_mobile',
        'recipient_name', 'recipient_mobile', 'message', 'status', 'redeemed_at',
    ];

    protected $casts = [
        'amount' => 'decimal:2',
        'remaining' => 'decimal:2',
        'redeemed_at' => 'datetime',
    ];

    public static function generateCode(): string
    {
        return 'AS-GIFT-' . random_int(1000, 9999);
    }

    public function isUsable(): bool
    {
        return $this->status === 'active' && (float) $this->remaining > 0;
    }

    /** @return float the amount actually consumed */
    public function consume(float $wanted): float
    {
        if (!$this->isUsable() || $wanted <= 0) {
            return 0.0;
        }

        $applied = min((float) $this->remaining, $wanted);
        $this->remaining = (float) $this->remaining - $applied;
        $this->status = $this->remaining <= 0 ? 'used' : 'active';
        $this->redeemed_at = now();
        $this->save();

        return $applied;
    }
}
