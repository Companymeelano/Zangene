<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * v6.2 — Verified reviews parity with the Android boutique.
 *
 * The original schema forced every review to belong to a registered customer.
 * Guests walking the storefront (and the offline-first app) can leave a review
 * with a display name instead, and the moderation queue needs a fast
 * (product_id, is_approved) lookup.
 */
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('reviews', function (Blueprint $table) {
            $table->foreignId('customer_id')->nullable()->change();
            $table->string('author_name', 80)->nullable()->after('customer_id');
            $table->index(['product_id', 'is_approved'], 'reviews_product_approved_index');
        });
    }

    public function down(): void
    {
        Schema::table('reviews', function (Blueprint $table) {
            $table->dropIndex('reviews_product_approved_index');
            $table->dropColumn('author_name');
            $table->foreignId('customer_id')->nullable(false)->change();
        });
    }
};
