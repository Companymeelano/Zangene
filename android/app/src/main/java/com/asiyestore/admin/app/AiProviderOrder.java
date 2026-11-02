package com.asiyestore.admin.app;
public final class AiProviderOrder {
    private AiProviderOrder() { }
    public static String[] forPreference(String preferred) {
        if ("openai".equals(preferred)) return new String[]{"openai", "gemini", "groq"};
        if ("groq".equals(preferred)) return new String[]{"groq", "gemini", "openai"};
        return new String[]{"gemini", "openai", "groq"};
    }
}
