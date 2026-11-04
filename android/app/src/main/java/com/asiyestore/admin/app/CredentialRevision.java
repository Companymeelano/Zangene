package com.asiyestore.admin.app;

/** Preserve existing device overrides while enabling the three newly supplied bundled keys. */
public final class CredentialRevision {
    public static final int CURRENT = 716;
    private CredentialRevision() { }
    public static boolean isOverrideActive(String id, int revision) {
        boolean updated = "gemini".equals(id) || "groq".equals(id) || "openai".equals(id);
        return !updated || revision >= CURRENT;
    }
}
