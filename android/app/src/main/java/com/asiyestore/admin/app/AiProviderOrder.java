package com.asiyestore.admin.app;

/** Preferred provider first, followed by all other configured providers. */
public final class AiProviderOrder {
    private AiProviderOrder() { }
    public static final String[] IDS = {"gemini", "openai", "groq", "gapgpt", "deepseek", "openrouter", "cerebras", "huggingface", "cloudflare", "maxrouter"};
    public static String[] forPreference(String preferred) {
        String[] result = new String[IDS.length];
        boolean found = false;
        for (String id : IDS) if (id.equals(preferred)) found = true;
        String first = found ? preferred : "gemini";
        result[0] = first;
        int pos = 1;
        for (String id : IDS) if (!id.equals(first)) result[pos++] = id;
        return result;
    }
    public static boolean supportsImage(String id) {
        return "gemini".equals(id) || "openai".equals(id) || "groq".equals(id) || "openrouter".equals(id) || "cerebras".equals(id) || "huggingface".equals(id);
    }
    public static String label(String id) {
        if ("gemini".equals(id)) return "Gemini";
        if ("openai".equals(id)) return "OpenAI";
        if ("groq".equals(id)) return "Groq";
        if ("gapgpt".equals(id)) return "GapGPT";
        if ("deepseek".equals(id)) return "DeepSeek";
        if ("openrouter".equals(id)) return "OpenRouter";
        if ("cerebras".equals(id)) return "Cerebras";
        if ("huggingface".equals(id)) return "Hugging Face";
        if ("cloudflare".equals(id)) return "Cloudflare Workers AI";
        if ("maxrouter".equals(id)) return "MaxRouter";
        return "سرویس ناشناخته";
    }
}
