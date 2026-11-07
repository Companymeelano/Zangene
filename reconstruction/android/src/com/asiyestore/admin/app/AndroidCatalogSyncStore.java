package com.asiyestore.admin.app;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/** SQLite adapter for a prepared, explicitly authorised catalog batch. */
public final class AndroidCatalogSyncStore implements CatalogSyncGuard.Store {
    private final SQLiteDatabase db;
    public AndroidCatalogSyncStore(SQLiteDatabase db) {
        if (db == null) throw new IllegalArgumentException("Database required");
        this.db=db;
    }
    @Override public boolean inTransaction() { return db.inTransaction(); }
    @Override public int updateIfUnchanged(CatalogSyncGuard.Change c) {
        if (!db.inTransaction()) throw new IllegalStateException("Caller transaction required");
        ContentValues values=new ContentValues();
        values.put("original_price",c.newOriginal);
        values.put("sale_price",c.newSale);
        values.put("stock",c.newStock);
        // Avoid overflow in the percentage calculation, retaining the legacy [0,100] display range.
        int discount = c.newSale >= c.newOriginal ? 0 : (int)Math.round((1.0 - (double)c.newSale / c.newOriginal) * 100.0);
        values.put("discount_percent",discount);
        return db.update("products",values,"sku = ? AND original_price = ? AND sale_price = ? AND stock = ?",
                new String[]{c.sku,Long.toString(c.expectedOriginal),Long.toString(c.expectedSale),Integer.toString(c.expectedStock)});
    }
}
