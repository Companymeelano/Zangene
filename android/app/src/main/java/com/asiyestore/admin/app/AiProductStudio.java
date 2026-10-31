package com.asiyestore.admin.app;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * ASIYE STORE — On-device AI Product Studio (v6.0).
 *
 * The merchant supplies only three inputs — product name, brand and one photo —
 * and the engine:
 *   1. normalises the Persian/Arabic glyphs and strips filler words,
 *   2. scores the input against the maison's curated knowledge base (brand + form
 *      factor + volume + concern keywords) and proposes the most probable product,
 *   3. asks for a one-tap confirmation,
 *   4. composes the whole listing: persuasive Persian copy, key benefits,
 *      ingredients, ritual of use, SEO title/description, keyword cloud,
 *      Instagram caption with hashtags, and a price bracket.
 *
 * Everything runs offline so the boutique owner never waits for a network round-trip.
 */
public final class AiProductStudio {

    private AiProductStudio() { }

    // ── Knowledge base ────────────────────────────────────────────────────
    private static final class Kb {
        String brandTokens;
        String nameTokens;
        String form;
        String title;
        String nameEn;
        String category;
        String ingredients;
        String usage;
        String volume;
        String country;
        String imageKey;
        long price;
        long oldPrice;
        String[] benefits;
        String hook;
        String audience;
        String concern;
    }

    private static final List<Kb> KB = new ArrayList<>();

