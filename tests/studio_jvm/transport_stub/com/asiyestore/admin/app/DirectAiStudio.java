package com.asiyestore.admin.app;
/** Minimal test double so the transport can be exercised on a plain JVM. */
public final class DirectAiStudio {
    public static final class ProviderHttpException extends Exception {
        public final int status;
        ProviderHttpException(int status) { super("provider HTTP " + status); this.status = status; }
    }
}
