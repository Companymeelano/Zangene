package com.asiyestore.admin.app;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * v6.8 · SMS AUTOPILOT.
 *
 * A complete, production-shaped SMS layer for the maison:
 *   • four real Iranian gateways (Kavenegar, Mellipayamak, SMS.ir, Ghasedak) + a sandbox mode
 *   • a durable outbox in SQLite, so a lost signal never loses a message
 *   • per-event templates with {placeholders}, editable by the merchant
 *   • quiet hours (transactional messages always pass), a daily cap, and retries
 *
 * Nothing here blocks the UI: every send runs on a single background executor.
 */
public final class SmsGateway {

    public static final String PROVIDER_KAVENEGAR  = "kavenegar";
    public static final String PROVIDER_MELLI      = "mellipayamak";
    public static final String PROVIDER_SMSIR      = "smsir";
    public static final String PROVIDER_GHASEDAK   = "ghasedak";
    public static final String PROVIDER_SANDBOX    = "sandbox";

    /** Keys that must never be skipped by quiet hours: the customer is waiting for them. */
    private static final String[] TRANSACTIONAL = { "otp", "order_placed", "order_paid", "order_shipped" };

    private static final ExecutorService POOL = Executors.newSingleThreadExecutor();

    private SmsGateway() { }

    // ══════════════════════════════════════════════════════════════════════
    //  TEMPLATE RENDERING
    // ══════════════════════════════════════════════════════════════════════
    /**
     * Fills {tokens} in a template. Unknown tokens are removed rather than shown raw,
     * so a merchant typing {nmae} never ships a broken message.
     */
    public static String render(String template, Map<String, String> vars) {
        if (template == null) return "";
        String out = template;
        if (vars != null) {
            for (Map.Entry<String, String> e : vars.entrySet()) {
                out = out.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
            }
        }
        // strip any leftover placeholder
        return out.replaceAll("\\{[a-zA-Z0-9_]+\\}", "").replaceAll("[ ]{2,}", " ").trim();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PUBLIC ENTRY POINTS
    // ══════════════════════════════════════════════════════════════════════
    /**
     * Enqueues a message from a named template and flushes the queue in the background.
     * Safe to call from the UI thread; never throws.
     */
    public static void notify(final Context context, final AsiyeDatabaseHelper db,
                              final String mobile, final String templateKey,
                              final Map<String, String> vars) {
        if (context == null || db == null || mobile == null) return;
        try {
            if (!isEnabled(db)) return;
            final String body = buildBody(db, templateKey, vars);
            if (body.isEmpty()) return;
            db.enqueueSms(mobile, templateKey, body);
            flushAsync(context, db);
        } catch (Throwable ignored) { }
    }

    /** Enqueues without checking the enable switch — used by the "test message" button. */
    public static String enqueueTest(AsiyeDatabaseHelper db, String mobile, String body) {
        if (db == null || mobile == null || mobile.trim().isEmpty()) return "شماره تست خالی است.";
        db.enqueueSms(mobile.trim(), "manual_test", body);
        return null;
    }

    public static boolean isEnabled(AsiyeDatabaseHelper db) {
        return db.getSettingBool("sms_enabled", true);
    }

    public static String providerLabel(String provider) {
        if (PROVIDER_KAVENEGAR.equals(provider)) return "کاوه‌نگار";
        if (PROVIDER_MELLI.equals(provider)) return "ملی پیامک";
        if (PROVIDER_SMSIR.equals(provider)) return "sms.ir";
        if (PROVIDER_GHASEDAK.equals(provider)) return "قاصدک";
        return "حالت آزمایشی (بدون ارسال)";
    }

    /** The body a template would produce right now — used by the settings preview. */
    public static String preview(AsiyeDatabaseHelper db, String key) {
        Map<String, String> demo = demoVars();
        String stored = db.getTemplateBody(key);
        String template = stored == null || stored.trim().isEmpty() ? defaultTemplate(key) : stored;
        return render(template, demo);
    }

    public static Map<String, String> demoVars() {
        Map<String, String> v = new HashMap<>();
        v.put("name", "سارا");
        v.put("store", "اسیه استور");
        v.put("owner", "خانم زنگنه");
        v.put("order", "AS-1405-0142");
        v.put("total", "۲٬۳۴۰٬۰۰۰ تومان");
        v.put("tracking", "۲۴۰۱۲۳۴۵۶۷۸۹۰۱۲۳۴۵۶۷۸");
        v.put("product", "کرم ضدلک گاتیو");
        v.put("days", "۴۵");
        v.put("code", "۸۴۲۱۹");
        v.put("coupon", "ASIYE20");
        v.put("percent", "۲۰");
        v.put("count", "۳");
        return v;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TEMPLATE LIBRARY — the merchant's own voice, pre-written
    // ══════════════════════════════════════════════════════════════════════
    public static String defaultTemplate(String key) {
        if ("otp".equals(key)) {
            return "اسیه استور\nکد ورود شما: {code}\nاین کد ۲ دقیقه معتبر است. آن را با کسی در میان نگذارید.";
        }
        if ("welcome".equals(key)) {
            return "{name} عزیز، به خانهٔ زیبایی اسیه خوش آمدید.\nحساب شما ساخته شد و از این پس سفارش‌ها، گواهی اصالت و مشاوره‌هایتان یک‌جا در دسترس است.\n{store} · مدیریت: {owner}";
        }
        if ("order_placed".equals(key)) {
            return "{name} عزیز، سفارش {order} با مبلغ {total} ثبت شد.\nبزودی توسط {owner} بررسی و آماده‌سازی می‌شود. سپاس از اعتماد شما.\n{store}";
        }
        if ("order_paid".equals(key)) {
            return "{name} عزیز، پرداخت سفارش {order} تأیید شد.\nبستهٔ شما با کاغذ کتان و مهر موم طلایی آماده می‌شود.\n{store}";
        }
        if ("order_shipped".equals(key)) {
            return "{name} عزیز، سفارش {order} تحویل پست شد.\nکد رهگیری: {tracking}\nاز این پس می‌توانید مسیر بسته را دنبال کنید.\n{store}";
        }
        if ("order_delivered".equals(key)) {
            return "{name} عزیز، بستهٔ {order} تحویل شد.\nاگر از خرید راضی هستید، نظر شما با نشان «خرید تأییدشده» در ویترین منتشر می‌شود.\n{store}";
        }
        if ("review_approved".equals(key)) {
            return "{name} عزیز، نظر شما منتشر شد و نشان «خرید تأییدشده» گرفت. سپاس از همراهی شما.\n{store}";
        }
        if ("reorder_reminder".equals(key)) {
            return "{name} عزیز، {days} روز از خرید {product} گذشته است.\nاگر محصول در حال پایان است، همین حالا سفارش مجدد خود را ثبت کنید تا در صف اولویت قرار بگیرد.\n{store}";
        }
        if ("birthday".equals(key)) {
            return "{name} عزیز، تولدتان مبارک.\nبه همین مناسبت کد {coupon} با {percent}٪ تخفیف تا پایان هفته برای شما فعال شد.\n{store} · {owner}";
        }
        if ("back_in_stock".equals(key)) {
            return "{name} عزیز، {product} دوباره موجود شد.\nتعداد محدود است؛ در صورت تمایل سریع‌تر ثبت کنید.\n{store}";
        }
        if ("cart_abandoned".equals(key)) {
            return "{name} عزیز، انتخاب‌های شما هنوز در سبدتان منتظر است.\nاگر سؤالی دربارهٔ ترکیبات یا مناسب‌بودن محصول دارید، همین پیام را پاسخ دهید.\n{store}";
        }
        if ("coupon_gift".equals(key)) {
            return "{name} عزیز، به پاس همراهی شما کد {coupon} با {percent}٪ تخفیف فعال شد.\n{store}";
        }
        if ("owner_alert".equals(key)) {
            return "مدیریت اسیه: سفارش تازه {order} از {name} به مبلغ {total} ثبت شد.";
        }
        if ("owner_daily".equals(key)) {
            return "گزارش روزانه اسیه: {count} سفارش تازه، موجودی نیازمند بررسی. جزئیات در پنل مدیریت.";
        }
        return "";
    }

    public static String defaultTitle(String key) {
        if ("otp".equals(key)) return "کد ورود یک‌بارمصرف";
        if ("welcome".equals(key)) return "خوش‌آمدگویی پس از ثبت‌نام";
        if ("order_placed".equals(key)) return "ثبت سفارش";
        if ("order_paid".equals(key)) return "تأیید پرداخت";
        if ("order_shipped".equals(key)) return "ارسال سفارش + کد رهگیری";
        if ("order_delivered".equals(key)) return "تحویل سفارش + دعوت به نظر";
        if ("review_approved".equals(key)) return "تأیید و انتشار نظر";
        if ("reorder_reminder".equals(key)) return "یادآور سفارش مجدد";
        if ("birthday".equals(key)) return "تبریک تولد + هدیه";
        if ("back_in_stock".equals(key)) return "بازگشت موجودی";
        if ("cart_abandoned".equals(key)) return "سبد خرید رهاشده";
        if ("coupon_gift".equals(key)) return "هدیهٔ کوپن اختصاصی";
        if ("owner_alert".equals(key)) return "اطلاع فوری به مدیریت";
        if ("owner_daily".equals(key)) return "گزارش روزانه به مدیریت";
        return key;
    }

    public static String[] allKeys() {
        return new String[]{ "otp", "welcome", "order_placed", "order_paid", "order_shipped",
                "order_delivered", "review_approved", "reorder_reminder", "birthday",
                "back_in_stock", "cart_abandoned", "coupon_gift", "owner_alert", "owner_daily" };
    }

    // ══════════════════════════════════════════════════════════════════════
    //  QUEUE
    // ══════════════════════════════════════════════════════════════════════
    public static void flushAsync(final Context context, final AsiyeDatabaseHelper db) {
        POOL.execute(new Runnable() {
            @Override public void run() {
                try { flush(context.getApplicationContext(), db); } catch (Throwable ignored) { }
            }
        });
    }

    /**
     * Sends everything pending, oldest first, respecting quiet hours and the daily cap.
     * @return number of messages actually delivered.
     */
    public static int flush(Context context, AsiyeDatabaseHelper db) {
        if (!isEnabled(db)) return 0;

        int cap = safeInt(db.getSetting("sms_daily_cap", "80"), 80);
        int sentToday = db.countSmsSentToday();
        if (sentToday >= cap) return 0;

        int delivered = 0;
        List<Models.SmsMessage> queue = db.pendingSms(40);
        for (Models.SmsMessage msg : queue) {
            if (sentToday >= cap) break;
            if (!isTransactional(msg.templateKey) && inQuietHours(db)) break;

            String error = deliver(context, db, msg.mobile, msg.body);
            if (error == null) {
                db.markSmsSent(msg.id);
                delivered++;
                sentToday++;
            } else {
                db.markSmsFailed(msg.id, error);
            }
        }
        return delivered;
    }

    private static boolean inQuietHours(AsiyeDatabaseHelper db) {
        int from = safeInt(db.getSetting("sms_quiet_from", "23"), 23);
        int to = safeInt(db.getSetting("sms_quiet_to", "8"), 8);
        if (from == to) return false;
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (from < to) return h >= from && h < to;
        return h >= from || h < to;
    }

    private static boolean isTransactional(String key) {
        if (key == null) return false;
        for (String t : TRANSACTIONAL) if (t.equals(key)) return true;
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PROVIDER TRANSPORT
    // ══════════════════════════════════════════════════════════════════════
    /** @return null on success, a short Persian error on failure. */
    private static String deliver(Context context, AsiyeDatabaseHelper db, String mobile, String body) {
        String provider = db.getSetting("sms_provider", PROVIDER_SANDBOX);
        String apiKey = db.getSetting("sms_api_key", "").trim();
        String sender = db.getSetting("sms_sender", "").trim();
        String user = db.getSetting("sms_username", "").trim();
        String pass = db.getSetting("sms_password", "").trim();

        if (PROVIDER_SANDBOX.equals(provider) || provider.isEmpty()) {
            // No gateway configured: the outbox still records the message, nothing leaves the device.
            return null;
        }
        if (apiKey.isEmpty() && !PROVIDER_MELLI.equals(provider)) return "کلید API تنظیم نشده است";

        try {
            if (PROVIDER_KAVENEGAR.equals(provider)) {
                String url = "https://api.kavenegar.com/v1/" + enc(apiKey) + "/sms/send.json"
                        + "?receptor=" + enc(normalizeMobile(mobile))
                        + "&sender=" + enc(sender)
                        + "&message=" + enc(body);
                return httpGet(url, null, null) ? null : "کاوه‌نگار پاسخ موفق نداد";
            }
            if (PROVIDER_MELLI.equals(provider)) {
                if (user.isEmpty() || pass.isEmpty()) return "نام کاربری یا رمز ملی‌پیامک خالی است";
                String json = "{\"username\":\"" + esc(user) + "\",\"password\":\"" + esc(pass)
                        + "\",\"to\":\"" + esc(normalizeMobile(mobile)) + "\",\"from\":\"" + esc(sender)
                        + "\",\"text\":\"" + esc(body) + "\",\"isflash\":false}";
                return httpPost("https://rest.payamak-panel.com/api/SendSMS/SendSMS",
                        json, "application/json", null) ? null : "ملی‌پیامک پاسخ موفق نداد";
            }
            if (PROVIDER_SMSIR.equals(provider)) {
                String json = "{\"lineNumber\":\"" + esc(sender) + "\",\"messageText\":\"" + esc(body)
                        + "\",\"mobiles\":[\"" + esc(normalizeMobile(mobile)) + "\"]}";
                return httpPost("https://api.sms.ir/v1/send/bulk", json, "application/json",
                        new String[]{ "x-api-key", apiKey }) ? null : "sms.ir پاسخ موفق نداد";
            }
            if (PROVIDER_GHASEDAK.equals(provider)) {
                String form = "receptor=" + enc(normalizeMobile(mobile))
                        + "&message=" + enc(body)
                        + "&linenumber=" + enc(sender);
                return httpPost("https://api.ghasedak.me/v2/sms/send/simple", form,
                        "application/x-www-form-urlencoded", new String[]{ "apikey", apiKey })
                        ? null : "قاصدک پاسخ موفق نداد";
            }
            return "درگاه ناشناخته: " + provider;
        } catch (Throwable t) {
            return "خطای شبکه: " + (t.getMessage() == null ? "نامشخص" : t.getMessage());
        }
    }

    /** Iranian numbers are sent as 09xxxxxxxxx; anything else is passed through. */
    public static String normalizeMobile(String raw) {
        String m = PersianFormatter.toAsciiDigits(raw == null ? "" : raw).replaceAll("[^0-9+]", "");
        if (m.startsWith("+98")) m = "0" + m.substring(3);
        if (m.startsWith("0098")) m = "0" + m.substring(4);
        if (m.startsWith("98") && m.length() == 12) m = "0" + m.substring(2);
        if (m.length() == 10 && m.startsWith("9")) m = "0" + m;
        return m;
    }

    public static boolean isValidMobile(String raw) {
        return normalizeMobile(raw).matches("^09\\d{9}$");
    }

    public static String maskedMobile(String raw) {
        String m = normalizeMobile(raw);
        if (m.length() < 7) return m;
        return m.substring(0, 4) + "***" + m.substring(m.length() - 3);
    }

    private static boolean httpGet(String url, String headerName, String headerValue) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(9000);
            conn.setReadTimeout(9000);
            conn.setRequestProperty("Accept", "application/json");
            if (headerName != null) conn.setRequestProperty(headerName, headerValue);
            int code = conn.getResponseCode();
            drain(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
            return code >= 200 && code < 300;
        } finally {
            conn.disconnect();
        }
    }

    private static boolean httpPost(String url, String body, String contentType, String[] header) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(9000);
            conn.setReadTimeout(9000);
            conn.setRequestProperty("Content-Type", contentType);
            conn.setRequestProperty("Accept", "application/json");
            if (header != null && header.length == 2) conn.setRequestProperty(header[0], header[1]);

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            drain(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
            return code >= 200 && code < 300;
        } finally {
            conn.disconnect();
        }
    }

    private static void drain(InputStream is) {
        if (is == null) return;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            while (r.readLine() != null) { /* consume the body */ }
        } catch (Exception ignored) { }
    }

    private static String enc(String v) {
        try { return URLEncoder.encode(v == null ? "" : v, "UTF-8"); }
        catch (Exception e) { return ""; }
    }

    private static String esc(String v) {
        if (v == null) return "";
        return v.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }

    private static int safeInt(String raw, int fallback) {
        try { return Integer.parseInt(raw.trim()); } catch (Exception e) { return fallback; }
    }

    public static String nowLabel() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
    }

    private static String buildBody(AsiyeDatabaseHelper db, String key, Map<String, String> vars) {
        if (!db.isTemplateEnabled(key)) return "";
        String stored = db.getTemplateBody(key);
        String template = stored == null || stored.trim().isEmpty() ? defaultTemplate(key) : stored;
        return render(template, vars);
    }
}