    static {
        KB.add(kb("گاتیو|gatieau|gatio",
                "ضدلک|روشن‌کننده|روشن کننده|سفید|whitening|لک|تیرگی|پوست|cream|کرم|150|۱۵۰",
                "کرم", "کرم روشن‌کننده و ضدلک تخصصی گاتیو (۱۵۰ میل)",
                "GATIEAU Skin Whitening Cream", "مراقبت تخصصی پوست",
                "سرامید، لیپوویت، کمپلکس ضدلک Hentowhite AF و ویتامین‌های A، C و E",
                "روزانه یک تا دو مرتبه روی پوست تمیز و خشک موضع مورد نظر به آرامی ماساژ داده شود؛ همراه با ضدآفتاب استفاده گردد.",
                "۱۵۰ میلی‌لیتر", "تحت لیسانس اروپا", "gatieau_whitening",
                1_550_000L, 2_380_000L,
                new String[]{"کاهش محسوس لک‌های سطحی و تیرگی نواحی مختلف پوست",
                        "یکدست‌سازی تناژ پوست و ایجاد درخشش طبیعی",
                        "تقویت سد دفاعی پوست با سرامید و لیپوویت",
                        "بافت سبک و جذب سریع، بدون چربی یا سنگینی"},
                "روشنایی که از خودِ پوست می‌آید، نه از لایه‌های پوشاننده",
                "افرادی که با لک، تیرگی یا تناژ ناهموار پوست روبه‌رو هستند",
                "لک و تیرگی"));

        KB.add(kb("گاتیو|gatieau|gatio",
                "ضدچروک|ضد چروک|لیفت|لیفتینگ|جوان|پپتید|پنجه کلاغی|antiwrinkle|anti-wrinkle|50|۵۰",
                "کرم", "کرم ضدچروک و لیفتینگ تخصصی گاتیو (۵۰ میل)",
                "GATIEAU Anti-Wrinkle Cream", "مراقبت تخصصی پوست",
                "کمپلکس پپتید جوان‌ساز، هیالورونیک اسید، ویتامین E و نیاسینامید",
                "هر شب و صبح مقدار مناسبی از کرم را با حرکات رو به بالا روی صورت و گردن ماساژ دهید.",
                "۵۰ میلی‌لیتر", "تحت لیسانس اروپا", "gatieau_antiwrinkle",
                1_390_000L, 1_610_000L,
                new String[]{"کاهش هدفمند خطوط پنجه کلاغی، خط لبخند و خط اخم",
                        "تحریک کلاژن‌سازی طبیعی و بازیابی قوام پوست",
                        "لیفت ملایم خط فک و گردن بدون تزریق",
                        "بافت ابریشمی و مناسب برای پوست‌های حساس"},
                "زمان را نمی‌توان متوقف کرد، اما می‌توان آن را هوشمندانه‌تر جا گذاشت",
                "افراد ۳۰ سال به بالا که با خطوط ریز و افتادگی خفیف روبه‌رو هستند",
                "چروک و لیفتینگ"));

        KB.add(kb("لاکچری کوین|luxury coin|luxurycoin|coin|کوین",
                "پنکک|پودر|کامپکت|مخملی|ضدآب|spf|104|102|میکاپ|آرایش|compact|powder",
                "پنکک", "پنکک ضدآب و مخملی لاکچری کوین SPF 25",
                "LUXURY COIN Waterproof Compact Powder", "میکاپ لوکس",
                "پودر معدنی میکرونیزه، کلاژن هیدرولیزشده، عصاره آلوئه‌ورا و ویتامین E",
                "با پد خشک برای پوشش مخملی روزانه یا با پد مرطوب جهت کاور حرفه‌ای مجلسی استفاده شود.",
                "شماره ۱۰۴ بژ طبیعی", "تحت لیسانس فرانسه", "luxury_coin",
                920_000L, 1_210_000L,
                new String[]{"پوشانندگی یکدست و مات مخملی بدون حس سنگینی",
                        "فرمول واترپروف مقاوم به رطوبت و تعریق",
                        "ضدآفتاب SPF 25 برای استفاده روزانه",
                        "غنی‌شده با کلاژن و آلوئه‌ورا؛ بدون اکسید شدن روی پوست"},
                "کاوری که در عکس‌ها هم همان‌قدر بی‌نقص دیده می‌شود",
                "افرادی که به دنبال میکاپ ماندگار و طبیعی برای مهمانی و روزمره هستند",
                "پوشش و ماندگاری"));

        KB.add(kb("هیرفرند|hair friend|hairfriend|هیر فرند",
                "تونیک|ریزش|رویش|تقویت مو|مو|اسپری|تونیک مو|hair|tonic|300|۳۰۰",
                "تونیک", "تونیک گیاهی تقویت و رویش مجدد مو هیرفرند (۳۰۰ میل)",
                "Hair Friend Herbal Hair Tonic", "تقویت و درمان مو",
                "عصاره چای سبز، شلتوک برنج، برگ زیتون، گل همیشه‌بهار و رزماری",
                "روزانه چند پاف روی پوست سر تمیز اسپری شده و ۲ دقیقه با نوک انگشتان ماساژ داده شود؛ نیازی به آبکشی نیست.",
                "۳۰۰ میلی‌لیتر", "فرمولاسیون تخصصی گیاهی", "hair_friend",
                1_560_000L, 1_950_000L,
                new String[]{"کاهش محسوس ریزش مو در دوره مصرف منظم",
                        "ضخیم‌تر شدن تارهای نازک و افزایش تراکم مو",
                        "تحریک گردش خون فولیکول و رویش موی جدید",
                        "بدون چربی، بدون رنگ و بدون نیاز به آبکشی"},
                "ریزش را متوقف کنید، پیش از آن‌که آینه خبرش را به شما بدهد",
                "افراد با ریزش فصلی، نازکی تار مو یا کاهش تراکم پس از زایمان",
                "ریزش و تقویت مو"));

        KB.add(kb("اوردینری|ordinary|the ordinary",
                "تونر|گلیکولیک|glycolic|لایه‌بردار|منافذ|pore|اسید|240|۲۴۰|AHA",
                "تونر", "تونر لایه‌بردار گلیکولیک اسید ۷٪ اوردینری (۲۴۰ میل)",
                "The Ordinary Glycolic Acid 7% Exfoliating Toner", "مراقبت تخصصی پوست",
                "گلیکولیک اسید ۷ درصد، عصاره ریشه جینسینگ، فلفل تاسمانی و آلوئه‌ورا",
                "شب‌ها با پد پنبه‌ای روی پوست تمیز صورت و گردن (به‌جز دور چشم) کشیده شود و روزها ضدآفتاب استفاده گردد.",
                "۲۴۰ میلی‌لیتر", "کانادا (اورجینال)", "ordinary_toner",
                2_890_000L, 3_450_000L,
                new String[]{"پاکسازی عمقی منافذ باز و کاهش نقطه‌های سیاه",
                        "رفع کدری و جای جوش با لایه‌برداری ملایم شبانه",
                        "ایجاد درخشش آینه‌ای بدون الکل و پارابن",
                        "بافت آبکی سبک و pH متعادل ۳٫۶"},
                "پوست شفاف، نتیجه‌ی صبرِ هوشمندانه است",
                "افرادی با منافذ باز، بافت ناهموار یا کدری پوست",
                "منافذ و کدری"));

        KB.add(kb("مورینگا|moringa|ویتابلا|vitabella|emo",
                "شامپو|درخت چای|teatree|tea tree|شوره|چربی|مو|پک|400|۴۰۰|shampoo",
                "شامپو", "پک تخصصی شامپو درخت چای ویتابلا و مورینگا امو",
                "VitaBella Tea Tree & Moringa EMO Duo", "تقویت و درمان مو",
                "روغن خالص برگ درخت چای، روغن هسته مورینگا، آمینواسیدها و ویتامین E",
                "روی موی مرطوب به مدت ۲ دقیقه ماساژ داده و با آب ولرم آبکشی نمایید.",
                "پک دوتایی کامل", "فرمولاسیون فاقد سولفات", "moringa_teatree",
                680_000L, 840_000L,
                new String[]{"تنظیم چربی کف سر و رفع شوره با روغن درخت چای",
                        "تغذیه و احیای ساقه مو با روغن هسته مورینگا",
                        "فاقد سولفات خشن؛ مناسب موی رنگ‌شده",
                        "نرمی، درخشش و کاهش وزش مو پس از استحمام"},
                "موی سالم، از یک شامپوی درست شروع می‌شود",
                "افرادی با موی چرب، شوره سر یا موی آسیب‌دیده",
                "چربی و شوره سر"));
    }

