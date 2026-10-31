package com.asiyestore.admin.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.asiyestore.admin.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MainActivity extends Activity {

    private enum EcosystemMode {
        CUSTOMER_STORE,
        ADMIN_SUITE
    }

    private AsiyeDatabaseHelper db;
    private final ApiSyncClient apiSyncClient = new ApiSyncClient();
    private final List<Models.CartItem> cart = new ArrayList<>();

    private EcosystemMode currentMode = EcosystemMode.CUSTOMER_STORE;
    private int customerTab = 0; // 0: Storefront, 1: Cart & BNPL, 2: Orders & Wishlist, 3: VIP Profile
    private int adminTab = 0;    // 0: Dashboard, 1: Products, 2: Orders, 3: Inventory, 4: Settings & Sync

    // Storefront filter state
    private String selectedCategory = "همه";
    private String searchQuery = "";
    private String sortMode = "newest";

    // Admin filter state
    private String adminOrderStatusFilter = "all";

    // Checkout state
    private String activeCouponCode = "";
    private int selectedShippingIdx = 0; // 0: VIP Courier, 1: Pishtaz, 2: Snapp Box
    private int selectedPaymentIdx = 0;  // 0: ZarinPal, 1: SnappPay 4x, 2: Tara 4x, 3: Direct Bank

    // Auth state
    private boolean adminAuthenticated = false;
    private String lastGeneratedOtp = "";

    // Root containers
    private TextView headerTitleView;
    private TextView headerSubtitleView;
    private Button modeSwitchButton;
    private FrameLayout bodyContainer;
    private LinearLayout bottomNavBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enforce strict RTL layout direction across the entire window
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        db = new AsiyeDatabaseHelper(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(LuxuryUiFactory.COLOR_IVORY);

        // 1. Luxury Top App Bar
        root.addView(buildTopHeader(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 2. Dynamic Screen Body
        bodyContainer = new FrameLayout(this);
        bodyContainer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.addView(bodyContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // 3. Bottom Navigation Bar
        bottomNavBar = new LinearLayout(this);
        bottomNavBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomNavBar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        bottomNavBar.setBackgroundColor(LuxuryUiFactory.COLOR_BLACK);
        bottomNavBar.setElevation(dp(8));
        bottomNavBar.setPadding(dp(4), dp(6), dp(4), dp(8));
        root.addView(bottomNavBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
        refreshUi();
    }

    @Override
    protected void onDestroy() {
        if (db != null) {
            db.close();
        }
        super.onDestroy();
    }

    private View buildTopHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(LuxuryUiFactory.COLOR_BLACK);
        header.setPadding(dp(14), dp(12), dp(14), dp(12));
        header.setElevation(dp(6));

        ImageView emblem = new ImageView(this);
        emblem.setImageResource(R.drawable.img_brand_emblem);
        emblem.setScaleType(ImageView.ScaleType.CENTER_CROP);
        emblem.setBackground(LuxuryUiFactory.roundedRect(this, LuxuryUiFactory.COLOR_BLACK, 10, LuxuryUiFactory.COLOR_GOLD, 1f));
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(42), dp(42));
        iconLp.leftMargin = dp(10);
        header.addView(emblem, iconLp);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setGravity(Gravity.RIGHT);

        headerTitleView = new TextView(this);
        headerTitleView.setText("ASIYE STORE");
        headerTitleView.setTextColor(LuxuryUiFactory.COLOR_GOLD);
        headerTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.5f);
        headerTitleView.setTypeface(Typeface.DEFAULT_BOLD);
        titles.addView(headerTitleView);

        headerSubtitleView = new TextView(this);
        headerSubtitleView.setText("ویترین زیبایی فوق‌لوکس");
        headerSubtitleView.setTextColor(LuxuryUiFactory.COLOR_PLATINUM);
        headerSubtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        titles.addView(headerSubtitleView);

        header.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        modeSwitchButton = LuxuryUiFactory.goldButton(this, "پنل مدیریت");
        modeSwitchButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        modeSwitchButton.setPadding(dp(12), dp(6), dp(12), dp(6));
        modeSwitchButton.setOnClickListener(v -> onToggleModeClicked());
        header.addView(modeSwitchButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return header;
    }

    private void onToggleModeClicked() {
        if (currentMode == EcosystemMode.CUSTOMER_STORE) {
            if (adminAuthenticated) {
                currentMode = EcosystemMode.ADMIN_SUITE;
                refreshUi();
            } else {
                showAdminLoginDialog();
            }
        } else {
            currentMode = EcosystemMode.CUSTOMER_STORE;
            refreshUi();
        }
    }

    private void showAdminLoginDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        int p = dp(18);
        box.setPadding(p, p, p, p);
        box.setBackgroundColor(LuxuryUiFactory.COLOR_WHITE);

        TextView info = new TextView(this);
        info.setText("ورود امن سطح دسترسی مدیریتی (RBAC)\nمشخصات پیش‌فرض مدیر کل:\nموبایل: ۰۹۱۲۰۰۰۰۰۰۰  |  رمز: Asiye@2026");
        info.setTextColor(LuxuryUiFactory.COLOR_MUTED_TEXT);
        info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        info.setGravity(Gravity.RIGHT);
        box.addView(info);

        EditText userField = LuxuryUiFactory.luxuryInput(this, "شماره موبایل یا ایمیل مدیر", false);
        userField.setText("09120000000");
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp1.topMargin = dp(12);
        box.addView(userField, lp1);

        EditText passField = LuxuryUiFactory.luxuryInput(this, "رمز عبور مدیریتی", false);
        passField.setText("Asiye@2026");
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.topMargin = dp(10);
        box.addView(passField, lp2);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("ورود به پنل مدیریت ASIYE STORE")
                .setView(box)
                .setPositiveButton("ورود به پنل ادمین", null)
                .setNegativeButton("انصراف", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button ok = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            ok.setOnClickListener(v -> {
                String u = PersianFormatter.toAsciiDigits(userField.getText().toString());
                String pw = passField.getText().toString().trim();
                if (("09120000000".equals(u) || "admin@asiyestore.ir".equalsIgnoreCase(u)) && pw.length() >= 6) {
                    adminAuthenticated = true;
                    currentMode = EcosystemMode.ADMIN_SUITE;
                    dialog.dismiss();
                    toast("خوش آمدید! وارد پنل مدیریت اجرایی شدید.");
                    refreshUi();
                } else {
                    toast("شماره موبایل یا رمز عبور مدیریتی معتبر نیست.");
                }
            });
        });
        dialog.show();
    }

    private void refreshUi() {
        if (currentMode == EcosystemMode.CUSTOMER_STORE) {
            headerTitleView.setText("ASIYE STORE");
            headerSubtitleView.setText("Your Beauty, Your Signature — فروشگاه لوکس");
            modeSwitchButton.setText("پنل مدیریت");
            buildCustomerBottomNav();
            renderCustomerScreen();
        } else {
            headerTitleView.setText("ASIYE ADMIN SUITE");
            headerSubtitleView.setText("پنل یکپارچه مدیریت فروش، سفارش و انبار");
            modeSwitchButton.setText("نمای فروشگاه");
            buildAdminBottomNav();
            renderAdminScreen();
        }
    }

    private void buildCustomerBottomNav() {
        bottomNavBar.removeAllViews();
        int cartQty = totalCartCount();
        String cartLabel = cartQty > 0
                ? "سبد (" + PersianFormatter.toPersianDigits(String.valueOf(cartQty)) + ")"
                : "سبد خرید";

        String[] tabs = new String[]{"ویترین", cartLabel, "سفارش‌ها", "باشگاه VIP"};
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            bottomNavBar.addView(createNavButton(tabs[i], customerTab == idx, () -> {
                customerTab = idx;
                refreshUi();
            }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
    }

    private void buildAdminBottomNav() {
        bottomNavBar.removeAllViews();
        String[] tabs = new String[]{"داشبورد", "محصولات", "سفارش‌ها", "انبارداری", "تنظیمات"};
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            bottomNavBar.addView(createNavButton(tabs[i], adminTab == idx, () -> {
                adminTab = idx;
                refreshUi();
            }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
    }

    private View createNavButton(String label, boolean selected, Runnable onClick) {
        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(dp(4), dp(6), dp(4), dp(6));
        if (selected) {
            tab.setBackground(LuxuryUiFactory.roundedRect(this, 0x26C6A16C, 10, LuxuryUiFactory.COLOR_GOLD, 1f));
        }

        View indicator = new View(this);
        indicator.setBackground(LuxuryUiFactory.roundedRect(
                this, selected ? LuxuryUiFactory.COLOR_GOLD : 0x00000000, 3, 0, 0));
        LinearLayout.LayoutParams indLp = new LinearLayout.LayoutParams(dp(22), dp(3));
        indLp.bottomMargin = dp(4);
        tab.addView(indicator, indLp);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        tv.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        tv.setTextColor(selected ? LuxuryUiFactory.COLOR_GOLD : LuxuryUiFactory.COLOR_PLATINUM);
        tv.setGravity(Gravity.CENTER);
        tab.addView(tv);

        tab.setOnClickListener(v -> onClick.run());
        return tab;
    }

    // =========================================================================
    // CUSTOMER STOREFRONT SCREENS
    // =========================================================================

    private void renderCustomerScreen() {
        bodyContainer.removeAllViews();
        switch (customerTab) {
            case 0:
                bodyContainer.addView(buildStorefrontCatalogScreen());
                break;
            case 1:
                bodyContainer.addView(buildCustomerCartCheckoutScreen());
                break;
            case 2:
                bodyContainer.addView(buildCustomerOrdersAndWishlistScreen());
                break;
            case 3:
                bodyContainer.addView(buildCustomerVipProfileScreen());
                break;
        }
    }

    private View buildStorefrontCatalogScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = createPageColumn();
        scroll.addView(container);

        // 1. Luxury Hero Banner
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        GradientDrawable heroBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF171717, 0xFF2B241C, 0xFF171717}
        );
        heroBg.setCornerRadius(dp(16));
        heroBg.setStroke(dp(1), LuxuryUiFactory.COLOR_GOLD);
        hero.setBackground(heroBg);
        hero.setPadding(dp(18), dp(18), dp(18), dp(18));

        TextView badge = LuxuryUiFactory.badge(this, "کلکسیون اختصاصی طلای ۲۴ عیار • ۲۰۲۶", 0x33C6A16C, LuxuryUiFactory.COLOR_SOFT_GOLD);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeLp.gravity = Gravity.RIGHT;
        hero.addView(badge, badgeLp);

        TextView heroTitle = new TextView(this);
        heroTitle.setText("زیبایی اصیل، امضای شخصی شما");
        heroTitle.setTextColor(LuxuryUiFactory.COLOR_WHITE);
        heroTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        heroTitle.setTypeface(Typeface.DEFAULT_BOLD);
        heroTitle.setGravity(Gravity.RIGHT);
        heroTitle.setPadding(0, dp(8), 0, dp(4));
        hero.addView(heroTitle);

        TextView heroSub = new TextView(this);
        heroSub.setText("خرید نقدی یا ۴ قسط بدون کارمزد اسنپ‌پی و تارا • ارسال رایگان سفارش‌های بالای ۳ میلیون تومان • کد هدیه: ASIYE20");
        heroSub.setTextColor(LuxuryUiFactory.COLOR_ROSE_BEIGE);
        heroSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        heroSub.setGravity(Gravity.RIGHT);
        hero.addView(heroSub);

        container.addView(hero, marginBottom(14));

        // 2. Search + Sort Row
        LinearLayout searchCard = LuxuryUiFactory.cardContainer(this, 12, 12);
        EditText searchBox = LuxuryUiFactory.luxuryInput(this, "جستجوی نام محصول، سرم، عطر یا کد SKU...", false);
        searchBox.setText(searchQuery);
        searchCard.addView(searchBox, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout sortRow = new LinearLayout(this);
        sortRow.setOrientation(LinearLayout.HORIZONTAL);
        sortRow.setGravity(Gravity.CENTER_VERTICAL);
        sortRow.setPadding(0, dp(8), 0, 0);

        String[][] sortOptions = {
                {"newest", "جدیدترین"},
                {"price_asc", "ارزان‌ترین"},
                {"price_desc", "لوکس‌ترین"},
                {"discount", "بیشترین تخفیف"}
        };
        for (String[] opt : sortOptions) {
            final String key = opt[0];
            Button chip = LuxuryUiFactory.outlineChip(this, opt[1], key.equals(sortMode));
            chip.setOnClickListener(v -> {
                sortMode = key;
                renderCustomerScreen();
            });
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.leftMargin = dp(6);
            sortRow.addView(chip, clp);
        }
        HorizontalScrollView sortScroll = new HorizontalScrollView(this);
        sortScroll.setHorizontalScrollBarEnabled(false);
        sortScroll.addView(sortRow);
        searchCard.addView(sortScroll);
        container.addView(searchCard, marginBottom(12));

        // 3. Category Filter Chips
        HorizontalScrollView catScroll = new HorizontalScrollView(this);
        catScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout catRow = new LinearLayout(this);
        catRow.setOrientation(LinearLayout.HORIZONTAL);
        String[] cats = {"همه", "مراقبت از پوست", "آرایش لب و صورت", "عطر و ادکلن لوکس", "مراقبت از مو"};
        for (String cat : cats) {
            Button cBtn = LuxuryUiFactory.outlineChip(this, cat, cat.equals(selectedCategory));
            cBtn.setOnClickListener(v -> {
                selectedCategory = cat;
                renderCustomerScreen();
            });
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.leftMargin = dp(8);
            catRow.addView(cBtn, clp);
        }
        catScroll.addView(catRow);
        container.addView(catScroll, marginBottom(14));

        // 4. Product List Container (live filtered)
        final LinearLayout productsHolder = new LinearLayout(this);
        productsHolder.setOrientation(LinearLayout.VERTICAL);
        container.addView(productsHolder);

        populateStorefrontProducts(productsHolder);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString();
                populateStorefrontProducts(productsHolder);
            }
        });

        return scroll;
    }

    private void populateStorefrontProducts(LinearLayout holder) {
        holder.removeAllViews();
        List<Models.Product> products = db.getProducts(true, selectedCategory, searchQuery, sortMode);
        if (products.isEmpty()) {
            LinearLayout empty = LuxuryUiFactory.cardContainer(this, 12, 24);
            TextView tv = new TextView(this);
            tv.setText("محصولی با این مشخصات در کلکسیون یافت نشد.");
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(LuxuryUiFactory.COLOR_MUTED_TEXT);
            empty.addView(tv);
            holder.addView(empty);
            return;
        }

        for (Models.Product product : products) {
            holder.addView(buildStorefrontProductCard(product), marginBottom(14));
        }
    }

    private View buildStorefrontProductCard(final Models.Product p) {
        LinearLayout card = LuxuryUiFactory.cardContainer(this, 14, 0);

        // Image Frame with Discount & Wishlist Badges
        FrameLayout imgFrame = new FrameLayout(this);
        ImageView img = new ImageView(this);
        img.setImageResource(resolveProductDrawable(p.imageKey));
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgFrame.addView(img, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(195)));

        if (p.discountPercent > 0) {
            TextView discBadge = LuxuryUiFactory.badge(
                    this,
                    PersianFormatter.toPersianDigits(String.valueOf(p.discountPercent)) + "٪ تخفیف ویژه",
                    LuxuryUiFactory.COLOR_BLACK,
                    LuxuryUiFactory.COLOR_GOLD
            );
            FrameLayout.LayoutParams dlp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP | Gravity.RIGHT);
            dlp.setMargins(dp(12), dp(12), dp(12), dp(12));
            imgFrame.addView(discBadge, dlp);
        }

        Button wishBtn = LuxuryUiFactory.outlineChip(this, p.wishlisted ? "♥ محبوب شما" : "♡ علاقه‌مندی", p.wishlisted);
        FrameLayout.LayoutParams wlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.LEFT);
        wlp.setMargins(dp(12), dp(12), dp(12), dp(12));
        wishBtn.setOnClickListener(v -> {
            p.wishlisted = !p.wishlisted;
            db.toggleWishlist(p.id, p.wishlisted);
            toast(p.wishlisted ? "به لیست علاقه‌مندی‌ها اضافه شد." : "از لیست علاقه‌مندی‌ها حذف شد.");
            renderCustomerScreen();
        });
        imgFrame.addView(wishBtn, wlp);

        imgFrame.setOnClickListener(v -> showProductDetailsDialog(p));
        card.addView(imgFrame);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(14), dp(12), dp(14), dp(14));

        TextView catTv = new TextView(this);
        catTv.setText(p.category + "  •  حجم: " + p.volume);
        catTv.setTextColor(LuxuryUiFactory.COLOR_GOLD_DARK);
        catTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        catTv.setTypeface(Typeface.DEFAULT_BOLD);
        catTv.setGravity(Gravity.RIGHT);
        info.addView(catTv);

        TextView titleTv = new TextView(this);
        titleTv.setText(p.nameFa);
        titleTv.setTextColor(LuxuryUiFactory.COLOR_BLACK);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.5f);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setGravity(Gravity.RIGHT);
        titleTv.setPadding(0, dp(3), 0, dp(4));
        info.addView(titleTv);

        TextView descTv = new TextView(this);
        descTv.setText(p.description);
        descTv.setTextColor(LuxuryUiFactory.COLOR_MUTED_TEXT);
        descTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        descTv.setMaxLines(2);
        descTv.setGravity(Gravity.RIGHT);
        info.addView(descTv);

        // Price Row
        LinearLayout priceRow = new LinearLayout(this);
        priceRow.setOrientation(LinearLayout.HORIZONTAL);
        priceRow.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        priceRow.setPadding(0, dp(10), 0, dp(6));

        TextView saleTv = new TextView(this);
        saleTv.setText(PersianFormatter.formatToman(p.salePrice));
        saleTv.setTextColor(LuxuryUiFactory.COLOR_BLACK);
        saleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        saleTv.setTypeface(Typeface.DEFAULT_BOLD);
        priceRow.addView(saleTv);

        if (p.originalPrice > p.salePrice) {
            TextView origTv = new TextView(this);
            origTv.setText("  (قبل: " + PersianFormatter.formatToman(p.originalPrice) + ")");
            origTv.setTextColor(LuxuryUiFactory.COLOR_PLATINUM);
            origTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            priceRow.addView(origTv);
        }
        info.addView(priceRow);

        // BNPL Installment Pill
        long perMonth = Math.round(p.salePrice / 4.0);
        TextView bnplTv = new TextView(this);
        bnplTv.setText("امکان خرید ۴ قسطه اسنپ‌پی / تارا: ماهی " + PersianFormatter.formatToman(perMonth));
        bnplTv.setTextColor(LuxuryUiFactory.COLOR_CHARCOAL);
        bnplTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        bnplTv.setBackground(LuxuryUiFactory.roundedRect(this, 0x22C6A16C, 8, LuxuryUiFactory.COLOR_GOLD, 1f));
        bnplTv.setPadding(dp(10), dp(6), dp(10), dp(6));
        bnplTv.setGravity(Gravity.RIGHT);
        info.addView(bnplTv, marginBottom(10));

        // Action Buttons Row
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button addBtn = LuxuryUiFactory.goldButton(this,
                p.stock > 0 ? "افزودن به سبد خرید" : "اتمام موجودی");
        addBtn.setEnabled(p.stock > 0);
        addBtn.setOnClickListener(v -> addToCart(p, p.volume));
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.4f);
        alp.leftMargin = dp(8);
        actions.addView(addBtn, alp);

        Button detailBtn = LuxuryUiFactory.darkButton(this, "بررسی تخصصی");
        detailBtn.setOnClickListener(v -> showProductDetailsDialog(p));
        actions.addView(detailBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        info.addView(actions);
        card.addView(info);
        return card;
    }

    private void showProductDetailsDialog(final Models.Product p) {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        box.setPadding(dp(18), dp(14), dp(18), dp(14));
        sv.addView(box);

        ImageView iv = new ImageView(this);
        iv.setImageResource(resolveProductDrawable(p.imageKey));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        box.addView(iv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(180)));

        TextView meta = new TextView(this);
        meta.setText("\nکد اصالت کالا (SKU): " + p.sku +
                "\nکشور سازنده: " + p.country +
                "\nحجم / وزن: " + p.volume +
                "\nموجودی قابل تحویل فوری: " + PersianFormatter.toPersianDigits(String.valueOf(p.stock)) + " عدد" +
                "\n\nتوضیحات تخصصی:\n" + p.description +
                "\n\nترکیبات موثر (Ingredients):\n" + (p.ingredients != null ? p.ingredients : "عصاره خالص طلا و ترکیبات گیاهی") +
                "\n\nروش مصرف توصیه‌شده:\n" + (p.usageMethod != null ? p.usageMethod : "طبق دستور روی بسته‌بندی"));
        meta.setTextColor(LuxuryUiFactory.COLOR_CHARCOAL);
        meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        meta.setGravity(Gravity.RIGHT);
        box.addView(meta);

        new AlertDialog.Builder(this)
                .setTitle(p.nameFa)
                .setView(sv)
                .setPositiveButton(p.stock > 0 ? "افزودن به سبد (" + PersianFormatter.formatToman(p.salePrice) + ")" : "ناموجود", (d, w) -> {
                    if (p.stock > 0) addToCart(p, p.volume);
                })
                .setNegativeButton("بستن", null)
                .show();
    }

    private void addToCart(Models.Product p, String variantLabel) {
        for (Models.CartItem item : cart) {
            if (item.product.id == p.id) {
                if (item.quantity + 1 > p.stock) {
                    toast("حداکثر موجودی قابل سفارش این محصول " + PersianFormatter.toPersianDigits(String.valueOf(p.stock)) + " عدد است.");
                    return;
                }
                item.quantity++;
                toast("تعداد «" + p.nameFa + "» در سبد افزایش یافت.");
                buildCustomerBottomNav();
                return;
            }
        }
        if (p.stock <= 0) {
            toast("موجودی این محصول به اتمام رسیده است.");
            return;
        }
        Models.CartItem ci = new Models.CartItem();
        ci.product = p;
        ci.selectedVariant = variantLabel != null ? variantLabel : "استاندارد";
        ci.quantity = 1;
        cart.add(ci);
        toast("«" + p.nameFa + "» به سبد خرید اضافه شد.");
        buildCustomerBottomNav();
    }

    private View buildCustomerCartCheckoutScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = createPageColumn();
        scroll.addView(container);

        container.addView(LuxuryUiFactory.sectionTitle(this, "سبد خرید هوشمند و تسویه اقساطی"));

        if (cart.isEmpty()) {
            LinearLayout empty = LuxuryUiFactory.cardContainer(this, 14, 28);
            TextView tv = new TextView(this);
            tv.setText("سبد خرید لوکس شما خالی است.\nاز ویترین فروشگاه محصولات مورد نظر خود را انتخاب نمایید.");
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(LuxuryUiFactory.COLOR_MUTED_TEXT);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            empty.addView(tv, marginBottom(14));

            Button goStore = LuxuryUiFactory.goldButton(this, "مشاهده ویترین محصولات لوکس");
            goStore.setOnClickListener(v -> {
                customerTab = 0;
                refreshUi();
            });
            empty.addView(goStore);
            container.addView(empty);
            return scroll;
        }

        long subtotal = 0L;
        for (int i = 0; i < cart.size(); i++) {
            final int idx = i;
            final Models.CartItem item = cart.get(i);
            subtotal += item.lineTotal();

            LinearLayout row = LuxuryUiFactory.cardContainer(this, 12, 12);
            TextView nameTv = new TextView(this);
            nameTv.setText(item.product.nameFa + " (" + item.selectedVariant + ")");
            nameTv.setTextColor(LuxuryUiFactory.COLOR_BLACK);
            nameTv.setTypeface(Typeface.DEFAULT_BOLD);
            nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            nameTv.setGravity(Gravity.RIGHT);
            row.addView(nameTv);

            TextView priceTv = new TextView(this);
            priceTv.setText("فی: " + PersianFormatter.formatToman(item.product.salePrice) +
                    "  |  جمع ردیف: " + PersianFormatter.formatToman(item.lineTotal()));
            priceTv.setTextColor(LuxuryUiFactory.COLOR_GOLD_DARK);
            priceTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            priceTv.setGravity(Gravity.RIGHT);
            priceTv.setPadding(0, dp(4), 0, dp(8));
            row.addView(priceTv);

            LinearLayout qtyBar = new LinearLayout(this);
            qtyBar.setOrientation(LinearLayout.HORIZONTAL);
            qtyBar.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

            Button plus = LuxuryUiFactory.outlineChip(this, "+ افزایش", false);
            plus.setOnClickListener(v -> {
                if (item.quantity + 1 <= item.product.stock) {
                    item.quantity++;
                    refreshUi();
                } else {
                    toast("بیش از موجودی انبار امکان‌پذیر نیست.");
                }
            });
            qtyBar.addView(plus);

            TextView qtyTv = new TextView(this);
            qtyTv.setText("  " + PersianFormatter.toPersianDigits(String.valueOf(item.quantity)) + " عدد  ");
            qtyTv.setTypeface(Typeface.DEFAULT_BOLD);
            qtyTv.setTextColor(LuxuryUiFactory.COLOR_BLACK);
            qtyBar.addView(qtyTv);

            Button minus = LuxuryUiFactory.outlineChip(this, "− کاهش / حذف", false);
            minus.setOnClickListener(v -> {
                if (item.quantity > 1) {
                    item.quantity--;
                } else {
                    cart.remove(idx);
                }
                refreshUi();
            });
            qtyBar.addView(minus);

            row.addView(qtyBar);
            container.addView(row, marginBottom(10));
        }

        // Coupon Box
        LinearLayout couponCard = LuxuryUiFactory.cardContainer(this, 12, 12);
        TextView cTitle = new TextView(this);
        cTitle.setText("کد تخفیف اختصاصی (پیشنهاد ویژه: ASIYE20 یا VIPGOLD)");
        cTitle.setTextColor(LuxuryUiFactory.COLOR_BLACK);
        cTitle.setTypeface(Typeface.DEFAULT_BOLD);
        cTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        couponCard.addView(cTitle, marginBottom(8));

        LinearLayout cRow = new LinearLayout(this);
        cRow.setOrientation(LinearLayout.HORIZONTAL);
        final EditText couponInput = LuxuryUiFactory.luxuryInput(this, "کد تخفیف را وارد کنید", false);
        couponInput.setText(activeCouponCode);
        LinearLayout.LayoutParams cilp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cilp.leftMargin = dp(8);
        cRow.addView(couponInput, cilp);

        final long finalSubtotal = subtotal;
        Button applyCoupon = LuxuryUiFactory.darkButton(this, "اعمال کد");
        applyCoupon.setOnClickListener(v -> {
            String entered = couponInput.getText().toString().trim().toUpperCase();
            long res = db.evaluateCouponDiscount(entered, finalSubtotal);
            if (res > 0) {
                activeCouponCode = entered;
                toast("کد تخفیف اعمال شد! سود شما: " + PersianFormatter.formatToman(res));
                refreshUi();
            } else if (res == -2L) {
                toast("حداقل مبلغ سبد خرید برای این کد رعایت نشده است.");
            } else {
                activeCouponCode = "";
                toast("کد تخفیف نامعتبر یا منقضی است.");
                refreshUi();
            }
        });
        cRow.addView(applyCoupon);
        couponCard.addView(cRow);
        container.addView(couponCard, marginBottom(12));

        // Shipping & Payment Method Selection
        LinearLayout optionsCard = LuxuryUiFactory.cardContainer(this, 12, 14);
        TextView shipHeader = new TextView(this);
        shipHeader.setText("روش ارسال مرسوله لوکس:");
        shipHeader.setTypeface(Typeface.DEFAULT_BOLD);
        shipHeader.setTextColor(LuxuryUiFactory.COLOR_BLACK);
        optionsCard.addView(shipHeader, marginBottom(6));

        final String[] shippingNames = {
                "پیک اختصاصی VIP (بسته‌بندی هدیه — ۹۵ هزار تومان)",
                "پست پیشتاز ویژه سراسر کشور (۶۵ هزار تومان)",
                "ارسال فوری اسنپ‌باکس (۱۱۰ هزار تومان)"
        };
        final long[] shippingCosts = {95_000L, 65_000L, 110_000L};

        Spinner shipSpinner = new Spinner(this);
        ArrayAdapter<String> shipAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, shippingNames);
        shipSpinner.setAdapter(shipAdapter);
        shipSpinner.setSelection(selectedShippingIdx);
        shipSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(pos -> {
            if (selectedShippingIdx != pos) {
                selectedShippingIdx = pos;
                refreshUi();
            }
        }));
        optionsCard.addView(shipSpinner, marginBottom(12));

        TextView payHeader = new TextView(this);
        payHeader.setText("انتخاب درگاه پرداخت یا خرید اقساطی (BNPL):");
        payHeader.setTypeface(Typeface.DEFAULT_BOLD);
        payHeader.setTextColor(LuxuryUiFactory.COLOR_BLACK);
        optionsCard.addView(payHeader, marginBottom(6));

        final String[] paymentLabels = {
                "درگاه امن زرین‌پال (پرداخت آنلاین شتاب)",
                "اسنپ‌پی (Snapp Pay — ۴ قسط ماهانه بدون کارمزد)",
                "اعتبار تارا (Tara — ۴ قسط اعتباری)",
                "درگاه مستقیم بانکی شاپرک"
        };
        final String[] paymentCodes = {"zarinpal", "snapp_pay", "tara", "direct_bank"};
        final int[] paymentInstallments = {1, 4, 4, 1};

        Spinner paySpinner = new Spinner(this);
        ArrayAdapter<String> payAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, paymentLabels);
        paySpinner.setAdapter(payAdapter);
        paySpinner.setSelection(selectedPaymentIdx);
        paySpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(pos -> {
            if (selectedPaymentIdx != pos) {
                selectedPaymentIdx = pos;
                refreshUi();
            }
        }));
        optionsCard.addView(paySpinner);
        container.addView(optionsCard, marginBottom(12));

        // Customer Recipient & Address Card
        LinearLayout addrCard = LuxuryUiFactory.cardContainer(this, 12, 14);
        final EditText nameInput = LuxuryUiFactory.luxuryInput(this, "نام و نام خانوادگی تحویل‌گیرنده", false);
        nameInput.setText(db.getSetting("customer_name", "مشتری ویژه ASIYE"));
        addrCard.addView(nameInput, marginBottom(8));

        final EditText mobileInput = LuxuryUiFactory.luxuryInput(this, "شماره موبایل (مثلا 09123456789)", true);
        mobileInput.setText(db.getSetting("customer_mobile", "09123456789"));
        addrCard.addView(mobileInput, marginBottom(8));

        final EditText addrInput = LuxuryUiFactory.luxuryInput(this, "آدرس دقیق پستی جهت ارسال VIP", false);
        addrInput.setText(db.getSetting("customer_address", "تهران، زعفرانیه، خیابان مقدس اردبیلی، پلاک ۱۸"));
        addrCard.addView(addrInput);
        container.addView(addrCard, marginBottom(12));

        // Final Invoice & Atomic Checkout Execution
        long evalDisc = db.evaluateCouponDiscount(activeCouponCode, subtotal);
        long discount = Math.max(0L, evalDisc);
        long afterDisc = Math.max(0L, subtotal - discount);
        long shipCost = afterDisc >= 3_000_000L ? 0L : shippingCosts[selectedShippingIdx];
        long payable = afterDisc + shipCost;
        int instCount = paymentInstallments[selectedPaymentIdx];

        LinearLayout summaryCard = LuxuryUiFactory.cardContainer(this, 14, 16);
        summaryCard.setBackground(LuxuryUiFactory.roundedRect(this, LuxuryUiFactory.COLOR_BLACK, 14, LuxuryUiFactory.COLOR_GOLD, 1.5f));

        StringBuilder inv = new StringBuilder();
        inv.append("جمع کل اقلام: ").append(PersianFormatter.formatToman(subtotal));
        if (discount > 0) {
            inv.append("\nتخفیف کوپن (").append(activeCouponCode).append("): ").append(PersianFormatter.formatToman(discount)).append(" −");
        }
        inv.append("\nهزینه ارسال: ").append(shipCost == 0 ? "رایگان (سفارش VIP بالای ۳ میلیون)" : PersianFormatter.formatToman(shipCost));
        inv.append("\n────────────────────────");
        inv.append("\nمبلغ نهایی قابل پرداخت: ").append(PersianFormatter.formatToman(payable));
        if (instCount > 1) {
            long each = Math.round((double) payable / instCount);
            inv.append("\nاقساط ").append(PersianFormatter.toPersianDigits(String.valueOf(instCount)))
               .append(" ماهه بدون سود: قسط اول امروز ").append(PersianFormatter.formatToman(each));
        }

        TextView invTv = new TextView(this);
        invTv.setText(inv.toString());
        invTv.setTextColor(LuxuryUiFactory.COLOR_WHITE);
        invTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        invTv.setGravity(Gravity.RIGHT);
        summaryCard.addView(invTv, marginBottom(14));

        Button payBtn = LuxuryUiFactory.goldButton(this,
                instCount > 1 ? "تایید و پرداخت قسط اول (" + PersianFormatter.formatToman(Math.round((double) payable / instCount)) + ")"
                              : "تکمیل خرید و پرداخت امن (" + PersianFormatter.formatToman(payable) + ")");
        payBtn.setOnClickListener(v -> {
            String cName = nameInput.getText().toString().trim();
            String cMob = PersianFormatter.toAsciiDigits(mobileInput.getText().toString());
            String cAddr = addrInput.getText().toString().trim();
            if (cName.length() < 3) {
                toast("لطفا نام کامل تحویل‌گیرنده را وارد کنید.");
                return;
            }
            if (!PersianFormatter.isValidIranMobile(cMob)) {
                toast("شماره موبایل باید ۱۱ رقم معتبر با پیش‌شماره 09 باشد.");
                return;
            }
            if (cAddr.length() < 10) {
                toast("لطفا آدرس دقیق پستی را وارد نمایید.");
                return;
            }

            db.setSetting("customer_name", cName);
            db.setSetting("customer_mobile", cMob);
            db.setSetting("customer_address", cAddr);

            try {
                Models.OrderRecord created = db.executeAtomicCheckout(
                        cart,
                        cName,
                        cMob,
                        cAddr,
                        shippingNames[selectedShippingIdx].split("\\(")[0].trim(),
                        shippingCosts[selectedShippingIdx],
                        paymentCodes[selectedPaymentIdx],
                        instCount,
                        activeCouponCode
                );
                cart.clear();
                activeCouponCode = "";
                customerTab = 2; // Jump straight to Orders & Tracking
                refreshUi();
                showOrderSuccessReceipt(created);
            } catch (IllegalStateException ex) {
                toast(ex.getMessage());
            }
        });
        summaryCard.addView(payBtn);
        container.addView(summaryCard, marginBottom(20));

        return scroll;
    }

    private void showOrderSuccessReceipt(Models.OrderRecord order) {
        if (order == null) return;
        new AlertDialog.Builder(this)
                .setTitle("سفارش لوکس شما ثبت شد!")
                .setMessage("شماره پیگیری اختصاصی: " + PersianFormatter.toPersianDigits(order.orderNumber) +
                        "\nمبلغ کل: " + PersianFormatter.formatToman(order.totalAmount) +
                        "\nروش پرداخت: " + PersianFormatter.paymentMethodLabelFa(order.paymentMethod) +
                        "\n\nموجودی انبار به صورت اتومیک کسر شد و امتیاز باشگاه مشتریان VIP به حساب شما افزوده گردید.")
                .setPositiveButton("مشاهده وضعیت مرسوله", null)
                .show();
    }

    private View buildCustomerOrdersAndWishlistScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = createPageColumn();
        scroll.addView(container);

        container.addView(LuxuryUiFactory.sectionTitle(this, "پیگیری لحظه‌ای سفارش‌ها"));

        List<Models.OrderRecord> orders = db.getOrders("all");
        for (Models.OrderRecord o : orders) {
            LinearLayout card = LuxuryUiFactory.cardContainer(this, 12, 14);

            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);

            TextView numTv = new TextView(this);
            numTv.setText("سفارش " + PersianFormatter.toPersianDigits(o.orderNumber));
            numTv.setTypeface(Typeface.DEFAULT_BOLD);
            numTv.setTextColor(LuxuryUiFactory.COLOR_BLACK);
            numTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            top.addView(numTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            top.addView(LuxuryUiFactory.badge(
                    this,
                    PersianFormatter.statusLabelFa(o.status),
                    PersianFormatter.statusColor(o.status),
                    LuxuryUiFactory.COLOR_WHITE));
            card.addView(top);

            StringBuilder details = new StringBuilder();
            details.append("تاریخ ثبت: ").append(o.createdAtLabel)
                   .append("\nمبلغ کل: ").append(PersianFormatter.formatToman(o.totalAmount))
                   .append("  |  ").append(PersianFormatter.paymentMethodLabelFa(o.paymentMethod));
            if (o.installmentsCount > 1) {
                details.append(" (").append(PersianFormatter.toPersianDigits(String.valueOf(o.installmentsCount))).append(" قسط)");
            }
            if (o.trackingCode != null && !o.trackingCode.isEmpty()) {
                details.append("\nکد رهگیری مرسوله: ").append(o.trackingCode);
            }
            details.append("\nاقلام سفارش:");
            for (Models.OrderItemRecord it : o.items) {
                details.append("\n • ").append(it.productName)
                       .append(" × ").append(PersianFormatter.toPersianDigits(String.valueOf(it.quantity)));
            }

            TextView detTv = new TextView(this);
            detTv.setText(details.toString());
            detTv.setTextColor(LuxuryUiFactory.COLOR_MUTED_TEXT);
            detTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            detTv.setGravity(Gravity.RIGHT);
            detTv.setPadding(0, dp(8), 0, 0);
            card.addView(detTv);

            container.addView(card, marginBottom(12));
        }

        // Wishlist Section
        container.addView(LuxuryUiFactory.sectionTitle(this, "لیست علاقه‌مندی‌های من"));
        List<Models.Product> wished = db.getWishlistedProducts();
        if (wished.isEmpty()) {
            LinearLayout wEmpty = LuxuryUiFactory.cardContainer(this, 12, 16);
            TextView tv = new TextView(this);
            tv.setText("هنوز محصولی به لیست علاقه‌مندی‌ها اضافه نکرده‌اید.");
            tv.setTextColor(LuxuryUiFactory.COLOR_MUTED_TEXT);
            tv.setGravity(Gravity.CENTER);
            wEmpty.addView(tv);
            container.addView(wEmpty);
        } else {
            for (Models.Product wp : wished) {
                LinearLayout wCard = LuxuryUiFactory.cardContainer(this, 12, 12);
                TextView wTitle = new TextView(this);
                wTitle.setText(wp.nameFa + " — " + PersianFormatter.formatToman(wp.salePrice));
                wTitle.setTypeface(Typeface.DEFAULT_BOLD);
                wTitle.setTextColor(LuxuryUiFactory.COLOR_BLACK);
                wCard.addView(wTitle, marginBottom(8));

                Button quickAdd = LuxuryUiFactory.goldButton(this, "افزودن سریع به سبد خرید");
                quickAdd.setOnClickListener(v -> addToCart(wp, wp.volume));
                wCard.addView(quickAdd);
                container.addView(wCard, marginBottom(10));
            }
        }

        return scroll;
    }

    private View buildCustomerVipProfileScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = createPageColumn();
        scroll.addView(container);

        // VIP Loyalty Card
        LinearLayout vipCard = LuxuryUiFactory.cardContainer(this, 16, 18);
        vipCard.setBackground(LuxuryUiFactory.roundedRect(this, LuxuryUiFactory.COLOR_BLACK, 16, LuxuryUiFactory.COLOR_GOLD, 1.5f));

        String pts = db.getSetting("loyalty_points", "480");
        TextView tierTv = new TextView(this);
        tierTv.setText("باشگاه مشتریان اختصاصی ASIYE SIGNATURE");
        tierTv.setTextColor(LuxuryUiFactory.COLOR_GOLD);
        tierTv.setTypeface(Typeface.DEFAULT_BOLD);
        tierTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        vipCard.addView(tierTv);

        TextView userTv = new TextView(this);
        userTv.setText("عضو ویژه: " + db.getSetting("customer_name", "مشتری ویژه") +
                "\nموبایل تاییدشده: " + PersianFormatter.toPersianDigits(db.getSetting("customer_mobile", "09120000000")) +
                "\nامتیاز وفاداری شما: " + PersianFormatter.toPersianDigits(pts) + " امتیاز طلایی");
        userTv.setTextColor(LuxuryUiFactory.COLOR_WHITE);
        userTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        userTv.setPadding(0, dp(8), 0, 0);
        vipCard.addView(userTv);
        container.addView(vipCard, marginBottom(14));

        // OTP Login / Verification Flow Card
        LinearLayout otpCard = LuxuryUiFactory.cardContainer(this, 12, 14);
        TextView otpTitle = new TextView(this);
        otpTitle.setText("احراز هویت پیامکی یک‌بارمصرف (OTP)");
        otpTitle.setTypeface(Typeface.DEFAULT_BOLD);
        otpTitle.setTextColor(LuxuryUiFactory.COLOR_BLACK);
        otpTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        otpCard.addView(otpTitle, marginBottom(8));

        final EditText mobField = LuxuryUiFactory.luxuryInput(this, "شماره موبایل ۱۱ رقمی (09...)", true);
        mobField.setText(db.getSetting("customer_mobile", "09123456789"));
        otpCard.addView(mobField, marginBottom(8));

        final EditText otpField = LuxuryUiFactory.luxuryInput(this, "کد تایید ۶ رقمی پیامک‌شده", true);
        otpCard.addView(otpField, marginBottom(10));

        LinearLayout otpBtns = new LinearLayout(this);
        otpBtns.setOrientation(LinearLayout.HORIZONTAL);

        Button sendOtpBtn = LuxuryUiFactory.darkButton(this, "ارسال کد تایید");
        sendOtpBtn.setOnClickListener(v -> {
            String mob = PersianFormatter.toAsciiDigits(mobField.getText().toString());
            if (!PersianFormatter.isValidIranMobile(mob)) {
                toast("شماره موبایل نامعتبر است (باید با 09 شروع شود و ۱۱ رقم باشد).");
                return;
            }
            lastGeneratedOtp = String.valueOf(100000 + new Random().nextInt(900000));
            otpField.setText(lastGeneratedOtp);
            toast("کد تایید ASIYE STORE: " + PersianFormatter.toPersianDigits(lastGeneratedOtp));
        });
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        sLp.leftMargin = dp(8);
        otpBtns.addView(sendOtpBtn, sLp);

        Button verifyBtn = LuxuryUiFactory.goldButton(this, "تایید و ورود");
        verifyBtn.setOnClickListener(v -> {
            String mob = PersianFormatter.toAsciiDigits(mobField.getText().toString());
            String code = PersianFormatter.toAsciiDigits(otpField.getText().toString());
            if (!lastGeneratedOtp.isEmpty() && lastGeneratedOtp.equals(code)) {
                db.setSetting("customer_mobile", mob);
                lastGeneratedOtp = "";
                toast("احراز هویت موبایل با موفقیت انجام شد!");
                refreshUi();
            } else {
                toast("ابتدا دکمه ارسال کد را بزنید و کد ۶ رقمی صحیح را وارد کنید.");
            }
        });
        otpBtns.addView(verifyBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        otpCard.addView(otpBtns);
        container.addView(otpCard, marginBottom(14));

        Button openAdmin = LuxuryUiFactory.darkButton(this, "ورود به پنل مدیریت اجرایی فروشگاه (Admin Suite)");
        openAdmin.setOnClickListener(v -> onToggleModeClicked());
        container.addView(openAdmin);

        return scroll;
    }

    // =========================================================================
    // EXECUTIVE ADMIN SUITE SCREENS
    // =========================================================================

    private void renderAdminScreen() {
        bodyContainer.removeAllViews();
        switch (adminTab) {
            case 0:
                bodyContainer.addView(buildAdminDashboardScreen());
                break;
            case 1:
                bodyContainer.addView(buildAdminProductsCrudScreen());
                break;
            case 2:
                bodyContainer.addView(buildAdminOrdersManagementScreen());
                break;
            case 3:
                bodyContainer.addView(buildAdminInventoryScreen());
                break;
            case 4:
                bodyContainer.addView(buildAdminSettingsAndSyncScreen());
                break;
        }
    }

    private View buildAdminDashboardScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = createPageColumn();
        scroll.addView(container);

        Models.DashboardSnapshot snap = db.getDashboardSnapshot();

        container.addView(LuxuryUiFactory.sectionTitle(this, "داشبورد فرماندهی ASIYE STORE"));

        // Responsive 2x2 KPI Grid
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(buildKpiCard("فروش تاییدشده کل", PersianFormatter.formatCompactMillions(snap.salesToday), LuxuryUiFactory.COLOR_GOLD),
                weightParams(1f, 8));
        row1.addView(buildKpiCard("سفارش‌های فعال", PersianFormatter.toPersianDigits(String.valueOf(snap.activeOrdersCount)) + " سفارش", 0xFF1D4ED8),
                weightParams(1f, 0));
        container.addView(row1, marginBottom(10));

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.addView(buildKpiCard("هشدار کسری انبار",
                        PersianFormatter.toPersianDigits(String.valueOf(snap.lowStockCount)) + " کالا",
                        snap.lowStockCount > 0 ? 0xFFB91C1C : 0xFF15803D),
                weightParams(1f, 8));
        row2.addView(buildKpiCard("سفارش اقساطی BNPL",
                        PersianFormatter.toPersianDigits(String.valueOf(snap.installmentOrdersCount)) + " پرونده",
                        0xFF15803D),
                weightParams(1f, 0));
        container.addView(row2, marginBottom(14));

        // 7-Day Revenue Native Canvas Chart
        LinearLayout chartCard = LuxuryUiFactory.cardContainer(this, 14, 14);
        TextView chartTitle = new TextView(this);
        chartTitle.setText("نمودار روند فروش ۷ روز اخیر (" + PersianFormatter.formatCompactMillions(snap.salesWeek) + ")");
        chartTitle.setTypeface(Typeface.DEFAULT_BOLD);
        chartTitle.setTextColor(LuxuryUiFactory.COLOR_BLACK);
        chartTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        chartCard.addView(chartTitle, marginBottom(8));

        SalesChartView chartView = new SalesChartView(this);
        chartView.setData(snap.sevenDayRevenue, snap.sevenDayLabels);
        chartCard.addView(chartView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(195)));
        container.addView(chartCard, marginBottom(14));

        // Quick Executive Actions
        LinearLayout quickRow = new LinearLayout(this);
        quickRow.setOrientation(LinearLayout.HORIZONTAL);
        Button addProdBtn = LuxuryUiFactory.goldButton(this, "+ تعریف محصول جدید");
        addProdBtn.setOnClickListener(v -> showProductEditorDialog(null));
        quickRow.addView(addProdBtn, weightParams(1f, 8));

        Button invBtn = LuxuryUiFactory.darkButton(this, "اصلاح موجودی انبار");
        invBtn.setOnClickListener(v -> {
            adminTab = 3;
            refreshUi();
        });
        quickRow.addView(invBtn, weightParams(1f, 0));
        container.addView(quickRow, marginBottom(14));

        // Low-Stock Alert List
        List<Models.Product> allProducts = db.getProducts(false, "همه", "", "newest");
        for (Models.Product p : allProducts) {
            if (p.stock <= p.lowStockThreshold) {
                LinearLayout alert = LuxuryUiFactory.cardContainer(this, 10, 12);
                alert.setBackground(LuxuryUiFactory.roundedRect(this, 0xFFFFF5F5, 10, 0xFFB91C1C, 1f));
                TextView tv = new TextView(this);
                tv.setText("هشدار موجودی بحرانی: «" + p.nameFa + "» فقط " +
                        PersianFormatter.toPersianDigits(String.valueOf(p.stock)) + " عدد در انبار باقی مانده است!");
                tv.setTextColor(0xFF991B1B);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
                alert.addView(tv);
                container.addView(alert, marginBottom(8));
            }
        }

        return scroll;
    }

    private View buildKpiCard(String label, String value, int accentColor) {
        LinearLayout card = LuxuryUiFactory.cardContainer(this, 12, 14);
        View bar = new View(this);
        bar.setBackground(LuxuryUiFactory.roundedRect(this, accentColor, 2, 0, 0));
        card.addView(bar, new LinearLayout.LayoutParams(dp(36), dp(4)));

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextColor(LuxuryUiFactory.COLOR_MUTED_TEXT);
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        lbl.setPadding(0, dp(8), 0, dp(4));
        card.addView(lbl);

        TextView val = new TextView(this);
        val.setText(value);
        val.setTextColor(LuxuryUiFactory.COLOR_BLACK);
        val.setTypeface(Typeface.DEFAULT_BOLD);
        val.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);
        card.addView(val);
        return card;
    }

    private View buildAdminProductsCrudScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = createPageColumn();
        scroll.addView(container);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = new TextView(this);
        t.setText("مدیریت کاتالوگ محصولات");
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        t.setTextColor(LuxuryUiFactory.COLOR_BLACK);
        head.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button createBtn = LuxuryUiFactory.goldButton(this, "+ محصول جدید");
        createBtn.setOnClickListener(v -> showProductEditorDialog(null));
        head.addView(createBtn);
        container.addView(head, marginBottom(12));

        List<Models.Product> products = db.getProducts(false, "همه", "", "newest");
        for (final Models.Product p : products) {
            LinearLayout card = LuxuryUiFactory.cardContainer(this, 12, 14);

            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);

            TextView nameTv = new TextView(this);
            nameTv.setText(p.nameFa);
            nameTv.setTypeface(Typeface.DEFAULT_BOLD);
            nameTv.setTextColor(LuxuryUiFactory.COLOR_BLACK);
            nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            top.addView(nameTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            top.addView(LuxuryUiFactory.badge(
                    this,
                    p.published ? "منتشرشده" : "پیش‌نویس / آرشیو",
                    p.published ? 0xFF15803D : 0xFF6B7280,
                    LuxuryUiFactory.COLOR_WHITE));
            card.addView(top);

            TextView sub = new TextView(this);
            sub.setText("کد SKU: " + p.sku + "  |  دسته: " + p.category +
                    "\nقیمت فروش: " + PersianFormatter.formatToman(p.salePrice) +
                    " (اصلی: " + PersianFormatter.formatToman(p.originalPrice) + ")" +
                    "\nموجودی انبار: " + PersianFormatter.toPersianDigits(String.valueOf(p.stock)) + " عدد");
            sub.setTextColor(LuxuryUiFactory.COLOR_MUTED_TEXT);
            sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            sub.setPadding(0, dp(6), 0, dp(10));
            card.addView(sub);

            LinearLayout btns = new LinearLayout(this);
            btns.setOrientation(LinearLayout.HORIZONTAL);

            Button editBtn = LuxuryUiFactory.goldButton(this, "ویرایش کامل");
            editBtn.setOnClickListener(v -> showProductEditorDialog(p));
            btns.addView(editBtn, weightParams(1f, 8));

            Button delBtn = LuxuryUiFactory.darkButton(this, "آرشیو / حذف امن");
            delBtn.setOnClickListener(v -> {
                String msg = db.archiveProductSafely(p.id);
                toast(msg);
                refreshUi();
            });
            btns.addView(delBtn, weightParams(1f, 0));

            card.addView(btns);
            container.addView(card, marginBottom(10));
        }

        return scroll;
    }

    private void showProductEditorDialog(final Models.Product existing) {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        int p = dp(16);
        box.setPadding(p, p, p, p);
        sv.addView(box);

        final EditText nameFaEt = LuxuryUiFactory.luxuryInput(this, "نام فارسی محصول (اجباری)", false);
        final EditText nameEnEt = LuxuryUiFactory.luxuryInput(this, "نام انگلیسی محصول", false);
        final EditText skuEt = LuxuryUiFactory.luxuryInput(this, "کد یکتای SKU (مثلا AS-LUX-700)", false);
        final EditText origEt = LuxuryUiFactory.luxuryInput(this, "قیمت اصلی به تومان (مثلا 2500000)", true);
        final EditText saleEt = LuxuryUiFactory.luxuryInput(this, "قیمت فروش با تخفیف به تومان", true);
        final EditText volEt = LuxuryUiFactory.luxuryInput(this, "حجم یا وزن (مثلا ۵۰ میلی‌لیتر)", false);
        final EditText stockEt = LuxuryUiFactory.luxuryInput(this, "موجودی اولیه انبار", true);
        final EditText descEt = LuxuryUiFactory.luxuryInput(this, "توضیحات تخصصی محصول", false);

        final String[] categories = {"مراقبت از پوست", "آرایش لب و صورت", "عطر و ادکلن لوکس", "مراقبت از مو"};
        final Spinner catSpinner = new Spinner(this);
        catSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        final String[] imgKeys = {"serum", "lipstick", "perfume"};
        final String[] imgLabels = {"تصویر سرم طلایی", "تصویر رژ لب مات", "تصویر عطر اختصاصی"};
        final Spinner imgSpinner = new Spinner(this);
        imgSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, imgLabels));

        final CheckBox pubCheck = new CheckBox(this);
        pubCheck.setText("نمایش فعال در ویترین فروشگاه");
        pubCheck.setChecked(true);

        if (existing != null) {
            nameFaEt.setText(existing.nameFa);
            nameEnEt.setText(existing.nameEn);
            skuEt.setText(existing.sku);
            origEt.setText(String.valueOf(existing.originalPrice));
            saleEt.setText(String.valueOf(existing.salePrice));
            volEt.setText(existing.volume);
            stockEt.setText(String.valueOf(existing.stock));
            stockEt.setEnabled(false); // Stock for existing products is adjusted via audited Inventory tab
            descEt.setText(existing.description);
            pubCheck.setChecked(existing.published);
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(existing.category)) catSpinner.setSelection(i);
            }
            for (int i = 0; i < imgKeys.length; i++) {
                if (imgKeys[i].equals(existing.imageKey)) imgSpinner.setSelection(i);
            }
        } else {
            skuEt.setText("AS-NEW-" + (100 + new Random().nextInt(899)));
            volEt.setText("۵۰ میلی‌لیتر");
            stockEt.setText("20");
        }

        box.addView(nameFaEt, marginBottom(8));
        box.addView(nameEnEt, marginBottom(8));
        box.addView(skuEt, marginBottom(8));
        box.addView(catSpinner, marginBottom(8));
        box.addView(imgSpinner, marginBottom(8));
        box.addView(origEt, marginBottom(8));
        box.addView(saleEt, marginBottom(8));
        box.addView(volEt, marginBottom(8));
        if (existing == null) box.addView(stockEt, marginBottom(8));
        box.addView(descEt, marginBottom(8));
        box.addView(pubCheck);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existing == null ? "افزودن محصول لوکس جدید" : "ویرایش محصول")
                .setView(sv)
                .setPositiveButton("ذخیره در کاتالوگ", null)
                .setNegativeButton("انصراف", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nFa = nameFaEt.getText().toString().trim();
            String sku = skuEt.getText().toString().trim();
            long orig = parseLongSafe(origEt.getText().toString(), -1);
            long sale = parseLongSafe(saleEt.getText().toString(), -1);
            int initStock = (int) parseLongSafe(stockEt.getText().toString(), 0);
            String desc = descEt.getText().toString().trim();

            if (nFa.isEmpty() || sku.isEmpty() || orig <= 0 || sale <= 0) {
                toast("لطفا نام فارسی، کد SKU و قیمت‌های معتبر را وارد کنید.");
                return;
            }
            if (desc.isEmpty()) desc = "محصول اختصاصی کلکسیون لوکس ASIYE STORE";

            String err = db.saveOrUpdateProduct(
                    existing != null ? existing.id : null,
                    sku,
                    nFa,
                    nameEnEt.getText().toString(),
                    categories[catSpinner.getSelectedItemPosition()],
                    desc,
                    "ایران / پاریس",
                    volEt.getText().toString(),
                    orig,
                    sale,
                    initStock,
                    pubCheck.isChecked(),
                    imgKeys[imgSpinner.getSelectedItemPosition()]
            );
            if (err != null) {
                toast(err);
            } else {
                dialog.dismiss();
                toast("محصول با موفقیت ذخیره شد و در ویترین بروز گردید.");
                refreshUi();
            }
        }));
        dialog.show();
    }

    private View buildAdminOrdersManagementScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = createPageColumn();
        scroll.addView(container);

        container.addView(LuxuryUiFactory.sectionTitle(this, "مدیریت سفارش‌ها و لجستیک"));

        HorizontalScrollView filterScroll = new HorizontalScrollView(this);
        filterScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout fRow = new LinearLayout(this);
        String[][] filters = {
                {"all", "همه سفارش‌ها"},
                {"paid", "پرداخت شده"},
                {"preparing", "در حال بسته‌بندی"},
                {"shipped", "ارسال شده"},
                {"delivered", "تحویل شده"},
                {"cancelled", "لغو شده"}
        };
        for (String[] f : filters) {
            final String code = f[0];
            Button b = LuxuryUiFactory.outlineChip(this, f[1], code.equals(adminOrderStatusFilter));
            b.setOnClickListener(v -> {
                adminOrderStatusFilter = code;
                renderAdminScreen();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = dp(8);
            fRow.addView(b, lp);
        }
        filterScroll.addView(fRow);
        container.addView(filterScroll, marginBottom(12));

        List<Models.OrderRecord> orders = db.getOrders(adminOrderStatusFilter);
        if (orders.isEmpty()) {
            LinearLayout empty = LuxuryUiFactory.cardContainer(this, 12, 20);
            TextView tv = new TextView(this);
            tv.setText("سفارشی در این وضعیت وجود ندارد.");
            tv.setGravity(Gravity.CENTER);
            empty.addView(tv);
            container.addView(empty);
            return scroll;
        }

        for (final Models.OrderRecord o : orders) {
            LinearLayout card = LuxuryUiFactory.cardContainer(this, 12, 14);
            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);

            TextView codeTv = new TextView(this);
            codeTv.setText(PersianFormatter.toPersianDigits(o.orderNumber) + " — " + o.customerName);
            codeTv.setTypeface(Typeface.DEFAULT_BOLD);
            codeTv.setTextColor(LuxuryUiFactory.COLOR_BLACK);
            codeTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            top.addView(codeTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            top.addView(LuxuryUiFactory.badge(
                    this,
                    PersianFormatter.statusLabelFa(o.status),
                    PersianFormatter.statusColor(o.status),
                    LuxuryUiFactory.COLOR_WHITE));
            card.addView(top);

            TextView meta = new TextView(this);
            meta.setText("موبایل: " + PersianFormatter.toPersianDigits(o.customerMobile) +
                    "\nآدرس: " + o.addressSummary +
                    "\nمبلغ: " + PersianFormatter.formatToman(o.totalAmount) + " (" + PersianFormatter.paymentMethodLabelFa(o.paymentMethod) + ")" +
                    "\nروش ارسال: " + o.shippingMethod +
                    (o.trackingCode != null && !o.trackingCode.isEmpty() ? "\nکد رهگیری پستی: " + o.trackingCode : ""));
            meta.setTextColor(LuxuryUiFactory.COLOR_MUTED_TEXT);
            meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            meta.setPadding(0, dp(6), 0, dp(10));
            card.addView(meta);

            Button manageBtn = LuxuryUiFactory.goldButton(this, "تغییر وضعیت و ثبت کد رهگیری");
            manageBtn.setOnClickListener(v -> showOrderStatusDialog(o));
            card.addView(manageBtn);

            container.addView(card, marginBottom(10));
        }

        return scroll;
    }

    private void showOrderStatusDialog(final Models.OrderRecord order) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        int p = dp(16);
        box.setPadding(p, p, p, p);

        final String[] statusCodes = {"paid", "confirmed", "preparing", "shipped", "delivered", "cancelled"};
        final String[] statusLabels = {
                "پرداخت شده",
                "تایید شده توسط کارشناس",
                "در حال بسته‌بندی VIP",
                "تحویل به پیک / پست",
                "تحویل شده به مشتری",
                "لغو سفارش (بازگشت خودکار موجودی به انبار)"
        };

        final Spinner stSpinner = new Spinner(this);
        stSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statusLabels));
        for (int i = 0; i < statusCodes.length; i++) {
            if (statusCodes[i].equals(order.status)) stSpinner.setSelection(i);
        }
        box.addView(stSpinner, marginBottom(10));

        final EditText trackingEt = LuxuryUiFactory.luxuryInput(this, "کد رهگیری پست یا پیک VIP (اختیاری)", false);
        trackingEt.setText(order.trackingCode);
        box.addView(trackingEt);

        new AlertDialog.Builder(this)
                .setTitle("مدیریت سفارش " + PersianFormatter.toPersianDigits(order.orderNumber))
                .setView(box)
                .setPositiveButton("بروزرسانی وضعیت", (d, w) -> {
                    String nextStatus = statusCodes[stSpinner.getSelectedItemPosition()];
                    String err = db.updateOrderStatus(order.id, nextStatus, trackingEt.getText().toString());
                    if (err != null) {
                        toast(err);
                    } else {
                        toast("وضعیت سفارش با موفقیت بروزرسانی شد.");
                        refreshUi();
                    }
                })
                .setNegativeButton("بستن", null)
                .show();
    }

    private View buildAdminInventoryScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = createPageColumn();
        scroll.addView(container);

        container.addView(LuxuryUiFactory.sectionTitle(this, "کنترل موجودی انبار و گردش کالا"));

        List<Models.Product> products = db.getProducts(false, "همه", "", "newest");
        for (final Models.Product p : products) {
            LinearLayout card = LuxuryUiFactory.cardContainer(this, 12, 12);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            TextView tvName = new TextView(this);
            tvName.setText(p.nameFa);
            tvName.setTypeface(Typeface.DEFAULT_BOLD);
            tvName.setTextColor(LuxuryUiFactory.COLOR_BLACK);
            info.addView(tvName);

            int stockColor = p.stock <= 0 ? 0xFFB91C1C : (p.stock <= p.lowStockThreshold ? 0xFFD97706 : 0xFF15803D);
            TextView tvStock = new TextView(this);
            tvStock.setText("موجودی فعلی: " + PersianFormatter.toPersianDigits(String.valueOf(p.stock)) + " عدد  |  SKU: " + p.sku);
            tvStock.setTextColor(stockColor);
            tvStock.setTypeface(Typeface.DEFAULT_BOLD);
            tvStock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            info.addView(tvStock);

            row.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            Button adjBtn = LuxuryUiFactory.goldButton(this, "ثبت گردش");
            adjBtn.setOnClickListener(v -> showInventoryAdjustDialog(p));
            row.addView(adjBtn);

            card.addView(row);
            container.addView(card, marginBottom(8));
        }

        container.addView(LuxuryUiFactory.sectionTitle(this, "لاگ حسابرسی گردش انبار (Stock Movements)"));
        List<Models.StockMovementRecord> movements = db.getStockMovements();
        for (Models.StockMovementRecord m : movements) {
            LinearLayout mCard = LuxuryUiFactory.cardContainer(this, 10, 10);
            String sign = m.deltaQuantity >= 0 ? "+" : "";
            TextView tv = new TextView(this);
            tv.setText(m.productName + "  (" + PersianFormatter.toPersianDigits(sign + m.deltaQuantity) + " عدد)" +
                    "\nعلت: " + m.reason + "  |  ثبت‌کننده: " + m.actor + "  |  " + m.createdAtLabel);
            tv.setTextColor(m.deltaQuantity >= 0 ? 0xFF15803D : 0xFF991B1B);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            mCard.addView(tv);
            container.addView(mCard, marginBottom(6));
        }

        return scroll;
    }

    private void showInventoryAdjustDialog(final Models.Product product) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        int p = dp(16);
        box.setPadding(p, p, p, p);

        final String[] types = {"in", "out", "adjustment"};
        final String[] labels = {"ورود کالا به انبار (+ افزایش)", "خروج کالا از انبار (− کاهش)", "انبارگردانی (تنظیم عدد دقیق موجودی)"};
        final Spinner typeSpinner = new Spinner(this);
        typeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));
        box.addView(typeSpinner, marginBottom(10));

        final EditText qtyEt = LuxuryUiFactory.luxuryInput(this, "تعداد (عدد صحیح مثبت)", true);
        qtyEt.setText("10");
        box.addView(qtyEt, marginBottom(10));

        final EditText reasonEt = LuxuryUiFactory.luxuryInput(this, "علت گردش انبار (مثلا محموله وارداتی جدید)", false);
        reasonEt.setText("شارژ محموله جدید انبار مرکزی");
        box.addView(reasonEt);

        new AlertDialog.Builder(this)
                .setTitle("تنظیم موجودی: " + product.nameFa)
                .setView(box)
                .setPositiveButton("ثبت اتومیک در انبار", (d, w) -> {
                    int qty = (int) parseLongSafe(qtyEt.getText().toString(), 0);
                    String reason = reasonEt.getText().toString().trim();
                    if (reason.length() < 3) {
                        toast("ثبت علت گردش انبار برای حسابرسی الزامی است.");
                        return;
                    }
                    String err = db.adjustInventory(product.id, types[typeSpinner.getSelectedItemPosition()], qty, reason);
                    if (err != null) {
                        toast(err);
                    } else {
                        toast("موجودی انبار و لاگ گردش کالا ثبت شد.");
                        refreshUi();
                    }
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    private View buildAdminSettingsAndSyncScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = createPageColumn();
        scroll.addView(container);

        // 1. Live Server Sync Card
        container.addView(LuxuryUiFactory.sectionTitle(this, "اتصال زنده به سرور بک‌اند لاراول (REST API)"));
        LinearLayout syncCard = LuxuryUiFactory.cardContainer(this, 12, 14);

        final EditText urlEt = LuxuryUiFactory.luxuryInput(this, "آدرس API سرور (مثلا https://asiyestore.ir/api/v1)", false);
        urlEt.setText(db.getSetting("api_base_url", "https://asiyestore.ir/api/v1"));
        syncCard.addView(urlEt, marginBottom(8));

        final EditText tokenEt = LuxuryUiFactory.luxuryInput(this, "توکن مدیریتی Bearer Sanctum (اختیاری)", false);
        tokenEt.setText(db.getSetting("api_bearer_token", ""));
        syncCard.addView(tokenEt, marginBottom(10));

        final TextView syncStatusTv = new TextView(this);
        syncStatusTv.setText("وضعیت فعلی: موتور پایگاه‌داده تراکنشی SQLite + آماده همگام‌سازی ابری");
        syncStatusTv.setTextColor(LuxuryUiFactory.COLOR_GOLD_DARK);
        syncStatusTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        syncCard.addView(syncStatusTv, marginBottom(10));

        Button syncBtn = LuxuryUiFactory.goldButton(this, "ذخیره و تست اتصال زنده با سرور");
        syncBtn.setOnClickListener(v -> {
            String u = urlEt.getText().toString().trim();
            String t = tokenEt.getText().toString().trim();
            db.setSetting("api_base_url", u);
            db.setSetting("api_bearer_token", t);
            syncStatusTv.setText("در حال برقراری ارتباط امن با " + u + " ...");
            apiSyncClient.syncCatalogFromBackend(u, t, db, (ok, msg, count) -> {
                syncStatusTv.setText(msg);
                toast(msg);
            });
        });
        syncCard.addView(syncBtn);
        container.addView(syncCard, marginBottom(16));

        // 2. Discount Coupon Generator
        container.addView(LuxuryUiFactory.sectionTitle(this, "مدیریت کدهای تخفیف و کمپین‌ها"));
        LinearLayout couponCard = LuxuryUiFactory.cardContainer(this, 12, 14);
        final EditText newCodeEt = LuxuryUiFactory.luxuryInput(this, "کد تخفیف جدید (مثلا GOLD30)", false);
        couponCard.addView(newCodeEt, marginBottom(8));

        final EditText newPctEt = LuxuryUiFactory.luxuryInput(this, "درصد تخفیف (مثلا 25)", true);
        couponCard.addView(newPctEt, marginBottom(8));

        final EditText newMinEt = LuxuryUiFactory.luxuryInput(this, "حداقل خرید به تومان (مثلا 1000000)", true);
        newMinEt.setText("800000");
        couponCard.addView(newMinEt, marginBottom(10));

        Button createCouponBtn = LuxuryUiFactory.darkButton(this, "+ ساخت کوپن تخفیف فعال");
        createCouponBtn.setOnClickListener(v -> {
            String code = newCodeEt.getText().toString().trim();
            int pct = (int) parseLongSafe(newPctEt.getText().toString(), 0);
            long minP = parseLongSafe(newMinEt.getText().toString(), 0);
            if (code.length() < 3 || pct <= 0 || pct > 90) {
                toast("کد تخفیف (حداقل ۳ حرف) و درصد معتبر (۱ تا ۹۰) وارد کنید.");
                return;
            }
            if (db.createCoupon(code, pct, 0, minP)) {
                toast("کوپن «" + code.toUpperCase() + "» ساخته شد و بلافاصله در سبد خرید فعال است!");
                refreshUi();
            } else {
                toast("این کد تخفیف قبلا ثبت شده است.");
            }
        });
        couponCard.addView(createCouponBtn, marginBottom(10));

        for (Models.CouponRecord c : db.getCoupons()) {
            TextView cItem = new TextView(this);
            String desc = c.discountPercent > 0
                    ? PersianFormatter.toPersianDigits(String.valueOf(c.discountPercent)) + "٪ تخفیف"
                    : PersianFormatter.formatToman(c.discountAmount);
            cItem.setText("• کد فعال: " + c.code + "  —  " + desc + " (حداقل خرید: " + PersianFormatter.formatToman(c.minPurchase) + ")");
            cItem.setTextColor(LuxuryUiFactory.COLOR_CHARCOAL);
            cItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            cItem.setPadding(0, dp(3), 0, dp(3));
            couponCard.addView(cItem);
        }
        container.addView(couponCard, marginBottom(20));

        return scroll;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private LinearLayout createPageColumn() {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        int p = dp(14);
        col.setPadding(p, p, p, dp(24));
        return col;
    }

    private LinearLayout.LayoutParams marginBottom(float dpVal) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(dpVal);
        return lp;
    }

    private LinearLayout.LayoutParams weightParams(float weight, float leftMarginDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        if (leftMarginDp > 0) lp.leftMargin = dp(leftMarginDp);
        return lp;
    }

    private int resolveProductDrawable(String key) {
        if ("lipstick".equals(key)) return R.drawable.img_lipstick;
        if ("perfume".equals(key)) return R.drawable.img_perfume;
        return R.drawable.img_serum;
    }

    private int totalCartCount() {
        int sum = 0;
        for (Models.CartItem i : cart) sum += i.quantity;
        return sum;
    }

    private long parseLongSafe(String raw, long fallback) {
        try {
            String clean = PersianFormatter.toAsciiDigits(raw).replaceAll("[^0-9-]", "");
            if (clean.isEmpty()) return fallback;
            return Long.parseLong(clean);
        } catch (Exception e) {
            return fallback;
        }
    }

    private int dp(float v) {
        return LuxuryUiFactory.dp(this, v);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private static final class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        interface OnSelected {
            void onPick(int position);
        }

        private final OnSelected callback;

        SimpleItemSelectedListener(OnSelected callback) {
            this.callback = callback;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            callback.onPick(position);
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
    }
}
