package com.asiyestore.admin.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** SQLite adapter; never commits, deletes, updates existing records or touches other tables. */
public final class AndroidReferenceDraftStore implements ReferenceDraftMigration.Store {
    private final SQLiteDatabase db;
    public AndroidReferenceDraftStore(SQLiteDatabase db) {
        if (db == null) throw new IllegalArgumentException("Database required");
        this.db = db;
    }
    @Override public boolean inTransaction() { return db.inTransaction(); }
    @Override public void ensureReferenceTable() {
        db.execSQL("CREATE TABLE IF NOT EXISTS product_reference_reviews (sku TEXT PRIMARY KEY, price_note TEXT NOT NULL)");
    }
    private boolean exists(String sql, String sku) {
        try (Cursor c = db.rawQuery(sql, new String[]{sku})) { return c.moveToFirst(); }
    }
    @Override public boolean hasProduct(String sku) {
        return exists("SELECT 1 FROM products WHERE sku = ? LIMIT 1", sku);
    }
    @Override public void insertDraft(ReferenceDraftMigration.Draft d) {
        ContentValues v = new ContentValues();
        v.put("sku",d.sku); v.put("ttac_serial",""); v.put("batch_code",""); v.put("expiry_label","");
        v.put("name_fa",d.nameFa); v.put("name_en",d.nameEn); v.put("brand",d.brand); v.put("category",d.category);
        v.put("description",d.description); v.put("ingredients","بررسی برچسب محصول لازم است.");
        v.put("usage_method","طبق راهنمای روی بسته‌بندی استفاده شود."); v.put("country","نامشخص"); v.put("volume","نیازمند بررسی");
        v.put("original_price",d.samplePrice); v.put("sale_price",d.samplePrice); v.put("stock",5); v.put("low_stock_threshold",5);
        v.put("published",0); v.put("image_key",d.imageKey); v.put("ai_generated",0);
        v.put("benefits","مشخصات و کاربردها باید با برچسب کالا تأیید شوند."); v.put("long_description",d.description);
        v.put("seo_title",d.nameFa.replace(" — پیش‌نویس","")); v.put("seo_description",d.description);
        v.put("keywords",d.keywords); v.put("instagram_caption",d.caption);
        v.put("youtube_caption",d.nameFa+"\n"+d.description+"\nبرای مشخصات کامل، اطلاعات روی بسته را بررسی کنید.");
        v.put("telegram_caption",d.nameFa+"\n"+d.description+"\nبرای اطلاع از قیمت روز با فروشگاه تماس بگیرید.");
        v.put("whatsapp_caption",d.nameFa+"\n"+d.description+"\nبرای جزئیات پیام بدهید.");
        db.insertOrThrow("products",null,v);
    }
    @Override public void insertPriceNoteIfAbsent(String sku, String note) {
        if (exists("SELECT 1 FROM product_reference_reviews WHERE sku = ? LIMIT 1",sku)) return;
        ContentValues v=new ContentValues(); v.put("sku",sku); v.put("price_note",note);
        db.insertOrThrow("product_reference_reviews",null,v);
    }
}
