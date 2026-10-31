<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Customer;
use App\Models\OtpCode;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\Facades\Validator;

class AuthController extends Controller
{
    /**
     * Normalize Persian/Arabic digits (۰-۹ / ۰-٩) to ASCII (0-9).
     */
    private function normalizeDigits(string $input): string
    {
        $persian = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];
        $arabic  = ['٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'];
        $ascii   = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];

        return str_replace($arabic, $ascii, str_replace($persian, $ascii, trim($input)));
    }

    public function sendOtp(Request $request)
    {
        $mobile = $this->normalizeDigits((string) $request->input('mobile', ''));
        $request->merge(['mobile' => $mobile]);

        $validator = Validator::make($request->all(), [
            'mobile' => ['required', 'string', 'regex:/^09[0-9]{9}$/'],
        ], [
            'mobile.regex' => 'شماره موبایل باید با 09 شروع شده و ۱۱ رقم معتبر باشد.',
        ]);

        if ($validator->fails()) {
            return response()->json(['errors' => $validator->errors()], 422);
        }

        $rateKey = 'send-otp:' . $mobile . ':' . $request->ip();
        if (RateLimiter::tooManyAttempts($rateKey, 5)) {
            return response()->json([
                'error' => 'تعداد درخواست‌های کد تایید بیش از حد مجاز است. لطفا ۲ دقیقه دیگر تلاش کنید.',
            ], 429);
        }
        RateLimiter::hit($rateKey, 120);

        $user = User::firstOrCreate(
            ['mobile' => $mobile],
            ['name' => 'مشتری ویژه ASIYE', 'role_id' => null, 'is_active' => true]
        );

        if (!$user->is_active) {
            return response()->json(['error' => 'حساب کاربری شما غیرفعال شده است.'], 403);
        }

        $otpPlain = (string) random_int(100000, 999999);
        $expiresSeconds = (int) config('auth.otp_expires', 120);

        // Invalidate previous unverified OTPs for this mobile
        OtpCode::where('mobile', $mobile)->whereNull('verified_at')->delete();

        OtpCode::create([
            'mobile' => $mobile,
            'code_hash' => Hash::make($otpPlain),
            'attempts' => 0,
            'expires_at' => now()->addSeconds($expiresSeconds),
            'ip_address' => $request->ip(),
        ]);

        // In production: dispatch async job to Kavenegar / SMS.ir adapter:
        // SendOtpSmsJob::dispatch($mobile, $otpPlain);

        return response()->json([
            'message' => 'کد تایید ۶ رقمی به شماره موبایل شما ارسال شد.',
            'expires_in' => $expiresSeconds,
        ]);
    }

    public function verifyOtp(Request $request)
    {
        $mobile = $this->normalizeDigits((string) $request->input('mobile', ''));
        $otp = $this->normalizeDigits((string) $request->input('otp', ''));
        $request->merge(['mobile' => $mobile, 'otp' => $otp]);

        $validator = Validator::make($request->all(), [
            'mobile' => ['required', 'string', 'regex:/^09[0-9]{9}$/'],
            'otp' => ['required', 'string', 'digits:6'],
        ]);

        if ($validator->fails()) {
            return response()->json(['errors' => $validator->errors()], 422);
        }

        $user = User::where('mobile', $mobile)->first();
        if (!$user || !$user->is_active) {
            return response()->json(['error' => 'کاربر معتبر یافت نشد. لطفا ابتدا درخواست کد تایید دهید.'], 404);
        }

        $otpRecord = OtpCode::where('mobile', $mobile)
            ->whereNull('verified_at')
            ->latest()
            ->first();

        if (!$otpRecord || now()->gt($otpRecord->expires_at)) {
            return response()->json(['error' => 'کد تایید منقضی شده یا نامعتبر است.'], 401);
        }

        if ($otpRecord->attempts >= 5) {
            return response()->json(['error' => 'تعداد تلاش‌های ناموفق بیش از حد مجاز است. کد جدید دریافت کنید.'], 429);
        }

        if (!Hash::check($otp, $otpRecord->code_hash)) {
            $otpRecord->increment('attempts');
            return response()->json(['error' => 'کد تایید وارد شده صحیح نیست.'], 401);
        }

        $otpRecord->update(['verified_at' => now()]);
        $user->update(['mobile_verified_at' => $user->mobile_verified_at ?? now()]);

        $customer = Customer::firstOrCreate(
            ['user_id' => $user->id],
            ['loyalty_points' => 100, 'tier' => 'silver']
        );

        $token = $user->createToken('ASIYE_STORE_CUSTOMER', ['customer'])->plainTextToken;

        return response()->json([
            'access_token' => $token,
            'token_type' => 'Bearer',
            'user' => $user->load('customer'),
            'customer_id' => $customer->id,
        ]);
    }

    public function adminLogin(Request $request)
    {
        $identifier = $this->normalizeDigits((string) $request->input('mobile_or_email', ''));
        $request->merge(['mobile_or_email' => $identifier]);

        $validator = Validator::make($request->all(), [
            'mobile_or_email' => ['required', 'string', 'max:190'],
            'password' => ['required', 'string', 'min:8'],
        ]);

        if ($validator->fails()) {
            return response()->json(['errors' => $validator->errors()], 422);
        }

        $rateKey = 'admin-login:' . $identifier . ':' . $request->ip();
        if (RateLimiter::tooManyAttempts($rateKey, 5)) {
            return response()->json(['error' => 'تلاش‌های ناموفق بیش از حد مجاز. لطفا ۵ دقیقه صبر کنید.'], 429);
        }

        $user = User::with('role.permissions')
            ->where('mobile', $identifier)
            ->orWhere('email', $identifier)
            ->first();

        if (!$user || !$user->password || !Hash::check($request->password, $user->password)) {
            RateLimiter::hit($rateKey, 300);
            return response()->json(['error' => 'مشخصات ورود اشتباه است.'], 401);
        }

        if (!$user->is_active || !$user->role_id) {
            return response()->json(['error' => 'شما دسترسی مدیریتی به پنل ادمین ندارید.'], 403);
        }

        RateLimiter::clear($rateKey);
        $abilities = $user->role->permissions->pluck('slug')->push('admin')->unique()->values()->all();
        $token = $user->createToken('ASIYE_STORE_ADMIN', $abilities)->plainTextToken;

        return response()->json([
            'message' => 'ورود مدیریتی موفقیت‌آمیز بود.',
            'access_token' => $token,
            'token_type' => 'Bearer',
            'user' => $user,
        ]);
    }

    public function me(Request $request)
    {
        return response()->json($request->user()->load(['role.permissions', 'customer.addresses']));
    }

    public function logout(Request $request)
    {
        $request->user()?->currentAccessToken()?->delete();
        return response()->json(['message' => 'خروج با موفقیت انجام شد.']);
    }
}
