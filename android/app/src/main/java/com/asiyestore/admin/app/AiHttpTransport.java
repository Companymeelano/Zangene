package com.asiyestore.admin.app;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Request bodies and secrets must never be logged; redirects are deliberately disabled. */
public final class AiHttpTransport {
    private AiHttpTransport() { }
    public static String post(String url, String header, String credential, String body, int limit) throws Exception {
        return post(url, header, credential, body, limit, 35000);
    }
    public static String post(String url, String header, String credential, String body, int limit, int readTimeout) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.setInstanceFollowRedirects(false); c.setConnectTimeout(15000); c.setReadTimeout(readTimeout);
            c.setRequestMethod("POST"); c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            c.setRequestProperty("Accept", "application/json");
            c.setRequestProperty(header, credential);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            c.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream out = c.getOutputStream()) { out.write(bytes); }
            int status = c.getResponseCode();
            if (status != 200) throw new DirectAiStudio.ProviderHttpException(status);
            try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192]; int n, total = 0;
                while ((n = in.read(buffer)) != -1) {
                    total += n; if (total > limit) throw new IllegalArgumentException("response too large");
                    out.write(buffer, 0, n);
                }
                return new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
        } finally { c.disconnect(); }
    }
}
