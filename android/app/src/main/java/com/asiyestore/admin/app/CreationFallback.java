package com.asiyestore.admin.app;

/** Fail over paid creation only on explicit rejection; unknown outcomes might already be billed. */
public final class CreationFallback {
    private CreationFallback() { }
    public static final class MissingCredential extends Exception { }
    public interface Attempt<T> { T run(String provider) throws Exception; }
    public interface Cancelled { boolean isCancelled(); }
    public static final class Result<T> {
        public final String provider; public final T value;
        Result(String provider, T value) { this.provider = provider; this.value = value; }
    }
    public static final class Failed extends Exception {
        public final boolean uncertain;
        public final String provider;
        public final String attempts;
        Failed(boolean uncertain, String provider, String attempts) {
            super("creation failed"); this.uncertain = uncertain; this.provider = provider; this.attempts = attempts;
        }
    }
    public static String[] preferredFirst(String preferred, String[] providers) {
        boolean found = false;
        for (String p : providers) if (p.equals(preferred)) found = true;
        if (!found) throw new IllegalArgumentException("unknown provider");
        String[] result = new String[providers.length]; result[0] = preferred;
        int i = 1; for (String p : providers) if (!p.equals(preferred)) result[i++] = p;
        return result;
    }
    private static boolean rejected(int status) {
        return status == 400 || status == 401 || status == 402 || status == 403 || status == 404 || status == 413 || status == 429;
    }
    public static <T> Result<T> run(String[] providers, Attempt<T> attempt, Cancelled cancelled) throws Failed {
        StringBuilder details = new StringBuilder();
        for (String provider : providers) {
            if (cancelled != null && cancelled.isCancelled()) throw new java.util.concurrent.CancellationException();
            try {
                T value = attempt.run(provider);
                if (value == null) throw new IllegalStateException("empty output");
                return new Result<T>(provider, value);
            } catch (MissingCredential e) {
                details.append(provider).append(" · کلید قابل استفاده نیست\n");
            } catch (DirectAiStudio.ProviderHttpException e) {
                details.append(provider).append(" · HTTP ").append(e.status).append("\n");
                if (!rejected(e.status)) throw new Failed(true, provider, details.toString());
            } catch (java.util.concurrent.CancellationException e) { throw e;
            } catch (Exception e) {
                throw new Failed(true, provider, details.toString());
            }
        }
        throw new Failed(false, "", details.toString());
    }
}