    private static Kb kb(String brand, String name, String form, String title, String nameEn,
                         String category, String ingredients, String usage, String volume,
                         String country, String imageKey, long price, long oldPrice,
                         String[] benefits, String hook, String audience, String concern) {
        Kb k = new Kb();
        k.brandTokens = brand; k.nameTokens = name; k.form = form; k.title = title; k.nameEn = nameEn;
        k.category = category; k.ingredients = ingredients; k.usage = usage; k.volume = volume;
        k.country = country; k.imageKey = imageKey; k.price = price; k.oldPrice = oldPrice;
        k.benefits = benefits; k.hook = hook; k.audience = audience; k.concern = concern;
        return k;
    }

    // ── Public payloads ───────────────────────────────────────────────────
    public static final class Analysis {
        public String detectedTitle;
        public String detectedBrand;
        public String category;
        public String formFactorFa;
        public String concernFa;
        public int confidence;
        public String rationaleFa;
        public String photoInsightFa;
        public String imageKey = "gatieau_whitening";
        public long suggestedPrice;
        public long suggestedOldPrice;
        public List<String> alternativesFa = new ArrayList<>();
        public KbInternal internal;
    }

    /** Opaque holder so MainActivity never depends on the private KB type. */
    public static final class KbInternal {
        String title, nameEn, category, ingredients, usage, volume, country, imageKey, hook, audience, concern;
        long price, oldPrice;
        String[] benefits;
    }

    public static final class Draft {
        public String nameFa;
        public String nameEn;
        public String category;
        public String description;
        public String longDescription;
        public String benefits;
        public String ingredients;
        public String usage;
        public String country;
        public String volume;
        public String keywords;
        public String seoTitle;
        public String seoDescription;
        public String instagramCaption;
        public String imageKey = "gatieau_whitening";
        public long suggestedPrice;
        public long suggestedOldPrice;
        public int stock = 12;
    }

