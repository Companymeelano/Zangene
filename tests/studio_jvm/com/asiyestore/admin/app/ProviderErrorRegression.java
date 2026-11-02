package com.asiyestore.admin.app;
public class ProviderErrorRegression {
    private static void check(boolean value, String msg) { if (!value) throw new AssertionError(msg); }
    public static void main(String[] args) {
        check(AiProviderError.message("gemini", 401, "response").contains("401"), "401 missing");
        check(AiProviderError.message("openai", 429, "response").contains("429"), "429 missing");
        check(AiProviderError.message("groq", 404, "response").contains("404"), "404 missing");
        check(AiProviderError.message("groq", 0, "network").contains("شبکه"), "network missing");
        check(AiProviderError.message("gemini", 0, "timeout").contains("زمان"), "timeout missing");
        check(AiProviderError.message("openai", 0, "missing").contains("موجود نیست"), "missing key");
        System.out.println("Provider diagnostics checks passed");
    }
}
