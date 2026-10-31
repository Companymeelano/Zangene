package com.asiyestore.admin.app;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Live Backend Sync Engine — connects the Android application directly to the Laravel v1 REST API
 * (/api/v1/products & /api/v1/admin/dashboard/stats) whenever the merchant configures their server URL.
 */
public final class ApiSyncClient {

    public interface SyncCallback {
        void onComplete(boolean success, String message, int syncedCount);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void syncCatalogFromBackend(final String rawBaseUrl, final String bearerToken, final AsiyeDatabaseHelper db, final SyncCallback callback) {
        executor.execute(() -> {
            if (rawBaseUrl == null || rawBaseUrl.trim().isEmpty()) {
                post(callback, false, "آدرس سرور خالی است. اپ در حالت پایگاه‌داده آفلاین محلی فعال است.", 0);
                return;
            }
            String base = rawBaseUrl.trim();
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            String endpoint = base.endsWith("/v1") ? base + "/products" : base + "/api/v1/products";

            HttpURLConnection conn = null;
            try {
                URL url = new URL(endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("Accept", "application/json");
                if (bearerToken != null && !bearerToken.trim().isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + bearerToken.trim());
                }

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                if (is != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    }
                }

                if (code < 200 || code >= 300) {
                    post(callback, false, "پاسخ سرور: HTTP " + code + " — حالت آفلاین محلی حفظ شد.", 0);
                    return;
                }

                JSONObject root = new JSONObject(sb.toString());
                JSONArray data = root.optJSONArray("data");
                if (data == null) {
                    post(callback, true, "اتصال به سرور برقرار شد (کاتالوگ خالی).", 0);
                    return;
                }

                int upserted = 0;
                for (int i = 0; i < data.length(); i++) {
                    JSONObject item = data.optJSONObject(i);
                    if (item == null) continue;
                    String sku = item.optString("sku", "");
                    String nameFa = item.optString("name_fa", "");
                    String nameEn = item.optString("name_en", "ASIYE Product");
                    long sale = Math.round(item.optDouble("sale_price", 0));
                    long orig = Math.round(item.optDouble("original_price", sale));
                    int stock = item.optInt("total_inventory", 15);
                    if (!sku.isEmpty() && !nameFa.isEmpty() && sale > 0) {
                        db.upsertSyncedProduct(sku, nameFa, nameEn, orig, sale, stock);
                        upserted++;
                    }
                }

                final int finalCount = upserted;
                post(callback, true, "اتصال موفق! " + PersianFormatter.toPersianDigits(String.valueOf(finalCount)) + " محصول با سرور همگام‌سازی شد.", finalCount);
            } catch (Exception e) {
                post(callback, false, "عدم دسترسی به سرور (" + e.getClass().getSimpleName() + ") — داده محلی فعال است.", 0);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void post(SyncCallback cb, boolean ok, String msg, int count) {
        mainHandler.post(() -> cb.onComplete(ok, msg, count));
    }
}
