package com.asiyestore.admin.app;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.util.Locale;

/** ONLY for a separate disposable offline preview; never use in a production build. */
public final class OfflineCatalogPreview {
    private OfflineCatalogPreview() { }
    public static void seedPreview(SQLiteDatabase db) {
        // Called during SQLiteOpenHelper.onCreate's transaction for a new isolated package.
        LuxuryCoinDraftImporter.addDrafts(new AndroidLuxuryCoinDraftStore(db));
        // Public demo only; these are historical screenshot prices and stock is fictitious.
        db.execSQL("UPDATE products SET published=1 WHERE sku LIKE 'LC155-%'");
    }
    public static int imageResource(Context context, String sku) {
        if (sku==null || LuxuryCoinDraftCatalog.assetPathForSku(sku)==null) return 0;
        return context.getResources().getIdentifier("lc155_"+sku.substring(6).toLowerCase(Locale.ROOT),"drawable",context.getPackageName());
    }
}
