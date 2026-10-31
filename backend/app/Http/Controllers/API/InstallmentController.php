<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\InstallmentOrder;
use App\Models\InstallmentProvider;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class InstallmentController extends Controller
{
    public function createRequest(Request $request)
    {
        $validated = $request->validate([
            'order_id' => ['required', 'integer', 'exists:orders,id'],
            'provider' => ['required', 'string', 'in:snapp_pay,tara,digipay,azkivam'],
            'installments' => ['required', 'integer', 'in:2,3,4,6,9,12'],
        ]);

        $customer = $request->user()?->customer;
        if (!$customer) {
            return response()->json(['error' => 'پروفایل مشتری یافت نشد.'], 403);
        }

        $order = $customer->orders()->findOrFail($validated['order_id']);
        if ($order->status !== 'pending_payment') {
            return response()->json(['error' => 'این سفارش در وضعیت قابل پرداخت اقساطی نیست.'], 422);
        }

        $provider = InstallmentProvider::where('slug', $validated['provider'])->first();
        if (!$provider || !$provider->is_active) {
            return response()->json(['error' => 'سرویس پرداخت اقساطی انتخاب‌شده در حال حاضر فعال نیست.'], 403);
        }

        if ($validated['installments'] > $provider->max_installments) {
            return response()->json([
                'error' => "حداکثر تعداد اقساط مجاز برای {$provider->name}، {$provider->max_installments} قسط است.",
            ], 422);
        }

        $perInstallment = round(((float) $order->total_amount) / $validated['installments'], 2);
        $requestId = strtoupper($provider->slug) . '-' . now()->format('YmdHis') . '-' . random_int(1000, 9999);

        $installmentOrder = InstallmentOrder::create([
            'order_id' => $order->id,
            'provider_id' => $provider->id,
            'installments_count' => $validated['installments'],
            'request_id' => $requestId,
            'amount' => $order->total_amount,
            'per_installment_amount' => $perInstallment,
            'status' => 'pending',
        ]);

        return response()->json([
            'installment_order_id' => $installmentOrder->id,
            'request_id' => $requestId,
            'provider' => $provider->slug,
            'installments_count' => $installmentOrder->installments_count,
            'per_installment_amount' => $perInstallment,
            'status' => 'pending',
            'message' => 'درخواست خرید اقساطی ثبت شد. در حال هدایت به درگاه اعتباری...',
        ], 201);
    }

    public function callback(Request $request, string $provider)
    {
        $validated = $request->validate([
            'request_id' => ['required', 'string'],
            'transaction_id' => ['required', 'string'],
            'status' => ['required', 'in:approved,rejected,failed'],
            'signature' => ['required', 'string'],
        ]);

        // Verify HMAC-SHA256 webhook signature from Snapp Pay / Tara
        $secretEnv = $provider === 'snapp_pay'
            ? (string) env('SNAPP_PAY_WEBHOOK_SECRET', '')
            : (string) env('TARA_WEBHOOK_SECRET', '');

        if ($secretEnv === '') {
            return response()->json(['error' => 'کلید تایید سرویس اقساط در سرور تنظیم نشده است.'], 503);
        }

        $payloadToSign = $validated['request_id'] . '|' . $validated['transaction_id'] . '|' . $validated['status'];
        $expectedSignature = hash_hmac('sha256', $payloadToSign, $secretEnv);

        if (!hash_equals($expectedSignature, $validated['signature'])) {
            return response()->json(['error' => 'امضای دیجیتال تراکنش نامعتبر است.'], 403);
        }

        return DB::transaction(function () use ($validated) {
            $installmentOrder = InstallmentOrder::with('order')
                ->where('request_id', $validated['request_id'])
                ->lockForUpdate()
                ->firstOrFail();

            if ($installmentOrder->status !== 'pending') {
                return response()->json(['status' => $installmentOrder->status, 'message' => 'این تراکنش قبلا پردازش شده است.']);
            }

            $installmentOrder->update([
                'status' => $validated['status'],
                'transaction_id' => $validated['transaction_id'],
            ]);

            if ($validated['status'] === 'approved') {
                $order = $installmentOrder->order;
                $prev = $order->status;
                $order->update(['status' => 'paid', 'payment_type' => 'installment']);
                $order->histories()->create([
                    'from_status' => $prev,
                    'status' => 'paid',
                    'notes' => "پرداخت اقساطی تایید شد (کد رهگیری: {$validated['transaction_id']}).",
                ]);
            }

            return response()->json([
                'status' => $validated['status'],
                'message' => $validated['status'] === 'approved'
                    ? 'تراکنش اقساطی با موفقیت تایید شد.'
                    : 'تراکنش اقساطی تایید نشد.',
            ]);
        });
    }
}
