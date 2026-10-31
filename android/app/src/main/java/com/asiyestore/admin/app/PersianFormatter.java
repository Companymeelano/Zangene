package com.asiyestore.admin.app;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class PersianFormatter {

    private static final char[] PERSIAN_DIGITS = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};

    private PersianFormatter() {}

    public static String toPersianDigits(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(PERSIAN_DIGITS[c - '0']);
            } else if (c >= '٠' && c <= '٩') {
                sb.append(PERSIAN_DIGITS[c - '٠']);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String toAsciiDigits(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= '۰' && c <= '۹') {
                sb.append((char) ('0' + (c - '۰')));
            } else if (c >= '٠' && c <= '٩') {
                sb.append((char) ('0' + (c - '٠')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    public static String formatToman(long amount) {
        long safe = Math.max(0L, amount);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return toPersianDigits(df.format(safe)) + " تومان";
    }

    public static String formatCompactMillions(long amount) {
        if (amount >= 1_000_000L) {
            double m = amount / 1_000_000.0;
            String formatted = String.format(Locale.US, "%.1f", m);
            if (formatted.endsWith(".0")) {
                formatted = formatted.substring(0, formatted.length() - 2);
            }
            return toPersianDigits(formatted) + " میلیون تومان";
        }
        return formatToman(amount);
    }

    public static boolean isValidIranMobile(String raw) {
        String clean = toAsciiDigits(raw);
        return clean.matches("^09\\d{9}$");
    }

    public static String statusLabelFa(String status) {
        if (status == null) return "نامشخص";
        switch (status) {
            case "pending_payment": return "در انتظار پرداخت";
            case "paid": return "پرداخت شده";
            case "confirmed": return "تایید کارشناس";
            case "preparing": return "در حال بسته‌بندی VIP";
            case "shipped": return "تحویل به پیک / پست";
            case "delivered": return "تحویل شده به مشتری";
            case "cancelled": return "لغو شده (بازگشت انبار)";
            case "returned": return "مرجوع شده";
            default: return status;
        }
    }

    public static int statusColor(String status) {
        if (status == null) return 0xFF78716C;
        switch (status) {
            case "pending_payment": return 0xFFB45309;
            case "paid":
            case "confirmed": return 0xFF1D4ED8;
            case "preparing": return 0xFF9A6B2F;
            case "shipped": return 0xFF6D28D9;
            case "delivered": return 0xFF15803D;
            case "cancelled":
            case "returned": return 0xFFB91C1C;
            default: return 0xFF57534E;
        }
    }

    public static String paymentMethodLabelFa(String code) {
        if (code == null) return "آنلاین";
        switch (code) {
            case "zarinpal": return "درگاه امن زرین‌پال";
            case "direct_bank": return "درگاه مستقیم شاپرک";
            case "snapp_pay": return "اسنپ‌پی (۴ قسط بدون کارمزد)";
            case "tara": return "اعتبار خرید اقساطی تارا";
            case "digipay": return "دیجی‌پی (اعتبار هوشمند)";
            case "azkivam": return "ازکی‌وام (کلکسیونی)";
            default: return code;
        }
    }
}
