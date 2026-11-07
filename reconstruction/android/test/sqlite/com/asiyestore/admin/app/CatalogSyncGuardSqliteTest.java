package com.asiyestore.admin.app;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public final class CatalogSyncGuardSqliteTest {
    private static void check(boolean value,String message) { if (!value) throw new AssertionError(message); }
    private static boolean exists(SQLiteDatabase db,String sql) { try (Cursor c=db.rawQuery(sql,new String[0])) { return c.moveToFirst(); } }
    public static void main(String[] args) throws Exception {
        String schema=new String(Files.readAllBytes(Paths.get(args[1])),StandardCharsets.UTF_8);
        try (SQLiteDatabase db=new SQLiteDatabase(args[0])) {
            db.execSQL(schema);
            db.execSQL("INSERT INTO products (sku,ttac_serial,batch_code,expiry_label,name_fa,name_en,category,description,original_price,sale_price,stock,published,image_key) VALUES ('A','','','','user name','','','',100,80,2,0,'user image')");
            db.execSQL("INSERT INTO products (sku,ttac_serial,batch_code,expiry_label,name_fa,name_en,category,description,original_price,sale_price,stock,published) VALUES ('B','','','','second','','','',50,40,1,1)");
            CatalogSyncGuard.Change a=new CatalogSyncGuard.Change("A",100,80,2,200,150,0);
            CatalogSyncGuard.Change stale=new CatalogSyncGuard.Change("B",50,40,999,70,60,1);
            AndroidCatalogSyncStore store=new AndroidCatalogSyncStore(db);
            try { CatalogSyncGuard.apply(store,Arrays.asList(a)); throw new AssertionError("no transaction"); }
            catch (IllegalStateException expected) { }
            try { store.updateIfUnchanged(a); throw new AssertionError("adapter requires transaction"); }
            catch (IllegalStateException expected) { }
            db.begin();
            try { CatalogSyncGuard.apply(store,Arrays.asList(a,new CatalogSyncGuard.Change("B",50,40,1,70,0,1))); throw new AssertionError("invalid price accepted"); }
            catch (IllegalArgumentException expected) { db.rollback(); }
            check(exists(db,"SELECT 1 FROM products WHERE sku='A' AND original_price=100 AND stock=2"),"invalid batch wrote data");
            db.begin();
            try { CatalogSyncGuard.apply(store,Arrays.asList(a,stale)); throw new AssertionError("stale accepted"); }
            catch (IllegalStateException expected) { db.rollback(); }
            check(exists(db,"SELECT 1 FROM products WHERE sku='A' AND original_price=100 AND sale_price=80 AND stock=2"),"batch rollback");
            db.begin();
            try { CatalogSyncGuard.apply(store,Arrays.asList(a,a)); throw new AssertionError("duplicate accepted"); }
            catch (IllegalArgumentException expected) { db.rollback(); }
            check(exists(db,"SELECT 1 FROM products WHERE sku='A' AND original_price=100 AND sale_price=80 AND stock=2"),"validate before write");
            db.begin(); CatalogSyncGuard.apply(store,Arrays.asList(a)); db.commit();
            check(exists(db,"SELECT 1 FROM products WHERE sku='A' AND original_price=200 AND sale_price=150 AND stock=0 AND discount_percent=25 AND name_fa='user name' AND published=0 AND image_key='user image'"),"update preserves other fields");
            check(exists(db,"SELECT 1 FROM products WHERE sku='B' AND original_price=50 AND sale_price=40 AND stock=1"),"other record intact");
            db.begin();
            try { CatalogSyncGuard.apply(store,Arrays.asList(new CatalogSyncGuard.Change("missing",0,0,0,1,1,0))); throw new AssertionError("missing accepted"); }
            catch (IllegalStateException expected) { db.rollback(); }
            check(exists(db,"SELECT count(*) FROM products HAVING count(*)=2"),"no insert");
        }
        System.out.println("Catalog compare-and-set guard tested against CPython SQLite; not Android or live API.");
    }
}