    // ── 1 · ANALYSIS ──────────────────────────────────────────────────────
    public static Analysis analyze(String rawName, String rawBrand, Bitmap photo) {
        String name = normalize(rawName);
        String brand = normalize(rawBrand);
        String combined = (name + " " + brand).trim();

        Analysis best = null;
        int bestScore = 0;
        List<String> altTitles = new ArrayList<>();

        for (Kb k : KB) {
            int score = 0;
            if (matchesAny(brand, k.brandTokens)) score += 45;
            else if (matchesAny(combined, k.brandTokens)) score += 26;

            int nameHits = 0;
            for (String tok : k.nameTokens.split("\\|")) {
                String t = normalize(tok);
                if (t.length() >= 2 && combined.contains(t)) nameHits++;
            }
            score += Math.min(38, nameHits * 11);

            if (combined.contains(normalize(k.form))) score += 14;
            if (combined.contains(normalize(k.concern))) score += 8;

            if (score >= 30) altTitles.add(k.title);

            if (score > bestScore) {
                bestScore = score;
                Analysis a = new Analysis();
                a.detectedTitle = k.title;
                a.detectedBrand = brandLineOf(k);
                a.category = k.category;
                a.formFactorFa = k.form;
                a.concernFa = k.concern;
                a.imageKey = k.imageKey;
                a.suggestedPrice = k.price;
                a.suggestedOldPrice = k.oldPrice;
                a.confidence = Math.min(97, 34 + bestScore);
                a.rationaleFa = rationale(k, brand, name, nameHits > 0);
                a.internal = toInternal(k);
                best = a;
            }
        }

        if (best == null) {
            best = genericAnalysis(combined, rawName, rawBrand);
        }

        for (String t : altTitles) {
            if (!t.equals(best.detectedTitle) && best.alternativesFa.size() < 3) {
                best.alternativesFa.add(t);
            }
        }
        if (photo != null) best.photoInsightFa = describePhoto(photo);
        return best;
    }

    private static KbInternal toInternal(Kb k) {
        KbInternal i = new KbInternal();
        i.title = k.title; i.nameEn = k.nameEn; i.category = k.category; i.ingredients = k.ingredients;
        i.usage = k.usage; i.volume = k.volume; i.country = k.country; i.imageKey = k.imageKey;
        i.hook = k.hook; i.audience = k.audience; i.concern = k.concern;
        i.price = k.price; i.oldPrice = k.oldPrice; i.benefits = k.benefits;
        return i;
    }

    private static String brandLineOf(Kb k) {
        int idx = k.title.indexOf(" تخصصی ");
        String base = k.nameEn == null ? "ASIYE STORE" : k.nameEn;
        int space = base.indexOf(' ');
        String brand = space > 0 ? base.substring(0, space) : base;
        return idx > 0 ? brand : brand;
    }

    private static String rationale(Kb k, String brand, String name, boolean nameHit) {
        StringBuilder sb = new StringBuilder();
        if (!brand.isEmpty()) sb.append("برند «").append(brand).append("» با خانواده ").append(k.form).append(" مجموعه تطبیق داده شد");
        else sb.append("بر اساس ساختار نام محصول، خانواده ").append(k.form).append(" تشخیص داده شد");
        if (nameHit) sb.append("؛ کلیدواژه‌های فرمول و کاربرد (").append(k.concern).append(") نیز تأیید شد");
        sb.append(".");
        return sb.toString();
    }

