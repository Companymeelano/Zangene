package com.asiyestore.admin.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * v6.8 · THE AUTOPILOT.
 *
 * Everything the owner would otherwise have to remember, done on a schedule:
 *   1. flush the SMS outbox (retries survive a dead connection)
 *   2. remind customers whose product is about to run out
 *   3. greet birthdays with a live coupon
 *   4. ask for a review once a parcel is delivered
 *   5. warn the owner when stock runs low, and send the morning digest
 *
 * Runs from a single inexact alarm at 10:00, plus an early pass shortly after install,
 * so nothing depends on the merchant opening the app.
 */
public final class AutomationReceiver extends BroadcastReceiver {

    public static final String ACTION_ROUTINE = "com.asiyestore.admin.ROUTINE";
    private static final int REQ = 6108;

    @Override
    public void onReceive(Context context, Intent intent) {
        AsiyeDatabaseHelper db = null;
        try {
            db = new AsiyeDatabaseHelper(context.getApplicationContext());
            SmsGateway.flush(context.getApplicationContext(), db);
            runDailyRoutine(context.getApplicationContext(), db);
        } catch (Throwable ignored) {
            // an automation must never crash the app that hosts it
        } finally {
            if (db != null) {
                try { db.close(); } catch (Throwable ignored) { }
            }
        }
    }

    /** Idempotent: calling it on every launch never stacks duplicate alarms. */
    public static void schedule(Context context) {
        try {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;

            Intent intent = new Intent(context, AutomationReceiver.class);
            intent.setAction(ACTION_ROUTINE);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
            PendingIntent pi = PendingIntent.getBroadcast(context, REQ, intent, flags);

            Calendar at = Calendar.getInstance();
            at.set(Calendar.HOUR_OF_DAY, 10);
            at.set(Calendar.MINUTE, 0);
            at.set(Calendar.SECOND, 0);
            if (at.getTimeInMillis() <= System.currentTimeMillis()) {
                at.add(Calendar.DAY_OF_YEAR, 1);
            }
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, at.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);
        } catch (Throwable ignored) { }
    }

    /**
     * The routine itself. Every step is individually guarded, so one failing query
     * cannot stop the rest of the day's work.
     */
    public static void runDailyRoutine(Context ctx, AsiyeDatabaseHelper db) {
        // 1 · replenishment reminders
        try {
            if (db.getSettingBool("auto_reorder", true)) {
                List<Models.ReorderDue> due = db.getReorderDue(8);
                for (Models.ReorderDue r : due) {
                    if (r.mobile == null || r.mobile.isEmpty()) continue;
                    if (!wasSentRecently(db, "reorder_reminder", r.mobile)) {
                        Map<String, String> v = baseVars(r.name);
                        v.put("product", r.productName);
                        v.put("days", PersianFormatter.toPersianDigits(String.valueOf(Math.max(1, r.days))));
                        send(ctx, db, r.mobile, "reorder_reminder", v);
                    }
                }
            }
        } catch (Throwable ignored) { }

        // 2 · birthdays, with a live coupon
        try {
            if (db.getSettingBool("auto_birthday", true)) {
                for (Models.AccountRecord a : db.getBirthdaysToday()) {
                    if (!a.smsOptin) continue;
                    Map<String, String> v = baseVars(a.name);
                    v.put("coupon", "BIRTHDAY20");
                    v.put("percent", PersianFormatter.toPersianDigits("20"));
                    send(ctx, db, a.mobile, "birthday", v);
                }
            }
        } catch (Throwable ignored) { }

        // 3 · review invitations for delivered parcels
        try {
            if (db.getSettingBool("auto_review_request", true)) {
                for (Models.OrderRecord o : db.getDeliveredAwaitingReview(5)) {
                    if (!wasSentRecently(db, "order_delivered", o.customerMobile)) {
                        Map<String, String> v = baseVars(o.customerName);
                        v.put("order", o.orderNumber);
                        send(ctx, db, o.customerMobile, "order_delivered", v);
                    }
                }
            }
        } catch (Throwable ignored) { }

        // 4 · the owner's morning digest
        try {
            if (db.getSettingBool("auto_owner_alert", true)) {
                String owner = db.getSetting("owner_mobile", "09044200993");
                if (owner != null && !owner.trim().isEmpty()) {
                    Map<String, String> v = baseVars("مدیریت");
                    v.put("count", PersianFormatter.toPersianDigits(String.valueOf(db.getTodayOrdersCount())));
                    send(ctx, db, owner, "owner_daily", v);
                }
            }
        } catch (Throwable ignored) { }

        // 5 · low stock, told once a day to the owner
        try {
            List<Models.Product> low = db.getLowStockProducts(3);
            if (!low.isEmpty()) {
                String owner = db.getSetting("owner_mobile", "09044200993");
                StringBuilder sb = new StringBuilder("مدیریت اسیه: ");
                for (int i = 0; i < low.size(); i++) {
                    if (i > 0) sb.append(" · ");
                    sb.append(low.get(i).nameFa)
                            .append(" (").append(PersianFormatter.toPersianDigits(String.valueOf(low.get(i).stock))).append(")");
                }
                sb.append(" در آستانهٔ اتمام موجودی است.");
                db.enqueueSms(owner, "owner_alert", sb.toString());
            }
        } catch (Throwable ignored) { }

        // 6 · deliver everything queued above
        try {
            SmsGateway.flush(ctx, db);
        } catch (Throwable ignored) { }
    }

    private static void send(Context ctx, AsiyeDatabaseHelper db, String mobile,
                             String templateKey, Map<String, String> vars) {
        SmsGateway.notify(ctx, db, mobile, templateKey, vars);
    }

    private static Map<String, String> baseVars(String name) {
        Map<String, String> v = new HashMap<>();
        v.put("name", name == null || name.trim().isEmpty() ? "مشتری گرامی" : name.trim());
        v.put("store", "اسیه استور");
        v.put("owner", "خانم زنگنه");
        return v;
    }

    /** A quiet anti-spam guard: the same message type is never repeated within 20 hours. */
    private static boolean wasSentRecently(AsiyeDatabaseHelper db, String templateKey, String mobile) {
        try {
            return db.wasSmsSentWithin(templateKey, mobile, 20L * 60 * 60 * 1000);
        } catch (Throwable t) {
            return false;
        }
    }
}
