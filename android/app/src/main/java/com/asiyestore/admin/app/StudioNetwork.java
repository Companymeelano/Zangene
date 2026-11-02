package com.asiyestore.admin.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** HTTPS transport for explicit user-provided images and an optional AI proxy. */
public final class StudioNetwork {
    private StudioNetwork() { }
    private static final int MAX_IMAGE = 8 * 1024 * 1024;
    private static HttpURLConnection connect(String address) throws Exception {
        URL url = new URL(address.trim());
        if (!"https".equalsIgnoreCase(url.getProtocol()) || url.getUserInfo() != null)
            throw new IllegalArgumentException("Only HTTPS addresses without credentials are supported");
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setInstanceFollowRedirects(false); c.setConnectTimeout(12000); c.setReadTimeout(30000);
        return c;
    }
    private static byte[] readLimited(InputStream in, int limit) throws Exception {
        try (InputStream src = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] b = new byte[8192]; int n, total = 0;
            while ((n = src.read(b)) != -1) {
                total += n; if (total > limit) throw new IllegalArgumentException("Response too large");
                out.write(b, 0, n);
            }
            return out.toByteArray();
        }
    }
    public static Bitmap downloadImage(String address) throws Exception {
        HttpURLConnection c = connect(address);
        try {
            c.setRequestProperty("Accept", "image/*");
            if (c.getResponseCode() != 200) throw new IllegalArgumentException("Image server HTTP " + c.getResponseCode());
            String type = c.getContentType();
            if (type == null || !type.toLowerCase(java.util.Locale.US).startsWith("image/"))
                throw new IllegalArgumentException("URL is not an image");
            if (c.getContentLengthLong() > MAX_IMAGE) throw new IllegalArgumentException("Image too large");
            byte[] data = readLimited(c.getInputStream(), MAX_IMAGE);
            BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0 || bounds.outWidth > 20000 || bounds.outHeight > 20000)
                throw new IllegalArgumentException("Invalid image");
            BitmapFactory.Options opt = new BitmapFactory.Options(); opt.inSampleSize = 1;
            while (Math.max(bounds.outWidth, bounds.outHeight) / opt.inSampleSize > 1200) opt.inSampleSize *= 2;
            Bitmap result = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
            if (result == null) throw new IllegalArgumentException("Unreadable image");
            return result;
        } finally { c.disconnect(); }
    }
    /** Contract described in tools/AI_STUDIO_API.md; proxy owns provider credentials. */
    public static AiProductStudio.Draft generate(String endpoint, String accessToken, String name, String brand, Bitmap photo) throws Exception {
        JSONObject req = new JSONObject(); req.put("name", name); req.put("brand", brand);
        if (photo != null) {
            ByteArrayOutputStream image = new ByteArrayOutputStream();
            if (!photo.compress(Bitmap.CompressFormat.JPEG, 75, image)) throw new IllegalArgumentException("Image encoding failed");
            req.put("image_jpeg_base64", Base64.encodeToString(image.toByteArray(), Base64.NO_WRAP));
        }
        byte[] body = req.toString().getBytes(StandardCharsets.UTF_8);
        if (body.length > MAX_IMAGE) throw new IllegalArgumentException("Image too large");
        URL endpointUrl = new URL(endpoint.trim());
        if (endpointUrl.getQuery() != null || endpointUrl.getRef() != null) throw new IllegalArgumentException("Invalid endpoint");
        if (accessToken == null || accessToken.length() < 32 || accessToken.contains("\n") || accessToken.contains("\r"))
            throw new IllegalArgumentException("Invalid studio access token");
        HttpURLConnection c = connect(endpoint);
        try {
            c.setReadTimeout(80000);
            c.setRequestProperty("Authorization", "Bearer " + accessToken);
            c.setRequestMethod("POST"); c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            c.setRequestProperty("Accept", "application/json"); c.setFixedLengthStreamingMode(body.length);
            try (java.io.OutputStream out = c.getOutputStream()) { out.write(body); }
            int status = c.getResponseCode();
            if (status != 200) throw new IllegalArgumentException("AI server HTTP " + status);
            String response = new String(readLimited(c.getInputStream(), 256 * 1024), StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(response);
            for (String required : new String[]{"nameFa", "category", "description"}) {
                Object value = obj.opt(required);
                if (!(value instanceof String) || ((String) value).trim().isEmpty())
                    throw new IllegalArgumentException("Missing field " + required);
            }
            AiProductStudio.Draft d = AiProductStudio.compose(AiProductStudio.analyze(name, brand, null), name, brand);
            // Optional fields only; no price, origin or claims are assumed when missing.
            for (String field : new String[]{"nameFa", "nameEn", "category", "description", "longDescription", "benefits", "ingredients", "usage", "country", "volume", "keywords", "seoTitle", "seoDescription", "instagramCaption"}) {
                if (!obj.has(field) || obj.isNull(field)) continue;
                Object value = obj.get(field);
                if (!(value instanceof String) || ((String) value).length() > 12000) throw new IllegalArgumentException("Invalid field " + field);
                AiProductStudio.Draft.class.getField(field).set(d, ((String) value).trim());
            }
            if (d.nameFa.isEmpty() || d.category.isEmpty()) throw new IllegalArgumentException("Missing name/category");
            return d;
        } finally { c.disconnect(); }
    }
}