    private static Analysis genericAnalysis(String combined, String rawName, String rawBrand) {
        Analysis a = new Analysis();
        String category;
        String form;
        String imageKey;
        long price;
        long oldPrice;
        if (combined.contains("شامپو") || combined.contains("مو") || combined.contains("تونیک") || combined.contains("hair")) {
            category = "تقویت و درمان مو"; form = "مراقبت مو"; imageKey = "hair_friend";
            price = 890_000L; oldPrice = 1_090_000L;
        } else if (combined.contains("پنکک") || combined.contains("رژ") || combined.contains("میکاپ") || combined.contains("پودر")) {
            category = "میکاپ لوکس"; form = "میکاپ"; imageKey = "luxury_coin";
            price = 860_000L; oldPrice = 1_020_000L;
        } else if (combined.contains("سرم") || combined.contains("تونر") || combined.contains("کرم") || combined.contains("پوست")) {
            category = "مراقبت تخصصی پوست"; form = "مراقبت پوست"; imageKey = "gatieau_whitening";
            price = 1_290_000L; oldPrice = 1_590_000L;
        } else {
            category = "مراقبت تخصصی پوست"; form = "مراقبت زیبایی"; imageKey = "gatieau_whitening";
            price = 990_000L; oldPrice = 1_180_000L;
        }
        a.detectedTitle = (rawName == null || rawName.trim().isEmpty()) ? "محصول جدید مجموعه" : rawName.trim();
        a.detectedBrand = (rawBrand == null || rawBrand.trim().isEmpty()) ? "ASIYE STORE" : rawBrand.trim();
        a.category = category;
        a.formFactorFa = form;
        a.concernFa = "مراقبت روزانه";
        a.imageKey = imageKey;
        a.confidence = 58;
        a.suggestedPrice = price;
        a.suggestedOldPrice = oldPrice;
        a.rationaleFa = "الگوی نام و برند به یک خانواده محصول شناخته‌شده نزدیک نبود؛ دسته «" + category + "» به‌عنوان نزدیک‌ترین گزینه پیشنهاد می‌شود و می‌توانید آن را تغییر دهید.";
        KbInternal i = new KbInternal();
        i.title = a.detectedTitle;
        i.nameEn = a.detectedBrand + " • SELECTION";
        i.category = category;
        i.ingredients = "فرمولاسیون تخصصی استاندارد با مواد اولیه درجه یک";
        i.usage = "طبق دستور مصرف درج‌شده روی بسته‌بندی، روی پوست یا موی تمیز استفاده شود.";
        i.volume = "نسخه اصلی";
        i.country = "اورجینال";
        i.imageKey = imageKey;
        i.hook = "انتخابی که تفاوت را در جزئیات نشان می‌دهد";
        i.audience = "مشتریان مجموعه که به دنبال کیفیت تأییدشده هستند";
        i.concern = "مراقبت روزانه";
        i.price = price; i.oldPrice = oldPrice;
        i.benefits = new String[]{"کیفیت اورجینال با ضمانت اصالت مجموعه",
                "فرمولاسیون متعادل و مناسب استفاده روزانه",
                "بسته‌بندی اختصاصی و ارسال ایمن VIP",
                "همراه با مشاوره تخصصی مدیریت مجموعه"};
        a.internal = i;
        return a;
    }

