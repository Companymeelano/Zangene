package com.asiyestore.admin.app;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Drawable names only. Does not touch user-provided photos or Android resources. */
public final class ProductImageRegistry {
    public static final String PLACEHOLDER = "ic_product_placeholder";
    private static final Map<String, String> IMAGES;
    static {
        Map<String, String> images = new LinkedHashMap<>();
        // Keep the original image keys stable for existing products.
        images.put("gatieau_whitening", "img_gatieau_whitening");
        images.put("gatieau_antiwrinkle", "img_gatieau_antiwrinkle");
        images.put("luxury_coin", "img_luxury_coin");
        images.put("hair_friend", "img_hair_friend");
        images.put("ordinary_toner", "img_ordinary_toner");
        images.put("moringa_teatree", "img_moringa_teatree");
        images.put("generic", PLACEHOLDER);
        // Sample reference images are not verification of product identity.
        images.put("reference_gatieau", "img_reference_gatieau");
        images.put("reference_wheat", "img_reference_wheat");
        images.put("reference_moringa", "img_reference_moringa");
        IMAGES = Collections.unmodifiableMap(images);
    }
    private ProductImageRegistry() { }

    /** Unknown, empty or absent keys must never impersonate a different product. */
    public static String drawableName(String key) {
        String resource = IMAGES.get(key);
        return resource == null ? PLACEHOLDER : resource;
    }
    public static Map<String, String> mappings() { return IMAGES; }
}
