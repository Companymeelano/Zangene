package com.asiyestore.admin.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Additive v10 draft migration recovered from the offline demo; no I/O or transaction ownership. */
public final class ReferenceDraftMigration {
    public interface Store {
        /** The caller must own a transaction that rolls back if any operation fails. */
        boolean inTransaction();
        void ensureReferenceTable();
        boolean hasProduct(String sku);
        void insertDraft(Draft draft);
        void insertPriceNoteIfAbsent(String sku, String note);
    }
    public static final class Draft {
        public final String sku, nameFa, nameEn, brand, category, description, imageKey, priceNote, keywords, caption;
        public final long samplePrice;
        private Draft(String sku, String nameFa, String nameEn, String brand, String category,
                      String description, String imageKey, long samplePrice, String priceNote,
                      String keywords, String caption) {
            this.sku=sku; this.nameFa=nameFa; this.nameEn=nameEn; this.brand=brand;
            this.category=category; this.description=description; this.imageKey=imageKey;
            this.samplePrice=samplePrice; this.priceNote=priceNote; this.keywords=keywords; this.caption=caption;
        }
    }
    private static final List<Draft> DRAFTS = Collections.unmodifiableList(Arrays.asList(
        new Draft("AS-REF-GATIEAU", "کرم ضدچروک گاتیو — پیش‌نویس", "GATIEAU Anti Wrinkle Cream", "گاتیو", "مراقبت تخصصی پوست",
            "کرم مراقبت پوست با عنوان ضدچروک روی بسته‌بندی. مشخصات، حجم و اصالت محصول را از روی کالای موجود بررسی کنید.", "reference_gatieau", 998000L,
            "نمونه مشاهده‌شده در ۶ نوامبر ۲۰۲۶؛ پیشنهاد یک فروشگاه برای بسته ۴۰ میلی‌لیتری است، نه میانگین بازار. تطابق حجم و قیمت روز باید تأیید شود: https://limavin.com/product/gatieau-anti-wrinkle-cream/",
            "گاتیو، کرم ضدچروک، مراقبت پوست، کرم صورت", "یک انتخاب برای تکمیل برنامه مراقبت پوست: کرم ضدچروک گاتیو. برای مشخصات دقیق و قیمت روز، از فروشگاه سؤال کنید. #گاتیو #مراقبت_پوست"),
        new Draft("AS-REF-WHEAT", "پودر جوانه گندم بین استار — پیش‌نویس", "Wheat Germ Powder", "بین استار", "خوراکی و مکمل",
            "پودر جوانه گندم در بسته‌بندی بین استار. وزن، ترکیبات، شرایط نگهداری و شیوه مصرف را از روی بسته تأیید کنید؛ این محصول حاوی گندم است.", "reference_wheat", 183500L,
            "نمونه‌های مشاهده‌شده در ۶ نوامبر ۲۰۲۶؛ میانگین دو قیمت ۱۹۲٬۰۰۰ و ۱۷۵٬۰۰۰ تومان است، نه قیمت قطعی روز. مشخصات بسته و تازگی قیمت را بررسی کنید: https://aghayebiz.com/product/wheat-germ-powder/ و https://bizma.ir/product/wheat-germ-powder-dr-biz/",
            "پودر جوانه گندم، بین استار، جوانه گندم، خوراکی", "پودر جوانه گندم بین استار؛ برای آشنایی با مشخصات و روش مصرف، برچسب بسته را مطالعه کنید. برای قیمت روز از فروشگاه بپرسید. #جوانه_گندم #بین_استار"),
        new Draft("AS-REF-MORINGA", "شامپو مورینگا اکسونیک — پیش‌نویس", "EXONIC Moringa", "اکسونیک", "تقویت و درمان مو",
            "محصول مراقبت مو مورینگا اکسونیک؛ مدل دقیق، حجم و کاربرد را پیش از عرضه با برچسب محصول تطبیق دهید.", "reference_moringa", 507500L,
            "نمونه‌های مشاهده‌شده در ۶ نوامبر ۲۰۲۶؛ میانگین دو قیمت ۵۶۵٬۰۰۰ و ۴۵۰٬۰۰۰ تومان است. مدل و وزن نمونه و تازگی قیمت نیازمند تأیید است: https://bizbabiz.com/product/exonic-moringa-oil-shampoo/ و https://bizma.ir/product/exonic-hair-shampoo-oil-moringa/",
            "اکسونیک، مورینگا، شامپو، مراقبت مو", "برای روتین مراقبت مو، محصول مورینگا اکسونیک را بیشتر بشناسید. برای مدل دقیق و قیمت روز با فروشگاه در ارتباط باشید. #اکسونیک #مورینگا"),
        new Draft("AS-REF-PURPLE", "محصول بنفش — مدل نامشخص", "Product identity pending", "", "مراقبت تخصصی پوست",
            "نام و مدل محصول از اسکرین‌شات با اطمینان خوانده نمی‌شود؛ پیش از انتشار عکس واضح و مشخصات واقعی وارد کنید.", "generic", 0L,
            "هویت و قیمت مشخص نیست. لطفاً تصویر واضح و قیمت معتبر ارائه کنید.", "مراقبت پوست", "پس از تأیید نام و مشخصات محصول، متن معرفی آماده شود.")
    ));
    private ReferenceDraftMigration() { }
    public static List<Draft> drafts() { return DRAFTS; }

    /** Call inside the existing SQLiteOpenHelper migration transaction, never on an open database outside one. */
    public static void applyFromVersion9(Store store, int oldVersion) {
        if (oldVersion != 9) throw new IllegalArgumentException("Expected a version 9 schema");
        if (store == null || !store.inTransaction()) throw new IllegalStateException("Migration requires caller transaction");
        store.ensureReferenceTable();
        for (Draft draft : DRAFTS) {
            if (store.hasProduct(draft.sku)) continue;
            store.insertDraft(draft);
            store.insertPriceNoteIfAbsent(draft.sku, draft.priceNote);
        }
    }
}
