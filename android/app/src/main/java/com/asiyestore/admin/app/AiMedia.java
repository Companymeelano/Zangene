package com.asiyestore.admin.app;

import android.content.Context;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Optional, explicitly initiated illustrative images; not faithful product photography. */
public final class AiMedia {
    private AiMedia() { }
    static String key(Context context, String id) throws Exception {
        try (InputStream in = context.getAssets().open("studio_provider_keys.json"); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] b = new byte[4096]; int n, total = 0;
            while ((n = in.read(b)) != -1) { total += n; if (total > 16384) throw new IllegalArgumentException("keys too large"); out.write(b, 0, n); }
            String k = new JSONObject(new String(out.toByteArray(), StandardCharsets.UTF_8)).optString(id, "").trim();
            if (k.isEmpty()) throw new IllegalArgumentException("missing key");
            return k;
        }
    }
    public static android.graphics.Bitmap generateImage(Context context, String provider, String prompt) throws Exception {
        if ("stability".equals(provider)) return stability(context, prompt);
        String key = key(context, provider), url, header, value;
        JSONObject body = new JSONObject();
        if ("fal".equals(provider)) {
            url = "https://fal.run/fal-ai/flux/dev"; header = "Authorization"; value = "Key " + key;
            body.put("prompt", prompt).put("num_images", 1);
        } else if ("deepai".equals(provider)) {
            url = "https://api.deepai.org/api/text2img"; header = "api-key"; value = key;
            body.put("text", prompt);
        } else if ("byteplus".equals(provider)) {
            url = "https://ark.ap-southeast.bytepluses.com/api/v3/images/generations"; header = "Authorization"; value = "Bearer " + key;
            body.put("model", "dola-seedream-5-0-pro-260628").put("prompt", prompt).put("size", "1K").put("response_format", "url").put("watermark", true);
        } else throw new IllegalArgumentException("unknown provider");
        JSONObject response = new JSONObject(AiHttpTransport.post(url, header, value, body.toString(), 256 * 1024, 120000));
        String result;
        if ("fal".equals(provider)) result = response.getJSONArray("images").getJSONObject(0).getString("url");
        else if ("deepai".equals(provider)) result = response.getString("output_url");
        else result = response.getJSONArray("data").getJSONObject(0).getString("url");
        java.net.URL parsed = new java.net.URL(result);
        if (!"https".equalsIgnoreCase(parsed.getProtocol()) || parsed.getUserInfo() != null || parsed.getHost().isEmpty()) throw new IllegalArgumentException("unsafe image URL");
        return StudioNetwork.downloadImage(result);
    }

    private static android.graphics.Bitmap stability(Context context, String prompt) throws Exception {
        String boundary = "ASIYE" + java.util.UUID.randomUUID().toString().replace("-", "");
        String data = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"prompt\"\r\n\r\n" + prompt
                + "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"output_format\"\r\n\r\npng\r\n--" + boundary + "--\r\n";
        byte[] body = data.getBytes(StandardCharsets.UTF_8);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL("https://api.stability.ai/v2beta/stable-image/generate/core").openConnection();
        try {
            conn.setInstanceFollowRedirects(false); conn.setConnectTimeout(15000); conn.setReadTimeout(120000);
            conn.setRequestMethod("POST"); conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + key(context, "stability"));
            conn.setRequestProperty("Accept", "image/*");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setFixedLengthStreamingMode(body.length);
            try (java.io.OutputStream out = conn.getOutputStream()) { out.write(body); }
            int status = conn.getResponseCode();
            if (status != 200) throw new DirectAiStudio.ProviderHttpException(status);
            String type = conn.getContentType();
            if (type == null || !type.toLowerCase(java.util.Locale.US).startsWith("image/")) throw new IllegalArgumentException("not image");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = conn.getInputStream()) { byte[] b = new byte[8192]; int n, count = 0;
                while ((n = in.read(b)) != -1) { count += n; if (count > 8 * 1024 * 1024) throw new IllegalArgumentException("image too large"); out.write(b, 0, n); }
            }
            byte[] bytes = out.toByteArray();
            android.graphics.BitmapFactory.Options bounds = new android.graphics.BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0 || bounds.outWidth > 20000 || bounds.outHeight > 20000) throw new IllegalArgumentException("invalid image");
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options(); options.inSampleSize = 1;
            while (Math.max(bounds.outWidth, bounds.outHeight) / options.inSampleSize > 1200) options.inSampleSize *= 2;
            android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            if (bmp == null) throw new IllegalArgumentException("invalid image");
            return bmp;
        } finally { conn.disconnect(); }
    }
}
