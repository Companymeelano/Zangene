package com.asiyestore.admin.app;
import android.content.Context;
import android.graphics.Bitmap;
public class PipelineRegression {
    private static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    private static void reset(String success) {
        AiHttpTransport.URLS.clear(); AiHttpTransport.BODIES.clear(); AiHttpTransport.KEYS.clear();
        StudioCredentials.VALUES.clear(); AiHttpTransport.successful = success;
        AiHttpTransport.content = "{\"nameFa\":\"Test\",\"category\":\"Tea\",\"description\":\"Draft\"}";
    }
    public static void main(String[] args) throws Exception {
        Context ctx = new Context();
        reset("api.gapgpt.app");
        StudioCredentials.VALUES.put("gapgpt", "replacement");
        AiProductStudio.Draft d = DirectAiStudio.generate(ctx, "Tea", "Brand", null, "gemini");
        check("Tea".equals(d.category));
        check(AiHttpTransport.URLS.size() == 4 && AiHttpTransport.URLS.get(3).contains("gapgpt"));
        check(AiHttpTransport.KEYS.get(3).equals("Bearer replacement"));
        reset("openrouter.ai");
        d = DirectAiStudio.generate(ctx, "Tea", "Brand", new Bitmap(), "deepseek");
        check("Draft".equals(d.description));
        check(AiHttpTransport.URLS.size() == 4);
        for (String u : AiHttpTransport.URLS) check(!u.contains("deepseek") && !u.contains("gapgpt"));
        check(AiHttpTransport.BODIES.get(3).contains("data:image/jpeg;base64,"));
        reset("openrouter.ai");
        StudioCredentials.VALUES.put("gemini", "bad-storage");
        d = DirectAiStudio.generate(ctx, "Tea", "", new Bitmap(), "gemini");
        check(AiHttpTransport.URLS.size() == 3 && !AiHttpTransport.URLS.get(0).contains("googleapis"));
        reset("");
        try { DirectAiStudio.generate(ctx, "Tea", "", new Bitmap(), "gemini"); throw new AssertionError(); }
        catch (DirectAiStudio.ProvidersFailedException e) {
            check(e.getMessage().contains("403") && e.getMessage().contains("401"));
            check(!e.getMessage().contains("test-key"));
        }
        reset("api.gapgpt.app");
        check(DirectAiStudio.probe(ctx, "gapgpt").contains("موفق"));
        AiHttpTransport.content = "{}";
        check(!DirectAiStudio.probe(ctx, "gapgpt").contains("موفق"));
        System.out.println("Direct provider fallback, vision and probe mock checks passed.");
    }
}
