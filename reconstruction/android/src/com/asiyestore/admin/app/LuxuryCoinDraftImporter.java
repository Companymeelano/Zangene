package com.asiyestore.admin.app;

/** Explicit opt-in draft import. Not an automatic database migration. */
public final class LuxuryCoinDraftImporter {
    public interface Store {
        boolean inTransaction();
        void ensureReferenceTable();
        boolean hasProduct(String sku);
        void insertDraft(LuxuryCoinDraftCatalog.Draft draft);
        void insertPriceNoteIfAbsent(String sku, String note);
    }
    private LuxuryCoinDraftImporter() { }
    /** Caller must roll back the enclosing transaction on any failure. Existing products are untouched. */
    public static int addDrafts(Store store) {
        if (store == null || !store.inTransaction()) throw new IllegalStateException("Caller transaction required");
        store.ensureReferenceTable();
        int inserted=0;
        for (LuxuryCoinDraftCatalog.Draft draft : LuxuryCoinDraftCatalog.drafts()) {
            if (store.hasProduct(draft.sku)) continue;
            store.insertDraft(draft);
            store.insertPriceNoteIfAbsent(draft.sku,draft.priceNote);
            inserted++;
        }
        return inserted;
    }
}
