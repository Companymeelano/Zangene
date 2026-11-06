package com.asiyestore.admin.app;

import java.util.HashMap;
import java.util.Map;

public final class ReferenceDraftMigrationTest {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static final class FakeStore implements ReferenceDraftMigration.Store {
        boolean transaction, table; int failAt = -1, inserts;
        Map<String,String> products = new HashMap<>(), notes = new HashMap<>();
        public boolean inTransaction() { return transaction; }
        public void ensureReferenceTable() { table = true; }
        public boolean hasProduct(String sku) { return products.containsKey(sku); }
        public void insertDraft(ReferenceDraftMigration.Draft d) {
            if (++inserts == failAt) throw new IllegalStateException("simulated failure");
            if (products.put(d.sku,d.nameFa) != null) throw new AssertionError("overwritten");
        }
        public void insertPriceNoteIfAbsent(String sku, String note) { if (!notes.containsKey(sku)) notes.put(sku,note); }
    }
    public static void main(String[] args) {
        FakeStore f = new FakeStore();
        try { ReferenceDraftMigration.applyFromVersion9(f,9); throw new AssertionError("no transaction accepted"); }
        catch (IllegalStateException expected) { check(!f.table && f.products.isEmpty(), "mutation before transaction check"); }
        try { ReferenceDraftMigration.applyFromVersion9(f,10); throw new AssertionError("wrong version accepted"); }
        catch (IllegalArgumentException expected) { check(!f.table, "mutation before version check"); }
        f.transaction = true;
        f.products.put("AS-REF-GATIEAU", "user edited");
        f.notes.put("AS-REF-GATIEAU", "user note");
        f.notes.put("AS-REF-WHEAT", "existing note");
        ReferenceDraftMigration.applyFromVersion9(f,9);
        check(f.table && f.products.size()==4 && f.inserts==3, "insert absent drafts only");
        check("user edited".equals(f.products.get("AS-REF-GATIEAU")), "preserve user product");
        check("user note".equals(f.notes.get("AS-REF-GATIEAU")), "preserve user note");
        check("existing note".equals(f.notes.get("AS-REF-WHEAT")), "preserve orphan note");
        ReferenceDraftMigration.applyFromVersion9(f,9);
        check(f.inserts==3 && f.products.size()==4, "idempotent");
        try { ReferenceDraftMigration.drafts().clear(); throw new AssertionError("mutable list"); }
        catch (UnsupportedOperationException expected) { }
        FakeStore failing = new FakeStore(); failing.transaction = true; failing.failAt = 2;
        try { ReferenceDraftMigration.applyFromVersion9(failing,9); throw new AssertionError("failure swallowed"); }
        catch (IllegalStateException expected) { check(failing.inserts==2, "failure propagated"); }
        System.out.println("Reference draft migration logic verified with fake store; real SQLite not exercised.");
    }
}
