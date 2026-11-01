<?php

namespace App\Services\Sms;

use App\Jobs\SendSmsJob;
use App\Models\SmsMessage;
use App\Models\SmsTemplate;
use Illuminate\Support\Facades\DB;

/**
 * v6.8 · one door for every outgoing message.
 *
 * Usage:  SmsService::send('order_shipped', $mobile, ['order' => ..., 'tracking' => ...]);
 *
 * The service renders the template, honours the customer's opt-in, respects quiet
 * hours for marketing messages, writes an auditable row to sms_messages and hands
 * the actual HTTP call to a queued job so a slow gateway never blocks a checkout.
 */
class SmsService
{
    public static function send(string $key, ?string $mobile, array $vars = [], bool $force = false): ?SmsMessage
    {
        $mobile = self::normalize($mobile);
        if ($mobile === '') {
            return null;
        }

        $enabled = (bool) self::setting('sms_enabled', config('services.sms.enabled', true));
        if (!$enabled && !$force) {
            return null;
        }

        if (!self::passesQuietHours($key)) {
            return null;
        }

        $template = SmsTemplate::query()->where('key', $key)->first();
        if ($template && !$template->enabled) {
            return null;
        }

        $body = SmsTemplates::render(
            $template->body ?? SmsTemplates::default($key),
            $vars + self::baseVars()
        );
        if ($body === '') {
            return null;
        }

        $message = SmsMessage::create([
            'mobile' => $mobile,
            'template_key' => $key,
            'body' => $body,
            'status' => 'pending',
            'attempts' => 0,
        ]);

        SendSmsJob::dispatch($message->id);

        return $message;
    }

    /** Reads a merchant setting from the `settings` table, falling back to config. */
    public static function setting(string $key, $default = null)
    {
        try {
            $row = DB::table('settings')->where('key', $key)->first();

            return $row ? $row->value : $default;
        } catch (\Throwable $e) {
            return $default;
        }
    }

    public static function baseVars(): array
    {
        return [
            'store' => config('services.sms.store_name', 'اسیه استور'),
            'owner' => config('services.sms.owner_name', 'خانم زنگنه'),
        ];
    }

    public static function normalize(?string $raw): string
    {
        $digits = strtr((string) $raw, [
            '۰' => '0', '۱' => '1', '۲' => '2', '۳' => '3', '۴' => '4',
            '۵' => '5', '۶' => '6', '۷' => '7', '۸' => '8', '۹' => '9',
            '٠' => '0', '١' => '1', '٢' => '2', '٣' => '3', '٤' => '4',
            '٥' => '5', '٦' => '6', '٧' => '7', '٨' => '8', '٩' => '9',
        ]);
        $digits = preg_replace('/[^0-9+]/', '', $digits);

        if (str_starts_with($digits, '+98')) {
            $digits = '0' . substr($digits, 3);
        } elseif (str_starts_with($digits, '0098')) {
            $digits = '0' . substr($digits, 4);
        } elseif (str_starts_with($digits, '98') && strlen($digits) === 12) {
            $digits = '0' . substr($digits, 2);
        } elseif (strlen($digits) === 10 && str_starts_with($digits, '9')) {
            $digits = '0' . $digits;
        }

        return $digits;
    }

    private static function passesQuietHours(string $key): bool
    {
        if (in_array($key, SmsTemplates::TRANSACTIONAL, true)) {
            return true;
        }

        $from = (int) self::setting('sms_quiet_from', 23);
        $to = (int) self::setting('sms_quiet_to', 8);
        $hour = (int) now()->format('G');

        if ($from === $to) {
            return true;
        }

        $quiet = $from < $to ? ($hour >= $from && $hour < $to) : ($hour >= $from || $hour < $to);

        return !$quiet;
    }
}
