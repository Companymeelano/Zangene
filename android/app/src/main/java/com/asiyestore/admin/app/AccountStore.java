package com.asiyestore.admin.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

/**
 * v6.8 · account identities for the maison.
 *
 * Mobile is the identity, a 5-digit PIN is the key, and the PIN is never stored in clear:
 * a per-account random salt plus SHA-256 keeps the file readable-by-nobody.
 * The session lives in SharedPreferences so a returning customer lands straight in her account.
 */
public final class AccountStore {

    public static final String PREF = "asiye_account";
    private static final String KEY_MOBILE = "session_mobile";

    private AccountStore() { }

    // ── session ────────────────────────────────────────────────────────────
    public static String sessionMobile(Context ctx) {
        return prefs(ctx).getString(KEY_MOBILE, "");
    }

    public static boolean isSignedIn(Context ctx) {
        String m = sessionMobile(ctx);
        return m != null && !m.isEmpty();
    }

    public static void signIn(Context ctx, String mobile) {
        prefs(ctx).edit().putString(KEY_MOBILE, SmsGateway.normalizeMobile(mobile)).apply();
    }

    public static void signOut(Context ctx) {
        prefs(ctx).edit().remove(KEY_MOBILE).apply();
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // ── hashing ────────────────────────────────────────────────────────────
    public static String newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return hex(salt);
    }

    public static String hashPin(String pin, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((salt == null ? "" : salt).getBytes("UTF-8"));
            return hex(md.digest(pin.getBytes("UTF-8")));
        } catch (Exception e) {
            return "";
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.US, "%02x", b));
        }
        return sb.toString();
    }

    // ── validation ─────────────────────────────────────────────────────────
    public static boolean isValidPin(String pin) {
        String p = PersianFormatter.toAsciiDigits(pin == null ? "" : pin).trim();
        return p.matches("^\\d{5,8}$");
    }

    public static String normalizePin(String pin) {
        return PersianFormatter.toAsciiDigits(pin == null ? "" : pin).trim();
    }

    /**
     * Creates the account after the OTP proved ownership of the number.
     * @return null on success, a Persian error otherwise.
     */
    public static String register(Context ctx, AsiyeDatabaseHelper db, String mobile, String name,
                                  String pin, String skinType, String birthday, boolean smsOptin) {
        String m = SmsGateway.normalizeMobile(mobile);
        if (!SmsGateway.isValidMobile(m)) return "شمارهٔ موبایل معتبر نیست.";
        if (name == null || name.trim().length() < 3) return "نام و نام خانوادگی را کامل بنویسید.";
        if (!isValidPin(pin)) return "رمز باید ۵ تا ۸ رقم باشد.";
        if (db.accountExists(m)) return "این شماره قبلاً ثبت‌نام کرده است؛ وارد شوید.";

        String salt = newSalt();
        long id = db.createAccount(m, name, hashPin(normalizePin(pin), salt), salt,
                skinType, birthday, smsOptin);
        if (id < 0) return "ساخت حساب ممکن نشد. دوباره تلاش کنید.";

        signIn(ctx, m);
        return null;
    }

    /** @return null on success, a Persian error otherwise. */
    public static String login(Context ctx, AsiyeDatabaseHelper db, String mobile, String pin) {
        String m = SmsGateway.normalizeMobile(mobile);
        if (!SmsGateway.isValidMobile(m)) return "شمارهٔ موبایل معتبر نیست.";
        Models.AccountRecord acc = db.getAccount(m);
        if (acc == null) return "حسابی با این شماره نداریم؛ ابتدا ثبت‌نام کنید.";

        String salt = db.getAccountSalt(m);
        String expected = db.getAccountHash(m);
        String actual = hashPin(normalizePin(pin), salt);
        if (expected == null || !expected.equals(actual)) return "رمز وارد شده درست نیست.";

        db.touchAccount(m);
        signIn(ctx, m);
        return null;
    }

    /** Replaces the PIN after a verified OTP. */
    public static String resetPin(AsiyeDatabaseHelper db, String mobile, String pin) {
        if (!isValidPin(pin)) return "رمز باید ۵ تا ۸ رقم باشد.";
        String salt = newSalt();
        db.updateAccountSecrets(SmsGateway.normalizeMobile(mobile), hashPin(normalizePin(pin), salt), salt);
        return null;
    }

    /** A friendly greeting that never shows an empty field. */
    public static String displayName(Models.AccountRecord acc) {
        if (acc == null || acc.name == null || acc.name.trim().isEmpty()) return "مشتری گرامی اسیه";
        return acc.name.trim();
    }
}
