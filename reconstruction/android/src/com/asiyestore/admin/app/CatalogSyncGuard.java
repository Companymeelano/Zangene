package com.asiyestore.admin.app;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Explicit, conflict-detecting updates to existing products only; no network or API assumptions. */
public final class CatalogSyncGuard {
    public interface Store {
        boolean inTransaction();
        /** Atomically update iff all expected values still match; return the affected row count. */
        int updateIfUnchanged(Change change);
    }
    public static final class Change {
        public final String sku;
        public final long expectedOriginal, expectedSale, newOriginal, newSale;
        public final int expectedStock, newStock;
        public Change(String sku, long expectedOriginal, long expectedSale, int expectedStock,
                      long newOriginal, long newSale, int newStock) {
            this.sku=sku; this.expectedOriginal=expectedOriginal; this.expectedSale=expectedSale;
            this.expectedStock=expectedStock; this.newOriginal=newOriginal; this.newSale=newSale; this.newStock=newStock;
        }
    }
    private CatalogSyncGuard() { }
    /** Caller must roll back the enclosing transaction on any exception. Never inserts or deletes. */
    public static void apply(Store store, List<Change> changes) {
        if (store == null || !store.inTransaction()) throw new IllegalStateException("Caller transaction required");
        if (changes == null) throw new IllegalArgumentException("Explicit changes required");
        Set<String> seen = new HashSet<>();
        // Validate the complete batch before the first write.
        for (Change c : changes) {
            if (c == null || c.sku == null || c.sku.trim().isEmpty() || !c.sku.equals(c.sku.trim())
                    || c.expectedOriginal < 0 || c.expectedSale < 0 || c.expectedStock < 0
                    || c.newOriginal <= 0 || c.newSale <= 0 || c.newStock < 0 || !seen.add(c.sku)) {
                throw new IllegalArgumentException("Invalid or duplicate catalog change");
            }
        }
        for (Change c : changes) {
            if (store.updateIfUnchanged(c) != 1) throw new IllegalStateException("Catalog conflict or missing product");
        }
    }
}
