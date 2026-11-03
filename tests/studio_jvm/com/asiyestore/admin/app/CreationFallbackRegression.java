package com.asiyestore.admin.app;
import java.util.ArrayList;
import java.util.Arrays;
public class CreationFallbackRegression {
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        final ArrayList<String> called = new ArrayList<>();
        String[] order = CreationFallback.preferredFirst("deepai", new String[]{"fal", "deepai", "byteplus", "stability"});
        check(Arrays.equals(order, new String[]{"deepai", "fal", "byteplus", "stability"}));
        CreationFallback.Result<String> good = CreationFallback.run(order, new CreationFallback.Attempt<String>() {
            public String run(String p) throws Exception { called.add(p); if (p.equals("deepai")) throw new DirectAiStudio.ProviderHttpException(429); return "output"; }
        }, null);
        check(good.provider.equals("fal") && called.size()==2);
        called.clear();
        try { CreationFallback.run(order, new CreationFallback.Attempt<String>() {
            public String run(String p) throws Exception { called.add(p); throw new java.net.SocketTimeoutException(); }
        }, null); throw new AssertionError(); } catch (CreationFallback.Failed e) { check(e.uncertain && called.size()==1); }
        called.clear();
        try { CreationFallback.run(order, new CreationFallback.Attempt<String>() {
            public String run(String p) throws Exception { called.add(p); throw new DirectAiStudio.ProviderHttpException(500); }
        }, null); throw new AssertionError(); } catch (CreationFallback.Failed e) { check(e.uncertain && called.size()==1); }
        called.clear();
        try { CreationFallback.run(order, new CreationFallback.Attempt<String>() {
            public String run(String p) throws Exception { called.add(p); throw new DirectAiStudio.ProviderHttpException(401); }
        }, null); throw new AssertionError(); } catch (CreationFallback.Failed e) { check(!e.uncertain && called.size()==4); }
        called.clear();
        good = CreationFallback.run(order, new CreationFallback.Attempt<String>() {
            public String run(String p) throws Exception { called.add(p); if (called.size()==1) throw new CreationFallback.MissingCredential(); return "ok"; }
        }, null);
        check(called.size()==2);
        called.clear();
        try { CreationFallback.run(order, new CreationFallback.Attempt<String>() {
            public String run(String p) throws Exception { called.add(p); throw new DirectAiStudio.ProviderHttpException(402); }
        }, new CreationFallback.Cancelled() { public boolean isCancelled() { return called.size() > 0; } });
        throw new AssertionError(); } catch (java.util.concurrent.CancellationException e) { check(called.size()==1); }
        System.out.println("Paid creation fallback and uncertain-outcome safeguards passed.");
    }
}
