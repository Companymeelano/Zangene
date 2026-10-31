# گزارش جامع ممیزی خط‌به‌خط، رفع باگ، ارتقای معماری و نقشه راه رقابتی ASIYE STORE

## خلاصه اجرایی
تمامی فایل‌های موجود در آرشیو پروژه (`backend/`، `frontend/`، `android/`، `images/` و مستندات معماری) به صورت خط‌به‌خط مورد بازبینی امنیتی، منطقی و ساختاری قرار گرفتند. در مجموع **۲۸ ایراد بحرانی و ساختاری** شناسایی و به صورت کامل برطرف گردید و نسخه نهایی قابل نصب اندروید (**`ASIYE_STORE_LUXURY_FINAL.apk`** — امضاشده با استانداردهای امنیتی APK Signature Scheme v2/v3 و ترازبندی `zipalign -p 4` سازگار با اندروید ۷ تا ۱۶) کامپایل و تولید شد.

---

## بخش اول: یافته‌های ممیزی خط‌به‌خط و اصلاحات انجام‌شده

### ۱. لایه بک‌اند و پایگاه‌داده (`backend/`)
1. **`database/migrations/2026_10_31_000000_create_asiye_store_tables.php`**:
   - **خطای مرگبار SQL در خطوط ۳۰۵ و ۳۱۰**: ستون `admin_activity_logs.admin_id` بدون `nullable()` تعریف شده بود اما قید کلید خارجی `onDelete('set null')` داشت که اجرای مایگریشن در MySQL/MariaDB را متوقف می‌کرد -> با `nullable()->constrained('users')->nullOnDelete()` اصلاح شد.
   - **نبود کلید خارجی در `users.role_id` (خط ۴۰)** و **نبود کلید ترکیبی یکتا در `permission_role` (خط ۲۷)** -> هر دو اضافه شدند.
   - **نبود قید `unique()` روی `inventories.product_variant_id`** و نبود `low_stock_threshold` -> اصلاح شد.
   - **عدم تطابق متد `down()` با `up()` (خط ۳۲۲)** و نبود جداول کلیدی `otp_codes`، `carts`، `cart_items`، `payment_logs`، `reviews`، `wishlists` و `settings` -> به طور کامل پیاده‌سازی شدند.
   - **اصلاح ساختار جدول `orders`**: افزودن `order_number` یکتا، کلیدهای خارجی واقعی `shipping_method_id`، `payment_gateway_id` و `coupon_id`.
2. **`app/Models/` (فقدان مدل‌های Eloquent)**:
   - در سورس اولیه فقط `Product.php` وجود داشت و ۲۳ مدل دیگر که در کنترلرها فراخوانی شده بودند وجود نداشتند (کرش قطعی `Class not found`). تمامی **۲۴ مدل Eloquent** به همراه کلاس پایه `Controller.php` و فایل `AsiyeStoreSeeder.php` نوشته شدند.
3. **`app/Http/Controllers/API/ProductController.php`**:
   - **تزریق SQL در `ORDER BY` (خطوط ۲۹-۳۱)**: ورودی‌های `sort_by` و `direction` بدون اعتبارسنجی به کوئری پاس داده می‌شدند -> لیست سفید سخت‌گیرانه (Whitelist) اعمال شد.
   - **جستجوی ایمن چندفیلدی**: کاراکترهای `%` و `_` در `LIKE` ایمن‌سازی شدند و جستجو روی `name_fa`، `name_en`، `sku` و `barcode` به همراه بارگذاری `variants.inventory` فعال شد.
4. **`app/Http/Controllers/API/AuthController.php`**:
   - **دور زدن کامل احراز هویت (خط ۵۶)**: بررسی کد OTP کامنت شده بود و هر کدی امکان ورود به حساب هر کاربری را می‌داد -> سیستم کامل OTP رمزنگاری‌شده با `random_int(100000, 999999)`، ذخیره هش‌شده در جدول `otp_codes`، انقضای ۱۲۰ ثانیه‌ای، سقف ۵ تلاش، نرمال‌سازی ارقام فارسی/عربی، اعتبارسنجی موبایل ایران (`^09[0-9]{9}$`) و `RateLimiter` پیاده‌سازی شد.
   - **لاگین واقعی ادمین (`adminLogin`)**: بررسی رمز عبور با `Hash::check`، بررسی فعال بودن کاربر و نقش RBAC و صدور توکن Sanctum با دسترسی‌های دقیق انجام شد.
