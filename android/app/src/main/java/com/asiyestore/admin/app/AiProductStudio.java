package com.asiyestore.admin.app;

import android.graphics.Bitmap;
import java.util.Locale;

/** Local, deterministic draft templates. No image recognition or web search is performed here. */
public final class AiProductStudio {
    private AiProductStudio() { }

    public static final class Analysis {
        public String detectedTitle = "";
        public String detectedBrand = "";
        public String category = "";
        public String formFactorFa = "";
        public int confidence = 0; // No statistical confidence is available in local mode.
        public String rationaleFa = "";
        public String photoInsightFa = "";
    }

    public static final class Draft {
        public String nameFa = "";
        public String nameEn = "";
        public String brand = "";
        public String category = "";
        public String description = "";
        public String longDescription = "";
        public String benefits = "";
        public String ingredients = "";
        public String usage = "";
        public String country = "";
        public String volume = "";
        public String keywords = "";
        public String seoTitle = "";
        public String seoDescription = "";
        public String instagramCaption = "";
        public String provenance = "";
        public String imageKey = "generic";
        public long suggestedPrice = 0;
        public long suggestedOldPrice = 0;
        public int stock = 0;
    }

    /** A keyword suggestion, not identification; the merchant can change the category. */
    public static Analysis analyze(String rawName, String rawBrand, Bitmap photo) {
        Analysis a = new Analysis();
        String name = rawName == null ? "" : rawName.trim();
        String normalized = name.toLowerCase(Locale.US).replace('ي', 'ی').replace('ك', 'ک').replace('\u200c', ' ');
        boolean tea = normalized.contains("چای") || normalized.contains("black tea");
        a.detectedTitle = name;
        a.detectedBrand = rawBrand == null ? "" : rawBrand.trim();
        a.category = tea ? "چای و نوشیدنی" : "سایر محصولات";
        a.formFactorFa = tea ? "چای" : "محصول";
        a.rationaleFa = "این پیشنهاد از نام واردشده ساخته شده است، نه تشخیص عکس یا جست‌وجوی اینترنتی. دسته و مشخصات را بررسی و تکمیل کنید.";
        return a;
    }

    /** Neutral editable copy; never invent ingredients, price, origin, or health claims. */
    public static Draft compose(Analysis a, String rawName, String rawBrand) {
        Draft d = new Draft();
        String name = rawName == null ? "" : rawName.trim();
        String brand = rawBrand == null ? "" : rawBrand.trim();
        d.nameFa = name; d.brand = brand; d.category = a.category;
        d.provenance = "پیش‌نویس محلی؛ عکس تحلیل نشده است.";
        String brandText = brand.isEmpty() ? "" : " از برند " + brand;
        d.description = name + brandText + ".";
        d.longDescription = d.description + "\nبرای اطلاع از مشخصات و نحوه استفاده، اطلاعات روی بسته‌بندی را بررسی کنید.";
        d.keywords = name + (brand.isEmpty() ? "" : "، " + brand) + "، " + a.category;
        d.seoTitle = trim(name + (brand.isEmpty() ? "" : " | " + brand), 60);
        d.seoDescription = trim(d.description, 160);
        d.instagramCaption = "✨ " + name + brandText + "\n\nبرای اطلاعات بیشتر با مجموعه تماس بگیرید.\n\n#اسیه_استور";
        return d;
    }

    /** Fill only empty marketing copy with neutral, clearly editable local text. Never infer product facts. */
    public static void fillEmptyCopy(Draft d) {
        if (d == null || d.nameFa == null || d.nameFa.trim().isEmpty()) return;
        Analysis a = analyze(d.nameFa, d.brand, null);
        if (d.category != null && !d.category.trim().isEmpty()) a.category = d.category.trim();
        Draft neutral = compose(a, d.nameFa, d.brand);
        if (empty(d.description)) d.description = neutral.description;
        if (empty(d.longDescription)) d.longDescription = neutral.longDescription;
        if (empty(d.keywords)) d.keywords = neutral.keywords;
        if (empty(d.seoTitle)) d.seoTitle = neutral.seoTitle;
        if (empty(d.seoDescription)) d.seoDescription = neutral.seoDescription;
        if (empty(d.instagramCaption)) d.instagramCaption = neutral.instagramCaption;
    }

    private static boolean empty(String s) { return s == null || s.trim().isEmpty(); }

    private static String trim(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
