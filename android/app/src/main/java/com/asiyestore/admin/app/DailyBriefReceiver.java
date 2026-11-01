package com.asiyestore.admin.app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.asiyestore.admin.R;

import java.util.Calendar;

/**
 * v6.4 — the boutique's daily briefing.
 * Fires every morning at 09:00 with yesterday's revenue, the review queue and the stock alarm.
 */
public class DailyBriefReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "asiye_daily_brief";
    public static final int NOTIFICATION_ID = 6401;
    private static final int REQUEST_CODE = 6402;

    @Override
    @SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent) {
        AsiyeDatabaseHelper db = new AsiyeDatabaseHelper(context);
        long now = System.currentTimeMillis();
        long revenue = db.getRevenueBetween(now - 86_400_000L, now);
        int orders = db.getOrderCountBetween(now - 86_400_000L, now);
        int pending = db.getPendingReviewsCount();
        int lowStock = db.getDashboardSnapshot().lowStockCount;
        db.close();

        String title = "خلاصهٔ صبح اسیه استور";
        String body = "فروش ۲۴ ساعت: " + PersianFormatter.formatToman(revenue)
                + " · سفارش: " + PersianFormatter.toPersianDigits(String.valueOf(orders))
                + " · نظر در انتظار: " + PersianFormatter.toPersianDigits(String.valueOf(pending))
                + " · کم‌موجود: " + PersianFormatter.toPersianDigits(String.valueOf(lowStock));

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "خلاصهٔ روزانه مدیریت", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("گزارش کوتاه فروش، نظرات و موجودی برای مدیریت مجموعه");
            manager.createNotificationChannel(channel);
        }

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent tap = PendingIntent.getActivity(context, REQUEST_CODE, open, flags);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);
        Notification notification = builder
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentIntent(tap)
                .build();

        manager.notify(NOTIFICATION_ID, notification);
    }

    /** Re-arms the 09:00 briefing; safe to call on every launch. */
    public static void schedule(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm == null) return;

        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, 9);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pending = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(context, DailyBriefReceiver.class), flags);

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pending);
    }

    public static void cancel(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm == null) return;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pending = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(context, DailyBriefReceiver.class), flags);
        alarm.cancel(pending);
    }
}
