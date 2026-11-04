package com.asiyestore.admin.app;
public final class CredentialRevisionRegression {
    private static void check(boolean v) { if (!v) throw new AssertionError(); }
    public static void main(String[] args) {
        for (String id : new String[]{"gemini", "openai", "groq"}) {
            check(!CredentialRevision.isOverrideActive(id, 0));
            check(CredentialRevision.isOverrideActive(id, 716));
        }
        check(CredentialRevision.isOverrideActive("gapgpt", 0));
        check(CredentialRevision.isOverrideActive("deepseek", 0));
        System.out.println("Credential revision priority regression checks passed.");
    }
}
