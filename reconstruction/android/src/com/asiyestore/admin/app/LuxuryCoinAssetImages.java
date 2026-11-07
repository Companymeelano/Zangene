package com.asiyestore.admin.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;

/** Loads only known draft images; returns null on missing asset so caller can use a placeholder. */
public final class LuxuryCoinAssetImages {
    private LuxuryCoinAssetImages() { }
    public static Bitmap load(Context context, String imageKey) {
        if (context == null) return null;
        String asset=LuxuryCoinDraftCatalog.assetPathForSku(imageKey);
        if (asset == null) return null;
        try (InputStream input=context.getAssets().open(asset)) {
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) { return null; }
    }
}
