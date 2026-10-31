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

    public static final long FREE_SHIPPING_THRESHOLD = 2_000_000L;
    public static final long STANDARD_SHIPPING_COST  = 65_000L;

    private static final String DB_NAME = "asiye_royal_v5.db";
    private static final int DB_VERSION = 2;

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
                "cold_chain INTEGER NOT NULL DEFAULT 0," +
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
                "image_key TEXT NOT NULL DEFAULT 'gatieau_whitening'," +
                "wishlisted INTEGER NOT NULL DEFAULT 0," +
                "benefits TEXT," +
                "long_description TEXT," +
                "seo_title TEXT," +
                "seo_description TEXT," +
                "keywords TEXT," +
                "instagram_caption TEXT," +
                "image_path TEXT," +
                "ai_generated INTEGER NOT NULL DEFAULT 0" +
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
                "cold_chain INTEGER NOT NULL DEFAULT 0," +
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

        seedSignatureCatalog(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Non-destructive v6.0 migration: keep catalog, orders and settings intact.
            alterAdd(db, "products", "benefits", "TEXT");
            alterAdd(db, "products", "long_description", "TEXT");
            alterAdd(db, "products", "seo_title", "TEXT");
            alterAdd(db, "products", "seo_description", "TEXT");
            alterAdd(db, "products", "keywords", "TEXT");
            alterAdd(db, "products", "instagram_caption", "TEXT");
            alterAdd(db, "products", "image_path", "TEXT");
            alterAdd(db, "products", "ai_generated", "INTEGER NOT NULL DEFAULT 0");
            seedV6Settings(db);
            return;
        }
        db.execSQL("DROP TABLE IF EXISTS order_items");
        db.execSQL("DROP TABLE IF EXISTS orders");
        db.execSQL("DROP TABLE IF EXISTS stock_movements");
        db.execSQL("DROP TABLE IF EXISTS coupons");
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS settings");
        onCreate(db);
    }

    private void alterAdd(SQLiteDatabase db, String table, String column, String type) {
        try (Cursor c = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (c.moveToNext()) {
                if (column.equalsIgnoreCase(c.getString(c.getColumnIndexOrThrow("name")))) return;
            }
        } catch (Exception ignored) { }
        try {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        } catch (Exception ignored) { }
    }

    /** Payment-gateway, installment and AI-studio defaults (editable from the admin panel). */
    public static void seedV6Settings(SQLiteDatabase db) {
        String[][] defaults = {
                // ── Online payment ──
                {"pay_online_enabled", "1"},
                {"pay_gateway_zarinpal", "1"},
                {"pay_gateway_shaparak", "1"},
                {"pay_gateway_idpay", "0"},
                {"pay_gateway_nextpay", "0"},
                {"pay_cod_enabled", "1"},
                {"pay_card_to_card", "1"},
                {"pay_card_number", "6037-9975-0000-0000"},
                {"pay_card_holder", "سرکار خانم زنگنه"},
                {"pay_merchant_id", "ASIYE-STORE-ZP"},
                {"pay_callback_url", "https://asiyestore.ir/api/v1/payment/callback"},
                {"pay_sandbox", "1"},
                {"pay_min_amount", "50000"},
                {"pay_auto_capture", "1"},
                {"pay_currency", "IRR"},
                {"pay_description", "خرید از مجموعه ASIYE STORE"},
                // ── Installments / BNPL ──
                {"inst_enabled", "1"},
                {"inst_zero_months", "4"},
                {"inst_extended_months", "6,12"},
                {"inst_interest_percent", "18"},
                {"inst_min_amount", "1000000"},
                {"inst_max_amount", "20000000"},
                {"inst_down_percent", "0"},
                {"inst_provider", "snapp_pay"},
                {"inst_verify_kyc", "1"},
                {"inst_auto_approve", "0"},
                {"inst_fee_payer", "merchant"},
                // ── AI studio ──
                {"ai_auto_caption", "1"},
                {"ai_tone", "لوکس و تخصصی"},
                {"ai_seo_city", "تهران"},
                {"ai_instagram_handle", "asiyeh.store"},
                {"ai_endpoint", ""}
        };
        for (String[] row : defaults) {
            ContentValues cv = new ContentValues();
            cv.put("key", row[0]);
            cv.put("value", row[1]);
            db.insertWithOnConflict("settings", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private void seedSignatureCatalog(SQLiteDatabase db) {
        long p1 = insertSeedProduct(db,
                "AS-GAT-150", "AS-GAT-150", "LOT-GTW-2026", "۱۴۰۸/۰۶", false, 60,
                "کرم روشن‌کننده و ضدلک تخصصی گاتیو",
                "GATIEAU • پرفروش‌ترین محصول پیج",
                "مراقبت تخصصی پوست",
                "فرمولاسیون پیشرفته حاوی سرامید، لیپوویت، کمپلکس ضدلک Hentowhite AF و ویتامین‌های A، C و E جهت رفع تیرگی نواحی مختلف، کاهش لک‌های پوستی و یکدست‌سازی درخشان تناژ پوست.",
                "سرامید، لیپوویت، کمپلکس ضدلک Hentowhite AF، ویتامین‌های A و C و E",
                "روزانه یک تا دو مرتبه روی پوست تمیز و خشک موضع مورد نظر به آرامی ماساژ داده شود.",
                "تحت لیسانس اروپا", "۱۵۰ میلی‌لیتر",
                2_380_000L, 1_550_000L, 24, 5, "gatieau_whitening");

        long p2 = insertSeedProduct(db,
                "AS-GAT-050", "AS-GAT-050", "LOT-GTA-2026", "۱۴۰۸/۰۴", false, 50,
                "کرم ضدچروک و لیفتینگ تخصصی گاتیو",
                "GATIEAU • کمپلکس پپتیدی جوان‌ساز",
                "مراقبت تخصصی پوست",
                "کاهش‌دهنده هدفمند خطوط پنجه کلاغی، خط اخم و چروک‌های صورت با تحریک کلاژن‌سازی طبیعی و بازگرداندن قوام، استحکام و لطافت ابریشمی به پوست صورت و گردن.",
                "کمپلکس پپتید جوان‌ساز، هیالورونیک اسید، ویتامین E و نیاسینامید",
                "هر شب و صبح مقدار مناسبی از کرم را با حرکات رو به بالا روی صورت و گردن ماساژ دهید.",
                "تحت لیسانس اروپا", "۵۰ میلی‌لیتر",
                1_610_000L, 1_390_000L, 18, 5, "gatieau_antiwrinkle");

        long p3 = insertSeedProduct(db,
                "AS-LC-104", "AS-LC-104", "LOT-LCP-104", "۱۴۰۸/۰۹", false, 90,
                "پنکک ضدآب و مخملی لاکچری کوین SPF 25",
                "LUXURY COIN • تحت لیسانس فرانسه",
                "میکاپ لوکس",
                "پودر میکرونیزه فوق‌سبک با پوشانندگی یکدست و مات مخملی، کاملاً واترپروف و قابل استفاده با پد مرطوب بدون تغییر رنگ یا ماسیدگی، غنی‌شده با کلاژن و آلوئه‌ورا.",
                "پودر معدنی میکرونیزه، کلاژن هیدرولیزشده، عصاره آلوئه‌ورا و ویتامین E",
                "با پد خشک برای پوشش مخملی روزانه یا با پد مرطوب جهت کاور حرفه‌ای مجلسی استفاده شود.",
                "تحت لیسانس فرانسه", "شماره ۱۰۴ بژ طبیعی",
                1_210_000L, 920_000L, 30, 5, "luxury_coin");

        long p4 = insertSeedProduct(db,
                "AS-HF-300", "AS-HF-300", "LOT-HFT-300", "۱۴۰۸/۰۵", false, 60,
                "تونیک گیاهی تقویت و رویش مجدد مو هیرفرند",
                "HAIR FRIEND • عصاره خالص گیاهی",
                "تقویت و درمان مو",
                "محلول تغذیه‌کننده مستقیم فولیکول و پیاز مو سرشار از عصاره چای سبز، شلتوک برنج، برگ زیتون و همیشه‌بهار؛ موثر در توقف ریزش، ضخیم‌سازی تارهای نازک و تحریک رویش مجدد مو.",
                "عصاره چای سبز، شلتوک برنج، برگ زیتون، گل همیشه‌بهار و رزماری",
                "روزانه چند پاف روی پوست سر تمیز اسپری شده و ۲ دقیقه با نوک انگشتان ماساژ داده شود (نیاز به آبکشی ندارد).",
                "فرمولاسیون تخصصی گیاهی", "۳۰۰ میلی‌لیتر",
                1_950_000L, 1_560_000L, 22, 5, "hair_friend");

        long p5 = insertSeedProduct(db,
                "AS-ORD-240", "AS-ORD-240", "LOT-ORD-771", "۱۴۰۸/۱۱", true, 90,
                "تونر لایه‌بردار گلیکولیک اسید ۷٪ اوردینری",
                "THE ORDINARY • اورجینال کانادا",
                "مراقبت تخصصی پوست",
                "تونر شفاف‌کننده و لایه‌بردار ملایم سطح پوست با اسیدیته استاندارد ۳.۶ حاوی ریشه جینسینگ و آلوئه‌ورا؛ پاکسازی عمقی منافذ باز، رفع کدری و جای جوش بدون الکل و پارابن.",
                "گلیکولیک اسید ۷ درصد، عصاره ریشه جینسینگ، فلفل تاسمانی و آلوئه‌ورا",
                "شب‌ها با پد پنبه‌ای روی پوست تمیز صورت و گردن (به‌جز دور چشم) کشیده شود و روزها ضدآفتاب استفاده گردد.",
                "کانادا (اورجینال)", "۲۴۰ میلی‌لیتر",
                3_450_000L, 2_890_000L, 4, 5, "ordinary_toner");

        insertSeedProduct(db,
                "AS-MRG-500", "AS-MRG-500", "LOT-MVB-502", "۱۴۰۸/۰۷", false, 50,
                "پک تخصصی شامپو درخت چای ویتابلا و مورینگا امو",
                "VITABELLA & MORINGA EMO • فاقد سولفات",
                "تقویت و درمان مو",
                "ترکیب مکمل شامپو پاکسازی‌کننده درخت چای ویتابلا برای تنظیم چربی و رفع شوره کف سر به همراه شامپو مغذی روغن مورینگا امو جهت احیا، نرمی و درخشش ساقه مو.",
                "روغن خالص برگ درخت چای، روغن هسته مورینگا، آمینواسیدها و ویتامین E",
                "روی موی مرطوب به مدت ۲ دقیقه ماساژ داده و با آب ولرم آبکشی نمایید.",
                "فرمولاسیون فاقد سولفات", "پک دوتایی کامل",
                840_000L, 680_000L, 26, 5, "moringa_teatree");

        // VIP Coupons
        insertSeedCoupon(db, "ASIYE20", 20, 0, 500_000L, 600_000L);
        insertSeedCoupon(db, "VIPGOLD", 0, 250_000L, 1_500_000L, 250_000L);

        // Order 1 (Shipped with postal tracking)
        ContentValues o1 = new ContentValues();
        o1.put("order_number", "AS-1405-9102");
        o1.put("customer_name", "سارا محمدی");
        o1.put("customer_mobile", "09123456789");
        o1.put("address_summary", "تهران، سعادت‌آباد، بلوار دریا، پلاک ۱۸");
        o1.put("shipping_method", "پست پیشتاز VIP");
        o1.put("payment_method", "snapp_pay");
        o1.put("installments_count", 4);
        o1.put("subtotal", 3_110_000L);
        o1.put("discount", 0L);
        o1.put("wallet_used", 0L);
        o1.put("shipping_cost", 0L);
        o1.put("total_amount", 3_110_000L);
        o1.put("cashback_earned", 0L);
        o1.put("vip_gift_box", 1);
        o1.put("cold_chain", 0);
        o1.put("scented_tissue", "بسته‌بندی اختصاصی ASIYE");
        o1.put("gift_message", "تقدیم با مهر و آرزوی درخشش همیشگی");
        o1.put("certificate_code", "AS-CERT-9102");
        o1.put("status", "shipped");
        o1.put("tracking_code", "29481000841102");
        o1.put("created_at_label", "دیروز — ۱۸:۴۰");
        long ord1Id = db.insert("orders", null, o1);
        insertSeedOrderItem(db, ord1Id, p1, "کرم روشن‌کننده و ضدلک تخصصی گاتیو", "۱۵۰ میلی‌لیتر", 1, 1_550_000L);
        insertSeedOrderItem(db, ord1Id, p4, "تونیک گیاهی تقویت و رویش مجدد مو هیرفرند", "۳۰۰ میلی‌لیتر", 1, 1_560_000L);

        // Order 2 (Preparing VIP Gift Box today)
        ContentValues o2 = new ContentValues();
        o2.put("order_number", "AS-1405-9148");
        o2.put("customer_name", "نازنین رضایی");
        o2.put("customer_mobile", "09354118820");
        o2.put("address_summary", "شیراز، معالی‌آباد، خیابان پزشکان، مجتمع صدف");
        o2.put("shipping_method", "پست پیشتاز VIP");
        o2.put("payment_method", "zarinpal");
        o2.put("installments_count", 1);
        o2.put("subtotal", 3_810_000L);
        o2.put("discount", 250_000L);
        o2.put("wallet_used", 0L);
        o2.put("shipping_cost", 0L);
        o2.put("total_amount", 3_560_000L);
        o2.put("cashback_earned", 0L);
        o2.put("vip_gift_box", 1);
        o2.put("cold_chain", 1);
        o2.put("scented_tissue", "بسته‌بندی اختصاصی ASIYE");
        o2.put("gift_message", "");
        o2.put("certificate_code", "AS-CERT-9148");
        o2.put("status", "preparing");
        o2.put("tracking_code", "");
        o2.put("created_at_label", "امروز — ۱۱:۲۰");
        long ord2Id = db.insert("orders", null, o2);
        insertSeedOrderItem(db, ord2Id, p5, "تونر لایه‌بردار گلیکولیک اسید ۷٪ اوردینری", "۲۴۰ میلی‌لیتر", 1, 2_890_000L);
        insertSeedOrderItem(db, ord2Id, p3, "پنکک ضدآب و مخملی لاکچری کوین SPF 25", "شماره ۱۰۲ بژ طبیعی", 1, 920_000L);
        ignored(p2);

        insertMovementInternal(db, p1, "کرم روشن‌کننده گاتیو", 24, "in", "شارژ پارت جدید اورجینال", "سرکار خانم زنگنه");
        insertMovementInternal(db, p5, "تونر گلیکولیک اوردینری", -1, "out", "فروش سفارش AS-1405-9148", "سیستم فروش");

        putSettingInternal(db, "api_base_url", "https://asiyestore.ir/api/v1");
        putSettingInternal(db, "customer_name", "");
        putSettingInternal(db, "customer_mobile", "");
        putSettingInternal(db, "customer_address", "");
        seedV6Settings(db);
    }

    private void ignored(long v) {}

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

    public Models.Product verifyAuthenticitySerial(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) return null;
        String clean = rawCode.trim().toUpperCase(Locale.US);
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT * FROM products WHERE UPPER(ttac_serial) = ? OR UPPER(sku) = ? OR UPPER(batch_code) = ?",
                new String[]{clean, clean, clean})) {
            if (c.moveToFirst()) return readProduct(c);
        }
        return null;
    }

    public List<Models.Product> getProducts(boolean onlyPublished, String categoryFilter, String searchQuery, String sortMode) {
        List<Models.Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (onlyPublished) sql.append(" AND published = 1");
        if (categoryFilter != null && !categoryFilter.isEmpty() && !"همه".equals(categoryFilter)) {
            if (categoryFilter.startsWith("منتخب")) {
                sql.append(" AND wishlisted = 1");
            } else {
                sql.append(" AND category = ?");
                args.add(categoryFilter);
            }
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String q = "%" + searchQuery.trim() + "%";
            sql.append(" AND (name_fa LIKE ? OR name_en LIKE ? OR sku LIKE ? OR description LIKE ?)");
            args.add(q); args.add(q); args.add(q); args.add(q);
        }
        if ("price_asc".equals(sortMode)) {
            sql.append(" ORDER BY sale_price ASC");
        } else if ("price_desc".equals(sortMode)) {
            sql.append(" ORDER BY sale_price DESC");
        } else {
            sql.append(" ORDER BY id ASC");
        }

        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(sql.toString(), args.toArray(new String[0]))) {
            while (c.moveToNext()) list.add(readProduct(c));
        }
        return list;
    }

    public int getWishlistCount() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM products WHERE wishlisted = 1 AND published = 1", null)) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    public void toggleWishlist(long productId, boolean wishlisted) {
        ContentValues cv = new ContentValues();
        cv.put("wishlisted", wishlisted ? 1 : 0);
        getWritableDatabase().update("products", cv, "id = ?", new String[]{String.valueOf(productId)});
    }

    private String safeString(Cursor c, String column) {
        int idx = c.getColumnIndex(column);
        return idx >= 0 ? c.getString(idx) : null;
    }

    private int safeInt(Cursor c, String column) {
        int idx = c.getColumnIndex(column);
        return idx >= 0 ? c.getInt(idx) : 0;
    }

    /**
     * Saves a fully AI-composed product (copy, SEO, Instagram caption and studio artwork path).
     * Returns null on success or a Persian error message.
     */
    public String saveAiProduct(Long existingId, String sku, String nameFa, String nameEn,
                                String category, String description, String longDescription,
                                String benefits, String ingredients, String usage, String country,
                                String volume, long origPrice, long salePrice, int stock,
                                boolean published, String imageKey, String imagePath,
                                String seoTitle, String seoDescription, String keywords,
                                String instagramCaption) {
        if (nameFa == null || nameFa.trim().isEmpty()) return "نام محصول را وارد کنید.";
        if (salePrice <= 0) return "قیمت فروش معتبر نیست.";
        if (origPrice < salePrice) origPrice = salePrice;
        int disc = origPrice > 0 ? (int) Math.round(((origPrice - salePrice) * 100.0) / origPrice) : 0;
        String normSku = (sku == null || sku.trim().isEmpty())
                ? ("AS-AI-" + (100 + new Random().nextInt(8999)))
                : sku.trim().toUpperCase(Locale.US);

        ContentValues cv = new ContentValues();
        cv.put("sku", normSku);
        cv.put("name_fa", nameFa.trim());
        cv.put("name_en", nameEn);
        cv.put("category", category);
        cv.put("description", description);
        cv.put("long_description", longDescription);
        cv.put("benefits", benefits);
        cv.put("ingredients", ingredients);
        cv.put("usage_method", usage);
        cv.put("country", country);
        cv.put("volume", volume);
        cv.put("original_price", origPrice);
        cv.put("sale_price", salePrice);
        cv.put("discount_percent", Math.max(0, disc));
        cv.put("stock", Math.max(0, stock));
        cv.put("published", published ? 1 : 0);
        cv.put("image_key", imageKey);
        cv.put("image_path", imagePath);
        cv.put("seo_title", seoTitle);
        cv.put("seo_description", seoDescription);
        cv.put("keywords", keywords);
        cv.put("instagram_caption", instagramCaption);
        cv.put("ai_generated", 1);

        SQLiteDatabase db = getWritableDatabase();
        try {
            if (existingId == null) {
                cv.put("ttac_serial", normSku);
                cv.put("batch_code", "LOT-AI-" + (100 + new Random().nextInt(899)));
                cv.put("expiry_label", "۱۴۰۹/۰۳");
                cv.put("low_stock_threshold", 5);
                long id = db.insertOrThrow("products", null, cv);
                insertMovementInternal(db, id, nameFa.trim(), Math.max(0, stock), "in",
                        "افزودن هوشمند کالا توسط استودیو هوش مصنوعی", "مدیریت");
            } else {
                db.update("products", cv, "id = ?", new String[]{String.valueOf(existingId)});
            }
            return null;
        } catch (Exception e) {
            return "کد کالا تکراری است؛ کد دیگری انتخاب کنید.";
        }
    }

    public List<Models.PaymentGatewayRecord> getPaymentGateways() {
        List<Models.PaymentGatewayRecord> list = new ArrayList<>();
        list.add(gateway("zarinpal", "درگاه امن زرین‌پال", "کارت‌های شتاب • تسویه لحظه‌ای", "pay_gateway_zarinpal", 0));
        list.add(gateway("shaparak", "درگاه مستقیم شاپرک", "اتصال بانکی رسمی • بدون واسطه", "pay_gateway_shaparak", 0));
        list.add(gateway("idpay", "درگاه آیدی‌پی", "پرداخت سریع با کارت‌های عضو شتاب", "pay_gateway_idpay", 1));
        list.add(gateway("nextpay", "درگاه نکست‌پی", "مناسب پرداخت‌های سازمانی", "pay_gateway_nextpay", 1));
        list.add(gateway("cod", "پرداخت در محل (پس‌کرایه)", "پرداخت نقدی هنگام تحویل مرسوله", "pay_cod_enabled", 0));
        return list;
    }

    private Models.PaymentGatewayRecord gateway(String code, String title, String sub, String key, int fee) {
        Models.PaymentGatewayRecord g = new Models.PaymentGatewayRecord();
        g.code = code;
        g.titleFa = title;
        g.subtitleFa = sub;
        g.enabled = "1".equals(getSetting(key, "0"));
        g.feePercent = fee;
        return g;
    }

    public long getSettingLong(String key, long fallback) {
        try {
            return Long.parseLong(PersianFormatter.toAsciiDigits(getSetting(key, String.valueOf(fallback))).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    public int getSettingInt(String key, int fallback) {
        return (int) getSettingLong(key, fallback);
    }

    public boolean getSettingBool(String key, boolean fallback) {
        String v = getSetting(key, fallback ? "1" : "0");
        return "1".equals(v) || "true".equalsIgnoreCase(v);
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
        p.benefits = safeString(c, "benefits");
        p.longDescription = safeString(c, "long_description");
        p.seoTitle = safeString(c, "seo_title");
        p.seoDescription = safeString(c, "seo_description");
        p.keywords = safeString(c, "keywords");
        p.instagramCaption = safeString(c, "instagram_caption");
        p.imagePath = safeString(c, "image_path");
        p.aiGenerated = safeInt(c, "ai_generated") == 1;
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
        if (nameFa == null || nameFa.trim().isEmpty()) return "نام فارسی محصول الزامی است.";
        if (salePrice <= 0) return "قیمت فروش باید بزرگ‌تر از صفر باشد.";
        if (origPrice < salePrice) origPrice = salePrice;
        int disc = origPrice > 0 ? (int) Math.round(((origPrice - salePrice) * 100.0) / origPrice) : 0;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        String normSku = (sku == null || sku.trim().isEmpty())
                ? ("AS-NEW-" + (100 + new Random().nextInt(899)))
                : sku.trim().toUpperCase(Locale.US);
        cv.put("sku", normSku);
        cv.put("name_fa", nameFa.trim());
        cv.put("name_en", (nameEn == null || nameEn.trim().isEmpty()) ? "ASIYE STORE • کالای اورجینال" : nameEn.trim());
        cv.put("category", category);
        cv.put("description", (description == null || description.trim().isEmpty())
                ? "محصول تخصصی اورجینال با ضمانت اصالت مجموعه ASIYE STORE."
                : description.trim());
        cv.put("country", (country == null || country.trim().isEmpty()) ? "اورجینال" : country.trim());
        cv.put("volume", (volume == null || volume.trim().isEmpty()) ? "نسخه اصلی" : volume.trim());
        cv.put("original_price", origPrice);
        cv.put("sale_price", salePrice);
        cv.put("discount_percent", Math.max(0, disc));
        cv.put("published", published ? 1 : 0);
        cv.put("image_key", (imageKey == null || imageKey.isEmpty()) ? "gatieau_whitening" : imageKey);

        try {
            if (existingId == null) {
                cv.put("ttac_serial", normSku);
                cv.put("batch_code", "LOT-" + (100 + new Random().nextInt(899)));
                cv.put("expiry_label", "۱۴۰۸/۰۹");
                cv.put("ingredients", "فرمولاسیون تخصصی استاندارد درماتولوژی");
                cv.put("usage_method", "طبق دستور مصرف درج‌شده روی بسته‌بندی محصول استفاده شود.");
                cv.put("stock", Math.max(0, initialStock));
                cv.put("low_stock_threshold", 5);
                long newId = db.insertOrThrow("products", null, cv);
                insertMovementInternal(db, newId, nameFa.trim(), Math.max(0, initialStock), "in", "ثبت کالای جدید در ویترین", "مدیریت");
            } else {
                cv.put("stock", Math.max(0, initialStock));
                db.update("products", cv, "id = ?", new String[]{String.valueOf(existingId)});
            }
            return null;
        } catch (Exception e) {
            return "کد شناسه (SKU) تکراری است.";
        }
    }

    public void setProductPublished(long productId, boolean published) {
        ContentValues cv = new ContentValues();
        cv.put("published", published ? 1 : 0);
        getWritableDatabase().update("products", cv, "id = ?", new String[]{String.valueOf(productId)});
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
                cv.put("ttac_serial", sku);
                cv.put("batch_code", "LOT-SYNC");
                cv.put("expiry_label", "۱۴۰۸/۰۶");
                cv.put("category", "مراقبت تخصصی پوست");
                cv.put("description", "همگام‌سازی‌شده از سرور ASIYE STORE");
                cv.put("published", 1);
                cv.put("image_key", "gatieau_whitening");
                db.insert("products", null, cv);
            }
        }
    }

    public String adjustInventory(long productId, String type, int quantity, String reason) {
        Models.Product p = getProductById(productId);
        if (p == null) return "محصول یافت نشد.";
        int newStock = "in".equals(type) ? p.stock + quantity : Math.max(0, p.stock - quantity);
        int delta = newStock - p.stock;
        ContentValues cv = new ContentValues();
        cv.put("stock", newStock);
        getWritableDatabase().update("products", cv, "id = ?", new String[]{String.valueOf(productId)});
        if (delta != 0) {
            insertMovementInternal(getWritableDatabase(), productId, p.nameFa, delta, type, reason, "مدیریت");
        }
        return null;
    }

    public String setExactInventory(long productId, int exactStock, String reason) {
        Models.Product p = getProductById(productId);
        if (p == null) return "محصول یافت نشد.";
        int safe = Math.max(0, exactStock);
        int delta = safe - p.stock;
        ContentValues cv = new ContentValues();
        cv.put("stock", safe);
        getWritableDatabase().update("products", cv, "id = ?", new String[]{String.valueOf(productId)});
        insertMovementInternal(getWritableDatabase(), productId, p.nameFa, delta, delta >= 0 ? "in" : "out",
                (reason == null || reason.trim().isEmpty()) ? "انبارگردانی دقیق مدیریت" : reason.trim(), "مدیریت");
        return null;
    }

    public List<Models.StockMovementRecord> getRecentMovements(int limit) {
        List<Models.StockMovementRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM stock_movements ORDER BY id DESC LIMIT ?", new String[]{String.valueOf(limit)})) {
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

    public List<Models.CouponRecord> getCoupons() {
        List<Models.CouponRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM coupons ORDER BY id DESC", null)) {
            while (c.moveToNext()) {
                Models.CouponRecord cp = new Models.CouponRecord();
                cp.id = c.getLong(c.getColumnIndexOrThrow("id"));
                cp.code = c.getString(c.getColumnIndexOrThrow("code"));
                cp.discountPercent = c.getInt(c.getColumnIndexOrThrow("discount_percent"));
                cp.discountAmount = c.getLong(c.getColumnIndexOrThrow("discount_amount"));
                cp.minPurchase = c.getLong(c.getColumnIndexOrThrow("min_purchase"));
                cp.maxDiscount = c.getLong(c.getColumnIndexOrThrow("max_discount"));
                cp.active = c.getInt(c.getColumnIndexOrThrow("active")) == 1;
                list.add(cp);
            }
        }
        return list;
    }

    public String createOrUpdateCoupon(String rawCode, int percent, long fixedAmount, long minPurchase, long maxDiscount) {
        if (rawCode == null || rawCode.trim().isEmpty()) return "کد تخفیف را وارد کنید.";
        String code = rawCode.trim().toUpperCase(Locale.US);
        ContentValues cv = new ContentValues();
        cv.put("code", code);
        cv.put("discount_percent", Math.max(0, Math.min(90, percent)));
        cv.put("discount_amount", Math.max(0L, fixedAmount));
        cv.put("min_purchase", Math.max(0L, minPurchase));
        cv.put("max_discount", Math.max(0L, maxDiscount));
        cv.put("active", 1);
        getWritableDatabase().insertWithOnConflict("coupons", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return null;
    }

    public void toggleCouponActive(long couponId, boolean active) {
        ContentValues cv = new ContentValues();
        cv.put("active", active ? 1 : 0);
        getWritableDatabase().update("coupons", cv, "id = ?", new String[]{String.valueOf(couponId)});
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

    public Models.OrderRecord executeCheckout(List<Models.CartItem> cart,
                                              String customerName,
                                              String customerMobile,
                                              String addressSummary,
                                              String paymentMethod,
                                              int installmentsCount,
                                              String couponCode,
                                              boolean vipGiftBox,
                                              String giftMessage) throws IllegalStateException {
        if (cart == null || cart.isEmpty()) throw new IllegalStateException("سبد خرید خالی است.");
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long subtotal = 0L;
            for (Models.CartItem item : cart) {
                Models.Product fresh = getProductById(item.product.id);
                if (fresh == null || fresh.stock < item.quantity) {
                    throw new IllegalStateException("موجودی کافی برای «" + item.product.nameFa + "» در انبار نیست.");
                }
                item.product = fresh;
                subtotal += fresh.salePrice * item.quantity;
            }
            long discount = 0L;
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                long eval = evaluateCouponDiscount(couponCode, subtotal);
                if (eval > 0) discount = eval;
            }
            long afterDisc = Math.max(0L, subtotal - discount);
            long shipping = afterDisc >= FREE_SHIPPING_THRESHOLD ? 0L : STANDARD_SHIPPING_COST;
            long total = afterDisc + shipping;
            String orderNumber = "AS-1405-" + (1000 + new Random().nextInt(8999));

            ContentValues ord = new ContentValues();
            ord.put("order_number", orderNumber);
            ord.put("customer_name", customerName);
            ord.put("customer_mobile", customerMobile);
            ord.put("address_summary", addressSummary);
            ord.put("shipping_method", "پست پیشتاز VIP");
            ord.put("payment_method", paymentMethod);
            ord.put("installments_count", installmentsCount);
            ord.put("subtotal", subtotal);
            ord.put("discount", discount);
            ord.put("wallet_used", 0L);
            ord.put("shipping_cost", shipping);
            ord.put("total_amount", total);
            ord.put("cashback_earned", 0L);
            ord.put("vip_gift_box", vipGiftBox ? 1 : 0);
            ord.put("cold_chain", 0);
            ord.put("scented_tissue", "بسته‌بندی اختصاصی ASIYE");
            ord.put("gift_message", giftMessage != null ? giftMessage.trim() : "");
            ord.put("certificate_code", "AS-CERT-" + (1000 + new Random().nextInt(8999)));
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
                insertMovementInternal(db, item.product.id, item.product.nameFa, -item.quantity, "out",
                        "سفارش " + orderNumber, customerName);
            }

            db.setTransactionSuccessful();
            return getOrderById(orderId);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * v6.0 checkout used by the professional installment / online-payment flow.
     * Keeps the same transactional guarantees while accepting the BNPL fee,
     * the human-readable payment label and the initial order status.
     */
    public Models.OrderRecord executeCheckoutAdvanced(List<Models.CartItem> cart,
                                                      String customerName,
                                                      String customerMobile,
                                                      String addressSummary,
                                                      String paymentLabel,
                                                      int installmentsCount,
                                                      String couponCode,
                                                      boolean vipGiftBox,
                                                      String giftMessage,
                                                      long extraFinancingFee,
                                                      String initialStatus) throws IllegalStateException {
        if (cart == null || cart.isEmpty()) throw new IllegalStateException("سبد خرید خالی است.");
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long subtotal = 0L;
            for (Models.CartItem item : cart) {
                Models.Product fresh = getProductById(item.product.id);
                if (fresh == null || fresh.stock < item.quantity) {
                    throw new IllegalStateException("موجودی کافی برای «" + item.product.nameFa + "» در انبار نیست.");
                }
                item.product = fresh;
                subtotal += fresh.salePrice * item.quantity;
            }
            long discount = 0L;
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                long eval = evaluateCouponDiscount(couponCode, subtotal);
                if (eval > 0) discount = eval;
            }
            long afterDisc = Math.max(0L, subtotal - discount);
            long shipping = afterDisc >= FREE_SHIPPING_THRESHOLD ? 0L : STANDARD_SHIPPING_COST;
            long fee = Math.max(0L, extraFinancingFee);
            long total = afterDisc + shipping + fee;
            String orderNumber = "AS-1405-" + (1000 + new Random().nextInt(8999));

            ContentValues ord = new ContentValues();
            ord.put("order_number", orderNumber);
            ord.put("customer_name", customerName);
            ord.put("customer_mobile", customerMobile);
            ord.put("address_summary", addressSummary);
            ord.put("shipping_method", "پست پیشتاز VIP");
            ord.put("payment_method", paymentLabel);
            ord.put("installments_count", Math.max(1, installmentsCount));
            ord.put("subtotal", subtotal);
            ord.put("discount", discount);
            ord.put("wallet_used", 0L);
            ord.put("shipping_cost", shipping);
            ord.put("total_amount", total);
            ord.put("cashback_earned", Math.round(total * 0.01));
            ord.put("vip_gift_box", vipGiftBox ? 1 : 0);
            ord.put("cold_chain", 0);
            ord.put("scented_tissue", "بسته‌بندی اختصاصی ASIYE");
            ord.put("gift_message", giftMessage != null ? giftMessage.trim() : "");
            ord.put("certificate_code", "AS-CERT-" + (1000 + new Random().nextInt(8999)));
            ord.put("status", initialStatus == null ? "paid" : initialStatus);
            ord.put("tracking_code", "");
            ord.put("created_at_label", nowTimeLabel());

            long orderId = db.insertOrThrow("orders", null, ord);
            for (Models.CartItem item : cart) {
                insertSeedOrderItem(db, orderId, item.product.id, item.product.nameFa,
                        item.selectedVariant, item.quantity, item.product.salePrice);
                ContentValues up = new ContentValues();
                up.put("stock", item.product.stock - item.quantity);
                db.update("products", up, "id = ?", new String[]{String.valueOf(item.product.id)});
                insertMovementInternal(db, item.product.id, item.product.nameFa, -item.quantity, "out",
                        "سفارش " + orderNumber, customerName);
            }

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

    public Models.OrderRecord getOrderByNumber(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) return null;
        String clean = PersianFormatter.toAsciiDigits(rawCode).trim().toUpperCase(Locale.US);
        String normalized = clean.startsWith("AS-") ? clean : "AS-" + clean;
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM orders WHERE UPPER(order_number) = ? OR UPPER(order_number) = ?",
                new String[]{clean, normalized})) {
            if (c.moveToFirst()) {
                Models.OrderRecord o = readOrder(c);
                o.items = getOrderItems(db, o.id);
                return o;
            }
        }
        return null;
    }

    public List<Models.TopProductRecord> getTopProducts(int limit) {
        List<Models.TopProductRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT product_name, SUM(quantity) AS units, SUM(quantity * unit_price) AS revenue " +
                        "FROM order_items GROUP BY product_name ORDER BY units DESC LIMIT ?",
                new String[]{String.valueOf(limit)})) {
            while (c.moveToNext()) {
                Models.TopProductRecord t = new Models.TopProductRecord();
                t.productName = c.getString(0);
                t.unitsSold = c.getInt(1);
                t.revenue = c.getLong(2);
                list.add(t);
            }
        }
        return list;
    }

    public String deleteProduct(long productId) {
        Models.Product p = getProductById(productId);
        if (p == null) return "محصول یافت نشد.";
        int usedInOrders = 0;
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM order_items WHERE product_id = ?",
                new String[]{String.valueOf(productId)})) {
            if (c.moveToFirst()) usedInOrders = c.getInt(0);
        }
        if (usedInOrders > 0) {
            setProductPublished(productId, false);
            return "این کالا در سوابق سفارش‌ها موجود است؛ به آرشیو امن منتقل شد.";
        }
        getWritableDatabase().delete("products", "id = ?", new String[]{String.valueOf(productId)});
        return "کالا از ویترین حذف شد.";
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
        o.shippingCost = c.getLong(c.getColumnIndexOrThrow("shipping_cost"));
        o.totalAmount = c.getLong(c.getColumnIndexOrThrow("total_amount"));
        o.vipGiftBox = c.getInt(c.getColumnIndexOrThrow("vip_gift_box")) == 1;
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
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        if (trackingCode != null) cv.put("tracking_code", trackingCode.trim());
        getWritableDatabase().update("orders", cv, "id = ?", new String[]{String.valueOf(orderId)});
        return null;
    }

    public Models.DashboardSnapshot getDashboardSnapshot() {
        Models.DashboardSnapshot snap = new Models.DashboardSnapshot();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COALESCE(SUM(total_amount),0), COUNT(*) FROM orders", null)) {
            if (c.moveToFirst()) {
                snap.totalRevenue = c.getLong(0);
                snap.totalOrdersCount = c.getInt(1);
            }
        }
        snap.avgOrderValue = snap.totalOrdersCount > 0 ? snap.totalRevenue / snap.totalOrdersCount : 0L;
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM orders WHERE status NOT IN ('delivered','cancelled','returned')", null)) {
            if (c.moveToFirst()) snap.activeOrdersCount = c.getInt(0);
        }
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM orders WHERE installments_count > 1", null)) {
            if (c.moveToFirst()) snap.installmentOrdersCount = c.getInt(0);
        }
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM orders WHERE vip_gift_box = 1", null)) {
            if (c.moveToFirst()) snap.vipGiftOrdersCount = c.getInt(0);
        }
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM orders WHERE created_at_label LIKE 'امروز%'", null)) {
            if (c.moveToFirst()) snap.todayOrdersCount = c.getInt(0);
        }
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM products WHERE published = 1 AND stock <= low_stock_threshold", null)) {
            if (c.moveToFirst()) snap.lowStockCount = c.getInt(0);
        }
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM products WHERE published = 1", null)) {
            if (c.moveToFirst()) snap.totalProductsCount = c.getInt(0);
        }
        try (Cursor c = db.rawQuery("SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE created_at_label LIKE 'امروز%'", null)) {
            if (c.moveToFirst()) snap.salesToday = c.getLong(0);
        }
        snap.sevenDayLabels = new String[]{"ش", "ی", "د", "س", "چ", "پ", "امروز"};
        snap.sevenDayRevenue = new long[]{15_200_000L, 19_400_000L, 18_100_000L, 24_600_000L, 23_200_000L, 29_500_000L,
                Math.max(35_400_000L, snap.totalRevenue)};
        long sum = 0;
        for (long v : snap.sevenDayRevenue) sum += v;
        snap.salesWeek = sum;
        snap.salesYesterday = snap.sevenDayRevenue[5];
        return snap;
    }
}
