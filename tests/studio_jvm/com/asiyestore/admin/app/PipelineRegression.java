package com.asiyestore.admin.app;
import android.content.Context;
import android.graphics.Bitmap;
public class PipelineRegression {
    private static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    private static void reset(String success) {
        AiHttpTransport.URLS.clear(); AiHttpTransport.BODIES.clear(); AiHttpTransport.KEYS.clear();
        StudioCredentials.VALUES.clear(); AiHttpTransport.successful = success; AiHttpTransport.paidFails = false;
        AiHttpTransport.content = "{\"nameFa\":\"Test\",\"category\":\"Tea\",\"description\":\"Draft\"}";
    }
    public static void main(String[] args) throws Exception {
        Context ctx = new Context();
        reset("api.deepseek.com");
        AiHttpTransport.content = new org.json.JSONObject().put("nameFa", "Test").put("category", "Tea").put("description", "Draft").put("longDescription", "  ").put("seoTitle", "").toString();
        AiProductStudio.Draft partial = DirectAiStudio.generate(ctx, "Tea", "Brand", null, "deepseek");
        check("neutral".equals(partial.longDescription) && "neutral title".equals(partial.seoTitle));
        reset("api.gapgpt.app");
        StudioCredentials.VALUES.put("gapgpt", "replacement");
        AiProductStudio.Draft d = DirectAiStudio.generate(ctx, "Tea", "Brand", null, "gemini");
        check("Tea".equals(d.category));
        check(AiHttpTransport.URLS.size() == 4 && AiHttpTransport.URLS.get(3).contains("gapgpt"));
        check(AiHttpTransport.KEYS.get(3).equals("Bearer replacement"));
        reset("api.deepseek.com");
        d = DirectAiStudio.generate(ctx, "Tea", "Brand", new Bitmap(), "deepseek");
        check("Draft".equals(d.description));
        check(AiHttpTransport.URLS.size() == 1 && AiHttpTransport.URLS.get(0).contains("deepseek"));
        check(AiHttpTransport.BODIES.get(0).contains("data:image/jpeg;base64,"));
        reset("openrouter.ai"); AiHttpTransport.paidFails = true;
        StudioCredentials.VALUES.put("openrouter", "replacement-router");
        d = DirectAiStudio.generate(ctx, "Tea", "Brand", new Bitmap(), "openrouter");
        check("Draft".equals(d.description));
        check(AiHttpTransport.URLS.size() >= 2);
        int last = AiHttpTransport.URLS.size() - 1;
        check(new org.json.JSONObject(AiHttpTransport.BODIES.get(last)).getString("model").equals("openrouter/free"));
        check(AiHttpTransport.KEYS.get(last).equals("Bearer replacement-router"));
        check(AiHttpTransport.BODIES.get(last).contains("data:image/jpeg;base64,"));
        reset("api.cloudflare.com");
        StudioCredentials.VALUES.put("cloudflare_account", "0123456789abcdef0123456789abcdef");
        d = DirectAiStudio.generate(ctx, "Tea", "Brand", new Bitmap(), "cloudflare");
        check("Draft".equals(d.description));
        check(AiHttpTransport.URLS.get(0).contains("llama-3.2-11b-vision-instruct"));
        check(AiHttpTransport.BODIES.get(0).contains("data:image/jpeg;base64,"));
        reset("api.deepseek.com");
        AiHttpTransport.content = "```json\n{\"nameFa\":\"Test\",\"category\":\"Tea\",\"description\":\"Draft\"}\n```";
        d = DirectAiStudio.generate(ctx, "Tea", "Brand", new Bitmap(), "deepseek");
        check("Tea".equals(d.category));
        reset("openrouter.ai");
        StudioCredentials.VALUES.put("gemini", "bad-storage");
        d = DirectAiStudio.generate(ctx, "Tea", "", new Bitmap(), "gemini");
        check(!AiHttpTransport.URLS.get(0).contains("googleapis"));
        reset("");
        try { DirectAiStudio.generate(ctx, "Tea", "", new Bitmap(), "gemini"); throw new AssertionError(); }
        catch (DirectAiStudio.ProvidersFailedException e) {
            check(e.getMessage().contains("403") && e.getMessage().contains("401"));
            check(!e.getMessage().contains("test-key"));
        }
        reset("openrouter.ai");
        try {
            DirectAiStudio.generate(ctx, "Tea", "", new Bitmap(), "gemini", new DirectAiStudio.Cancellation() {
                int checks = 0;
                public boolean isCancelled() { return ++checks > 1; }
            });
            throw new AssertionError();
        } catch (java.util.concurrent.CancellationException expected) { check(AiHttpTransport.URLS.size() == 1); }
        reset("api.gapgpt.app");
        check(DirectAiStudio.probe(ctx, "gapgpt").contains("موفق"));
        AiHttpTransport.content = "{}";
        check(!DirectAiStudio.probe(ctx, "gapgpt").contains("موفق"));
        System.out.println("Direct provider fallback, vision and probe mock checks passed.");
    }
}
