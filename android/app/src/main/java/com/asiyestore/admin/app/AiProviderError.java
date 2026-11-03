package com.asiyestore.admin.app;

/** Safe diagnostic messages; do not include credentials or provider response bodies. */
public final class AiProviderError {
    private AiProviderError() { }
    public static String message(String provider, int status, String kind) {
        String label = AiProviderOrder.label(provider);
        String reason;
        switch (status) {
            case 400: reason = "درخواست یا عکس پذیرفته نشد (HTTP 400)."; break;
            case 402: reason = "پرداخت یا اعتبار کافی در دسترس نیست؛ موجودی، سقف کلید و صورتحساب را در پنل بررسی کنید (HTTP 402)."; break;
            case 401: reason = "احراز هویت رد شد؛ اعتبار کلید را در پنل سرویس بررسی کنید (HTTP 401)."; break;
            case 403: reason = "دسترسی رد شد؛ مجوز پروژه، کلید و محدودیت منطقه را بررسی کنید (HTTP 403)."; break;
            case 404: reason = "مدل یا مسیر در این حساب در دسترس نیست (HTTP 404)."; break;
            case 413: reason = "حجم عکس بیش از حد مجاز سرویس است (HTTP 413)."; break;
            case 429: reason = "محدودیت درخواست یا سهمیه؛ وضعیت حساب را بررسی کنید (HTTP 429)."; break;
            default:
                if (status >= 500) reason = "سرویس ارائه‌دهنده در دسترس نبود (HTTP " + status + ").";
                else if (status > 0) reason = "پاسخ سرویس HTTP " + status + ".";
                else if ("missing".equals(kind)) reason = "کلید این سرویس در بسته موجود نیست.";
                else if ("timeout".equals(kind)) reason = "زمان اتصال یا پاسخ به پایان رسید.";
                else if ("network".equals(kind)) reason = "اتصال شبکه یا دسترسی به سرویس برقرار نشد.";
                else reason = "پاسخ سرویس قابل پردازش نبود؛ ممکن است مدل یا قالب خروجی ناسازگار باشد.";
        }
        return label + " — " + reason;
    }
}
