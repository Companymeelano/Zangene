<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Order extends Model
{
    public const VALID_STATUSES = [
        'pending_payment',
        'paid',
        'confirmed',
        'preparing',
        'ready_to_ship',
        'shipped',
        'delivered',
        'cancelled',
        'returned',
        'refunded',
    ];

    protected $fillable = [
        'order_number',
        'customer_id',
        'address_id',
        'shipping_method_id',
        'payment_gateway_id',
        'coupon_id',
        'subtotal_amount',
        'shipping_cost',
        'discount_amount',
        'wallet_deduction',
        'total_amount',
        'cashback_earned',
        'status',
        'payment_type',
        'is_vip_gift_box',
        'scented_tissue',
        'gift_card_message',
        'complimentary_samples',
        'cold_chain_packaging',
        'notes',
    ];

    protected $casts = [
        'subtotal_amount' => 'decimal:2',
        'shipping_cost' => 'decimal:2',
        'discount_amount' => 'decimal:2',
        'wallet_deduction' => 'decimal:2',
        'total_amount' => 'decimal:2',
        'cashback_earned' => 'decimal:2',
        'is_vip_gift_box' => 'boolean',
        'cold_chain_packaging' => 'boolean',
    ];

    public function customer()
    {
        return $this->belongsTo(Customer::class);
    }

    public function address()
    {
        return $this->belongsTo(CustomerAddress::class, 'address_id');
    }

    public function shippingMethod()
    {
        return $this->belongsTo(ShippingMethod::class);
    }

    public function paymentGateway()
    {
        return $this->belongsTo(PaymentGateway::class);
    }

    public function coupon()
    {
        return $this->belongsTo(Coupon::class);
    }

    public function items()
    {
        return $this->hasMany(OrderItem::class);
    }

    public function histories()
    {
        return $this->hasMany(OrderStatusHistory::class)->orderBy('created_at');
    }

    public function payments()
    {
        return $this->hasMany(Payment::class);
    }

    public function installmentOrders()
    {
        return $this->hasMany(InstallmentOrder::class);
    }

    public function shipments()
    {
        return $this->hasMany(Shipment::class);
    }
}
