<?php

namespace App\Services\Sms;

/**
 * v6.8 · the maison's message library.
 *
 * Every template is written in the shop's own voice and uses {tokens} that the
 * SmsService fills from the event payload. Merchants can override any of them
 * from the admin panel; the database copy always wins over these defaults.
 */
class SmsTemplates
{
    public const DEFAULTS = [
        'otp' => "اسیه استور\nکد ورود شما: {code}\nاین کد ۲ دقیقه معتبر است. آن را با کسی در میان نگذارید.",

        'welcome' => "{name} عزیز، به خانهٔ زیبایی اسیه خوش آمدید.\n"
            . "حساب شما ساخته شد و از این پس سفارش‌ها، گواهی اصالت و مشاوره‌هایتان یک‌جا در دسترس است.\n"
            . "{store} · مدیریت: {owner}",

        'order_placed' => "{name} عزیز، سفارش {order} با مبلغ {total} ثبت شد.\n"
            . "به‌زودی توسط {owner} بررسی و آماده‌سازی می‌شود. سپاس از اعتماد شما.\n{store}",

        'order_paid' => "{name} عزیز، پرداخت سفارش {order} تأیید شد.\n"
            . "بستهٔ شما با کاغذ کتان و مهر موم طلایی آماده می‌شود.\n{store}",

        'order_shipped' => "{name} عزیز، سفارش {order} تحویل پست شد.\n"
            . "کد رهگیری: {tracking}\nاز این پس می‌توانید مسیر بسته را دنبال کنید.\n{store}",

        'order_delivered' => "{name} عزیز، بستهٔ {order} تحویل شد.\n"
            . "اگر از خرید راضی هستید، نظر شما با نشان «خرید تأییدشده» در ویترین منتشر می‌شود.\n{store}",

        'review_approved' => "{name} عزیز، نظر شما منتشر شد و نشان «خرید تأییدشده» گرفت. سپاس از همراهی شما.\n{store}",

        'reorder_reminder' => "{name} عزیز، {days} روز از خرید {product} گذشته است.\n"
            . "اگر محصول در حال پایان است، همین حالا سفارش مجدد خود را ثبت کنید تا در صف اولویت قرار بگیرد.\n{store}",

        'birthday' => "{name} عزیز، تولدتان مبارک.\n"
            . "به همین مناسبت کد {coupon} با {percent}٪ تخفیف تا پایان هفته برای شما فعال شد.\n{store} · {owner}",

        'back_in_stock' => "{name} عزیز، {product} دوباره موجود شد.\n"
            . "تعداد محدود است؛ در صورت تمایل سریع‌تر ثبت کنید.\n{store}",

        'cart_abandoned' => "{name} عزیز، انتخاب‌های شما هنوز در سبدتان منتظر است.\n"
            . "اگر سؤالی دربارهٔ ترکیبات یا مناسب‌بودن محصول دارید، همین پیام را پاسخ دهید.\n{store}",

        'coupon_gift' => "{name} عزیز، به پاس همراهی شما کد {coupon} با {percent}٪ تخفیف فعال شد.\n{store}",

        'owner_alert' => "مدیریت اسیه: سفارش تازه {order} از {name} به مبلغ {total} ثبت شد.",

        'owner_daily' => "گزارش روزانه اسیه: {count} سفارش تازه، موجودی نیازمند بررسی. جزئیات در پنل مدیریت.",
    ];

    /** Transactional messages ignore quiet hours — the customer is waiting for them. */
    public const TRANSACTIONAL = ['otp', 'order_placed', 'order_paid', 'order_shipped'];

    public static function default(string $key): string
    {
        return self::DEFAULTS[$key] ?? '';
    }

    public static function render(string $template, array $vars): string
    {
        $out = $template;
        foreach ($vars as $k => $v) {
            $out = str_replace('{' . $k . '}', (string) $v, $out);
        }

        // never ship a half-filled message
        $out = preg_replace('/\{[a-zA-Z0-9_]+\}/', '', $out);

        return trim(preg_replace('/ {2,}/', ' ', $out));
    }
}
