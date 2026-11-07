package com.asiyestore.admin.app;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class LuxuryCoinDraftImportTest {
    private static void check(boolean value,String msg) { if (!value) throw new AssertionError(msg); }
    private static boolean exists(SQLiteDatabase db,String sql) { try(Cursor c=db.rawQuery(sql,new String[0])) { return c.moveToFirst(); } }
    public static void main(String[] args) throws Exception {
        String schema=new String(Files.readAllBytes(Paths.get(args[1])),StandardCharsets.UTF_8);
        try(SQLiteDatabase db=new SQLiteDatabase(args[0])) {
            db.execSQL(schema);
            db.execSQL("CREATE TABLE orders (id INTEGER PRIMARY KEY, customer_name TEXT)");
            db.execSQL("INSERT INTO orders VALUES (1,'customer')");
            db.execSQL("INSERT INTO products (sku,ttac_serial,batch_code,expiry_label,name_fa,name_en,category,description,original_price,sale_price,stock,published) VALUES ('LC155-DAY','','','','user edit','','','',999,999,21,1)");
            db.execSQL("CREATE TRIGGER fail_eye BEFORE INSERT ON products WHEN NEW.sku='LC155-EYE' BEGIN SELECT RAISE(ABORT,'test failure'); END");
            AndroidLuxuryCoinDraftStore store=new AndroidLuxuryCoinDraftStore(db);
            try { LuxuryCoinDraftImporter.addDrafts(store); throw new AssertionError("transaction absent"); }
            catch(IllegalStateException expected) { }
            db.begin();
            try { LuxuryCoinDraftImporter.addDrafts(store); throw new AssertionError("failure swallowed"); }
            catch(IllegalStateException expected) { check(expected.getMessage().contains("test failure"),"wrong exception"); db.rollback(); }
            check(exists(db,"SELECT count(*) FROM products HAVING count(*)=1"),"rollback products");
            check(!exists(db,"SELECT 1 FROM sqlite_master WHERE type='table' AND name='product_reference_reviews'"),"rollback table");
            db.execSQL("DROP TRIGGER fail_eye");
            db.execSQL("CREATE TABLE product_reference_reviews (sku TEXT PRIMARY KEY, price_note TEXT NOT NULL)");
            db.execSQL("INSERT INTO product_reference_reviews VALUES ('LC155-NIGHT','user note')");
            db.begin(); check(LuxuryCoinDraftImporter.addDrafts(store)==29,"twenty-nine new"); db.commit();
            check(exists(db,"SELECT count(*) FROM products HAVING count(*)=30"),"thirty records");
            check(exists(db,"SELECT 1 FROM products WHERE sku='LC155-DAY' AND name_fa='user edit' AND stock=21 AND published=1 AND sale_price=999"),"existing product untouched");
            check(exists(db,"SELECT 1 FROM products WHERE sku='LC155-NIGHT' AND stock=5 AND published=0 AND sale_price=6600000 AND image_key='LC155-NIGHT'"),"draft initialized");
            check(exists(db,"SELECT 1 FROM product_reference_reviews WHERE sku='LC155-NIGHT' AND price_note='user note'"),"note preserved");
            check(exists(db,"SELECT 1 FROM orders WHERE id=1 AND customer_name='customer'"),"order preserved");
            db.begin(); check(LuxuryCoinDraftImporter.addDrafts(store)==0,"idempotence"); db.commit();
            for (LuxuryCoinDraftCatalog.Draft draft : LuxuryCoinDraftCatalog.drafts()) {
                check(Files.isRegularFile(Paths.get(args[2],draft.assetPath)),"image missing");
                check(draft.assetPath.equals(LuxuryCoinDraftCatalog.assetPathForSku(draft.sku)),"lookup mismatch");
            }
            check(LuxuryCoinDraftCatalog.assetPathForSku("../../etc/passwd")==null,"arbitrary paths prohibited");
        }
        System.out.println("30 product draft import exercised against CPython SQLite, not Android or a live app.");
    }
}
