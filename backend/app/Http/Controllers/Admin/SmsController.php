<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\SmsMessage;
use App\Models\SmsTemplate;
use App\Services\Sms\SmsService;
use App\Services\Sms\SmsTemplates;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;

/**
 * v6.8 · merchant control centre for messaging: settings, template library,
 * a live queue view and a one-tap test message.
 */
class SmsController extends Controller
{
    /** GET /v1/admin/sms/overview */
    public function overview()
    {
        $today = now()->startOfDay();

        return response()->json([
            'enabled' => (bool) SmsService::setting('sms_enabled', true),
            'driver' => config('services.sms.driver'),
            'sender' => config('services.sms.sender'),
            'queue' => [
                'pending' => SmsMessage::where('status', 'pending')->count(),
                'sent_today' => SmsMessage::where('status', 'sent')->where('sent_at', '>=', $today)->count(),
                'failed' => SmsMessage::where('status', 'failed')->count(),
            ],
            'templates' => SmsTemplate::orderBy('id')->get(),
            'recent' => SmsMessage::latest('id')->limit(20)->get(),
        ]);
    }

    /** PUT /v1/admin/sms/settings */
    public function saveSettings(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'enabled' => ['sometimes', 'boolean'],
            'driver' => ['sometimes', 'in:kavenegar,mellipayamak,smsir,ghasedak,sandbox'],
            'sender' => ['sometimes', 'nullable', 'string', 'max:32'],
            'api_key' => ['sometimes', 'nullable', 'string', 'max:190'],
            'username' => ['sometimes', 'nullable', 'string', 'max:120'],
            'password' => ['sometimes', 'nullable', 'string', 'max:120'],
            'quiet_from' => ['sometimes', 'integer', 'between:0,23'],
            'quiet_to' => ['sometimes', 'integer', 'between:0,23'],
            'daily_cap' => ['sometimes', 'integer', 'between:1,1000'],
        ]);

        if ($validator->fails()) {
            return response()->json(['errors' => $validator->errors()], 422);
        }

        $map = [
            'enabled' => 'sms_enabled',
            'sender' => 'sms_sender',
            'api_key' => 'sms_api_key',
            'username' => 'sms_username',
            'password' => 'sms_password',
            'quiet_from' => 'sms_quiet_from',
            'quiet_to' => 'sms_quiet_to',
            'daily_cap' => 'sms_daily_cap',
        ];
        foreach ($map as $field => $key) {
            if ($request->has($field)) {
                DB::table('settings')->updateOrInsert(
                    ['key' => $key],
                    ['value' => is_bool($request->input($field))
                        ? ($request->boolean($field) ? '1' : '0')
                        : (string) $request->input($field)]
                );
            }
        }

        if ($request->filled('driver')) {
            config(['services.sms.driver' => $request->input('driver')]);
            DB::table('settings')->updateOrInsert(['key' => 'sms_driver'], ['value' => $request->input('driver')]);
        }

        return response()->json(['message' => 'تنظیمات پیامک ذخیره شد.']);
    }

    /** PUT /v1/admin/sms/templates/{key} */
    public function saveTemplate(Request $request, string $key)
    {
        $validator = Validator::make($request->all(), [
            'body' => ['required', 'string', 'min:5', 'max:1000'],
            'enabled' => ['sometimes', 'boolean'],
            'title' => ['sometimes', 'string', 'max:128'],
        ]);

        if ($validator->fails()) {
            return response()->json(['errors' => $validator->errors()], 422);
        }

        $template = SmsTemplate::updateOrCreate(
            ['key' => $key],
            [
                'title' => $request->input('title', $template->title ?? $key),
                'body' => $request->input('body'),
                'enabled' => $request->boolean('enabled', true),
            ]
        );

        return response()->json(['message' => 'قالب ذخیره شد.', 'template' => $template]);
    }

    /** POST /v1/admin/sms/test */
    public function test(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'mobile' => ['required', 'string', 'regex:/^09[0-9]{9}$/'],
            'template' => ['sometimes', 'string', 'max:64'],
        ]);

        if ($validator->fails()) {
            return response()->json(['errors' => $validator->errors()], 422);
        }

        $key = $request->input('template', 'welcome');
        SmsService::send($key, $request->input('mobile'), [
            'name' => 'مشتری گرامی',
            'code' => '۸۴۲۱۹',
            'order' => 'AS-1405-0142',
            'total' => '۲٬۳۴۰٬۰۰۰ تومان',
            'product' => 'کرم ضدلک گاتیو',
            'days' => '۴۵',
            'coupon' => 'ASIYE20',
            'percent' => '۲۰',
            'count' => '۳',
        ], true);

        return response()->json(['message' => 'پیام تست در صف ارسال قرار گرفت.']);
    }

    /** POST /v1/admin/sms/retry — pushes failed messages back into the queue. */
    public function retry()
    {
        $count = SmsMessage::whereIn('status', ['pending', 'failed'])->update(['status' => 'pending', 'attempts' => 0]);

        return response()->json(['message' => 'پیام‌ها دوباره در صف قرار گرفتند.', 'count' => $count]);
    }

    /** GET /v1/admin/sms/templates/defaults — lets the panel offer "reset to factory". */
    public function defaults()
    {
        return response()->json(['templates' => SmsTemplates::DEFAULTS]);
    }
}
