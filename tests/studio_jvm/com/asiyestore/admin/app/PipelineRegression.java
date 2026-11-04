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
        reset("api.groq.com");
        StudioCredentials.VALUES.put("groq", "new-device-credential");
        AiProductStudio.Draft d=DirectAiStudio.generate(ctx,"Tea","Brand",new Bitmap(),"gemini");
        check("Tea".equals(d.category)); check(AiHttpTransport.URLS.size()==3);
        check(AiHttpTransport.URLS.get(0).contains("googleapis") && AiHttpTransport.URLS.get(1).contains("api.openai.com") && AiHttpTransport.URLS.get(2).contains("api.groq.com"));
        check(AiHttpTransport.KEYS.get(2).equals("Bearer new-device-credential"));
        check(AiHttpTransport.BODIES.get(0).contains("inline_data") && AiHttpTransport.BODIES.get(2).contains("data:image/jpeg;base64,"));
        reset("api.openai.com");
        AiHttpTransport.content = new org.json.JSONObject().put("nameFa","Test").put("category","Tea").put("description","Draft").put("longDescription","  ").toString();
        d=DirectAiStudio.generate(ctx,"Tea","Brand",null,"deepseek");
        check("neutral".equals(d.longDescription)); check(AiHttpTransport.URLS.size()==2 && AiHttpTransport.URLS.get(0).contains("googleapis"));
        reset("");
        try { DirectAiStudio.generate(ctx,"Tea","Brand",new Bitmap(),"openrouter"); throw new AssertionError(); }
        catch (DirectAiStudio.ProvidersFailedException e) { check(e.getMessage().contains("403") && e.getMessage().contains("401")); check(!e.getMessage().contains("test-key")); check(AiHttpTransport.URLS.size()==3); }
        reset("api.openai.com");
        try { DirectAiStudio.generate(ctx,"Tea","",new Bitmap(),"gemini",new DirectAiStudio.Cancellation() { int n=0; public boolean isCancelled() { return ++n>1; } }); throw new AssertionError(); }
        catch (java.util.concurrent.CancellationException expected) { check(AiHttpTransport.URLS.size()==1); }
        reset("api.groq.com"); check(DirectAiStudio.probe(ctx,"groq").contains("موفق"));
        check(DirectAiStudio.probe(ctx,"deepseek").contains("ناشناخته"));
        AiHttpTransport.content="{}"; check(!DirectAiStudio.probe(ctx,"groq").contains("موفق"));
        System.out.println("Three-provider fallback, vision and probe mock checks passed.");
    }
}
