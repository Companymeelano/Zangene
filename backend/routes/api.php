<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\API\AuthController;
use App\Http\Controllers\API\BeautyConciergeController;
use App\Http\Controllers\API\CartController;
use App\Http\Controllers\API\InstallmentController;
use App\Http\Controllers\API\MarketplaceFeedController;
use App\Http\Controllers\API\OrderStatusController;
use App\Http\Controllers\API\ProductController;
use App\Http\Controllers\API\ReviewController;
use App\Http\Controllers\Admin\DashboardController;
use App\Http\Controllers\Admin\InventoryController;
use App\Http\Controllers\Admin\ProductManagementController;
use App\Http\Controllers\Customer\CheckoutController;
use App\Http\Middleware\EnsureRoleOrPermission;
use App\Http\Middleware\EnsureTransactionSafety;

Route::prefix('v1')->middleware([EnsureTransactionSafety::class])->group(function () {
    // 1. Public Catalog, Authentication, AI Beauty Concierge, Authenticity & Comparison Feeds
    Route::post('/auth/send-otp', [AuthController::class, 'sendOtp']);
    Route::post('/auth/verify-otp', [AuthController::class, 'verifyOtp']);
    Route::post('/auth/admin/login', [AuthController::class, 'adminLogin']);

    Route::get('/catalog/meta', [ProductController::class, 'catalogMeta']);
    Route::get('/products', [ProductController::class, 'index']);
    Route::get('/products/{id}', [ProductController::class, 'show'])->whereNumber('id');

    // Verified Reviews — public read, moderated write (v6.2)
    Route::get('/reviews/summary', [ReviewController::class, 'summary']);
    Route::get('/products/{id}/reviews', [ReviewController::class, 'index'])->whereNumber('id');
    Route::post('/products/{id}/reviews', [ReviewController::class, 'store'])
        ->whereNumber('id')
        ->middleware('throttle:12,1');

    // AI Skin Analyzer, Fragrance Sommelier & Anti-Counterfeit TTAC Verification
    Route::post('/concierge/skin-analyze', [BeautyConciergeController::class, 'analyzeSkin']);
    Route::post('/concierge/fragrance-finder', [BeautyConciergeController::class, 'matchFragrance']);
    Route::get('/authenticity/verify/{code}', [BeautyConciergeController::class, 'verifyAuthenticity']);

    // Official Torob & Emalls Price/Stock Feeds
    Route::get('/feeds/torob', [MarketplaceFeedController::class, 'torob']);
    Route::get('/feeds/emalls', [MarketplaceFeedController::class, 'emalls']);

    // Signed Installment Provider Webhook Callback (HMAC verified in controller)
    Route::post('/installment/callback/{provider}', [InstallmentController::class, 'callback']);

    // 2. Authenticated Customer Endpoints (Sanctum)
    Route::middleware(['auth:sanctum'])->group(function () {
        Route::get('/auth/me', [AuthController::class, 'me']);
        Route::post('/auth/logout', [AuthController::class, 'logout']);

        // Gold Cashback Wallet & Smart Replenishment CRM
        Route::get('/customer/wallet', [BeautyConciergeController::class, 'walletSummary']);
        Route::get('/customer/replenishments', [BeautyConciergeController::class, 'customerReplenishments']);

        // Cart & Coupon
        Route::get('/cart', [CartController::class, 'index']);
        Route::post('/cart/add', [CartController::class, 'add']);
        Route::delete('/cart/items/{variantId}', [CartController::class, 'remove'])->whereNumber('variantId');
        Route::post('/cart/coupon', [CartController::class, 'applyCoupon']);

        // Atomic Checkout, Installment Initiation & Order Tracking
        Route::post('/checkout', [CheckoutController::class, 'processCheckout']);
        Route::post('/installment/request', [InstallmentController::class, 'createRequest']);
        Route::get('/orders/track/{orderId}', [OrderStatusController::class, 'track'])->whereNumber('orderId');
    });

    // 3. Protected Executive Admin Endpoints (Sanctum + RBAC)
    Route::prefix('admin')
        ->middleware(['auth:sanctum', EnsureRoleOrPermission::class . ':super-admin|store-manager|warehouse-manager'])
        ->group(function () {
            Route::get('/dashboard/stats', [DashboardController::class, 'stats']);
            Route::get('/orders', [DashboardController::class, 'orders']);
            Route::post('/orders/{id}/status', [OrderStatusController::class, 'updateStatus'])->whereNumber('id');

            Route::get('/products', [ProductManagementController::class, 'index']);
            Route::post('/products', [ProductManagementController::class, 'store']);
            Route::put('/products/{id}', [ProductManagementController::class, 'update'])->whereNumber('id');
            Route::delete('/products/{id}', [ProductManagementController::class, 'destroy'])->whereNumber('id');

            Route::get('/reviews/pending', [ReviewController::class, 'pending']);
            Route::post('/reviews/{id}/approve', [ReviewController::class, 'approve'])->whereNumber('id');
            Route::delete('/reviews/{id}', [ReviewController::class, 'destroy'])->whereNumber('id');

            Route::get('/inventory', [InventoryController::class, 'index']);
            Route::post('/inventory/adjust', [InventoryController::class, 'adjust']);
            Route::get('/inventory/movements', [InventoryController::class, 'movements']);
        });
});
