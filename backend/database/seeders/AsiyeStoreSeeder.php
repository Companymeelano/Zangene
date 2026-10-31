<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use App\Models\Role;
use App\Models\Permission;
use App\Models\User;
use App\Models\Brand;
use App\Models\Category;
use App\Models\Product;
use App\Models\PaymentGateway;
use App\Models\InstallmentProvider;
use App\Models\ShippingMethod;
use App\Models\Coupon;

class AsiyeStoreSeeder extends Seeder
{
    public function run(): void
    {
        // 1. Roles & Permissions
        $superAdmin = Role::firstOrCreate(['slug' => 'super-admin'], ['name' => 'مدیر کل اکوسیستم']);
        $storeManager = Role::firstOrCreate(['slug' => 'store-manager'], ['name' => 'مدیر فروشگاه']);
        $warehouseManager = Role::firstOrCreate(['slug' => 'warehouse-manager'], ['name' => 'مدیر انبار']);

        $permissions = [
            ['slug' => 'manage-products', 'name' => 'مدیریت محصولات'],
            ['slug' => 'manage-orders', 'name' => 'مدیریت سفارش‌ها'],
            ['slug' => 'manage-inventory', 'name' => 'مدیریت انبار'],
            ['slug' => 'manage-finance', 'name' => 'مدیریت مالی و اقساط'],
            ['slug' => 'view-dashboard', 'name' => 'مشاهده داشبورد مدیریتی'],
        ];

        $permIds = [];
        foreach ($permissions as $perm) {
            $p = Permission::firstOrCreate(['slug' => $perm['slug']], ['name' => $perm['name']]);
            $permIds[] = $p->id;
        }
        $superAdmin->permissions()->sync($permIds);
        $storeManager->permissions()->sync($permIds);

        // 2. Default Super Admin
        User::firstOrCreate(
            ['mobile' => '09120000000'],
            [
                'name' => 'مدیریت ارشد ASIYE STORE',
                'email' => 'admin@asiyestore.ir',
                'password' => Hash::make('Asiye@Luxury2026!'),
                'role_id' => $superAdmin->id,
                'is_active' => true,
                'mobile_verified_at' => now(),
            ]
        );

        // 3. Brand & Categories
        $brand = Brand::firstOrCreate(
            ['slug' => 'asiye-signature'],
            [
                'name_fa' => 'برند اختصاصی آسیه (ASIYE)',
                'name_en' => 'ASIYE SIGNATURE',
                'logo' => '/images/logo_horizontal_luxury.png',
                'description' => 'فرمولاسیون فوق‌لوکس زیبایی با عصاره طلای ۲۴ عیار و ترکیبات گیاهی کمیاب',
            ]
        );

        $catSkincare = Category::firstOrCreate(['slug' => 'skin-care'], ['name_fa' => 'مراقبت از پوست', 'name_en' => 'Skin Care', 'order' => 1]);
        $catMakeup = Category::firstOrCreate(['slug' => 'lip-makeup'], ['name_fa' => 'آرایش لب و صورت', 'name_en' => 'Makeup', 'order' => 2]);
        $catPerfume = Category::firstOrCreate(['slug' => 'fragrance'], ['name_fa' => 'عطر و ادکلن لوکس', 'name_en' => 'Fragrance', 'order' => 3]);
        $catHair = Category::firstOrCreate(['slug' => 'hair-care'], ['name_fa' => 'مراقبت از مو', 'name_en' => 'Hair Care', 'order' => 4]);

        // 4. Signature Luxury Products
        $serum = Product::firstOrCreate(
            ['sku' => 'AS-SRM-001'],
            [
                'brand_id' => $brand->id,
                'category_id' => $catSkincare->id,
                'name_fa' => 'سرم طلایی احیاکننده پوست ۲۴ عیار',
                'name_en' => 'Golden Revitalizing Facial Serum',
                'slug' => 'golden-revitalizing-serum',
                'description' => 'سرم لوکس حاوی عصاره طلای خالص و هیالورونیک اسید چندمولکولی برای بازسازی سلولی، لیفتینگ و درخشش ابریشمی پوست.',
                'features' => 'بدون پارابن، تست درماتولوژی، ضمانت اصالت، جذب فوری',
                'usage_method' => 'روزانه ۲ تا ۳ قطره صبح و شب روی پوست تمیز صورت و گردن ماساژ داده شود.',
                'ingredients' => '24K Colloidal Gold, Multi-Molecular Hyaluronic Acid, Damask Rose Essence, Niacinamide',
                'country' => 'ایران (تحت لیسانس پاریس)',
                'volume_weight' => '۳۰ میلی‌لیتر',
                'skin_type' => 'مناسب انواع پوست',
                'original_price' => 2300000,
                'sale_price' => 1850000,
                'discount_percent' => 20,
                'published' => true,
            ]
        );
        $serumVar30 = $serum->variants()->firstOrCreate(['size' => '۳۰ میلی‌لیتر'], ['price_offset' => 0]);
        $serumVar30->inventory()->firstOrCreate([], ['quantity' => 28, 'low_stock_threshold' => 5]);
        $serumVar50 = $serum->variants()->firstOrCreate(['size' => '۵۰ میلی‌لیتر'], ['price_offset' => 650000]);
        $serumVar50->inventory()->firstOrCreate([], ['quantity' => 12, 'low_stock_threshold' => 5]);
        $serum->images()->firstOrCreate(['path' => '/images/product_serum_luxury.png'], ['is_primary' => true, 'order' => 1]);

        $lipstick = Product::firstOrCreate(
            ['sku' => 'AS-LIP-042'],
            [
                'brand_id' => $brand->id,
                'category_id' => $catMakeup->id,
                'name_fa' => 'رژ لب مخملی مات اختصاصی ASIYE',
                'name_en' => 'Exclusive Velvet Matte Lipstick',
                'slug' => 'exclusive-velvet-matte-lipstick',
                'description' => 'رژ لب مات با بافت مخملی فوق‌سبک، ویتامین E و ماندگاری ۱۲ ساعته بدون ایجاد خشکی، الهام‌گرفته از پالت طلایی شامپاینی.',
                'features' => 'ماندگاری ۱۲ ساعته، فاقد سرب و پارابن، غنی‌شده با شی‌باتر',
                'usage_method' => 'از مرکز لب به سمت گوشه‌ها با حرکت یکنواخت استفاده شود.',
                'ingredients' => 'Shea Butter, Vitamin E, Natural Mineral Pigments, Jojoba Seed Oil',
                'country' => 'ایران (فرمولاسیون فرانسه)',
                'volume_weight' => '۴ گرم',
                'original_price' => 520000,
                'sale_price' => 450000,
                'discount_percent' => 13,
                'published' => true,
            ]
        );
        $lipVar1 = $lipstick->variants()->firstOrCreate(['color' => 'رز بژ مخملی', 'color_hex' => '#C98A7B'], ['price_offset' => 0]);
        $lipVar1->inventory()->firstOrCreate([], ['quantity' => 42, 'low_stock_threshold' => 8]);
        $lipVar2 = $lipstick->variants()->firstOrCreate(['color' => 'شامپاینی نود', 'color_hex' => '#C6A16C'], ['price_offset' => 0]);
        $lipVar2->inventory()->firstOrCreate([], ['quantity' => 4, 'low_stock_threshold' => 6]);
        $lipstick->images()->firstOrCreate(['path' => '/images/product_lipstick_luxury.png'], ['is_primary' => true, 'order' => 1]);

        $perfume = Product::firstOrCreate(
            ['sku' => 'AS-PRF-999'],
            [
                'brand_id' => $brand->id,
                'category_id' => $catPerfume->id,
                'name_fa' => 'ادوپرفیوم اختصاصی ASIYE Signature',
                'name_en' => 'ASIYE Signature Eau De Parfum',
                'slug' => 'asiye-signature-eau-de-parfum',
                'description' => 'سمفونی مسحورکننده‌ای از نت‌های رز دمشقی، زعفران سلطنتی، کهربا و چوب صندل گرم؛ امضای فراموش‌نشدنی وقار و زیبایی.',
                'features' => 'غلظت Eau De Parfum، پخش بوی عالی، ماندگاری ۲۴ ساعته',
                'usage_method' => 'روی نقاط نبض‌دار (مچ دست و گردن) از فاصله ۱۵ سانتی‌متری اسپری شود.',
                'ingredients' => 'Damask Rose, Saffron, Ambergris, Sandalwood, White Musk',
                'country' => 'فرانسه / بسته‌بندی اختصاصی',
                'volume_weight' => '۱۰۰ میلی‌لیتر',
                'original_price' => 2450000,
                'sale_price' => 2100000,
                'discount_percent' => 14,
                'published' => true,
            ]
        );
        $perfVar = $perfume->variants()->firstOrCreate(['size' => '۱۰۰ میلی‌لیتر'], ['price_offset' => 0]);
        $perfVar->inventory()->firstOrCreate([], ['quantity' => 19, 'low_stock_threshold' => 5]);
        $perfume->images()->firstOrCreate(['path' => '/images/product_perfume_luxury.png'], ['is_primary' => true, 'order' => 1]);

        $caviar = Product::firstOrCreate(
            ['sku' => 'AS-CRM-108'],
            [
                'brand_id' => $brand->id,
                'category_id' => $catSkincare->id,
                'name_fa' => 'کرم شب خاویار سیاه و پپتید طلایی',
                'name_en' => 'Imperial Caviar & Gold Peptide Night Crème',
                'slug' => 'imperial-caviar-gold-night-creme',
                'description' => 'کرم بازسازی‌کننده عمیق شبانه با عصاره خاویار سیاه و کمپلکس مس-پپتید برای تغذیه عمقی و استحکام سد دفاعی پوست.',
                'country' => 'سوئیس / فرانسه',
                'volume_weight' => '۵۰ میلی‌لیتر',
                'ttac_code' => 'AS-AUTH-2026-9902',
                'cold_chain_required' => true,
                'replenishment_days' => 60,
                'original_price' => 3100000,
                'sale_price' => 2690000,
                'discount_percent' => 13,
                'published' => true,
            ]
        );
        $cavVar = $caviar->variants()->firstOrCreate(['size' => '۵۰ میلی‌لیتر'], ['price_offset' => 0]);
        $cavVar->inventory()->firstOrCreate([], ['quantity' => 14, 'low_stock_threshold' => 5]);
        $caviar->images()->firstOrCreate(['path' => '/images/product_caviar_cream_luxury.jpg'], ['is_primary' => true, 'order' => 1]);

        $elixir = Product::firstOrCreate(
            ['sku' => 'AS-ELX-305'],
            [
                'brand_id' => $brand->id,
                'category_id' => $catHair->id,
                'name_fa' => 'الکسیر ابریشم و طلای ۲۴ عیار مو',
                'name_en' => '24K Gold & Silk Hair Elixir',
                'slug' => '24k-gold-silk-hair-elixir',
                'description' => 'الکسیر کریستالی حاوی ورقه‌های معلق طلای ۲۴ عیار و روغن آرگان خالص برای احیای ساقه مو و درخشش آینه‌ای.',
                'country' => 'فرانسه',
                'volume_weight' => '۱۰۰ میلی‌لیتر',
                'ttac_code' => 'AS-AUTH-2026-9905',
                'cold_chain_required' => false,
                'replenishment_days' => 75,
                'original_price' => 1580000,
                'sale_price' => 1290000,
                'discount_percent' => 18,
                'published' => true,
            ]
        );
        $elxVar = $elixir->variants()->firstOrCreate(['size' => '۱۰۰ میلی‌لیتر'], ['price_offset' => 0]);
        $elxVar->inventory()->firstOrCreate([], ['quantity' => 22, 'low_stock_threshold' => 5]);
        $elixir->images()->firstOrCreate(['path' => '/images/product_hair_elixir_luxury.jpg'], ['is_primary' => true, 'order' => 1]);

        $coffret = Product::firstOrCreate(
            ['sku' => 'AS-DSC-777'],
            [
                'brand_id' => $brand->id,
                'category_id' => $catPerfume->id,
                'name_fa' => 'کافره مخمل هدیه VIP و پک تستر عطر Discovery',
                'name_en' => 'VIP Velvet Coffret & Fragrance Discovery Set',
                'slug' => 'vip-velvet-fragrance-discovery-set',
                'description' => 'شامل ۳ ویال کریستالی ۵ میل عطرهای اختصاصی در جعبه مخمل مشکی با مهر طلایی AS و ۱۰۰٪ بازگشت وجه روی خرید نسخه اصلی.',
                'country' => 'پاریس / بسته‌بندی دست‌ساز',
                'volume_weight' => '۳ × ۵ میلی‌لیتر',
                'ttac_code' => 'AS-AUTH-2026-9906',
                'cold_chain_required' => true,
                'replenishment_days' => 30,
                'original_price' => 980000,
                'sale_price' => 790000,
                'discount_percent' => 19,
                'published' => true,
            ]
        );
        $cofVar = $coffret->variants()->firstOrCreate(['size' => '۳ × ۵ میلی‌لیتر'], ['price_offset' => 0]);
        $cofVar->inventory()->firstOrCreate([], ['quantity' => 16, 'low_stock_threshold' => 5]);
        $coffret->images()->firstOrCreate(['path' => '/images/vip_gift_box_luxury.jpg'], ['is_primary' => true, 'order' => 1]);

        // 5. Gateways, 4 BNPL Installment Providers, Shipping & Coupons
        PaymentGateway::firstOrCreate(['slug' => 'zarinpal'], ['name' => 'درگاه پرداخت امن زرین‌پال', 'is_active' => true]);
        PaymentGateway::firstOrCreate(['slug' => 'direct_bank'], ['name' => 'درگاه مستقیم بانکی (شاپرک)', 'is_active' => true]);

        InstallmentProvider::firstOrCreate(['slug' => 'snapp_pay'], ['name' => 'سرویس اقساطی اسنپ‌پی (۴ قسط بدون کارمزد)', 'max_installments' => 4, 'is_active' => true]);
        InstallmentProvider::firstOrCreate(['slug' => 'tara'], ['name' => 'اعتبار خرید اقساطی تارا', 'max_installments' => 6, 'is_active' => true]);
        InstallmentProvider::firstOrCreate(['slug' => 'digipay'], ['name' => 'دیجی‌پی — پرداخت اعتباری هوشمند', 'max_installments' => 12, 'is_active' => true]);
        InstallmentProvider::firstOrCreate(['slug' => 'azkivam'], ['name' => 'ازکی‌وام — خرید اقساطی کلکسیونی', 'max_installments' => 12, 'is_active' => true]);

        ShippingMethod::firstOrCreate(['slug' => 'vip_courier'], ['name' => 'پیک اختصاصی VIP (بسته‌بندی لوکس)', 'base_cost' => 95000, 'estimated_delivery' => 'تحویل امروز (تهران)', 'is_active' => true]);
        ShippingMethod::firstOrCreate(['slug' => 'pishtaz_post'], ['name' => 'پست پیشتاز ویژه سراسر کشور', 'base_cost' => 65000, 'estimated_delivery' => '۲ تا ۴ روز کاری', 'is_active' => true]);
        ShippingMethod::firstOrCreate(['slug' => 'snapp_box'], ['name' => 'ارسال فوری اسنپ‌باکس', 'base_cost' => 110000, 'estimated_delivery' => 'زیر ۲ ساعت', 'is_active' => true]);

        Coupon::firstOrCreate(
            ['code' => 'ASIYE20'],
            [
                'discount_percent' => 20,
                'min_purchase' => 500000,
                'max_discount' => 600000,
                'start_at' => now()->subDay(),
                'end_at' => now()->addYear(),
                'usage_limit' => 500,
                'usage_per_customer' => 2,
                'is_active' => true,
            ]
        );

        Coupon::firstOrCreate(
            ['code' => 'VIPGOLD'],
            [
                'discount_amount' => 350000,
                'min_purchase' => 1500000,
                'start_at' => now()->subDay(),
                'end_at' => now()->addYear(),
                'usage_limit' => 200,
                'usage_per_customer' => 1,
                'is_active' => true,
            ]
        );

        Coupon::firstOrCreate(
            ['code' => 'REFILL10'],
            [
                'discount_percent' => 10,
                'min_purchase' => 200000,
                'max_discount' => 500000,
                'start_at' => now()->subDay(),
                'end_at' => now()->addYear(),
                'usage_limit' => 1000,
                'usage_per_customer' => 10,
                'is_active' => true,
            ]
        );
    }
}
