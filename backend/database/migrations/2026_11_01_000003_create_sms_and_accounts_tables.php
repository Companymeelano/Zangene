<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

/**
 * v6.8 · messaging + customer profile.
 * Non-destructive: only adds tables and nullable columns.
 */
return new class extends Migration
{
    public function up(): void
    {
        Schema::create('sms_templates', function (Blueprint $table) {
            $table->id();
            $table->string('key', 64)->unique();
            $table->string('title', 128);
            $table->text('body');
            $table->boolean('enabled')->default(true);
            $table->timestamps();
        });

        Schema::create('sms_messages', function (Blueprint $table) {
            $table->id();
            $table->string('mobile', 16)->index();
            $table->string('template_key', 64)->index();
            $table->text('body');
            $table->string('status', 16)->default('pending')->index();
            $table->unsignedTinyInteger('attempts')->default(0);
            $table->text('error')->nullable();
            $table->timestamp('sent_at')->nullable();
            $table->timestamps();
            $table->index(['mobile', 'template_key', 'sent_at']);
        });

        Schema::table('users', function (Blueprint $table) {
            if (!Schema::hasColumn('users', 'skin_type')) {
                $table->string('skin_type', 32)->default('نامشخص')->after('email');
            }
            if (!Schema::hasColumn('users', 'birthday')) {
                $table->string('birthday', 16)->nullable()->after('skin_type');
            }
            if (!Schema::hasColumn('users', 'sms_optin')) {
                $table->boolean('sms_optin')->default(true)->after('birthday');
            }
        });

        // seed the library so nothing is ever blank on a fresh install
        $now = now();
        foreach (\App\Services\Sms\SmsTemplates::DEFAULTS as $key => $body) {
            DB::table('sms_templates')->updateOrInsert(
                ['key' => $key],
                [
                    'title' => \Illuminate\Support\Str::headline($key),
                    'body' => $body,
                    'enabled' => true,
                    'created_at' => $now,
                    'updated_at' => $now,
                ]
            );
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('sms_messages');
        Schema::dropIfExists('sms_templates');
    }
};
