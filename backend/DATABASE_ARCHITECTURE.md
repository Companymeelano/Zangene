# ASIYE STORE — DATABASE ARCHITECTURE (Laravel Migrations Blueprint)

## 1. CORE TABLES
- users, roles, permissions, role_user, permission_role (RBAC)
- customers, customer_addresses

## 2. PRODUCT CATALOG
- products, product_variants, product_images, categories, brands
- product_category, product_attributes, product_attribute_values
- inventories, stock_movements, product_batches

## 3. ORDERS & LOGISTICS
- orders, order_items, order_status_histories
- shipping_methods, shipments, shipment_tracking

## 4. PAYMENTS & INSTALLMENTS
- payments, payment_gateways, payment_logs
- installment_orders, installment_providers (Snapp Pay, Tara)

## 5. MARKETING & CONTENT
- coupons, coupon_usages, reviews, wishlists, wishlist_items
- banners, pages, articles, notifications

## 6. SYSTEM & LOGS
- settings, admin_activity_logs, refunds, returns

---

## 7. KEY DESIGN PRINCIPLES
- Currency: Decimal (Toman/Rial conversion handled in logic)
- Dates: Database Standard (UTC) / UI (Solar Hijri - Shamsi)
- Transactions: Used for Orders & Inventory
- Security: RBAC, Secret Storage, Rate Limiting

---

## 8. MIGRATION SKELETON (PHP/Laravel)

### Schema: users
```php
Schema::create('users', function (Blueprint $table) {
    $table->id();
    $table->string('name');
    $table->string('mobile')->unique();
    $table->string('email')->nullable();
    $table->string('password');
    $table->string('role_id')->nullable();
    $table->rememberToken();
    $table->timestamps();
});
```

### Schema: products
```php
Schema::create('products', function (Blueprint $table) {
    $table->id();
    $table->string('name_fa');
    $table->string('name_en');
    $table->unsignedBigInteger('brand_id');
    $table->unsignedBigInteger('category_id');
    $table->text('description');
    $table->decimal('original_price', 15, 2);
    $table->decimal('sale_price', 15, 2);
    $table->integer('discount_percent')->default(0);
    $table->boolean('published')->default(true);
    $table->timestamps();
    $table->softDeletes();
});
```

### Schema: orders
```php
Schema::create('orders', function (Blueprint $table) {
    $table->id();
    $table->unsignedBigInteger('customer_id');
    $table->unsignedBigInteger('address_id');
    $table->decimal('total_amount', 15, 2);
    $table->string('status')->default('pending_payment'); // pending_payment, paid, confirmed, preparing, ready_to_ship, shipped, delivered, cancelled, returned, refunded
    $table->string('payment_method');
    $table->string('shipping_method');
    $table->timestamps();
});
```

### Schema: installment_orders
```php
Schema::create('installment_orders', function (Blueprint $table) {
    $table->id();
    $table->unsignedBigInteger('order_id');
    $table->string('provider'); // snapp_pay, tara
    $table->string('request_id')->nullable();
    $table->string('transaction_id')->nullable();
    $table->decimal('amount', 15, 2);
    $table->string('status'); // pending, approved, rejected, failed
    $table->timestamps();
});
```

### Schema: stock_movements
```php
Schema::create('stock_movements', function (Blueprint $table) {
    $table->id();
    $table->unsignedBigInteger('product_variant_id');
    $table->integer('quantity');
    $table->string('type'); // in, out, adjustment
    $table->string('reason');
    $table->unsignedBigInteger('user_id')->nullable(); // Admin ID
    $table->timestamps();
});
```

---

## 9. API STRUCTURE (RESTful)

### Customer Side
- GET /api/v1/products
- GET /api/v1/products/{id}
- POST /api/v1/cart/add
- POST /api/v1/orders/checkout
- GET /api/v1/orders/{id}/status
- POST /api/v1/auth/login (Mobile OTP)

### Admin Side (Web & Android)
- GET /api/v1/admin/dashboard/stats
- POST /api/v1/admin/products
- PUT /api/v1/admin/products/{id}
- GET /api/v1/admin/orders
- POST /api/v1/admin/orders/{id}/status
- POST /api/v1/admin/inventory/adjust

---

## 10. INTEGRATION ADAPTERS
- PaymentAdapter (ZarinPal, DirectBank)
- InstallmentAdapter (SnappPay, Tara)
- ShippingAdapter (Post, PrivateCourier, Snapp)
- SMSAdapter (Notification events)

---

## 11. NEXT STEPS
1. Create Laravel project structure
2. Generate migrations for all tables
3. Seed initial data (Categories, Brands, Roles)
4. Implement API Controllers
5. Connect Frontend (HTML/JS) to API endpoints
6. Start Android Kotlin project (MVVM)

---

**STATUS:** Visual Identity (Logo, Icons, HTML/CSS Prototype) COMPLETED. Database Architecture DESIGNED.
**NEXT:** Implementation of Backend Laravel Structure & Migrations.
