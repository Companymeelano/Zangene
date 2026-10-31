<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // 1. Gold Cashback Wallet on Customers + Ledger
        Schema::table('customers', function (Blueprint $table) {
            $table->decimal('wallet_balance', 15, 2)->default(250000)->after('loyalty_points');
        });

        Schema::create('wallet_transactions', function (Blueprint $table) {
            $table->id();
            $table->foreignId('customer_id')->constrained('customers')->cascadeOnDelete();
            $table->foreignId('order_id')->nullable()->constrained('orders')->nullOnDelete();
            $table->decimal('amount', 15, 2); // positive = credit/cashback, negative = debit
            $table->string('type', 32); // welcome_gift, cashback_reward, order_redemption, refund_credit
            $table->string('description');
            $table->timestamps();
            $table->index(['customer_id', 'created_at']);
        });

        // 2. FEFO Batch Tracking & TTAC Anti-Counterfeit Authenticity Vault
        Schema::table('products', function (Blueprint $table) {
            $table->unsignedSmallInteger('replenishment_cycle_days')->default(45)->after('discount_percent');
        });

        Schema::create('product_batches', function (Blueprint $table) {
            $table->id();
            $table->foreignId('product_variant_id')->constrained('product_variants')->cascadeOnDelete();
            $table->string('batch_code', 48)->unique();
            $table->date('manufactured_at');
            $table->date('expires_at')->index(); // Indexed for FEFO (First-Expired, First-Out)
            $table->integer('quantity')->default(0);
            $table->string('storage_temperature', 40)->default('15-22°C Controlled');
            $table->timestamps();
        });

        Schema::create('authenticity_certificates', function (Blueprint $table) {
            $table->id();
            $table->string('serial_code', 64)->unique(); // QR / Hologram / TTAC UID
            $table->foreignId('product_id')->constrained('products')->cascadeOnDelete();
            $table->string('batch_code', 48);
            $table->string('ttac_uid', 48)->nullable()->index();
            $table->string('origin_laboratory')->default('Laboratoire Haute Beauté — Paris / Tehran');
            $table->date('manufactured_at');
            $table->date('expires_at');
            $table->boolean('cold_chain_verified')->default(true);
            $table->foreignId('owner_customer_id')->nullable()->constrained('customers')->nullOnDelete();
            $table->unsignedInteger('verification_scans_count')->default(0);
            $table->timestamp('first_verified_at')->nullable();
            $table->timestamps();
        });

        // 3. White-Glove VIP Gift Concierge & Cold-Chain Logistics on Orders
        Schema::table('orders', function (Blueprint $table) {
            $table->boolean('is_vip_gift_box')->default(false)->after('payment_type');
            $table->string('scented_tissue', 60)->nullable()->after('is_vip_gift_box');
            $table->text('gift_card_message')->nullable()->after('scented_tissue');
            $table->json('complimentary_samples')->nullable()->after('gift_card_message');
            $table->boolean('cold_chain_packaging')->default(false)->after('complimentary_samples');
            $table->decimal('wallet_deducted_amount', 15, 2)->default(0)->after('discount_amount');
            $table->decimal('cashback_earned_amount', 15, 2)->default(0)->after('total_amount');
        });

        // 4. AI Skin & Olfactory Diagnostic Profiles
        Schema::create('beauty_diagnostic_profiles', function (Blueprint $table) {
            $table->id();
            $table->foreignId('customer_id')->nullable()->constrained('customers')->nullOnDelete();
            $table->string('skin_type', 40)->nullable();
            $table->string('primary_concern', 60)->nullable();
            $table->string('undertone', 40)->nullable();
            $table->string('fragrance_family', 60)->nullable();
            $table->unsignedTinyInteger('hydration_score')->default(78);
            $table->unsignedTinyInteger('radiance_score')->default(84);
            $table->json('recommended_product_ids')->nullable();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('beauty_diagnostic_profiles');
        Schema::table('orders', function (Blueprint $table) {
            $table->dropColumn([
                'is_vip_gift_box',
                'scented_tissue',
                'gift_card_message',
                'complimentary_samples',
                'cold_chain_packaging',
                'wallet_deducted_amount',
                'cashback_earned_amount',
            ]);
        });
        Schema::dropIfExists('authenticity_certificates');
        Schema::dropIfExists('product_batches');
        Schema::table('products', function (Blueprint $table) {
            $table->dropColumn('replenishment_cycle_days');
        });
        Schema::dropIfExists('wallet_transactions');
        Schema::table('customers', function (Blueprint $table) {
            $table->dropColumn('wallet_balance');
        });
    }
};