    // ── 2 · COPY COMPOSITION ──────────────────────────────────────────────
    public static Draft compose(Analysis a, String rawName, String rawBrand) {
        KbInternal i = a.internal;
        Draft d = new Draft();
        String brand = (rawBrand == null ? "" : rawBrand.trim());
        if (brand.isEmpty()) brand = "ASIYE STORE";

        d.nameFa = safeTitle(rawName, i.title);
        d.nameEn = (brand + " • " + shortEn(i.nameEn)).toUpperCase(Locale.US);
        d.category = a.category;
        d.volume = i.volume;
        d.country = i.country;
        d.ingredients = i.ingredients;
        d.usage = i.usage;
        d.imageKey = i.imageKey;
        d.suggestedPrice = i.price;
        d.suggestedOldPrice = i.oldPrice;

        d.description = "«" + d.nameFa + "» با " + lowerFirst(i.ingredients)
                + "؛ انتخابی برای " + i.concern + " با ضمانت اصالت و مشاوره تخصصی مجموعه.";

        StringBuilder longDesc = new StringBuilder();
        longDesc.append("✨ ").append(i.hook).append("\n\n");
        longDesc.append(d.nameFa).append(" یکی از انتخاب‌های منتخب مجموعه است که برای ")
                .append(i.audience).append(" پیشنهاد می‌شود. ")
                .append("این محصول با فرمولاسیون ").append(lowerFirst(i.ingredients))
                .append(" طراحی شده تا در کنار روتین روزانه شما، نتیجه‌ای قابل مشاهده و پیوسته بسازد.\n\n");
        longDesc.append("چرا این محصول؟ ").append(i.benefits[0]).append("؛ ")
                .append(i.benefits[1]).append(". بافت سبک و سازگار آن باعث می‌شود ")
                .append("بدون ایجاد حساسیت یا سنگینی، در چند هفتهٔ نخست هم تفاوت را حس کنید.\n\n");
        longDesc.append("روش استفاده: ").append(i.usage)
                .append("\n\nنکته مدیریت مجموعه: برای رسیدن به بهترین نتیجه، مصرف منظم را قطع نکنید و ")
                .append("در صورت داشتن پوست حساس، ابتدا تست موضعی انجام دهید. ")
                .append("همراه هر سفارش، گواهی اصالت و بسته‌بندی اختصاصی ارسال می‌شود.");
        d.longDescription = longDesc.toString();

        StringBuilder bul = new StringBuilder();
        for (String b : i.benefits) bul.append("• ").append(b).append("\n");
        bul.append("• گواهی اصالت و ضمانت بازگشت کالا");
        d.benefits = bul.toString().trim();

        d.keywords = buildKeywords(d.nameFa, brand, a.category, i.concern, i.volume);
        d.seoTitle = trim60(d.nameFa);
        d.seoDescription = trim160("خرید " + d.nameFa + " اورجینال با ضمانت اصالت از مجموعه ASIYE STORE. "
                + "مناسب " + lcConcern(i.concern) + "، همراه با مشاوره تخصصی مدیریت مجموعه، ارسال سریع VIP و پرداخت ۴ قسط بدون کارمزد.");
        d.instagramCaption = buildInstagram(d.nameFa, brand, i, a);
        return d;
    }

    private static String buildInstagram(String title, String brand, KbInternal i, Analysis a) {
        StringBuilder sb = new StringBuilder();
        sb.append("✨ ").append(title).append("\n\n");
        sb.append(i.hook).append(" 🤍\n\n");
        for (int x = 0; x < Math.min(4, i.benefits.length); x++) {
            sb.append("• ").append(i.benefits[x]).append("\n");
        }
        sb.append("\n📌 حجم: ").append(i.volume).append("  |  اصالت: ۱۰۰٪ اورجینال\n");
        sb.append("💬 برای مشاوره و انتخاب درست، دایرکت پیام بدهید یا به مدیریت مجموعه پیام دهید.\n\n");
        sb.append("#" ).append("اسیه_استور #پوست_شفاف #مراقبت_پوست #لوکس #اورجینال ")
                .append("#").append(slug(a.category)).append(" ")
                .append("#").append(slug(i.concern)).append(" ")
                .append("#").append(brand.replace(" ", "_")).append("\n");
        sb.append("#asiyestore #skincare #luxurybeauty #instabeauty");
        return sb.toString().trim();
    }

