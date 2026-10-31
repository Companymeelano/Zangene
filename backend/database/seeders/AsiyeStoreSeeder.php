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
        Role::firstOrCreate(['slug' => 'warehouse-manager'], ['name' => 'مدیر انبار']);

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

        // 2. Owner & Executive Manager Account — سرکار خانم زنگنه
        User::firstOrCreate(
            ['mobile' => '09044200993'],
            [
                'name' => 'سرکار خانم زنگنه — مدیریت ASIYE STORE',
                'email' => 'admin@asiyestore.ir',
                'password' => Hash::make('Asiye@2026'),
                'role_id' => $superAdmin->id,
                'is_active' => true,
                'mobile_verified_at' => now(),
            ]
        );

        // 3. Curated Brands & Categories matching @asiyeh.store
        $brandGatieau = Brand::firstOrCreate(['slug' => 'gatieau'], ['name_fa' => 'گاتیو (GATIEAU)', 'name_en' => 'GATIEAU']);
        $brandLuxuryCoin = Brand::firstOrCreate(['slug' => 'luxury-coin'], ['name_fa' => 'لاکچری کوین (LUXURY COIN)', 'name_en' => 'LUXURY COIN']);
        $brandOrdinary = Brand::firstOrCreate(['slug' => 'the-ordinary'], ['name_fa' => 'اوردینری (The Ordinary)', 'name_en' => 'The Ordinary']);
        $brandHairFriend = Brand::firstOrCreate(['slug' => 'hair-friend'], ['name_fa' => 'هیرفرند و ویتابلا', 'name_en' => 'Hair Friend & VitaBella']);

        $catSkincare = Category::firstOrCreate(['slug' => 'skin-care'], ['name_fa' => 'مراقبت تخصصی پوست', 'name_en' => 'Skin Care', 'order' => 1]);
        $catMakeup = Category::firstOrCreate(['slug' => 'makeup'], ['name_fa' => 'میکاپ لوکس', 'name_en' => 'Luxury Makeup', 'order' => 2]);
        $catHair = Category::firstOrCreate(['slug' => 'hair-care'], ['name_fa' => 'تقویت و درمان مو', 'name_en' => 'Hair Care', 'order' => 3]);

        // 4. Exact 6 Flagship Products from @asiyeh.store Instagram
        $p1 = Product::firstOrCreate(
            ['sku' => 'AS-GAT-150'],
            [
                'brand_id' => $brandGatieau->id,
                'category_id' => $catSkincare->id,
                'name_fa' => 'کرم روشن‌کننده و ضدلک تخصصی گاتیو (۱۵۰ میل)',
                'name_en' => 'GATIEAU Skin Whitening Cream 150ml',
                'slug' => 'gatieau-skin-whitening-cream-150ml',
                'description' => 'فرمولاسیون پیشرفته حاوی سرامید، لیپوویت، کمپلکس ضدلک Hentowhite AF و ویتامین‌های A، C و E جهت رفع تیرگی نواحی مختلف و یکدست‌سازی تناژ پوست.',
                'ingredients' => 'Ceramide, Hentowhite AF, Lipovit, Vitamins A, C & E',
                'country' => 'تحت لیسانس اروپا',
                'volume_weight' => '۱۵۰ میلی‌لیتر',
                'ttac_code' => 'AS-GAT-150',
                'original_price' => 2380000,
                'sale_price' => 1550000,
                'discount_percent' => 35,
                'published' => true,
            ]
        );
        $p1->variants()->firstOrCreate(['size' => '۱۵۰ میلی‌لیتر'], ['price_offset' => 0])
            ->inventory()->firstOrCreate([], ['quantity' => 24, 'low_stock_threshold' => 5]);
        $p1->images()->firstOrCreate(['path' => '/images/insta_gatieau_whitening.jpg'], ['is_primary' => true, 'order' => 1]);

        $p2 = Product::firstOrCreate(
            ['sku' => 'AS-GAT-050'],
            [
                'brand_id' => $brandGatieau->id,
                'category_id' => $catSkincare->id,
                'name_fa' => 'کرم ضدچروک و لیفتینگ تخصصی گاتیو (۵۰ میل)',
                'name_en' => 'GATIEAU Anti-Wrinkle Peptide Cream 50ml',
                'slug' => 'gatieau-anti-wrinkle-cream-50ml',
                'description' => 'کاهش‌دهنده هدفمند خطوط پنجه کلاغی، خط اخم و چروک‌های صورت با تحریک کلاژن‌سازی طبیعی و افزایش قوام و لطافت ابریشمی پوست.',
                'ingredients' => 'Advanced Peptide Complex, Hyaluronic Acid, Vitamin E',
                'country' => 'تحت لیسانس اروپا',
                'volume_weight' => '۵۰ میلی‌لیتر',
                'ttac_code' => 'AS-GAT-050',
                'original_price' => 1610000,
                'sale_price' => 1390000,
                'discount_percent' => 14,
                'published' => true,
            ]
        );
        $p2->variants()->firstOrCreate(['size' => '۵۰ میلی‌لیتر'], ['price_offset' => 0])
            ->inventory()->firstOrCreate([], ['quantity' => 18, 'low_stock_threshold' => 5]);
        $p2->images()->firstOrCreate(['path' => '/images/insta_gatieau_antiwrinkle.jpg'], ['is_primary' => true, 'order' => 1]);

        $p3 = Product::firstOrCreate(
            ['sku' => 'AS-LC-104'],
            [
                'brand_id' => $brandLuxuryCoin->id,
                'category_id' => $catMakeup->id,
                'name_fa' => 'پنکک ضدآب و مخملی لاکچری کوین SPF 25',
                'name_en' => 'LUXURY COIN Waterproof Compact Powder SPF 25',
                'slug' => 'luxury-coin-waterproof-powder',
                'description' => 'پودر میکرونیزه ابریشمی حاوی کلاژن و ویتامین E، کاملاً واترپروف و قابل استفاده با پد مرطوب بدون تغییر رنگ یا ماسیدگی.',
                'ingredients' => 'Micronized Mineral Powder, Collagen, Aloe Vera, Vitamin E',
                'country' => 'تحت لیسانس فرانسه',
                'volume_weight' => 'شماره ۱۰۱ تا ۱۰۶',
                'ttac_code' => 'AS-LC-104',
                'original_price' => 1210000,
                'sale_price' => 920000,
                'discount_percent' => 24,
                'published' => true,
            ]
        );
        $p3->variants()->firstOrCreate(['color' => 'شماره ۱۰۴ — بژ طبیعی'], ['price_offset' => 0])
            ->inventory()->firstOrCreate([], ['quantity' => 30, 'low_stock_threshold' => 5]);
        $p3->images()->firstOrCreate(['path' => '/images/insta_luxury_coin_powder.jpg'], ['is_primary' => true, 'order' => 1]);

        $p4 = Product::firstOrCreate(
            ['sku' => 'AS-HF-300'],
            [
                'brand_id' => $brandHairFriend->id,
                'category_id' => $catHair->id,
                'name_fa' => 'تونیک گیاهی تقویت و رویش مجدد مو هیرفرند (۳۰۰ میل)',
                'name_en' => 'Hair Friend Herbal Hair Tonic 300ml',
                'slug' => 'hair-friend-herbal-tonic-300ml',
                'description' => 'محلول ۱۰۰٪ گیاهی تقویت فولیکول و ریشه مو سرشار از عصاره چای سبز، شلتوک برنج، برگ زیتون و همیشه‌بهار جهت توقف ریزش و تحریک رویش مجدد.',
                'ingredients' => 'Green Tea Extract, Rice Husk, Olive Leaf, Calendula',
                'country' => 'ایران (فرمولاسیون دانش‌بنیان)',
                'volume_weight' => '۳۰۰ میلی‌لیتر',
                'ttac_code' => 'AS-HF-300',
                'original_price' => 1950000,
                'sale_price' => 1560000,
                'discount_percent' => 20,
                'published' => true,
            ]
        );
        $p4->variants()->firstOrCreate(['size' => '۳۰۰ میلی‌لیتر'], ['price_offset' => 0])
            ->inventory()->firstOrCreate([], ['quantity' => 22, 'low_stock_threshold' => 5]);
        $p4->images()->firstOrCreate(['path' => '/images/insta_hair_friend_tonic.jpg'], ['is_primary' => true, 'order' => 1]);

        $p5 = Product::firstOrCreate(
            ['sku' => 'AS-ORD-240'],
            [
                'brand_id' => $brandOrdinary->id,
                'category_id' => $catSkincare->id,
                'name_fa' => 'تونر لایه‌بردار گلیکولیک اسید ۷٪ اوردینری (۲۴۰ میل)',
                'name_en' => 'The Ordinary Glycolic Acid 7% Exfoliating Toner 240ml',
                'slug' => 'the-ordinary-glycolic-acid-7-toner-240ml',
                'description' => 'تونر شفاف‌کننده و لایه‌بردار ملایم سطح پوست با pH استاندارد ۳.۶ حاوی ریشه جینسینگ و آلوئه‌ورا؛ پاکسازی منافذ باز و رفع تیرگی و جای جوش.',
                'ingredients' => 'Glycolic Acid 7%, Tasmanian Pepperberry, Ginseng Root, Aloe Vera',
                'country' => 'کانادا (اورجینال)',
                'volume_weight' => '۲۴۰ میلی‌لیتر',
                'ttac_code' => 'AS-ORD-240',
                'original_price' => 3450000,
                'sale_price' => 2890000,
                'discount_percent' => 16,
                'published' => true,
            ]
        );
        $p5->variants()->firstOrCreate(['size' => '۲۴۰ میلی‌لیتر'], ['price_offset' => 0])
            ->inventory()->firstOrCreate([], ['quantity' => 12, 'low_stock_threshold' => 4]);
        $p5->images()->firstOrCreate(['path' => '/images/insta_ordinary_glycolic.jpg'], ['is_primary' => true, 'order' => 1]);

        $p6 = Product::firstOrCreate(
            ['sku' => 'AS-MRG-500'],
            [
                'brand_id' => $brandHairFriend->id,
                'category_id' => $catHair->id,
                'name_fa' => 'پک تخصصی شامپو درخت چای ویتابلا و مورینگا امو',
                'name_en' => 'VitaBella Tea Tree & Moringa EMO Hair Care Duo',
                'slug' => 'vitabella-teatree-moringa-emo-duo',
                'description' => 'پک مکمل شامپو پاکسازی‌کننده درخت چای ویتابلا برای تنظیم چربی و رفع شوره کف سر به همراه شامپو مغذی روغن مورینگا امو جهت احیا و درخشش ساقه مو.',
                'ingredients' => 'Pure Tea Tree Oil, Moringa Seed Oil, Amino Acids, Vitamin E',
                'country' => 'فرمولاسیون تخصصی فاقد سولفات',
                'volume_weight' => '۲ × ۲۵۰ میلی‌لیتر',
                'ttac_code' => 'AS-MRG-500',
                'original_price' => 840000,
                'sale_price' => 680000,
                'discount_percent' => 19,
                'published' => true,
            ]
        );
        $p6->variants()->firstOrCreate(['size' => 'پک دوتایی کامل'], ['price_offset' => 0])
            ->inventory()->firstOrCreate([], ['quantity' => 26, 'low_stock_threshold' => 5]);
        $p6->images()->firstOrCreate(['path' => '/images/insta_moringa_teatree_set.jpg'], ['is_primary' => true, 'order' => 1]);

        // 5. Gateways, BNPL & Coupons
        PaymentGateway::firstOrCreate(['slug' => 'zarinpal'], ['name' => 'درگاه پرداخت امن زرین‌پال', 'is_active' => true]);
        InstallmentProvider::firstOrCreate(['slug' => 'snapp_pay'], ['name' => 'سرویس اقساطی اسنپ‌پی (۴ قسط بدون کارمزد)', 'max_installments' => 4, 'is_active' => true]);
        InstallmentProvider::firstOrCreate(['slug' => 'tara'], ['name' => 'اعتبار خرید اقساطی تارا', 'max_installments' => 6, 'is_active' => true]);
        InstallmentProvider::firstOrCreate(['slug' => 'digipay'], ['name' => 'دیجی‌پی — پرداخت اعتباری هوشمند', 'max_installments' => 12, 'is_active' => true]);

        ShippingMethod::firstOrCreate(['slug' => 'pishtaz_post'], ['name' => 'پست پیشتاز ویژه سراسر کشور', 'base_cost' => 65000, 'estimated_delivery' => '۲ تا ۴ روز کاری', 'is_active' => true]);
        ShippingMethod::firstOrCreate(['slug' => 'vip_courier'], ['name' => 'ارسال اختصاصی فوری', 'base_cost' => 95000, 'estimated_delivery' => 'هماهنگی مستقیم', 'is_active' => true]);

        Coupon::firstOrCreate(['code' => 'ASIYE15'], [
            'discount_percent' => 15,
            'min_purchase' => 500000,
            'max_discount' => 600000,
            'start_at' => now()->subDay(),
            'end_at' => now()->addYear(),
            'usage_limit' => 500,
            'usage_per_customer' => 3,
            'is_active' => true,
        ]);
    }
}
