package com.asiyestore.admin.app;

import android.content.Context;

/** Drawable lookup only; callers must prefer their existing, user-selected image path. */
public final class AndroidProductImages {
    private AndroidProductImages() { }
    public static int resolve(Context context, String key, int placeholderResource) {
        if (context == null || placeholderResource == 0) {
            throw new IllegalArgumentException("Context and placeholder resource required");
        }
        String name = ProductImageRegistry.drawableName(key);
        if (ProductImageRegistry.PLACEHOLDER.equals(name)) return placeholderResource;
        int id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return id == 0 ? placeholderResource : id;
    }
}
