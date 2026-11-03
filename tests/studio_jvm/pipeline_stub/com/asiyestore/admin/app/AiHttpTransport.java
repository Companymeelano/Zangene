package com.asiyestore.admin.app;
import java.util.ArrayList;
import org.json.JSONObject;
public final class AiHttpTransport {
    static final ArrayList<String> URLS = new ArrayList<>(), BODIES = new ArrayList<>(), KEYS = new ArrayList<>();
    static String successful = "", content = "{\"nameFa\":\"Test\",\"category\":\"Tea\",\"description\":\"Draft\"}";
    public static String post(String url, String header, String key, String body, int limit) throws Exception {
        URLS.add(url); BODIES.add(body); KEYS.add(key);
        if (!url.contains(successful) || successful.isEmpty()) throw new DirectAiStudio.ProviderHttpException(url.contains("googleapis") ? 403 : 401);
        return new JSONObject().put("choices", new org.json.JSONArray().put(new JSONObject().put("message", new JSONObject().put("content", content)))).toString();
    }
}
