package com.asiyestore.admin.app;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class RealSqliteMigrationTest {
    private static void check(boolean condition,String message) { if (!condition) throw new AssertionError(message); }
    private static boolean exists(SQLiteDatabase db,String sql,String... args) {
        try (Cursor c=db.rawQuery(sql,args)) { return c.moveToFirst(); }
    }
    public static void main(String[] args) throws Exception {
        String schema=new String(Files.readAllBytes(Paths.get(args[1])),StandardCharsets.UTF_8);
        try (SQLiteDatabase db=new SQLiteDatabase(args[0])) {
            db.execSQL(schema);
            db.execSQL("CREATE TABLE orders (id INTEGER PRIMARY KEY, customer_name TEXT)");
            db.execSQL("INSERT INTO orders VALUES (42,'customer data')");
            db.execSQL("INSERT INTO products (sku,ttac_serial,batch_code,expiry_label,name_fa,name_en,category,description,original_price,sale_price,stock,published) VALUES ('AS-REF-GATIEAU','','','','user edit','','','',123,123,27,1)");
            db.execSQL("CREATE TRIGGER fail_wheat BEFORE INSERT ON products WHEN NEW.sku = 'AS-REF-WHEAT' BEGIN SELECT RAISE(ABORT,'injected error'); END");
            db.begin();
            try {
                ReferenceDraftMigration.applyFromVersion9(new AndroidReferenceDraftStore(db),9);
                throw new AssertionError("expected SQL failure");
            } catch (IllegalStateException expected) {
                check(expected.getMessage().contains("injected error"), "unexpected failure: " + expected.getMessage());
                db.rollback();
            }
            check(!exists(db,"SELECT 1 FROM sqlite_master WHERE type='table' AND name='product_reference_reviews'"),"table creation rolled back");
            check(exists(db,"SELECT 1 FROM products WHERE sku=? AND name_fa='user edit' AND stock=27 AND published=1","AS-REF-GATIEAU"),"existing product preserved after rollback");
            check(exists(db,"SELECT 1 FROM orders WHERE id=42 AND customer_name='customer data'"),"unrelated data preserved");
            db.execSQL("DROP TRIGGER fail_wheat");
            db.execSQL("CREATE TABLE product_reference_reviews (sku TEXT PRIMARY KEY, price_note TEXT NOT NULL)");
            db.execSQL("INSERT INTO product_reference_reviews VALUES ('AS-REF-WHEAT','user note')");
            db.begin(); ReferenceDraftMigration.applyFromVersion9(new AndroidReferenceDraftStore(db),9); db.commit();
            check(exists(db,"SELECT 1 FROM products WHERE sku=? AND name_fa='user edit' AND stock=27 AND published=1","AS-REF-GATIEAU"),"existing product preserved");
            check(exists(db,"SELECT 1 FROM product_reference_reviews WHERE sku='AS-REF-WHEAT' AND price_note='user note'"),"existing note preserved");
            check(exists(db,"SELECT 1 FROM products WHERE sku='AS-REF-MORINGA' AND stock=5 AND published=0 AND sale_price=507500"),"unpublished draft inserted");
            check(exists(db,"SELECT count(*) FROM products HAVING count(*)=4"),"four products total");
            check(exists(db,"SELECT 1 FROM orders WHERE id=42 AND customer_name='customer data'"),"order preserved");
            db.begin(); ReferenceDraftMigration.applyFromVersion9(new AndroidReferenceDraftStore(db),9); db.commit();
            check(exists(db,"SELECT count(*) FROM products HAVING count(*)=4"),"idempotence");
        }
        System.out.println("Java migration adapter exercised against CPython SQLite (not Android SQLite).");
    }
}
