<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

/**
 * v6.9 · gift cards, loyalty tiers and a same-day city courier.
 * Only additive: a new table, two profile columns and one shipping method row.
 */
return new class extends Migration
{
    public function up(): void
    {
        Schema::create('gift_cards', function (Blueprint $table) {
            $table->id();
            $table->string('code', 24)->unique();
            $table->decimal('amount', 15, 2);
            $table->decimal('remaining', 15, 2);
            $table->string('buyer_name')->nullable();
            $table->string('buyer_mobile', 16)->nullable();
            $table->string('recipient_name')->nullable();
            $table->string('recipient_mobile', 16)->nullable();
            $table->text('message')->nullable();
            $table->string('status', 16)->default('active')->index();
            $table->timestamp('redeemed_at')->nullable();
            $table->timestamps();
        });

        Schema::table('customers', function (Blueprint $table) {
            if (!Schema::hasColumn('customers', 'lifetime_spend')) {
                $table->decimal('lifetime_spend', 15, 2)->default(0);
            }
            if (!Schema::hasColumn('customers', 'tier_percent')) {
                $table->unsignedTinyInteger('tier_percent')->default(0);
            }
        });

        // the same-day courier is just another shipping method for the checkout
        DB::table('shipping_methods')->updateOrInsert(
            ['slug' => 'city-courier'],
            [
                'name' => 'پیک شهری (تحویل همان روز)',
                'base_cost' => (float) env('COURIER_FEE', 95000),
                'estimated_delivery' => env('COURIER_ETA', '۲ تا ۴ ساعت'),
                'is_active' => true,
                'created_at' => now(),
                'updated_at' => now(),
            ]
        );
    }

    public function down(): void
    {
        Schema::dropIfExists('gift_cards');
        DB::table('shipping_methods')->where('slug', 'city-courier')->delete();
    }
};