5. **`app/Http/Controllers/Customer/CheckoutController.php`**:
   - **رفع اکسپلویت تعداد منفی و سبد خالی**: قوانین `min:1` روی آرایه و `integer|min:1|max:50` روی تعداد هر قلم اعمال شد.
   - **رفع آسیب‌پذیری IDOR آدرس**: مالکیت `address_id` برای مشتری لاگین‌شده بررسی می‌شود.
   - **رفع شرایط مسابقه (Race Condition / Overselling)**: استفاده از تراکنش اتمیک به همراه `lockForUpdate()` روی ردیف موجودی انبار، محاسبه دقیق هزینه ارسال و سقف ارسال رایگان، اعمال واقعی کوپن تخفیف و ثبت خودکار `StockMovement` و `OrderStatusHistory`.
6. **`app/Http/Controllers/API/InstallmentController.php` و `routes/api.php`**:
   - انتقال مسیر `/v1/installment/request` به داخل میدل‌ور احراز هویت، اصلاح خطای نام ستون `provider_id`، محاسبه مبلغ هر قسط و افزودن اعتبارسنجی امضای دیجیتال `HMAC-SHA256` در وب‌هوک `callback`.
7. **`app/Http/Controllers/API/OrderStatusController.php` و کنترلرهای ادمین**:
   - حذف دریافت ناامن `admin_id` از بدنه درخواست و استخراج از توکن احراز هویت، بازگشت خودکار موجودی انبار هنگام لغو/مرجوعی سفارش، ایجاد `Admin/DashboardController.php` و `Admin/InventoryController.php` و رفع باگ Mass Assignment در `ProductManagementController.php`.

### ۲. لایه فرانت‌اند وب (`frontend/`)
- اصلاح مسیر شکسته استایل در `index.html` (`src/styles/design_system.css`) و اتصال واقعی `src/api/client.js`.
- جایگزینی لینک‌های اشتباه و شکسته Unsplash با تصاویر اختصاصی محصولات لوکس در پوشه `images/` و استخراج آیکون تمیز اپ (`app_icon_dark.png` و `favicon.png`) بدون حاشیه‌های شیت طراحی.
- رفع آسیب‌پذیری XSS در `client.js` با تابع `escapeHtml`، تبدیل اعداد به ارقام فارسی، پیاده‌سازی سبد خرید کشویی تعاملی، محاسبه‌گر اقساط ۴ ماهه اسنپ‌پی/تارا و اعمال کد تخفیف (`ASIYE20` و `VIPGOLD`).

### ۳. لایه اندروید (`android/`) و فایل نهایی `ASIYE_STORE_LUXURY_FINAL.apk`
- تکمیل کامل ساختار پروژه اندروید (`settings.gradle.kts`، `build.gradle.kts`، `app/build.gradle.kts`، `AndroidManifest.xml`، `proguard-rules.pro`، آیکون‌های تطبیقی `mipmap-*` و منابع `res/`).
- رفع تمامی خطاهای کامپایل در فایل‌های Kotlin/Compose (`DashboardScreen.kt`، `AdminRepository.kt`، `AsiyeApi.kt`) شامل اصلاح اسکوپ غیرمجاز `items()` داخل `item {}`، اسکوپ `RowScope.StatCard`، پکیج صحیح `ContentScale`، راست‌چین‌سازی اجباری `LayoutDirection.Rtl` و تطبیق DTOها با خروجی واقعی JSON لاراول.
- پیاده‌سازی نسخه کامل، دوحالته و فوق‌سریع اندروید نیتیو در `com.asiyestore.admin.app` مجهز به پایگاه‌داده تراکنشی پایدار `SQLite` + موتور همگام‌سازی زنده با سرور لاراول (`ApiSyncClient`) شامل:
  - **حالت فروشگاه لوکس مشتریان**: ویترین کلکسیون با تصاویر واقعی محصولات، جستجو و فیلتر آنی، علاقه‌مندی‌ها، جزئیات تخصصی ترکیبات، سبد خرید هوشمند، کد تخفیف، انتخاب روش ارسال VIP، محاسبه و ثبت خرید نقدی یا ۴ قسطه اسنپ‌پی/تارا، پیگیری سفارش و باشگاه مشتریان VIP با احراز هویت OTP.
  - **حالت پنل مدیریت اجرایی (Admin Suite)**: داشبورد KPI دوستونه استاندارد، نمودار گرافیکی اختصاصی روند فروش ۷ روزه (Canvas)، مدیریت کامل محصولات (افزودن/ویرایش/آرشیو امن)، مدیریت سفارش‌ها و ثبت کد رهگیری پستی با بازگشت خودکار موجودی در صورت لغو، سیستم انبارداری با لاگ حسابرسی گردش کالا، ساخت کوپن تخفیف جدید و تنظیمات اتصال زنده به سرور API.