    private static String buildKeywords(String title, String brand, String category, String concern, String volume) {
        Set<String> set = new HashSet<>(Arrays.asList(
                "خرید " + title,
                title,
                brand + " اصل",
                brand,
                category,
                concern,
                "خرید اورجینال",
                "قیمت " + title,
                "مشاوره تخصصی پوست و مو",
                "فروشگاه اینترنتی لوکس",
                "آسیه استور",
                "ASIYE STORE",
                volume
        ));
        StringBuilder sb = new StringBuilder();
        for (String k : set) {
            if (k == null || k.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append("، ");
            sb.append(k.trim());
        }
        return sb.toString();
    }

    // ── 3 · PHOTO INTELLIGENCE ────────────────────────────────────────────
    public static String describePhoto(Bitmap bmp) {
        if (bmp == null) return "";
        int w = Math.min(64, bmp.getWidth());
        int h = Math.min(64, bmp.getHeight());
        Bitmap small = Bitmap.createScaledBitmap(bmp, w, h, true);
        long r = 0, g = 0, b = 0;
        int bright = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int c = small.getPixel(x, y);
                r += (c >> 16) & 0xFF; g += (c >> 8) & 0xFF; b += c & 0xFF;
                int lum = (int) (0.299 * ((c >> 16) & 0xFF) + 0.587 * ((c >> 8) & 0xFF) + 0.114 * (c & 0xFF));
                if (lum > 170) bright++;
            }
        }
        int n = w * h;
        int ar = (int) (r / n), ag = (int) (g / n), ab = (int) (b / n);
        String tone;
        if (ar > ag && ar > ab) tone = "گرم و کرم‌رنگ";
        else if (ab > ar && ab > ag) tone = "سرد و آبی‌روشن";
        else if (Math.abs(ar - ag) < 12 && Math.abs(ag - ab) < 12) tone = "خنثی و مینیمال";
        else tone = "طلایی و گرم";

        String bg = bright > n * 0.55 ? "پس‌زمینه روشن و یکدست (مناسب جداسازی خودکار)"
                : (bright < n * 0.18 ? "پس‌زمینه تیره و دراماتیک" : "پس‌زمینه با نور طبیعی");

        return "تصویر تحلیل شد: تناژ " + tone + "، " + bg + "، با ابعاد "
                + PersianFormatter.toPersianDigits(String.valueOf(bmp.getWidth())) + "×"
                + PersianFormatter.toPersianDigits(String.valueOf(bmp.getHeight()))
                + " پیکسل. آماده ساخت تصویر استودیویی ابسیدین–طلایی.";
    }

    // ── helpers ───────────────────────────────────────────────────────────
    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.US)
                .replace('ي', 'ی').replace('ك', 'ک').replace('\u200c', ' ')
                .replace('‌', ' ').replace('_', ' ')
                .replaceAll("\\s+", " ").trim();
    }

    private static boolean matchesAny(String haystack, String pipeTokens) {
        if (haystack.isEmpty()) return false;
        for (String t : pipeTokens.split("\\|")) {
            String tok = normalize(t);
            if (tok.length() >= 2 && haystack.contains(tok)) return true;
        }
        return false;
    }

    private static String safeTitle(String raw, String fallback) {
        String t = raw == null ? "" : raw.trim();
        if (t.length() < 3) return fallback;
        return t;
    }

    private static String shortEn(String nameEn) {
        if (nameEn == null) return "SELECTION";
        String[] parts = nameEn.split(" ");
        return parts.length > 1 ? parts[0] + " " + parts[1] : parts[0];
    }

    private static String lowerFirst(String s) {
        return s == null || s.isEmpty() ? "" : s;
    }

    private static String lcConcern(String concern) {
        if (concern == null) return "مراقبت روزانه";
        return concern.replace("لک و تیرگی", "رفع لک و یکدست‌سازی پوست")
                .replace("چروک و لیفتینگ", "کاهش چروک و لیفت پوست")
                .replace("پوشش و ماندگاری", "میکاپ ماندگار و طبیعی")
                .replace("ریزش و تقویت مو", "کنترل ریزش و تقویت مو")
                .replace("منافذ و کدری", "پاکسازی منافذ و رفع کدری")
                .replace("چربی و شوره سر", "تنظیم چربی و رفع شوره سر");
    }

    private static String slug(String s) {
        if (s == null) return "beauty";
        return s.replace(" ", "_").replace("‌", "_");
    }

    private static String trim60(String s) {
        if (s == null) return "";
        String t = s.length() > 60 ? s.substring(0, 58) + "…" : s;
        return t;
    }

    private static String trim160(String s) {
        if (s == null) return "";
        return s.length() > 158 ? s.substring(0, 156) + "…" : s;
    }
}
