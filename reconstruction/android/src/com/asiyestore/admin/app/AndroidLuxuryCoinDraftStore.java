package com.asiyestore.admin.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** Android adapter: adds only unpublished catalog drafts inside caller's transaction. */
public final class AndroidLuxuryCoinDraftStore implements LuxuryCoinDraftImporter.Store {
    private final SQLiteDatabase db;
    public AndroidLuxuryCoinDraftStore(SQLiteDatabase db) {
        if (db == null) throw new IllegalArgumentException("Database required");
        this.db=db;
    }
    public boolean inTransaction() { return db.inTransaction(); }
    private void requireTransaction() {
        if (!db.inTransaction()) throw new IllegalStateException("Caller transaction required");
    }
    public void ensureReferenceTable() {
        requireTransaction();
        db.execSQL("CREATE TABLE IF NOT EXISTS product_reference_reviews (sku TEXT PRIMARY KEY, price_note TEXT NOT NULL)");
    }
    private boolean exists(String sql,String sku) {
        try (Cursor c=db.rawQuery(sql,new String[]{sku})) { return c.moveToFirst(); }
    }
    public boolean hasProduct(String sku) {
        requireTransaction();
        return exists("SELECT 1 FROM products WHERE sku = ? LIMIT 1",sku);
    }
    public void insertDraft(LuxuryCoinDraftCatalog.Draft d) {
        requireTransaction();
        ContentValues v=new ContentValues();
        v.put("sku",d.sku); v.put("ttac_serial",""); v.put("batch_code",""); v.put("expiry_label","");
        v.put("name_fa",d.nameFa); v.put("name_en",d.nameEn); v.put("brand",d.brand); v.put("category",d.category);
        v.put("description",d.shortDescription); v.put("long_description",d.description); v.put("usage_method",d.usage);
        v.put("ingredients","نیازمند تأیید برچسب کالا"); v.put("country","نامشخص"); v.put("volume",d.size);
        v.put("original_price",d.referencePriceToman); v.put("sale_price",d.referencePriceToman);
        v.put("stock",5); v.put("low_stock_threshold",5); v.put("published",0); v.put("ai_generated",0);
        // image_key contains a local draft ID; the UI must prefer an existing user image_path.
        v.put("image_key",d.sku); v.put("seo_title",d.seoTitle); v.put("seo_description",d.seoDescription);
        v.put("keywords",d.keywords); v.put("instagram_caption",d.instagram); v.put("youtube_caption",d.youtube);
        v.put("telegram_caption",d.telegram); v.put("whatsapp_caption",d.whatsapp);
        db.insertOrThrow("products",null,v);
    }
    public void insertPriceNoteIfAbsent(String sku,String note) {
        requireTransaction();
        if (exists("SELECT 1 FROM product_reference_reviews WHERE sku = ? LIMIT 1",sku)) return;
        ContentValues v=new ContentValues(); v.put("sku",sku); v.put("price_note",note);
        db.insertOrThrow("product_reference_reviews",null,v);
    }
}
