<?php

namespace App\Http\Controllers\Customer;

use App\Http\Controllers\Controller;
use App\Models\Customer;
use App\Models\Order;
use App\Models\SmsMessage;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

/**
 * v6.8 · the customer's own surface: profile, preferences, order book.
 * Everything here is scoped to the authenticated user — no id is ever trusted from the client.
 */
class AccountController extends Controller
{
    /** GET /v1/account */
    public function show(Request $request)
    {
        $user = $request->user();
        $customer = Customer::firstWhere('user_id', $user->id);

        return response()->json([
            'user' => [
                'name' => $user->name,
                'mobile' => $user->mobile,
                'email' => $user->email,
                'skin_type' => $user->skin_type,
                'birthday' => $user->birthday,
                'sms_optin' => (bool) $user->sms_optin,
                'member_since' => $user->created_at?->toDateString(),
            ],
            'stats' => [
                'orders' => $customer ? Order::where('customer_id', $customer->id)->count() : 0,
                'lifetime_spend' => $customer
                    ? (int) Order::where('customer_id', $customer->id)->sum('total_amount')
                    : 0,
                'tier' => $customer->tier ?? 'member',
            ],
            'addresses' => $customer ? $customer->addresses()->get() : [],
        ]);
    }

    /** PUT /v1/account — profile + notification consent in one calm call. */
    public function update(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => ['sometimes', 'string', 'min:3', 'max:80'],
            'email' => ['sometimes', 'nullable', 'email', 'max:120'],
            'skin_type' => ['sometimes', 'in:نامشخص,خشک,چرب,مختلط,حساس'],
            'birthday' => ['sometimes', 'nullable', 'string', 'max:16'],
            'sms_optin' => ['sometimes', 'boolean'],
        ]);

        if ($validator->fails()) {
            return response()->json(['errors' => $validator->errors()], 422);
        }

        $user = $request->user();
        $user->fill($validator->validated())->save();

        return response()->json(['message' => 'پروفایل شما به‌روزرسانی شد.', 'user' => $user]);
    }

    /** GET /v1/account/orders */
    public function orders(Request $request)
    {
        $customer = Customer::firstWhere('user_id', $request->user()->id);
        if (!$customer) {
            return response()->json(['orders' => []]);
        }

        $orders = Order::with('items')
            ->where('customer_id', $customer->id)
            ->latest('id')
            ->limit(40)
            ->get();

        return response()->json(['count' => $orders->count(), 'orders' => $orders]);
    }

    /** POST /v1/account/sms-test — lets a customer verify their own number. */
    public function smsTest(Request $request)
    {
        $user = $request->user();

        if (!$user->sms_optin) {
            return response()->json(['error' => 'ابتدا دریافت پیامک را فعال کنید.'], 422);
        }

        $sent = SmsMessage::where('mobile', $user->mobile)
            ->where('created_at', '>=', now()->subMinutes(10))
            ->count();

        if ($sent >= 3) {
            return response()->json(['error' => 'تعداد درخواست‌ها زیاد است؛ کمی بعد تلاش کنید.'], 429);
        }

        \App\Services\Sms\SmsService::send('welcome', $user->mobile, [
            'name' => $user->name ?: 'مشتری گرامی',
        ], true);

        return response()->json(['message' => 'پیام تست در صف ارسال قرار گرفت.']);
    }
}
