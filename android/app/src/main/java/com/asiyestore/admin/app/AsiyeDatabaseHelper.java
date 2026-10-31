package com.asiyestore.admin.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class AsiyeDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "asiye_imperial_v3.db";
    private static final int DB_VERSION = 1;

    public AsiyeDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "sku TEXT UNIQUE NOT NULL," +
                "ttac_serial TEXT NOT NULL," +
                "batch_code TEXT NOT NULL," +
                "expiry_label TEXT NOT NULL," +
                "cold_chain INTEGER NOT NULL DEFAULT 1," +
                "replenish_days INTEGER NOT NULL DEFAULT 45," +
                "name_fa TEXT NOT NULL," +
                "name_en TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "description TEXT NOT NULL," +
                "ingredients TEXT," +
                "usage_method TEXT," +
                "country TEXT," +
                "volume TEXT," +
                "original_price INTEGER NOT NULL," +
                "sale_price INTEGER NOT NULL," +
                "discount_percent INTEGER NOT NULL DEFAULT 0," +
                "stock INTEGER NOT NULL DEFAULT 0," +
                "low_stock_threshold INTEGER NOT NULL DEFAULT 5," +
                "published INTEGER NOT NULL DEFAULT 1," +
                "image_key TEXT NOT NULL DEFAULT 'serum'," +
                "wishlisted INTEGER NOT NULL DEFAULT 0" +
                ")");

        db.execSQL("CREATE TABLE coupons (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "code TEXT UNIQUE NOT NULL," +
                "discount_percent INTEGER NOT NULL DEFAULT 0," +
                "discount_amount INTEGER NOT NULL DEFAULT 0," +
                "min_purchase INTEGER NOT NULL DEFAULT 0," +
                "max_discount INTEGER NOT NULL DEFAULT 0," +
                "active INTEGER NOT NULL DEFAULT 1" +
                ")");

        db.execSQL("CREATE TABLE orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_number TEXT UNIQUE NOT NULL," +
                "customer_name TEXT NOT NULL," +
                "customer_mobile TEXT NOT NULL," +
                "address_summary TEXT NOT NULL," +
                "shipping_method TEXT NOT NULL," +
                "payment_method TEXT NOT NULL," +
                "installments_count INTEGER NOT NULL DEFAULT 1," +
                "subtotal INTEGER NOT NULL," +
                "discount INTEGER NOT NULL DEFAULT 0," +
                "wallet_used INTEGER NOT NULL DEFAULT 0," +
                "shipping_cost INTEGER NOT NULL DEFAULT 0," +
                "total_amount INTEGER NOT NULL," +
                "cashback_earned INTEGER NOT NULL DEFAULT 0," +
                "vip_gift_box INTEGER NOT NULL DEFAULT 1," +
                "cold_chain INTEGER NOT NULL DEFAULT 1," +
                "scented_tissue TEXT," +
                "gift_message TEXT," +
                "certificate_code TEXT," +
                "status TEXT NOT NULL DEFAULT 'paid'," +
                "tracking_code TEXT," +
                "created_at_label TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE order_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL," +
                "product_id INTEGER NOT NULL," +
                "product_name TEXT NOT NULL," +
                "variant_label TEXT NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "unit_price INTEGER NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE wallet_transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "amount INTEGER NOT NULL," +
                "type TEXT NOT NULL," +
                "description TEXT NOT NULL," +
                "created_at_label TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE certificates (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cert_code TEXT UNIQUE NOT NULL," +
                "product_name TEXT NOT NULL," +
                "sku TEXT NOT NULL," +
                "batch_code TEXT NOT NULL," +
                "owner_name TEXT NOT NULL," +
                "issued_at_label TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE stock_movements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "product_id INTEGER NOT NULL," +
                "product_name TEXT NOT NULL," +
                "delta_quantity INTEGER NOT NULL," +
                "type TEXT NOT NULL," +
                "reason TEXT NOT NULL," +
                "actor TEXT NOT NULL," +
                "created_at_label TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE settings (" +
                "key TEXT PRIMARY KEY," +
                "value TEXT" +
                ")");

        seedImperialEcosystem(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS order_items");
        db.execSQL("DROP TABLE IF EXISTS orders");
        db.execSQL("DROP TABLE IF EXISTS wallet_transactions");
        db.execSQL("DROP TABLE IF EXISTS certificates");
        db.execSQL("DROP TABLE IF EXISTS stock_movements");
        db.execSQL("DROP TABLE IF EXISTS coupons");
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS settings");
        onCreate(db);
    }

    private void seedImperialEcosystem(SQLiteDatabase db) {
        // All 6 Signature Products — each mapped to its own bespoke studio visual!
        long p1 = insertSeedProduct(db, "AS-SRM-001", "AS-AUTH-2026-9901", "LOT-24K-881", "۱۴۰۷/۰۸", true, 45,
                "سرم طلایی احیاکننده پوست ۲۴ عیار",
                "24K Golden Revitalizing Cellular Serum",
                "مراقبت سلولی پوست",
                "سرم سلولی حاوی ورقه‌های طلای کلوئیدی ۲۴ عیار، هیالورونیک اسید چندمولکولی و نیاسینامید برای بازسازی سلولی، لیفتینگ و درخشش ابریشمی پوست.",
                "24K Colloidal Gold, Multi-Molecular Hyaluronic Acid, Damask Rose Essence, Niacinamide",
                "روزانه ۲ تا ۳ قطره صبح و شب روی پوست تمیز صورت و گردن ماساژ داده شود.",
                "پاریس / لابراتوار اختصاصی ASIYE", "۳۰ میلی‌لیتر",
                2_300_000L, 1_850_000L, 28, 5, "serum");

        long p2 = insertSeedProduct(db, "AS-CRM-108", "AS-AUTH-2026-9902", "LOT-CAV-502", "۱۴۰۷/۰۶", true, 60,
                "کرم شب خاویار سیاه و پپتید طلایی",
                "Imperial Caviar & Gold Peptide Night Crème",
                "مراقبت سلولی پوست",
                "کرم بازسازی‌کننده عمیق شبانه با عصاره خاویار سیاه و کمپلکس مس-پپتید برای تغذیه عمقی و استحکام سد دفاعی پوست.",
                "Black Caviar Extract, Copper Tripeptide-1, Ceramide NP, Squalane",
                "هر شب با کاردک طلایی همراه محصول روی پوست صورت و گردن استفاده شود.",
                "سوئیس / فرانسه", "۵۰ میلی‌لیتر",
                3_100_000L, 2_690_000L, 14, 5, "caviar");

        long p3 = insertSeedProduct(db, "AS-LIP-042", "AS-AUTH-2026-9903", "LOT-VLV-319", "۱۴۰۷/۱۰", false, 90,
                "رژ لب مخملی مات اختصاصی ASIYE Couture",
                "Haute Couture Velvet Matte Lipstick",
                "آرایش کوتور لب",
                "رژ لب مات با بافت مخملی فوق‌سبک، غنی‌شده با شی‌باتر ارگانیک و ویتامین E با ماندگاری ۱۲ ساعته بدون ایجاد خشکی.",
                "Organic Shea Butter, Vitamin E, Jojoba Oil, Pure Mineral Pigments",
                "از مرکز لب به سمت گوشه‌ها با حرکت یکنواخت استفاده شود.",
                "فرانسه / پالت اختصاصی", "۴ گرم",
                520_000L, 450_000L, 4, 6, "lipstick");

        long p4 = insertSeedProduct(db, "AS-PRF-999", "TTAC-8849201140", "LOT-PAR-999", "۱۴۰۸/۱۲", true, 120,
                "ادوپرفیوم سلطنتی ASIYE Signature",
                "ASIYE Signature Eau De Parfum Intense",
                "عطر و نیش پرفیوم",
                "سمفونی مسحورکننده‌ای از نت‌های رز دمشقی، زعفران سلطنتی، کهربا و چوب صندل گرم؛ امضای فراموش‌نشدنی وقار با ماندگاری ۲۴ ساعته.",
                "Damask Rose, Royal Saffron, Ambergris, Sandalwood, White Musk",
                "روی نقاط نبض‌دار از فاصله ۱۵ سانتی‌متری اسپری شود.",
                "فرانسه (گرس)", "۱۰۰ میلی‌لیتر",
                2_450_000L, 2_100_000L, 19, 5, "perfume");

        insertSeedProduct(db, "AS-ELX-305", "AS-AUTH-2026-9905", "LOT-ELX-712", "۱۴۰۷/۰۹", false, 75,
                "الکسیر ابریشم و طلای ۲۴ عیار مو",
                "L'Elixir D'Exception 24K Gold Hair Oil",
                "الکسیر طلایی مو",
                "الکسیر کریستالی حاوی ذرات معلق طلای ۲۴ عیار و روغن آرگان مراکشی برای احیای ساقه مو و درخشش آینه‌ای بدون حس سنگینی.",
                "24K Gold Flakes, Pure Moroccan Argan Oil, Hydrolyzed Silk Protein",
                "چند قطره روی ساقه موهای نم‌دار یا خشک ماساژ داده شود.",
                "فرانسه", "۱۰۰ میلی‌لیتر",
                1_580_000L, 1_290_000L, 22, 5, "elixir");

        insertSeedProduct(db, "AS-DSC-777", "AS-AUTH-2026-9906", "LOT-VIP-777", "۱۴۰۸/۰۵", true, 30,
                "کافره مخمل هدیه VIP و پک تستر عطر Discovery",
                "VIP Velvet Coffret & 3x5ml Fragrance Discovery Set",
                "عطر و نیش پرفیوم",
                "شامل ۳ ویال کریستالی عطرهای اختصاصی در جعبه مخمل مشکی با مهر موم طلایی AS + کارت هدیه ۱۰۰٪ بازگشت وجه برای خرید نسخه اصلی.",
                "3x5ml Crystal Extrait de Parfum Vials + Wax-Sealed Gold Envelope",
                "هر ویال را در موقعیت‌های روزانه و شبانه تست کرده و امضای بویایی خود را بیابید.",
                "پاریس / بسته‌بندی دست‌ساز", "۳ × ۵ میلی‌لیتر",
                980_000L, 790_000L, 16, 5, "giftbox");

        // Coupons (including Smart Replenishment CRM coupon REFILL10)
        insertSeedCoupon(db, "ASIYE20", 20, 0, 500_000L, 700_000L);
        insertSeedCoupon(db, "VIPGOLD", 0, 350_000L, 1_500_000L, 350_000L);
        insertSeedCoupon(db, "REFILL10", 10, 0, 200_000L, 500_000L);

        // Welcome Gold Wallet Credit + Initial Sample Certificate
        insertWalletTxInternal(db, 250_000L, "welcome_gift", "هدیه خوش‌آمدگویی عضویت باشگاه الماس سیاه ASIYE");
        insertCertInternal(db, "AS-CERT-884100", "سرم طلایی احیاکننده پوست ۲۴ عیار", "AS-SRM-001", "LOT-24K-881", "سارا محمدی");

        // Sample VIP Orders with Gift Concierge & Cold-Chain
        ContentValues o1 = new ContentValues();
        o1.put("order_number", "AS-1405-8841");
        o1.put("customer_name", "سارا محمدی");
        o1.put("customer_mobile", "09123456789");
        o1.put("address_summary", "تهران، الهیه، خیابان فرشته، پلاک ۲۴، واحد ۸");
        o1.put("shipping_method", "پیک اختصاصی VIP زنجیره سرد");
        o1.put("payment_method", "snapp_pay");
        o1.put("installments_count", 4);
        o1.put("subtotal", 3_950_000L);
        o1.put("discount", 350_000L);
        o1.put("wallet_used", 0L);
        o1.put("shipping_cost", 0L);
        o1.put("total_amount", 3_600_000L);
        o1.put("cashback_earned", 180_000L);
        o1.put("vip_gift_box", 1);
        o1.put("cold_chain", 1);
        o1.put("scented_tissue", "رز دمشقی و عود سلطنتی");
        o1.put("gift_message", "تولدت مبارک عزیزم — با عشق جاودان");
        o1.put("certificate_code", "AS-CERT-884100");
        o1.put("status", "preparing");
        o1.put("tracking_code", "VIP-COLD-99201");
        o1.put("created_at_label", "امروز — ۱۱:۴۰");
        long ord1Id = db.insert("orders", null, o1);
        insertSeedOrderItem(db, ord1Id, p1, "سرم طلایی احیاکننده پوست ۲۴ عیار", "۳۰ میلی‌لیتر", 1, 1_850_000L);
        insertSeedOrderItem(db, ord1Id, p4, "ادوپرفیوم سلطنتی ASIYE Signature", "۱۰۰ میلی‌لیتر", 1, 2_100_000L);

        ContentValues o2 = new ContentValues();
        o2.put("order_number", "AS-1405-8839");
        o2.put("customer_name", "نازنین رضایی");
        o2.put("customer_mobile", "09131112233");
        o2.put("address_summary", "اصفهان، چهارباغ بالا، مجتمع پارسیان، واحد ۱۲");
        o2.put("shipping_method", "پست پیشتاز ویژه");
        o2.put("payment_method", "digipay");
        o2.put("installments_count", 4);
        o2.put("subtotal", 3_140_000L);
        o2.put("discount", 0L);
        o2.put("wallet_used", 140_000L);
        o2.put("shipping_cost", 0L);
        o2.put("total_amount", 3_000_000L);
        o2.put("cashback_earned", 150_000L);
        o2.put("vip_gift_box", 1);
        o2.put("cold_chain", 1);
        o2.put("scented_tissue", "وانیل ماداگاسکار و کهربا");
        o2.put("gift_message", "");
        o2.put("certificate_code", "AS-CERT-883902");
        o2.put("status", "shipped");
        o2.put("tracking_code", "294810004512984410");
        o2.put("created_at_label", "امروز — ۰۹:۱۵");
        long ord2Id = db.insert("orders", null, o2);
        insertSeedOrderItem(db, ord2Id, p2, "کرم شب خاویار سیاه و پپتید طلایی", "۵۰ میلی‌لیتر", 1, 2_690_000L);
        insertSeedOrderItem(db, ord2Id, p3, "رژ لب مخملی مات اختصاصی ASIYE Couture", "شامپاینی نود", 1, 450_000L);

        // FEFO Stock Movements
        insertMovementInternal(db, p1, "سرم طلایی احیاکننده ۲۴ عیار", 30, "in", "ورود بچ LOT-24K-881 (استاندارد FEFO)", "مدیر کل");
        insertMovementInternal(db, p1, "سرم طلایی احیاکننده ۲۴ عیار", -1, "out", "خروج FEFO سفارش AS-1405-8841", "سیستم هوشمند");
        insertMovementInternal(db, p3, "رژ لب مخملی مات ASIYE Couture", -1, "out", "خروج FEFO سفارش AS-1405-8839", "سیستم هوشمند");

        // Settings
        putSettingInternal(db, "api_base_url", "https://asiyestore.ir/api/v1");
        putSettingInternal(db, "customer_name", "مهمان سلطنتی ASIYE");
        putSettingInternal(db, "customer_mobile", "09123456789");
        putSettingInternal(db, "customer_address", "تهران، زعفرانیه، خیابان مقدس اردبیلی، پلاک ۱۸");
        putSettingInternal(db, "wallet_balance", "250000");
        putSettingInternal(db, "loyalty_points", "1480");
    }

    private long insertSeedProduct(SQLiteDatabase db, String sku, String ttac, String batch, String exp,
                                   boolean cold, int repDays, String nameFa, String nameEn,
                                   String cat, String desc, String ing, String usage, String country,
                                   String vol, long orig, long sale, int stock, int threshold, String imgKey) {
        int disc = orig > 0 ? (int) Math.round(((orig - sale) * 100.0) / orig) : 0;
        ContentValues cv = new ContentValues();
        cv.put("sku", sku);
        cv.put("ttac_serial", ttac);
        cv.put("batch_code", batch);
        cv.put("expiry_label", exp);
        cv.put("cold_chain", cold ? 1 : 0);
        cv.put("replenish_days", repDays);
        cv.put("name_fa", nameFa);
        cv.put("name_en", nameEn);
        cv.put("category", cat);
        cv.put("description", desc);
        cv.put("ingredients", ing);
        cv.put("usage_method", usage);
        cv.put("country", country);
        cv.put("volume", vol);
        cv.put("original_price", orig);
        cv.put("sale_price", sale);
        cv.put("discount_percent", Math.max(0, disc));
        cv.put("stock", stock);
        cv.put("low_stock_threshold", threshold);
        cv.put("published", 1);
        cv.put("image_key", imgKey);
        cv.put("wishlisted", 0);
        return db.insert("products", null, cv);
    }

    private void insertSeedCoupon(SQLiteDatabase db, String code, int pct, long amt, long minP, long maxD) {
        ContentValues cv = new ContentValues();
        cv.put("code", code);
        cv.put("discount_percent", pct);
        cv.put("discount_amount", amt);
        cv.put("min_purchase", minP);
        cv.put("max_discount", maxD);
        cv.put("active", 1);
        db.insert("coupons", null, cv);
    }

    private void insertSeedOrderItem(SQLiteDatabase db, long orderId, long productId, String name, String variant, int qty, long price) {
        ContentValues cv = new ContentValues();
        cv.put("order_id", orderId);
        cv.put("product_id", productId);
        cv.put("product_name", name);
        cv.put("variant_label", variant);
        cv.put("quantity", qty);
        cv.put("unit_price", price);
        db.insert("order_items", null, cv);
    }

    private void insertWalletTxInternal(SQLiteDatabase db, long amount, String type, String desc) {
        ContentValues cv = new ContentValues();
        cv.put("amount", amount);
        cv.put("type", type);
        cv.put("description", desc);
        cv.put("created_at_label", nowTimeLabel());
        db.insert("wallet_transactions", null, cv);
    }

    private void insertCertInternal(SQLiteDatabase db, String certCode, String pName, String sku, String batch, String owner) {
        ContentValues cv = new ContentValues();
        cv.put("cert_code", certCode);
        cv.put("product_name", pName);
        cv.put("sku", sku);
        cv.put("batch_code", batch);
        cv.put("owner_name", owner);
        cv.put("issued_at_label", nowTimeLabel());
        db.insertWithOnConflict("certificates", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void insertMovementInternal(SQLiteDatabase db, long productId, String name, int delta, String type, String reason, String actor) {
        ContentValues cv = new ContentValues();
        cv.put("product_id", productId);
        cv.put("product_name", name);
        cv.put("delta_quantity", delta);
        cv.put("type", type);
        cv.put("reason", reason);
        cv.put("actor", actor);
        cv.put("created_at_label", nowTimeLabel());
        db.insert("stock_movements", null, cv);
    }

    private void putSettingInternal(SQLiteDatabase db, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        cv.put("value", value);
        db.insertWithOnConflict("settings", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private String nowTimeLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        return "امروز — " + PersianFormatter.toPersianDigits(sdf.format(new Date()));
    }

    public String getSetting(String key, String fallback) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT value FROM settings WHERE key = ?", new String[]{key})) {
            if (c.moveToFirst()) {
                String v = c.getString(0);
                return v != null ? v : fallback;
            }
        }
        return fallback;
    }

    public void setSetting(String key, String value) {
        putSettingInternal(getWritableDatabase(), key, value);
    }

    public long getWalletBalance() {
        try {
            return Long.parseLong(getSetting("wallet_balance", "250000"));
        } catch (Exception e) {
            return 250_000L;
        }
    }

    public List<Models.WalletTransactionRecord> getWalletTransactions() {
        List<Models.WalletTransactionRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM wallet_transactions ORDER BY id DESC LIMIT 25", null)) {
            while (c.moveToNext()) {
                Models.WalletTransactionRecord r = new Models.WalletTransactionRecord();
                r.id = c.getLong(c.getColumnIndexOrThrow("id"));
                r.amount = c.getLong(c.getColumnIndexOrThrow("amount"));
                r.type = c.getString(c.getColumnIndexOrThrow("type"));
                r.description = c.getString(c.getColumnIndexOrThrow("description"));
                r.createdAtLabel = c.getString(c.getColumnIndexOrThrow("created_at_label"));
                list.add(r);
            }
        }
        return list;
    }

    public List<Models.CertificateRecord> getCertificates() {
        List<Models.CertificateRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM certificates ORDER BY id DESC LIMIT 25", null)) {
            while (c.moveToNext()) {
                Models.CertificateRecord r = new Models.CertificateRecord();
                r.id = c.getLong(c.getColumnIndexOrThrow("id"));
                r.certCode = c.getString(c.getColumnIndexOrThrow("cert_code"));
                r.productName = c.getString(c.getColumnIndexOrThrow("product_name"));
                r.sku = c.getString(c.getColumnIndexOrThrow("sku"));
                r.batchCode = c.getString(c.getColumnIndexOrThrow("batch_code"));
                r.ownerName = c.getString(c.getColumnIndexOrThrow("owner_name"));
                r.issuedAtLabel = c.getString(c.getColumnIndexOrThrow("issued_at_label"));
                list.add(r);
            }
        }
        return list;
    }

    public Models.Product verifyAuthenticitySerial(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) return null;
        String clean = rawCode.trim().toUpperCase(Locale.US);
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT * FROM products WHERE UPPER(ttac_serial) = ? OR UPPER(sku) = ? OR UPPER(batch_code) = ?",
                new String[]{clean, clean, clean})) {
            if (c.moveToFirst()) return readProduct(c);
        }
        // Also check minted digital ownership certificates
        try (Cursor c = db.rawQuery("SELECT sku FROM certificates WHERE UPPER(cert_code) = ?", new String[]{clean})) {
            if (c.moveToFirst()) {
                String sku = c.getString(0);
                try (Cursor p = db.rawQuery("SELECT * FROM products WHERE sku = ?", new String[]{sku})) {
                    if (p.moveToFirst()) return readProduct(p);
                }
            }
        }
        return null;
    }

    public List<Models.Product> getProducts(boolean onlyPublished, String categoryFilter, String searchQuery, String sortMode) {
        List<Models.Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (onlyPublished) {
            sql.append(" AND published = 1");
        }
        if (categoryFilter != null && !categoryFilter.isEmpty() && !"همه".equals(categoryFilter)) {
            sql.append(" AND category = ?");
            args.add(categoryFilter);
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String q = "%" + searchQuery.trim() + "%";
            sql.append(" AND (name_fa LIKE ? OR name_en LIKE ? OR sku LIKE ? OR batch_code LIKE ?)");
            args.add(q);
            args.add(q);
            args.add(q);
            args.add(q);
        }

        if ("price_asc".equals(sortMode)) {
            sql.append(" ORDER BY sale_price ASC");
        } else if ("price_desc".equals(sortMode)) {
            sql.append(" ORDER BY sale_price DESC");
        } else if ("discount".equals(sortMode)) {
            sql.append(" ORDER BY discount_percent DESC");
        } else {
            sql.append(" ORDER BY id ASC");
        }

        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(sql.toString(), args.toArray(new String[0]))) {
            while (c.moveToNext()) {
                list.add(readProduct(c));
            }
        }
        return list;
    }

    public List<Models.Product> getWishlistedProducts() {
        List<Models.Product> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM products WHERE wishlisted = 1 AND published = 1 ORDER BY id DESC", null)) {
            while (c.moveToNext()) {
                list.add(readProduct(c));
            }
        }
        return list;
    }

    public void toggleWishlist(long productId, boolean wishlisted) {
        ContentValues cv = new ContentValues();
        cv.put("wishlisted", wishlisted ? 1 : 0);
        getWritableDatabase().update("products", cv, "id = ?", new String[]{String.valueOf(productId)});
    }

    private Models.Product readProduct(Cursor c) {
        Models.Product p = new Models.Product();
        p.id = c.getLong(c.getColumnIndexOrThrow("id"));
        p.sku = c.getString(c.getColumnIndexOrThrow("sku"));
        p.ttacSerial = c.getString(c.getColumnIndexOrThrow("ttac_serial"));
        p.batchCode = c.getString(c.getColumnIndexOrThrow("batch_code"));
        p.expiryLabel = c.getString(c.getColumnIndexOrThrow("expiry_label"));
        p.coldChain = c.getInt(c.getColumnIndexOrThrow("cold_chain")) == 1;
        p.replenishDays = c.getInt(c.getColumnIndexOrThrow("replenish_days"));
        p.nameFa = c.getString(c.getColumnIndexOrThrow("name_fa"));
        p.nameEn = c.getString(c.getColumnIndexOrThrow("name_en"));
        p.category = c.getString(c.getColumnIndexOrThrow("category"));
        p.description = c.getString(c.getColumnIndexOrThrow("description"));
        p.ingredients = c.getString(c.getColumnIndexOrThrow("ingredients"));
        p.usageMethod = c.getString(c.getColumnIndexOrThrow("usage_method"));
        p.country = c.getString(c.getColumnIndexOrThrow("country"));
        p.volume = c.getString(c.getColumnIndexOrThrow("volume"));
        p.originalPrice = c.getLong(c.getColumnIndexOrThrow("original_price"));
        p.salePrice = c.getLong(c.getColumnIndexOrThrow("sale_price"));
        p.discountPercent = c.getInt(c.getColumnIndexOrThrow("discount_percent"));
        p.stock = c.getInt(c.getColumnIndexOrThrow("stock"));
        p.lowStockThreshold = c.getInt(c.getColumnIndexOrThrow("low_stock_threshold"));
        p.published = c.getInt(c.getColumnIndexOrThrow("published")) == 1;
        p.imageKey = c.getString(c.getColumnIndexOrThrow("image_key"));
        p.wishlisted = c.getInt(c.getColumnIndexOrThrow("wishlisted")) == 1;
        return p;
    }

    public Models.Product getProductById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM products WHERE id = ?", new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return readProduct(c);
        }
        return null;
    }

    public String saveOrUpdateProduct(Long existingId, String sku, String nameFa, String nameEn,
                                      String category, String description, String country, String volume,
                                      long origPrice, long salePrice, int initialStock, boolean published, String imageKey) {
        if (salePrice > origPrice) {
            return "قیمت فروش نمی‌تواند بیشتر از قیمت اصلی باشد.";
        }
        int disc = origPrice > 0 ? (int) Math.round(((origPrice - salePrice) * 100.0) / origPrice) : 0;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        String normSku = sku.trim().toUpperCase(Locale.US);
        cv.put("sku", normSku);
        cv.put("name_fa", nameFa.trim());
        cv.put("name_en", nameEn.trim().isEmpty() ? "ASIYE Haute Beauté" : nameEn.trim());
        cv.put("category", category);
        cv.put("description", description.trim());
        cv.put("country", country.trim().isEmpty() ? "فرانسه / ایران" : country.trim());
        cv.put("volume", volume.trim().isEmpty() ? "۵۰ میلی‌لیتر" : volume.trim());
        cv.put("original_price", origPrice);
        cv.put("sale_price", salePrice);
        cv.put("discount_percent", Math.max(0, disc));
        cv.put("published", published ? 1 : 0);
        cv.put("image_key", imageKey);

        try {
            if (existingId == null) {
                cv.put("ttac_serial", "AS-AUTH-" + (100000 + new Random().nextInt(899999)));
                cv.put("batch_code", "LOT-IMP-" + (100 + new Random().nextInt(899)));
                cv.put("expiry_label", "۱۴۰۸/۰۶");
                cv.put("cold_chain", 1);
                cv.put("replenish_days", 45);
                cv.put("stock", Math.max(0, initialStock));
                cv.put("low_stock_threshold", 5);
                long newId = db.insertOrThrow("products", null, cv);
                if (initialStock > 0) {
                    insertMovementInternal(db, newId, nameFa.trim(), initialStock, "in", "ورود بچ جدید (استاندارد FEFO)", "مدیر کل");
                }
            } else {
                db.update("products", cv, "id = ?", new String[]{String.valueOf(existingId)});
            }
            return null;
        } catch (Exception e) {
            return "کد SKU تکراری است یا خطا در ذخیره‌سازی رخ داد.";
        }
    }

    public void upsertSyncedProduct(String sku, String nameFa, String nameEn, long orig, long sale, int stock) {
        SQLiteDatabase db = getWritableDatabase();
        try (Cursor c = db.rawQuery("SELECT id FROM products WHERE sku = ?", new String[]{sku})) {
            int disc = orig > 0 ? (int) Math.round(((orig - sale) * 100.0) / orig) : 0;
            ContentValues cv = new ContentValues();
            cv.put("name_fa", nameFa);
            cv.put("name_en", nameEn);
            cv.put("original_price", orig);
            cv.put("sale_price", sale);
            cv.put("discount_percent", Math.max(0, disc));
            cv.put("stock", stock);
            if (c.moveToFirst()) {
                db.update("products", cv, "id = ?", new String[]{String.valueOf(c.getLong(0))});
            } else {
                cv.put("sku", sku);
                cv.put("ttac_serial", "AS-SYNC-" + sku);
                cv.put("batch_code", "LOT-SYNC-01");
                cv.put("expiry_label", "۱۴۰۸/۰۱");
                cv.put("cold_chain", 1);
                cv.put("replenish_days", 45);
                cv.put("category", "مراقبت سلولی پوست");
                cv.put("description", "همگام‌سازی‌شده از سرور مرکزی ASIYE STORE");
                cv.put("country", "فرانسه");
                cv.put("volume", "استاندارد");
                cv.put("published", 1);
                cv.put("image_key", "serum");
                db.insert("products", null, cv);
            }
        }
    }

    public String archiveProductSafely(long productId) {
        SQLiteDatabase db = getWritableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM order_items oi JOIN orders o ON o.id = oi.order_id " +
                "WHERE oi.product_id = ? AND o.status IN ('pending_payment','paid','confirmed','preparing','shipped')",
                new String[]{String.valueOf(productId)})) {
            if (c.moveToFirst() && c.getInt(0) > 0) {
                ContentValues cv = new ContentValues();
                cv.put("published", 0);
                db.update("products", cv, "id = ?", new String[]{String.valueOf(productId)});
                return "به دلیل وجود سفارش فعال، محصول حذف فیزیکی نشد و به حالت «آرشیو امن» تغییر یافت.";
            }
        }
        db.delete("products", "id = ?", new String[]{String.valueOf(productId)});
        return "محصول از کاتالوگ حذف شد.";
    }

    public String adjustInventory(long productId, String type, int quantity, String reason) {
        if (quantity <= 0) return "تعداد باید بزرگ‌تر از صفر باشد.";
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Models.Product p = getProductById(productId);
            if (p == null) return "محصول یافت نشد.";

            int newStock;
            int delta;
            if ("in".equals(type)) {
                newStock = p.stock + quantity;
                delta = quantity;
            } else if ("out".equals(type)) {
                if (p.stock < quantity) {
                    return "موجودی فعلی (" + PersianFormatter.toPersianDigits(String.valueOf(p.stock)) + ") کمتر از مقدار خروجی است.";
                }
                newStock = p.stock - quantity;
                delta = -quantity;
            } else {
                newStock = quantity;
                delta = quantity - p.stock;
            }

            ContentValues cv = new ContentValues();
            cv.put("stock", newStock);
            db.update("products", cv, "id = ?", new String[]{String.valueOf(productId)});
            insertMovementInternal(db, productId, p.nameFa, delta, type, reason + " [بچ: " + p.batchCode + "]", "مدیر انبار FEFO");

            db.setTransactionSuccessful();
            return null;
        } finally {
            db.endTransaction();
        }
    }

    public List<Models.StockMovementRecord> getStockMovements() {
        List<Models.StockMovementRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM stock_movements ORDER BY id DESC LIMIT 60", null)) {
            while (c.moveToNext()) {
                Models.StockMovementRecord m = new Models.StockMovementRecord();
                m.id = c.getLong(c.getColumnIndexOrThrow("id"));
                m.productId = c.getLong(c.getColumnIndexOrThrow("product_id"));
                m.productName = c.getString(c.getColumnIndexOrThrow("product_name"));
                m.deltaQuantity = c.getInt(c.getColumnIndexOrThrow("delta_quantity"));
                m.type = c.getString(c.getColumnIndexOrThrow("type"));
                m.reason = c.getString(c.getColumnIndexOrThrow("reason"));
                m.actor = c.getString(c.getColumnIndexOrThrow("actor"));
                m.createdAtLabel = c.getString(c.getColumnIndexOrThrow("created_at_label"));
                list.add(m);
            }
        }
        return list;
    }

    public long evaluateCouponDiscount(String rawCode, long subtotal) {
        if (rawCode == null || rawCode.trim().isEmpty() || subtotal <= 0) return 0L;
        String code = rawCode.trim().toUpperCase(Locale.US);
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM coupons WHERE code = ? AND active = 1", new String[]{code})) {
            if (!c.moveToFirst()) return -1L;
            long minP = c.getLong(c.getColumnIndexOrThrow("min_purchase"));
            if (subtotal < minP) return -2L;
            int pct = c.getInt(c.getColumnIndexOrThrow("discount_percent"));
            long amt = c.getLong(c.getColumnIndexOrThrow("discount_amount"));
            long maxD = c.getLong(c.getColumnIndexOrThrow("max_discount"));

            long disc = pct > 0 ? Math.round(subtotal * (pct / 100.0)) : amt;
            if (maxD > 0 && disc > maxD) disc = maxD;
            return Math.min(disc, subtotal);
        }
    }

    public List<Models.CouponRecord> getCoupons() {
        List<Models.CouponRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM coupons ORDER BY id DESC", null)) {
            while (c.moveToNext()) {
                Models.CouponRecord r = new Models.CouponRecord();
                r.id = c.getLong(c.getColumnIndexOrThrow("id"));
                r.code = c.getString(c.getColumnIndexOrThrow("code"));
                r.discountPercent = c.getInt(c.getColumnIndexOrThrow("discount_percent"));
                r.discountAmount = c.getLong(c.getColumnIndexOrThrow("discount_amount"));
                r.minPurchase = c.getLong(c.getColumnIndexOrThrow("min_purchase"));
                r.maxDiscount = c.getLong(c.getColumnIndexOrThrow("max_discount"));
                r.active = c.getInt(c.getColumnIndexOrThrow("active")) == 1;
                list.add(r);
            }
        }
        return list;
    }

    public boolean createCoupon(String code, int percent, long fixedAmount, long minPurchase) {
        ContentValues cv = new ContentValues();
        cv.put("code", code.trim().toUpperCase(Locale.US));
        cv.put("discount_percent", Math.max(0, Math.min(90, percent)));
        cv.put("discount_amount", Math.max(0, fixedAmount));
        cv.put("min_purchase", Math.max(0, minPurchase));
        cv.put("max_discount", fixedAmount > 0 ? fixedAmount : 1_500_000L);
        cv.put("active", 1);
        return getWritableDatabase().insertWithOnConflict("coupons", null, cv, SQLiteDatabase.CONFLICT_IGNORE) != -1;
    }

    /**
     * Imperial Atomic Checkout:
     * - Deducts FEFO stock safely
     * - Applies Coupon + Gold Cashback Wallet balance
     * - Records White-Glove Velvet Gift Concierge, Scented Tissue, Gift Card & Cold-Chain options
     * - Credits 5% Instant Gold Cashback to Wallet Ledger
     * - Mints a personalized Digital Certificate of Authenticity (AS-CERT-XXXXXX)
     */
    public Models.OrderRecord executeImperialCheckout(List<Models.CartItem> cart,
                                                      String customerName,
                                                      String customerMobile,
                                                      String addressSummary,
                                                      String shippingMethod,
                                                      long baseShippingCost,
                                                      String paymentMethod,
                                                      int installmentsCount,
                                                      String couponCode,
                                                      boolean useGoldWallet,
                                                      boolean vipGiftBox,
                                                      boolean coldChain,
                                                      String scentedTissue,
                                                      String giftMessage) throws IllegalStateException {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("سبد تشریفاتی خالی است.");
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long subtotal = 0L;
            for (Models.CartItem item : cart) {
                if (item.quantity <= 0) throw new IllegalStateException("تعداد نامعتبر.");
                Models.Product fresh = getProductById(item.product.id);
                if (fresh == null || !fresh.published) {
                    throw new IllegalStateException("محصول «" + item.product.nameFa + "» غیرفعال است.");
                }
                if (fresh.stock < item.quantity) {
                    throw new IllegalStateException("موجودی کافی برای «" + fresh.nameFa + "» در انبار نیست.");
                }
                item.product = fresh;
                subtotal += fresh.salePrice * item.quantity;
            }

            long discount = 0L;
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                long eval = evaluateCouponDiscount(couponCode, subtotal);
                if (eval > 0) discount = eval;
            }

            long afterCoupon = Math.max(0L, subtotal - discount);
            long currentWallet = getWalletBalance();
            long walletDeducted = useGoldWallet ? Math.min(currentWallet, afterCoupon) : 0L;
            long afterWallet = Math.max(0L, afterCoupon - walletDeducted);
            long shipping = afterWallet >= 3_000_000L ? 0L : baseShippingCost;
            long total = afterWallet + shipping;
            long cashbackEarned = Math.max(15_000L, Math.round(total * 0.05));

            String orderNumber = "AS-1405-" + (1000 + new Random().nextInt(8999));
            String certCode = "AS-CERT-" + (100000 + new Random().nextInt(899999));

            ContentValues ord = new ContentValues();
            ord.put("order_number", orderNumber);
            ord.put("customer_name", customerName);
            ord.put("customer_mobile", customerMobile);
            ord.put("address_summary", addressSummary);
            ord.put("shipping_method", shippingMethod);
            ord.put("payment_method", paymentMethod);
            ord.put("installments_count", Math.max(1, installmentsCount));
            ord.put("subtotal", subtotal);
            ord.put("discount", discount);
            ord.put("wallet_used", walletDeducted);
            ord.put("shipping_cost", shipping);
            ord.put("total_amount", total);
            ord.put("cashback_earned", cashbackEarned);
            ord.put("vip_gift_box", vipGiftBox ? 1 : 0);
            ord.put("cold_chain", coldChain ? 1 : 0);
            ord.put("scented_tissue", scentedTissue);
            ord.put("gift_message", giftMessage);
            ord.put("certificate_code", certCode);
            ord.put("status", "paid");
            ord.put("tracking_code", "");
            ord.put("created_at_label", nowTimeLabel());

            long orderId = db.insertOrThrow("orders", null, ord);

            for (Models.CartItem item : cart) {
                insertSeedOrderItem(db, orderId, item.product.id, item.product.nameFa,
                        item.selectedVariant, item.quantity, item.product.salePrice);

                ContentValues up = new ContentValues();
                up.put("stock", item.product.stock - item.quantity);
                db.update("products", up, "id = ?", new String[]{String.valueOf(item.product.id)});

                insertMovementInternal(db, item.product.id, item.product.nameFa,
                        -item.quantity, "out", "خروج FEFO سفارش " + orderNumber + " [" + item.product.batchCode + "]", "کانسیرژ فروش");
            }

            // Mint Digital Authenticity Certificate for primary luxury item
            Models.Product heroProd = cart.get(0).product;
            insertCertInternal(db, certCode, heroProd.nameFa, heroProd.sku, heroProd.batchCode, customerName);

            // Update Gold Cashback Wallet & Ledger
            long updatedWallet = (currentWallet - walletDeducted) + cashbackEarned;
            putSettingInternal(db, "wallet_balance", String.valueOf(updatedWallet));
            if (walletDeducted > 0) {
                insertWalletTxInternal(db, -walletDeducted, "order_redemption", "کسر اعتبار کیف پول بابت سفارش " + orderNumber);
            }
            insertWalletTxInternal(db, cashbackEarned, "cashback_reward", "پاداش ۵٪ کش‌بک طلایی سفارش " + orderNumber);

            long currentPts = Long.parseLong(getSetting("loyalty_points", "1480"));
            putSettingInternal(db, "loyalty_points", String.valueOf(currentPts + Math.max(20L, total / 10_000L)));

            db.setTransactionSuccessful();
            return getOrderById(orderId);
        } finally {
            db.endTransaction();
        }
    }

    public List<Models.OrderRecord> getOrders(String statusFilter) {
        List<Models.OrderRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM orders";
        String[] args = null;
        if (statusFilter != null && !statusFilter.isEmpty() && !"all".equals(statusFilter)) {
            sql += " WHERE status = ?";
            args = new String[]{statusFilter};
        }
        sql += " ORDER BY id DESC";

        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                Models.OrderRecord o = readOrder(c);
                o.items = getOrderItems(db, o.id);
                list.add(o);
            }
        }
        return list;
    }

    public Models.OrderRecord getOrderById(long orderId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM orders WHERE id = ?", new String[]{String.valueOf(orderId)})) {
            if (c.moveToFirst()) {
                Models.OrderRecord o = readOrder(c);
                o.items = getOrderItems(db, o.id);
                return o;
            }
        }
        return null;
    }

    private Models.OrderRecord readOrder(Cursor c) {
        Models.OrderRecord o = new Models.OrderRecord();
        o.id = c.getLong(c.getColumnIndexOrThrow("id"));
        o.orderNumber = c.getString(c.getColumnIndexOrThrow("order_number"));
        o.customerName = c.getString(c.getColumnIndexOrThrow("customer_name"));
        o.customerMobile = c.getString(c.getColumnIndexOrThrow("customer_mobile"));
        o.addressSummary = c.getString(c.getColumnIndexOrThrow("address_summary"));
        o.shippingMethod = c.getString(c.getColumnIndexOrThrow("shipping_method"));
        o.paymentMethod = c.getString(c.getColumnIndexOrThrow("payment_method"));
        o.installmentsCount = c.getInt(c.getColumnIndexOrThrow("installments_count"));
        o.subtotal = c.getLong(c.getColumnIndexOrThrow("subtotal"));
        o.discount = c.getLong(c.getColumnIndexOrThrow("discount"));
        o.walletUsed = c.getLong(c.getColumnIndexOrThrow("wallet_used"));
        o.shippingCost = c.getLong(c.getColumnIndexOrThrow("shipping_cost"));
        o.totalAmount = c.getLong(c.getColumnIndexOrThrow("total_amount"));
        o.cashbackEarned = c.getLong(c.getColumnIndexOrThrow("cashback_earned"));
        o.vipGiftBox = c.getInt(c.getColumnIndexOrThrow("vip_gift_box")) == 1;
        o.coldChain = c.getInt(c.getColumnIndexOrThrow("cold_chain")) == 1;
        o.scentedTissue = c.getString(c.getColumnIndexOrThrow("scented_tissue"));
        o.giftMessage = c.getString(c.getColumnIndexOrThrow("gift_message"));
        o.certificateCode = c.getString(c.getColumnIndexOrThrow("certificate_code"));
        o.status = c.getString(c.getColumnIndexOrThrow("status"));
        o.trackingCode = c.getString(c.getColumnIndexOrThrow("tracking_code"));
        o.createdAtLabel = c.getString(c.getColumnIndexOrThrow("created_at_label"));
        return o;
    }

    private List<Models.OrderItemRecord> getOrderItems(SQLiteDatabase db, long orderId) {
        List<Models.OrderItemRecord> items = new ArrayList<>();
        try (Cursor c = db.rawQuery("SELECT * FROM order_items WHERE order_id = ?", new String[]{String.valueOf(orderId)})) {
            while (c.moveToNext()) {
                Models.OrderItemRecord item = new Models.OrderItemRecord();
                item.productId = c.getLong(c.getColumnIndexOrThrow("product_id"));
                item.productName = c.getString(c.getColumnIndexOrThrow("product_name"));
                item.variantLabel = c.getString(c.getColumnIndexOrThrow("variant_label"));
                item.quantity = c.getInt(c.getColumnIndexOrThrow("quantity"));
                item.unitPrice = c.getLong(c.getColumnIndexOrThrow("unit_price"));
                items.add(item);
            }
        }
        return items;
    }

    public String updateOrderStatus(long orderId, String newStatus, String trackingCode) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Models.OrderRecord order = getOrderById(orderId);
            if (order == null) return "سفارش یافت نشد.";

            boolean wasCancelled = "cancelled".equals(order.status) || "returned".equals(order.status);
            boolean willCancel = "cancelled".equals(newStatus) || "returned".equals(newStatus);

            if (wasCancelled && !willCancel) {
                return "سفارش لغوشده قابل بازگردانی به حالت فعال نیست.";
            }

            if (!wasCancelled && willCancel) {
                for (Models.OrderItemRecord item : order.items) {
                    Models.Product p = getProductById(item.productId);
                    if (p != null) {
                        ContentValues up = new ContentValues();
                        up.put("stock", p.stock + item.quantity);
                        db.update("products", up, "id = ?", new String[]{String.valueOf(p.id)});
                        insertMovementInternal(db, p.id, p.nameFa, item.quantity, "return_restore",
                                "بازگشت خودکار FEFO بابت لغو " + order.orderNumber, "پنل مدیریت");
                    }
                }
            }

            ContentValues cv = new ContentValues();
            cv.put("status", newStatus);
            if (trackingCode != null && !trackingCode.trim().isEmpty()) {
                cv.put("tracking_code", trackingCode.trim());
            }
            db.update("orders", cv, "id = ?", new String[]{String.valueOf(orderId)});

            db.setTransactionSuccessful();
            return null;
        } finally {
            db.endTransaction();
        }
    }

    public Models.DashboardSnapshot getDashboardSnapshot() {
        Models.DashboardSnapshot snap = new Models.DashboardSnapshot();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor c = db.rawQuery(
                "SELECT COALESCE(SUM(total_amount),0), COUNT(*) FROM orders WHERE status NOT IN ('cancelled','returned')", null)) {
            if (c.moveToFirst()) {
                snap.salesToday = c.getLong(0);
                snap.totalOrdersCount = c.getInt(1);
            }
        }
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM orders WHERE status IN ('pending_payment','paid','confirmed','preparing','shipped')", null)) {
            if (c.moveToFirst()) snap.activeOrdersCount = c.getInt(0);
        }
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM orders WHERE installments_count > 1 AND status NOT IN ('cancelled','returned')", null)) {
            if (c.moveToFirst()) snap.installmentOrdersCount = c.getInt(0);
        }
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM orders WHERE vip_gift_box = 1", null)) {
            if (c.moveToFirst()) snap.vipGiftOrdersCount = c.getInt(0);
        }
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM products WHERE stock <= low_stock_threshold", null)) {
            if (c.moveToFirst()) snap.lowStockCount = c.getInt(0);
        }
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM products", null)) {
            if (c.moveToFirst()) snap.totalProductsCount = c.getInt(0);
        }

        snap.sevenDayLabels = new String[]{"ش", "ی", "د", "س", "چ", "پ", "امروز"};
        snap.sevenDayRevenue = new long[]{
                24_500_000L, 31_200_000L, 28_800_000L, 39_400_000L, 36_900_000L, 48_200_000L,
                Math.max(6_600_000L, snap.salesToday + 45_000_000L)
        };
        long sum = 0;
        for (long v : snap.sevenDayRevenue) sum += v;
        snap.salesWeek = sum;

        return snap;
    }
}
