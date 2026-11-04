package com.asiyestore.admin.app;
import java.util.ArrayList;
import org.json.JSONObject;
public final class AiHttpTransport {
    static final ArrayList<String> URLS = new ArrayList<>(), BODIES = new ArrayList<>(), KEYS = new ArrayList<>();
    static boolean paidFails = false;
    static String successful = "", content = "{\"nameFa\":\"Test\",\"category\":\"Tea\",\"description\":\"Draft\"}";
    public static String post(String url, String header, String key, String body, int limit) throws Exception {
        URLS.add(url); BODIES.add(body); KEYS.add(key);
        if (paidFails && url.contains("openrouter.ai") && new JSONObject(body).optString("model").equals("openai/gpt-4o-mini")) throw new DirectAiStudio.ProviderHttpException(402);
        if (!url.contains(successful) || successful.isEmpty()) throw new DirectAiStudio.ProviderHttpException(url.contains("googleapis") ? 403 : 401);
        if (url.contains("googleapis")) return new JSONObject().put("candidates", new org.json.JSONArray().put(new JSONObject().put("content", new JSONObject().put("parts", new org.json.JSONArray().put(new JSONObject().put("text", content)))))).toString();
        if (url.contains("api.cloudflare.com")) return new JSONObject().put("success", true).put("result", new JSONObject().put("response", content)).toString();
        return new JSONObject().put("choices", new org.json.JSONArray().put(new JSONObject().put("message", new JSONObject().put("content", content)))).toString();
    }
}
