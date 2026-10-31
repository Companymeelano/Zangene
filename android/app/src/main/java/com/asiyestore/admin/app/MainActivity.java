package com.asiyestore.admin.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

/**
 * ASIYE STORE — Haute Beauté Imperial Ecosystem v3.0
 * Native RTL Luxury Storefront + AI Beauty Atelier + TTAC Authenticity + 4-BNPL + Gold Wallet + Executive Admin Suite
 */
public final class MainActivity extends Activity {

    // Haute Couture Palette (Obsidian, Champagne Gold, Warm Silk Ivory)
    private static final int COL_OBSIDIAN      = 0xFF0A0A0C;
    private static final int COL_OBSIDIAN_CARD = 0xFF17161B;
    private static final int COL_GOLD          = 0xFFC6A16C;
    private static final int COL_GOLD_LIGHT    = 0xFFE2C28E;
    private static final int COL_GOLD_DARK     = 0xFF9B7844;
    private static final int COL_IVORY_BG      = 0xFFFBF9F5;
    private static final int COL_CREAM_SOFT    = 0xFFF3ECE0;
    private static final int COL_CARD_WHITE    = 0xFFFFFFFF;
    private static final int COL_TEXT_MAIN     = 0xFF1F1E1C;
    private static final int COL_TEXT_MUTED    = 0xFF6B655D;
    private static final int COL_BORDER        = 0xFFE6DEC9;
    private static final int COL_SUCCESS       = 0xFF1C6B4A;
    private static final int COL_WARNING       = 0xFFA36A1E;
    private static final int COL_DANGER        = 0xFFA63232;

    private AsiyeDatabaseHelper db;

    // App Mode & Navigation State
    private boolean adminMode = false;
    private boolean adminAuthenticated = false;
    private int customerTab = 0; // 0=Boutique, 1=AI Atelier, 2=TTAC Authenticity, 3=Concierge Cart, 4=VIP Privée
    private int atelierSubTab = 0; // 0=AI Skin, 1=Lip Try-On, 2=Fragrance Sommelier
    private int adminTab = 0;    // 0=Dashboard, 1=Products, 2=Orders+Barcode, 3=FEFO Inventory, 4=Settings/Feeds

    // Storefront State
    private String selectedCategory = "همه";
    private String searchQuery = "";
    private String sortMode = "newest";
    private String adminOrderStatusFilter = "all";

    // AI Atelier Interactive State
    private int selectedSkinTypeIdx = 0;
    private int selectedSkinConcernIdx = 0;
    private int selectedSkinToneIdx = 0;
    private int selectedLipShadeIdx = 0;
    private int selectedFragranceMoodIdx = 0;
    private Models.Product verifiedAuthenticityProduct = null;
    private String lastVerifiedCode = "AS-AUTH-2026-9901";

    // Cart, Gift Concierge, Gold Wallet & BNPL State
    private final List<Models.CartItem> cart = new ArrayList<>();
    private String appliedCouponCode = "";
    private String selectedPaymentMethod = "snapp_pay";
    private int selectedShippingIndex = 0;
    private boolean useGoldWallet = true;
    private boolean giftVipVelvetBox = true;
    private boolean giftColdChain = true;
    private int selectedTissueScentIdx = 0;
    private String customGiftMessage = "";

    // Root Containers
    private LinearLayout contentContainer;
    private LinearLayout bottomNavContainer;
    private TextView modeSwitchBtn;
    private TextView headerSubtitle;
    private TextView headerWalletChip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window window = getWindow();
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(COL_OBSIDIAN);
            window.setNavigationBarColor(COL_OBSIDIAN);
            window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        db = new AsiyeDatabaseHelper(this);
        verifiedAuthenticityProduct = db.verifyAuthenticitySerial(lastVerifiedCode);

        // Pre-populate luxury signature serum in cart for instant showcase
        List<Models.Product> initial = db.getProducts(true, "همه", "", "newest");
        if (!initial.isEmpty()) {
            Models.CartItem seedCart = new Models.CartItem();
            seedCart.product = initial.get(0);
            seedCart.selectedVariant = initial.get(0).volume;
            seedCart.quantity = 1;
            cart.add(seedCart);
        }

        setContentView(buildRootShell());
        renderCurrentScreen();
    }

    private View buildRootShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(COL_IVORY_BG);

        // Obsidian Imperial Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundColor(COL_OBSIDIAN);
        header.setPadding(dp(14), dp(10), dp(14), dp(10));
        header.setElevation(dp(6));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout brandBox = new LinearLayout(this);
        brandBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams brandLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        brandBox.setLayoutParams(brandLp);

        ImageView logoImg = new ImageView(this);
        logoImg.setImageResource(R.drawable.img_logo_horizontal);
        logoImg.setScaleType(ImageView.ScaleType.FIT_START);
        logoImg.setAdjustViewBounds(true);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(142), dp(34));
        logoImg.setLayoutParams(logoLp);

        headerSubtitle = new TextView(this);
        headerSubtitle.setText("مزون زیبایی سلطنتی • اصالت TTAC • ۴ قسط بدون کارمزد");
        headerSubtitle.setTextColor(COL_GOLD_LIGHT);
        headerSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        headerSubtitle.setPadding(0, dp(2), 0, 0);

        brandBox.addView(logoImg);
        brandBox.addView(headerSubtitle);

        // Gold Wallet Chip
        headerWalletChip = new TextView(this);
        headerWalletChip.setTextColor(COL_GOLD_LIGHT);
        headerWalletChip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        headerWalletChip.setTypeface(Typeface.DEFAULT_BOLD);
        headerWalletChip.setPadding(dp(9), dp(6), dp(9), dp(6));
        headerWalletChip.setBackground(roundedShape(0x24C6A16C, dp(16), COL_GOLD, dp(1)));
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wLp.setMarginEnd(dp(6));
        headerWalletChip.setLayoutParams(wLp);
        headerWalletChip.setOnClickListener(v -> {
            if (!adminMode) {
                customerTab = 4;
                renderCurrentScreen();
            }
        });

        modeSwitchBtn = new TextView(this);
        modeSwitchBtn.setText("پنل مدیریت");
        modeSwitchBtn.setTextColor(COL_OBSIDIAN);
        modeSwitchBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        modeSwitchBtn.setTypeface(Typeface.DEFAULT_BOLD);
        modeSwitchBtn.setPadding(dp(11), dp(6), dp(11), dp(6));
        modeSwitchBtn.setBackground(roundedShape(COL_GOLD, dp(16), COL_GOLD_LIGHT, dp(1)));
        modeSwitchBtn.setOnClickListener(v -> handleModeToggle());

        topRow.addView(brandBox);
        topRow.addView(headerWalletChip);
        topRow.addView(modeSwitchBtn);
        header.addView(topRow);

        View goldHairline = new View(this);
        goldHairline.setBackgroundColor(COL_GOLD);
        goldHairline.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        scrollView.setLayoutParams(scrollLp);

        contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(dp(14), dp(12), dp(14), dp(24));
        scrollView.addView(contentContainer);

        bottomNavContainer = new LinearLayout(this);
        bottomNavContainer.setOrientation(LinearLayout.HORIZONTAL);
        bottomNavContainer.setBackgroundColor(COL_OBSIDIAN);
        bottomNavContainer.setPadding(dp(4), dp(7), dp(4), dp(9));
        bottomNavContainer.setElevation(dp(10));

        root.addView(header);
        root.addView(goldHairline);
        root.addView(scrollView);
        root.addView(bottomNavContainer);
        return root;
    }

    private void handleModeToggle() {
        if (adminMode) {
            adminMode = false;
            renderCurrentScreen();
            return;
        }
        if (adminAuthenticated) {
            adminMode = true;
            renderCurrentScreen();
            return;
        }
        showAdminAuthDialog();
    }

    private void showAdminAuthDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        box.setPadding(dp(20), dp(14), dp(20), dp(8));

        TextView hint = new TextView(this);
        hint.setText("احراز هویت مدیر ارشد (RBAC)\nموبایل پیش‌فرض: ۰۹۱۲۰۰۰۰۰۰۰  |  رمز عبور: Asiye@2026");
        hint.setTextColor(COL_TEXT_MUTED);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        box.addView(hint);

        EditText userField = createInput("موبایل یا ایمیل مدیر", "09120000000");
        EditText passField = createInput("رمز عبور امنیتی", "Asiye@2026");
        passField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        box.addView(space(dp(10)));
        box.addView(userField);
        box.addView(space(dp(8)));
        box.addView(passField);

        new AlertDialog.Builder(this)
                .setTitle("ورود به فرماندهی اجرایی ASIYE")
                .setView(box)
                .setPositiveButton("ورود ایمن", (d, w) -> {
                    String u = userField.getText().toString().trim();
                    String p = passField.getText().toString().trim();
                    if (("09120000000".equals(u) || "admin@asiyestore.ir".equalsIgnoreCase(u))
                            && "Asiye@2026".equals(p)) {
                        adminAuthenticated = true;
                        adminMode = true;
                        toast("خوش آمدید؛ پنل فرماندهی امپریال فعال شد.");
                        renderCurrentScreen();
                    } else {
                        toast("اطلاعات ورود مدیر نامعتبر است.");
                    }
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    private void renderCurrentScreen() {
        contentContainer.removeAllViews();
        long walletBal = db.getWalletBalance();
        headerWalletChip.setText("کیف طلایی: " + PersianFormatter.toPersianDigits(String.valueOf(walletBal / 1000)) + "هزار");

        if (!adminMode) {
            headerWalletChip.setVisibility(View.VISIBLE);
            modeSwitchBtn.setText("پنل مدیریت");
            headerSubtitle.setText("مزون زیبایی سلطنتی • اصالت TTAC • ۴ قسط بدون کارمزد");
            renderCustomerBottomNav();
            if (customerTab == 0) renderCustomerStorefront();
            else if (customerTab == 1) renderAiBeautyAtelier();
            else if (customerTab == 2) renderAuthenticityVerifier();
            else if (customerTab == 3) renderCustomerCartAndConcierge();
            else renderCustomerVipPrivee();
        } else {
            headerWalletChip.setVisibility(View.GONE);
            modeSwitchBtn.setText("نمای مزون مشتری");
            headerSubtitle.setText("فرماندهی اجرایی • انبار FEFO • بارکدخوان • فید ترب");
            renderAdminBottomNav();
            if (adminTab == 0) renderAdminDashboard();
            else if (adminTab == 1) renderAdminProducts();
            else if (adminTab == 2) renderAdminOrders();
            else if (adminTab == 3) renderAdminInventoryFefo();
            else renderAdminSettingsAndFeeds();
        }
    }

    private void renderCustomerBottomNav() {
        bottomNavContainer.removeAllViews();
        int cartQty = totalCartItems();
        String cartLabel = cartQty > 0
                ? "سبد (" + PersianFormatter.toPersianDigits(String.valueOf(cartQty)) + ")"
                : "سبد خرید";

        String[] labels = new String[]{"ویترین", "آتلیه AI", "اصالت TTAC", cartLabel, "الماس سیاه"};
        String[] icons  = new String[]{"✦", "◈", "✓", "❖", "♛"};

        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            bottomNavContainer.addView(buildNavTab(icons[i], labels[i], customerTab == i, () -> {
                customerTab = idx;
                renderCurrentScreen();
            }));
        }
    }

    private void renderAdminBottomNav() {
        bottomNavContainer.removeAllViews();
        String[] labels = new String[]{"داشبورد", "کاتالوگ", "سفارش/بارکد", "انبار FEFO", "فید و سرور"};
        String[] icons  = new String[]{"◈", "✦", "❖", "▣", "⚙"};

        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            bottomNavContainer.addView(buildNavTab(icons[i], labels[i], adminTab == i, () -> {
                adminTab = idx;
                renderCurrentScreen();
            }));
        }
    }

    private View buildNavTab(String symbol, String title, boolean selected, Runnable onClick) {
        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        tab.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tab.setPadding(dp(2), dp(4), dp(2), dp(4));
        if (selected) {
            tab.setBackground(roundedShape(0x28C6A16C, dp(12), COL_GOLD, dp(1)));
        }

        TextView sym = new TextView(this);
        sym.setText(symbol);
        sym.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        sym.setTextColor(selected ? COL_GOLD_LIGHT : 0xFFA39B8B);
        sym.setGravity(Gravity.CENTER);

        TextView lbl = new TextView(this);
        lbl.setText(title);
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        lbl.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        lbl.setTextColor(selected ? COL_GOLD_LIGHT : 0xFFA39B8B);
        lbl.setGravity(Gravity.CENTER);
        lbl.setSingleLine(true);

        tab.addView(sym);
        tab.addView(lbl);
        tab.setOnClickListener(v -> onClick.run());
        return tab;
    }

    // =========================================================================
    // CUSTOMER TAB 0: IMPERIAL BOUTIQUE STOREFRONT
    // =========================================================================

    private void renderCustomerStorefront() {
        // Full-Bleed Editorial Atelier Hero Card with Real Studio Banner Image
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(20), COL_GOLD, dp(1)));
        hero.setPadding(dp(14), dp(14), dp(14), dp(16));

        FrameLayout bannerFrame = new FrameLayout(this);
        ImageView heroImg = new ImageView(this);
        heroImg.setImageResource(R.drawable.img_hero_atelier);
        heroImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        heroImg.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(165)));
        bannerFrame.addView(heroImg);

        TextView editionPill = new TextView(this);
        editionPill.setText("IMPERIAL EDITION • HAUTE BEAUTÉ 2026");
        editionPill.setTextColor(COL_OBSIDIAN);
        editionPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        editionPill.setTypeface(Typeface.DEFAULT_BOLD);
        editionPill.setPadding(dp(10), dp(4), dp(10), dp(4));
        editionPill.setBackground(roundedShape(COL_GOLD_LIGHT, dp(14), COL_GOLD, 0));
        FrameLayout.LayoutParams epLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.RIGHT);
        epLp.setMargins(dp(10), dp(10), dp(10), dp(10));
        editionPill.setLayoutParams(epLp);
        bannerFrame.addView(editionPill);
        hero.addView(bannerFrame);

        TextView heroTitle = new TextView(this);
        heroTitle.setText("هنر جاودان زیبایی، در قاب طلای ۲۴ عیار و مخمل سیاه");
        heroTitle.setTextColor(0xFFFFFFFF);
        heroTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        heroTitle.setTypeface(Typeface.DEFAULT_BOLD);
        heroTitle.setPadding(0, dp(12), 0, dp(4));

        TextView heroDesc = new TextView(this);
        heroDesc.setText("مشاور پوست هوش مصنوعی • تست مجازی شید رژ لب • اصالت‌سنجی هولوگرام TTAC • ۴ قسط بدون کارمزد + ۵٪ کش‌بک طلایی آنی");
        heroDesc.setTextColor(0xFFD6CFC4);
        heroDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);

        LinearLayout heroBtnRow = new LinearLayout(this);
        heroBtnRow.setOrientation(LinearLayout.HORIZONTAL);
        heroBtnRow.setPadding(0, dp(12), 0, 0);

        Button aiBtn = goldButton("ورود به آتلیه هوش مصنوعی ✦", () -> {
            customerTab = 1;
            renderCurrentScreen();
        });
        aiBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f));

        Button authBtn = outlineButton("استعلام اصالت TTAC", () -> {
            customerTab = 2;
            renderCurrentScreen();
        });
        authBtn.setTextColor(COL_GOLD_LIGHT);
        authBtn.setBackground(roundedShape(0x1AC6A16C, dp(12), COL_GOLD, dp(1)));
        LinearLayout.LayoutParams abLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        abLp.setMarginStart(dp(8));
        authBtn.setLayoutParams(abLp);

        heroBtnRow.addView(aiBtn);
        heroBtnRow.addView(authBtn);

        hero.addView(heroTitle);
        hero.addView(heroDesc);
        hero.addView(heroBtnRow);
        contentContainer.addView(hero);
        contentContainer.addView(space(dp(12)));

        // Search & Sort Row
        LinearLayout filterCard = cardContainer();
        EditText searchInput = createInput("جستجو در نام محصول، کد SKU یا بچ‌کد...", searchQuery);
        Button searchBtn = goldButton("اعمال فیلتر", () -> {
            searchQuery = searchInput.getText().toString();
            renderCurrentScreen();
        });

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        sLp.setMarginEnd(dp(8));
        searchInput.setLayoutParams(sLp);
        searchRow.addView(searchInput);
        searchRow.addView(searchBtn);
        filterCard.addView(searchRow);

        HorizontalScrollView catScroll = new HorizontalScrollView(this);
        catScroll.setHorizontalScrollBarEnabled(false);
        catScroll.setPadding(0, dp(10), 0, 0);
        LinearLayout catRow = new LinearLayout(this);
        catRow.setOrientation(LinearLayout.HORIZONTAL);

        String[] categories = new String[]{"همه", "مراقبت سلولی پوست", "آرایش کوتور لب", "عطر و نیش پرفیوم", "الکسیر طلایی مو"};
        for (String cat : categories) {
            boolean active = cat.equals(selectedCategory);
            TextView chip = new TextView(this);
            chip.setText(cat);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            chip.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            chip.setTextColor(active ? COL_OBSIDIAN : COL_TEXT_MAIN);
            chip.setPadding(dp(13), dp(7), dp(13), dp(7));
            chip.setBackground(roundedShape(active ? COL_GOLD : COL_CREAM_SOFT, dp(18), COL_BORDER, dp(1)));
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cLp.setMarginEnd(dp(7));
            chip.setLayoutParams(cLp);
            chip.setOnClickListener(v -> {
                selectedCategory = cat;
                renderCurrentScreen();
            });
            catRow.addView(chip);
        }
        catScroll.addView(catRow);
        filterCard.addView(catScroll);
        contentContainer.addView(filterCard);
        contentContainer.addView(space(dp(12)));

        // Product Cards (All 6 Bespoke Studio Images!)
        List<Models.Product> products = db.getProducts(true, selectedCategory, searchQuery, sortMode);
        for (Models.Product p : products) {
            contentContainer.addView(buildCustomerProductCard(p));
            contentContainer.addView(space(dp(12)));
        }
    }

    private View buildCustomerProductCard(Models.Product p) {
        LinearLayout card = cardContainer();
        card.setPadding(0, 0, 0, dp(14));

        FrameLayout imgFrame = new FrameLayout(this);
        ImageView img = new ImageView(this);
        img.setImageResource(resolveProductImage(p.imageKey));
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(205)));
        imgFrame.addView(img);

        // Discount Badge
        if (p.discountPercent > 0) {
            TextView discBadge = new TextView(this);
            discBadge.setText(PersianFormatter.toPersianDigits(p.discountPercent + "٪ تخفیف مزون"));
            discBadge.setTextColor(0xFFFFFFFF);
            discBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            discBadge.setTypeface(Typeface.DEFAULT_BOLD);
            discBadge.setPadding(dp(10), dp(5), dp(10), dp(5));
            discBadge.setBackground(roundedShape(COL_OBSIDIAN, dp(14), COL_GOLD, dp(1)));
            FrameLayout.LayoutParams bLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.RIGHT);
            bLp.setMargins(dp(10), dp(10), dp(10), dp(10));
            discBadge.setLayoutParams(bLp);
            imgFrame.addView(discBadge);
        }

        // TTAC & Cold-Chain Authenticity Pill
        TextView authPill = new TextView(this);
        authPill.setText(p.coldChain ? "✓ اصالت TTAC • زنجیره سرد ۱۸°C" : "✓ اصالت هولوگرام TTAC");
        authPill.setTextColor(0xFFFFFFFF);
        authPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        authPill.setTypeface(Typeface.DEFAULT_BOLD);
        authPill.setPadding(dp(9), dp(4), dp(9), dp(4));
        authPill.setBackground(roundedShape(0xD91C6B4A, dp(12), 0x55FFFFFF, dp(1)));
        FrameLayout.LayoutParams apLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.RIGHT);
        apLp.setMargins(dp(10), dp(10), dp(10), dp(10));
        authPill.setLayoutParams(apLp);
        imgFrame.addView(authPill);

        // Wishlist Toggle
        TextView wishBtn = new TextView(this);
        wishBtn.setText(p.wishlisted ? "♥" : "♡");
        wishBtn.setTextColor(p.wishlisted ? COL_DANGER : COL_OBSIDIAN);
        wishBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        wishBtn.setGravity(Gravity.CENTER);
        wishBtn.setBackground(roundedShape(0xEEFFFFFF, dp(19), COL_BORDER, dp(1)));
        FrameLayout.LayoutParams wLp = new FrameLayout.LayoutParams(dp(38), dp(38), Gravity.TOP | Gravity.LEFT);
        wLp.setMargins(dp(10), dp(10), dp(10), dp(10));
        wishBtn.setLayoutParams(wLp);
        wishBtn.setOnClickListener(v -> {
            db.toggleWishlist(p.id, !p.wishlisted);
            renderCurrentScreen();
        });
        imgFrame.addView(wishBtn);

        card.addView(imgFrame);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(12), dp(14), 0);

        TextView catMeta = new TextView(this);
        catMeta.setText(p.category + "  •  بچ: " + p.batchCode + "  •  انقضا: " + p.expiryLabel);
        catMeta.setTextColor(COL_GOLD_DARK);
        catMeta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        catMeta.setTypeface(Typeface.DEFAULT_BOLD);

        TextView title = new TextView(this);
        title.setText(p.nameFa);
        title.setTextColor(COL_TEXT_MAIN);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(3), 0, dp(1));

        TextView subEn = new TextView(this);
        subEn.setText(p.nameEn);
        subEn.setTextColor(COL_TEXT_MUTED);
        subEn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);

        LinearLayout priceRow = new LinearLayout(this);
        priceRow.setOrientation(LinearLayout.HORIZONTAL);
        priceRow.setGravity(Gravity.CENTER_VERTICAL);
        priceRow.setPadding(0, dp(8), 0, dp(4));

        TextView salePrice = new TextView(this);
        salePrice.setText(PersianFormatter.formatToman(p.salePrice));
        salePrice.setTextColor(COL_TEXT_MAIN);
        salePrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.5f);
        salePrice.setTypeface(Typeface.DEFAULT_BOLD);
        priceRow.addView(salePrice);

        if (p.originalPrice > p.salePrice) {
            TextView oldPrice = new TextView(this);
            oldPrice.setText("  " + PersianFormatter.formatToman(p.originalPrice));
            oldPrice.setPaintFlags(oldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            oldPrice.setTextColor(COL_TEXT_MUTED);
            oldPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            priceRow.addView(oldPrice);
        }

        // BNPL + 5% Gold Cashback Strip
        long perInstallment = Math.round(p.salePrice / 4.0);
        long cashback = Math.round(p.salePrice * 0.05);
        TextView bnplStrip = new TextView(this);
        bnplStrip.setText("✦ ۴ قسط " + PersianFormatter.formatToman(perInstallment) + "  |  +" + PersianFormatter.formatToman(cashback) + " کش‌بک طلایی");
        bnplStrip.setTextColor(COL_SUCCESS);
        bnplStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        bnplStrip.setPadding(dp(10), dp(6), dp(10), dp(6));
        bnplStrip.setBackground(roundedShape(0xFFEAF5EF, dp(10), 0xFFBFE0CE, dp(1)));

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, dp(10), 0, 0);

        Button detailBtn = outlineButton("شناسنامه و اصالت", () -> showProductDetailsDialog(p));
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        dLp.setMarginEnd(dp(8));
        detailBtn.setLayoutParams(dLp);

        Button addBtn = goldButton(p.stock > 0 ? "افزودن تشریفاتی" : "ناموجود", () -> {
            if (p.stock <= 0) {
                toast("این محصول موقتاً ناموجود است.");
                return;
            }
            addToCart(p, p.volume, 1);
        });
        addBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.25f));

        btnRow.addView(detailBtn);
        btnRow.addView(addBtn);

        body.addView(catMeta);
        body.addView(title);
        body.addView(subEn);
        body.addView(priceRow);
        body.addView(bnplStrip);
        body.addView(btnRow);
        card.addView(body);

        return card;
    }

    private void showProductDetailsDialog(Models.Product p) {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        box.setPadding(dp(20), dp(14), dp(20), dp(10));
        sv.addView(box);

        TextView desc = new TextView(this);
        desc.setText(p.description + "\n\n" +
                "• ترکیبات کلیدی: " + p.ingredients + "\n" +
                "• نحوه مصرف: " + p.usageMethod + "\n" +
                "• مبدا فرمولاسیون: " + p.country + " (" + p.volume + ")\n" +
                "• کد اصالت هولوگرام TTAC: " + p.ttacSerial + "\n" +
                "• بچ‌کد آزمایشگاهی: " + p.batchCode + " (انقضا: " + p.expiryLabel + ")");
        desc.setTextColor(COL_TEXT_MAIN);
        desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        desc.setLineSpacing(dp(3), 1f);
        box.addView(desc);

        String[] variants = "lipstick".equals(p.imageKey)
                ? new String[]{"شامپاینی نود (NUDE-01)", "رز بژ مخملی (ROSE-02)", "یاقوت پاریسی (RUBY-03)", "توت سلطنتی (BERRY-04)"}
                : new String[]{p.volume, "نسخه کافره هدیه مخمل VIP"};

        Spinner varSpinner = new Spinner(this);
        varSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, variants));
        box.addView(space(dp(10)));
        box.addView(varSpinner);

        new AlertDialog.Builder(this)
                .setTitle(p.nameFa)
                .setView(sv)
                .setPositiveButton("افزودن به سبد تشریفاتی", (d, w) -> {
                    if (p.stock > 0) addToCart(p, (String) varSpinner.getSelectedItem(), 1);
                })
                .setNeutralButton("استعلام گواهی اصالت", (d, w) -> {
                    lastVerifiedCode = p.ttacSerial;
                    verifiedAuthenticityProduct = p;
                    customerTab = 2;
                    renderCurrentScreen();
                })
                .setNegativeButton("بستن", null)
                .show();
    }

    // =========================================================================
    // CUSTOMER TAB 1: AI BEAUTY ATELIER (SKIN AI + VIRTUAL LIP TRY-ON + SOMMELIER)
    // =========================================================================

    private void renderAiBeautyAtelier() {
        contentContainer.addView(sectionHeader("آتلیه هوشمند زیبایی ASIYE AI", "مشاور سلولی پوست • تست مجازی شید رژ لب • عطرشناس اختصاصی"));

        // Sub-tab selector
        LinearLayout subNav = new LinearLayout(this);
        subNav.setOrientation(LinearLayout.HORIZONTAL);
        subNav.setPadding(0, 0, 0, dp(12));
        String[] subTitles = new String[]{"مشاور پوست AI", "تست مجازی رژ لب", "عطرشناس سلطنتی"};
        for (int i = 0; i < subTitles.length; i++) {
            final int idx = i;
            boolean active = atelierSubTab == i;
            TextView tab = new TextView(this);
            tab.setText(subTitles[i]);
            tab.setGravity(Gravity.CENTER);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            tab.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            tab.setTextColor(active ? COL_OBSIDIAN : COL_GOLD_LIGHT);
            tab.setPadding(dp(8), dp(9), dp(8), dp(9));
            tab.setBackground(roundedShape(active ? COL_GOLD : COL_OBSIDIAN_CARD, dp(14), COL_GOLD, dp(1)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i < 2) lp.setMarginEnd(dp(6));
            tab.setLayoutParams(lp);
            tab.setOnClickListener(v -> {
                atelierSubTab = idx;
                renderCurrentScreen();
            });
            subNav.addView(tab);
        }
        contentContainer.addView(subNav);

        if (atelierSubTab == 0) renderSkinAiSubModule();
        else if (atelierSubTab == 1) renderLipTryOnSubModule();
        else renderFragranceSommelierSubModule();
    }

    private void renderSkinAiSubModule() {
        LinearLayout darkCard = new LinearLayout(this);
        darkCard.setOrientation(LinearLayout.VERTICAL);
        darkCard.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(18), COL_GOLD, dp(1)));
        darkCard.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView title = new TextView(this);
        title.setText("اسکن و نسخه سلولی هوش مصنوعی پوست");
        title.setTextColor(COL_GOLD_LIGHT);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        darkCard.addView(title);
        darkCard.addView(space(dp(10)));

        String[] skinTypes = new String[]{"پوست خشک و دهیدراته", "پوست مختلط (T-Zone)", "پوست نرمال تا حساس", "پوست نیازمند لیفتینگ"};
        String[] concerns = new String[]{"کدری و افت درخشش", "خطوط ظریف و افت کلاژن", "خشکی عمیق سد دفاعی"};

        Spinner typeSp = new Spinner(this);
        typeSp.setBackground(roundedShape(COL_CREAM_SOFT, dp(10), COL_GOLD, dp(1)));
        typeSp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, skinTypes));
        typeSp.setSelection(selectedSkinTypeIdx);

        Spinner concSp = new Spinner(this);
        concSp.setBackground(roundedShape(COL_CREAM_SOFT, dp(10), COL_GOLD, dp(1)));
        concSp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, concerns));
        concSp.setSelection(selectedSkinConcernIdx);

        darkCard.addView(typeSp);
        darkCard.addView(space(dp(8)));
        darkCard.addView(concSp);
        darkCard.addView(space(dp(10)));

        // Custom Canvas Skin Diagnostic Meters
        int hyd = selectedSkinTypeIdx == 0 ? 54 : (selectedSkinTypeIdx == 1 ? 68 : 76);
        int firm = selectedSkinConcernIdx == 1 ? 58 : 74;
        int rad = 91;

        AtelierCanvasViews.SkinDiagnosticBarsView gauge = new AtelierCanvasViews.SkinDiagnosticBarsView(this);
        gauge.setScores(hyd, firm, rad);
        gauge.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(138)));
        darkCard.addView(gauge);

        Button analyzeBtn = goldButton("به‌روزرسانی آنالیز هوش مصنوعی پوست", () -> {
            selectedSkinTypeIdx = typeSp.getSelectedItemPosition();
            selectedSkinConcernIdx = concSp.getSelectedItemPosition();
            toast("نسخه درمانی اختصاصی پوست شما بازمحاسبه شد.");
            renderCurrentScreen();
        });
        darkCard.addView(space(dp(8)));
        darkCard.addView(analyzeBtn);

        TextView rxBox = new TextView(this);
        rxBox.setText("✦ نسخه پیشنهادی صبح و شب شما:\n" +
                "۱. نوبت صبح: سرم طلایی احیاکننده ۲۴ عیار (افزایش درخشش و آبرسانی چندمولکولی)\n" +
                "۲. نوبت شب: کرم خاویار سیاه و پپتید طلایی (بازسازی سد لیپیدی و تحریک کلاژن)\n" +
                "• امتیاز ویژه مزون: خرید یک‌کلیکه پک کامل با کد تخفیف خودکار ASIYE20");
        rxBox.setTextColor(0xFFEAE3D5);
        rxBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        rxBox.setLineSpacing(dp(3), 1f);
        rxBox.setPadding(0, dp(12), 0, dp(10));
        darkCard.addView(rxBox);

        Button bundleBtn = goldButton("افزودن پک روتین صبح و شب + تخفیف ASIYE20", () -> {
            List<Models.Product> all = db.getProducts(true, "همه", "", "newest");
            for (Models.Product p : all) {
                if ("AS-SRM-001".equals(p.sku) || "AS-CRM-108".equals(p.sku)) {
                    addToCartSilent(p, p.volume, 1);
                }
            }
            appliedCouponCode = "ASIYE20";
            toast("پک روتین پوست + کوپن ۲۰٪ ASIYE20 به سبد اضافه شد!");
            customerTab = 3;
            renderCurrentScreen();
        });
        darkCard.addView(bundleBtn);
        contentContainer.addView(darkCard);
    }

    private void renderLipTryOnSubModule() {
        LinearLayout card = cardContainer();

        int[] skinColors = new int[]{0xFFF5E1D3, 0xFFE5C1A8, 0xFFC99B7B};
        String[] skinNames = new String[]{"پوست بلوری روشن", "گندمی طبیعی", "برنزه طلایی"};

        int[] lipColors = new int[]{0xFFC68B73, 0xFFB86B77, 0xFF9E1B32, 0xFF6A1F3B, 0xFFB3543E, 0xFFD97F6C};
        String[] lipNames = new String[]{
                "شامپاینی نود (NUDE-01)",
                "رز بژ مخملی (ROSE-02)",
                "یاقوت پاریسی (RUBY-03)",
                "توت سلطنتی (BERRY-04)",
                "تراکوتا ابریشمی (TERRA-05)",
                "هلویی طلایی (PEACH-06)"
        };

        AtelierCanvasViews.LipTryOnCanvasView lipCanvas = new AtelierCanvasViews.LipTryOnCanvasView(this);
        lipCanvas.updateLook(skinColors[selectedSkinToneIdx], lipColors[selectedLipShadeIdx], lipNames[selectedLipShadeIdx]);
        lipCanvas.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(200)));
        card.addView(lipCanvas);
        card.addView(space(dp(12)));

        TextView toneLbl = new TextView(this);
        toneLbl.setText("۱. انتخاب تناژ رنگ پوست شما:");
        toneLbl.setTextColor(COL_TEXT_MAIN);
        toneLbl.setTypeface(Typeface.DEFAULT_BOLD);
        toneLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        card.addView(toneLbl);
        card.addView(space(dp(6)));

        LinearLayout toneRow = new LinearLayout(this);
        toneRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < skinNames.length; i++) {
            final int idx = i;
            boolean active = selectedSkinToneIdx == i;
            TextView tChip = new TextView(this);
            tChip.setText(skinNames[i]);
            tChip.setGravity(Gravity.CENTER);
            tChip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            tChip.setTextColor(COL_TEXT_MAIN);
            tChip.setPadding(dp(8), dp(7), dp(8), dp(7));
            tChip.setBackground(roundedShape(skinColors[i], dp(12), active ? COL_OBSIDIAN : COL_BORDER, dp(active ? 2 : 1)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i < 2) lp.setMarginEnd(dp(6));
            tChip.setLayoutParams(lp);
            tChip.setOnClickListener(v -> {
                selectedSkinToneIdx = idx;
                renderCurrentScreen();
            });
            toneRow.addView(tChip);
        }
        card.addView(toneRow);
        card.addView(space(dp(12)));

        TextView shadeLbl = new TextView(this);
        shadeLbl.setText("۲. انتخاب شید مخملی رژ لب ASIYE Couture:");
        shadeLbl.setTextColor(COL_TEXT_MAIN);
        shadeLbl.setTypeface(Typeface.DEFAULT_BOLD);
        shadeLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        card.addView(shadeLbl);
        card.addView(space(dp(8)));

        HorizontalScrollView swatchScroll = new HorizontalScrollView(this);
        swatchScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout swatchRow = new LinearLayout(this);
        swatchRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < lipNames.length; i++) {
            final int idx = i;
            boolean active = selectedLipShadeIdx == i;
            TextView sw = new TextView(this);
            sw.setText(lipNames[i]);
            sw.setTextColor(0xFFFFFFFF);
            sw.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            sw.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            sw.setPadding(dp(12), dp(9), dp(12), dp(9));
            sw.setBackground(roundedShape(lipColors[i], dp(14), active ? COL_GOLD_LIGHT : 0x33FFFFFF, dp(active ? 3 : 1)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            sw.setLayoutParams(lp);
            sw.setOnClickListener(v -> {
                selectedLipShadeIdx = idx;
                renderCurrentScreen();
            });
            swatchRow.addView(sw);
        }
        swatchScroll.addView(swatchRow);
        card.addView(swatchScroll);
        card.addView(space(dp(14)));

        Button orderShadeBtn = goldButton("افزودن شید «" + lipNames[selectedLipShadeIdx] + "» به سبد", () -> {
            List<Models.Product> all = db.getProducts(true, "همه", "", "newest");
            for (Models.Product p : all) {
                if ("AS-LIP-042".equals(p.sku) && p.stock > 0) {
                    addToCart(p, lipNames[selectedLipShadeIdx], 1);
                    return;
                }
            }
        });
        card.addView(orderShadeBtn);
        contentContainer.addView(card);
    }

    private void renderFragranceSommelierSubModule() {
        LinearLayout darkCard = new LinearLayout(this);
        darkCard.setOrientation(LinearLayout.VERTICAL);
        darkCard.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(18), COL_GOLD, dp(1)));
        darkCard.setPadding(dp(16), dp(16), dp(16), dp(16));

        String[] moods = new String[]{
                "مجالس شبانه و امضای سلطنتی (گرم و کهربایی)",
                "جلسات رسمی و کاریزمای روزانه (گلی و چوبی)",
                "هدیه عاشقانه و کلکسیونی (رز دمشقی و مشک سفید)"
        };

        TextView head = new TextView(this);
        head.setText("عطرشناس اختصاصی و هرم بویایی نیش پرفیوم");
        head.setTextColor(COL_GOLD_LIGHT);
        head.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);
        head.setTypeface(Typeface.DEFAULT_BOLD);
        darkCard.addView(head);
        darkCard.addView(space(dp(10)));

        Spinner moodSp = new Spinner(this);
        moodSp.setBackground(roundedShape(COL_CREAM_SOFT, dp(10), COL_GOLD, dp(1)));
        moodSp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, moods));
        moodSp.setSelection(selectedFragranceMoodIdx);
        darkCard.addView(moodSp);
        darkCard.addView(space(dp(8)));

        Button matchBtn = outlineButton("کشف امضای بویایی من", () -> {
            selectedFragranceMoodIdx = moodSp.getSelectedItemPosition();
            renderCurrentScreen();
        });
        matchBtn.setTextColor(COL_GOLD_LIGHT);
        matchBtn.setBackground(roundedShape(0x22C6A16C, dp(12), COL_GOLD, dp(1)));
        darkCard.addView(matchBtn);
        darkCard.addView(space(dp(12)));

        TextView pyramid = new TextView(this);
        pyramid.setText("▲ نت آغازین (Top Notes): زعفران سلطنتی، ترنج کالابریا و فلفل صورتی\n\n" +
                "◆ نت میانی (Heart Notes): رز دمشقی مخملی، یاسمن شب‌بو و زنبق فلورانس\n\n" +
                "▼ نت پایانی (Base Notes): کهربای سیاه، چوب صندل میسور، وانیل ماداگاسکار و مشک سفید\n\n" +
                "• ماندگاری تضمین‌شده: ۲۴ ساعت روی پوست و ۷۲ ساعت روی الیاف ابریشم");
        pyramid.setTextColor(0xFFF3ECE0);
        pyramid.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        pyramid.setPadding(dp(12), dp(12), dp(12), dp(12));
        pyramid.setBackground(roundedShape(0x16FFFFFF, dp(12), COL_GOLD_DARK, dp(1)));
        darkCard.addView(pyramid);
        darkCard.addView(space(dp(12)));

        Button fullBottleBtn = goldButton("سفارش ادوپرفیوم ۱۰۰ میل ASIYE Signature", () -> {
            for (Models.Product p : db.getProducts(true, "همه", "", "newest")) {
                if ("AS-PRF-999".equals(p.sku)) { addToCart(p, p.volume, 1); break; }
            }
        });
        darkCard.addView(fullBottleBtn);
        darkCard.addView(space(dp(8)));

        Button discoveryBtn = outlineButton("سفارش پک تستر ۳×۵ میل Discovery با بازگشت وجه", () -> {
            for (Models.Product p : db.getProducts(true, "همه", "", "newest")) {
                if ("AS-DSC-777".equals(p.sku)) { addToCart(p, p.volume, 1); break; }
            }
        });
        discoveryBtn.setTextColor(COL_GOLD_LIGHT);
        discoveryBtn.setBackground(roundedShape(0x22C6A16C, dp(12), COL_GOLD, dp(1)));
        darkCard.addView(discoveryBtn);

        contentContainer.addView(darkCard);
    }

    // =========================================================================
    // CUSTOMER TAB 2: ANTI-COUNTERFEIT TTAC & HOLOGRAM CERTIFICATE VERIFIER
    // =========================================================================

    private void renderAuthenticityVerifier() {
        contentContainer.addView(sectionHeader("سامانه استعلام اصالت هولوگرام و کد رهگیری TTAC",
                "تضمین ۱۰۰٪ اصالت کالای اورجینال، بچ‌کد آزمایشگاهی و سلامت زنجیره سرد"));

        LinearLayout searchCard = cardContainer();
        EditText serialInput = createInput("کد هولوگرام، TTAC یا SKU محصول...", lastVerifiedCode);

        HorizontalScrollView sampleScroll = new HorizontalScrollView(this);
        sampleScroll.setHorizontalScrollBarEnabled(false);
        sampleScroll.setPadding(0, dp(8), 0, dp(10));
        LinearLayout sampleRow = new LinearLayout(this);
        sampleRow.setOrientation(LinearLayout.HORIZONTAL);
        String[] sampleCodes = new String[]{"AS-AUTH-2026-9901", "TTAC-8849201140", "AS-AUTH-2026-9902", "AS-AUTH-2026-9905"};
        for (String sc : sampleCodes) {
            TextView chip = new TextView(this);
            chip.setText(sc);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            chip.setTextColor(COL_OBSIDIAN);
            chip.setPadding(dp(10), dp(5), dp(10), dp(5));
            chip.setBackground(roundedShape(COL_CREAM_SOFT, dp(12), COL_GOLD, dp(1)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(6));
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                lastVerifiedCode = sc;
                verifiedAuthenticityProduct = db.verifyAuthenticitySerial(sc);
                renderCurrentScreen();
            });
            sampleRow.addView(chip);
        }
        sampleScroll.addView(sampleRow);

        Button verifyBtn = goldButton("استعلام آنی گواهی اصالت محصول", () -> {
            lastVerifiedCode = serialInput.getText().toString().trim();
            verifiedAuthenticityProduct = db.verifyAuthenticitySerial(lastVerifiedCode);
            if (verifiedAuthenticityProduct == null) {
                toast("هشدار: این کد در سامانه اصالت ASIYE یافت نشد!");
            } else {
                toast("اصالت محصول با موفقیت تایید شد ✓");
            }
            renderCurrentScreen();
        });

        searchCard.addView(serialInput);
        searchCard.addView(sampleScroll);
        searchCard.addView(verifyBtn);
        contentContainer.addView(searchCard);
        contentContainer.addView(space(dp(12)));

        // Gold-Framed Holographic Digital Certificate Card
        if (verifiedAuthenticityProduct != null) {
            Models.Product vp = verifiedAuthenticityProduct;
            LinearLayout cert = new LinearLayout(this);
            cert.setOrientation(LinearLayout.VERTICAL);
            cert.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(18), COL_GOLD, dp(2)));
            cert.setPadding(dp(18), dp(18), dp(18), dp(18));

            TextView seal = new TextView(this);
            seal.setText("✦ CERTIFICATE OF HAUTE BEAUTÉ AUTHENTICITY ✦");
            seal.setTextColor(COL_GOLD_LIGHT);
            seal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            seal.setTypeface(Typeface.DEFAULT_BOLD);
            seal.setGravity(Gravity.CENTER);

            TextView okTitle = new TextView(this);
            okTitle.setText("✓ اصالت کالا و سلامت زنجیره سرد تایید شد");
            okTitle.setTextColor(0xFF6EE7B7);
            okTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            okTitle.setTypeface(Typeface.DEFAULT_BOLD);
            okTitle.setGravity(Gravity.CENTER);
            okTitle.setPadding(0, dp(8), 0, dp(10));

            TextView details = new TextView(this);
            details.setText("• نام محصول: " + vp.nameFa + "\n" +
                    "• شماره هولوگرام / TTAC: " + vp.ttacSerial + "\n" +
                    "• بچ‌کد تولید (Batch Lot): " + vp.batchCode + "\n" +
                    "• تاریخ انقضا: " + vp.expiryLabel + "\n" +
                    "• مبدا تامین: " + vp.country + "\n" +
                    "• پایش دمایی انبار: نگهداری در دمای کنترل‌شده ۱۸ درجه سانتی‌گراد");
            details.setTextColor(0xFFF3ECE0);
            details.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            details.setLineSpacing(dp(4), 1f);

            cert.addView(seal);
            cert.addView(okTitle);
            cert.addView(details);
            contentContainer.addView(cert);
        }

        // Minted Buyer Ownership Certificates Vault
        contentContainer.addView(space(dp(14)));
        contentContainer.addView(sectionHeader("گاوصندوق گواهی‌های اصالت صادرشده به نام شما",
                "با هر خرید موفق، شناسنامه دیجیتال اصالت به نام شما ثبت می‌شود"));
        for (Models.CertificateRecord cr : db.getCertificates()) {
            LinearLayout cRow = cardContainer();
            TextView tv = new TextView(this);
            tv.setText("♛ کد گواهی مالکیت: " + cr.certCode + "\n" +
                    cr.productName + "  |  بچ: " + cr.batchCode + "\n" +
                    "خریدار: " + cr.ownerName + "  •  " + cr.issuedAtLabel);
            tv.setTextColor(COL_TEXT_MAIN);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            cRow.addView(tv);
            contentContainer.addView(cRow);
            contentContainer.addView(space(dp(8)));
        }
    }

    // =========================================================================
    // CUSTOMER TAB 3: CONCIERGE CART + GIFT ATELIER + GOLD WALLET + 4-BNPL HUB
    // =========================================================================

    private void renderCustomerCartAndConcierge() {
        contentContainer.addView(sectionHeader("سبد تشریفاتی و کانسیرژ هدیه VIP",
                "بسته‌بندی مخمل مشکی با مهر موم طلایی AS + کش‌بک طلایی + ۴ درگاه اقساطی"));

        if (cart.isEmpty()) {
            LinearLayout empty = cardContainer();
            empty.setGravity(Gravity.CENTER);
            TextView msg = new TextView(this);
            msg.setText("سبد تشریفاتی شما در حال حاضر خالی است.");
            msg.setTextColor(COL_TEXT_MUTED);
            msg.setGravity(Gravity.CENTER);
            msg.setPadding(0, dp(16), 0, dp(14));
            Button goShop = goldButton("مشاهده کالکشن سلطنتی", () -> {
                customerTab = 0;
                renderCurrentScreen();
            });
            empty.addView(msg);
            empty.addView(goShop);
            contentContainer.addView(empty);
            return;
        }

        for (int i = 0; i < cart.size(); i++) {
            final int idx = i;
            Models.CartItem item = cart.get(i);
            LinearLayout row = cardContainer();
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            ImageView thumb = new ImageView(this);
            thumb.setImageResource(resolveProductImage(item.product.imageKey));
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(dp(64), dp(64));
            tLp.setMarginEnd(dp(10));
            thumb.setLayoutParams(tLp);
            row.addView(thumb);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView name = new TextView(this);
            name.setText(item.product.nameFa);
            name.setTextColor(COL_TEXT_MAIN);
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
            name.setTypeface(Typeface.DEFAULT_BOLD);

            TextView variant = new TextView(this);
            variant.setText("نسخه: " + item.selectedVariant + "  •  " + PersianFormatter.formatToman(item.lineTotal()));
            variant.setTextColor(COL_GOLD_DARK);
            variant.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);

            info.addView(name);
            info.addView(variant);
            row.addView(info);

            LinearLayout qtyBox = new LinearLayout(this);
            qtyBox.setOrientation(LinearLayout.HORIZONTAL);
            qtyBox.setGravity(Gravity.CENTER_VERTICAL);

            TextView plus = miniCircleBtn("+", () -> {
                if (item.quantity + 1 > item.product.stock) {
                    toast("بیش از موجودی انبار مجاز نیست.");
                    return;
                }
                item.quantity++;
                renderCurrentScreen();
            });
            TextView qtyTxt = new TextView(this);
            qtyTxt.setText(PersianFormatter.toPersianDigits(String.valueOf(item.quantity)));
            qtyTxt.setTextColor(COL_TEXT_MAIN);
            qtyTxt.setTypeface(Typeface.DEFAULT_BOLD);
            qtyTxt.setPadding(dp(8), 0, dp(8), 0);
            TextView minus = miniCircleBtn("−", () -> {
                if (item.quantity > 1) item.quantity--;
                else cart.remove(idx);
                renderCurrentScreen();
            });

            qtyBox.addView(plus);
            qtyBox.addView(qtyTxt);
            qtyBox.addView(minus);
            row.addView(qtyBox);

            contentContainer.addView(row);
            contentContainer.addView(space(dp(8)));
        }

        // White-Glove Velvet Gift Concierge & Cold-Chain Card (with real studio photo!)
        LinearLayout giftCard = cardContainer();
        giftCard.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(16), COL_GOLD, dp(1)));

        LinearLayout giftTop = new LinearLayout(this);
        giftTop.setOrientation(LinearLayout.HORIZONTAL);
        giftTop.setGravity(Gravity.CENTER_VERTICAL);

        ImageView giftImg = new ImageView(this);
        giftImg.setImageResource(R.drawable.img_vip_gift_box);
        giftImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams gLp = new LinearLayout.LayoutParams(dp(74), dp(74));
        gLp.setMarginEnd(dp(12));
        giftImg.setLayoutParams(gLp);

        TextView giftHead = new TextView(this);
        giftHead.setText("تشریفات هدیه سلطنتی ASIYE (رایگان)\nجعبه مخمل مشکی با مهر موم طلایی AS + ۲ سمپل رایگان نیش‌پرفیوم");
        giftHead.setTextColor(COL_GOLD_LIGHT);
        giftHead.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        giftHead.setTypeface(Typeface.DEFAULT_BOLD);
        giftHead.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        giftTop.addView(giftImg);
        giftTop.addView(giftHead);
        giftCard.addView(giftTop);
        giftCard.addView(space(dp(8)));

        CheckBox velvetChk = new CheckBox(this);
        velvetChk.setText("ارسال در جعبه هدیه مخمل مشکی با روبان ابریشمی طلاکوب");
        velvetChk.setTextColor(0xFFF3ECE0);
        velvetChk.setChecked(giftVipVelvetBox);
        velvetChk.setOnCheckedChangeListener((b, c) -> giftVipVelvetBox = c);

        CheckBox coldChk = new CheckBox(this);
        coldChk.setText("بسته‌بندی ایزوله دمایی زنجیره سرد (محافظت سرم‌ها و عطر در برابر گرما)");
        coldChk.setTextColor(0xFFF3ECE0);
        coldChk.setChecked(giftColdChain);
        coldChk.setOnCheckedChangeListener((b, c) -> giftColdChain = c);

        String[] tissueScents = new String[]{
                "رایحه کاغذ ابریشمی: رز دمشقی و عود سلطنتی",
                "رایحه کاغذ ابریشمی: وانیل ماداگاسکار و کهربا",
                "رایحه کاغذ ابریشمی: ترنج سفید پاریسی",
                "بدون رایحه (مخصوص افراد حساس)"
        };
        Spinner scentSp = new Spinner(this);
        scentSp.setBackground(roundedShape(COL_CREAM_SOFT, dp(10), COL_GOLD, dp(1)));
        scentSp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tissueScents));
        scentSp.setSelection(selectedTissueScentIdx);

        EditText giftMsgInput = createInput("متن اختصاصی کارت پستال طلاکوب هدیه (اختیاری)...", customGiftMessage);

        giftCard.addView(velvetChk);
        giftCard.addView(coldChk);
        giftCard.addView(space(dp(6)));
        giftCard.addView(scentSp);
        giftCard.addView(space(dp(8)));
        giftCard.addView(giftMsgInput);
        contentContainer.addView(giftCard);
        contentContainer.addView(space(dp(10)));

        // Coupon + Gold Cashback Wallet Card
        long subtotal = calculateCartSubtotal();
        long couponDisc = appliedCouponCode.isEmpty() ? 0L : Math.max(0L, db.evaluateCouponDiscount(appliedCouponCode, subtotal));
        long walletBalance = db.getWalletBalance();
        long walletDeduction = useGoldWallet ? Math.min(walletBalance, Math.max(0L, subtotal - couponDisc)) : 0L;
        long afterDiscounts = Math.max(0L, subtotal - couponDisc - walletDeduction);

        LinearLayout loyaltyCard = cardContainer();
        EditText couponInput = createInput("کد تخفیف (ASIYE20 ، VIPGOLD یا REFILL10)", appliedCouponCode);
        Button applyCouponBtn = outlineButton("اعمال کد", () -> {
            String code = couponInput.getText().toString().trim().toUpperCase();
            long eval = db.evaluateCouponDiscount(code, subtotal);
            if (eval == -1L) toast("کد تخفیف معتبر نیست.");
            else if (eval == -2L) toast("حداقل خرید این کوپن رعایت نشده است.");
            else {
                appliedCouponCode = code;
                toast("تخفیف " + PersianFormatter.formatToman(eval) + " اعمال شد.");
                renderCurrentScreen();
            }
        });

        LinearLayout cRow = new LinearLayout(this);
        cRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams ciLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ciLp.setMarginEnd(dp(8));
        couponInput.setLayoutParams(ciLp);
        cRow.addView(couponInput);
        cRow.addView(applyCouponBtn);
        loyaltyCard.addView(cRow);

        CheckBox walletChk = new CheckBox(this);
        walletChk.setText("کسر از موجودی کیف پول طلایی شما (" + PersianFormatter.formatToman(walletBalance) + ")");
        walletChk.setTextColor(COL_SUCCESS);
        walletChk.setTypeface(Typeface.DEFAULT_BOLD);
        walletChk.setChecked(useGoldWallet);
        walletChk.setOnCheckedChangeListener((b, c) -> {
            useGoldWallet = c;
            renderCurrentScreen();
        });
        loyaltyCard.addView(space(dp(6)));
        loyaltyCard.addView(walletChk);
        contentContainer.addView(loyaltyCard);
        contentContainer.addView(space(dp(10)));

        // Shipping, Address & 4-Provider BNPL Hub
        String[] shipNames = new String[]{"پیک اختصاصی VIP زنجیره سرد تهران", "پست پیشتاز ویژه بیمه اصلالت", "تیپاکس اکسپرس کشوری"};
        long[] shipCosts = new long[]{95_000L, 65_000L, 80_000L};
        long effectiveShipping = afterDiscounts >= 3_000_000L ? 0L : shipCosts[selectedShippingIndex];
        long payableTotal = afterDiscounts + effectiveShipping;
        long instantCashback = Math.max(15_000L, Math.round(payableTotal * 0.05));

        LinearLayout payCard = cardContainer();
        EditText nameInput = createInput("نام تحویل‌گیرنده", db.getSetting("customer_name", ""));
        EditText mobileInput = createInput("موبایل (جهت صدور گواهی اصالت)", db.getSetting("customer_mobile", ""));
        EditText addrInput = createInput("نشانی دقیق تحویل تشریفاتی", db.getSetting("customer_address", ""));

        payCard.addView(nameInput);
        payCard.addView(space(dp(8)));
        payCard.addView(mobileInput);
        payCard.addView(space(dp(8)));
        payCard.addView(addrInput);
        payCard.addView(space(dp(10)));

        TextView bnplHead = new TextView(this);
        bnplHead.setText("انتخاب درگاه پرداخت نقدی یا اعتباری اقساطی (BNPL):");
        bnplHead.setTextColor(COL_TEXT_MAIN);
        bnplHead.setTypeface(Typeface.DEFAULT_BOLD);
        payCard.addView(bnplHead);
        payCard.addView(space(dp(6)));

        String[][] gateways = new String[][]{
                {"snapp_pay", "اسنپ‌پی — ۴ قسط بدون کارمزد (تایید آنی)"},
                {"tara", "اعتبار تارا — ۴ قسط ماهانه بدون ضامن"},
                {"digipay", "دیجی‌پی — پرداخت اعتباری هوشمند ۴ قسطه"},
                {"azkivam", "ازکی‌وام — خرید اقساطی پک‌های کلکسیونی"},
                {"zarinpal", "درگاه مستقیم شاپرک / زرین‌پال (کلیه کارت‌های عضو شتاب)"}
        };

        for (String[] gw : gateways) {
            boolean sel = gw[0].equals(selectedPaymentMethod);
            TextView opt = new TextView(this);
            opt.setText((sel ? "◉  " : "○  ") + gw[1]);
            opt.setTextColor(sel ? COL_OBSIDIAN : COL_TEXT_MAIN);
            opt.setTypeface(sel ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            opt.setPadding(dp(12), dp(9), dp(12), dp(9));
            opt.setBackground(roundedShape(sel ? 0x33C6A16C : COL_CREAM_SOFT, dp(12), sel ? COL_GOLD : COL_BORDER, dp(1)));
            opt.setOnClickListener(v -> {
                selectedPaymentMethod = gw[0];
                renderCurrentScreen();
            });
            payCard.addView(opt);
            payCard.addView(space(dp(6)));
        }

        if (!"zarinpal".equals(selectedPaymentMethod)) {
            long qest = Math.round(payableTotal / 4.0);
            TextView schedule = new TextView(this);
            schedule.setText("✦ جدول اقساط بدون بهره:\n" +
                    "• قسط اول (امروز): " + PersianFormatter.formatToman(qest) + "\n" +
                    "• اقساط دوم تا چهارم (۳۰، ۶۰ و ۹۰ روز آینده): هر قسط " + PersianFormatter.formatToman(qest));
            schedule.setTextColor(COL_SUCCESS);
            schedule.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            schedule.setPadding(dp(10), dp(8), dp(10), dp(8));
            schedule.setBackground(roundedShape(0xFFEAF5EF, dp(10), 0xFFBFE0CE, dp(1)));
            payCard.addView(schedule);
            payCard.addView(space(dp(8)));
        }

        // Invoice Summary Box
        LinearLayout inv = new LinearLayout(this);
        inv.setOrientation(LinearLayout.VERTICAL);
        inv.setPadding(dp(14), dp(12), dp(14), dp(12));
        inv.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(14), COL_GOLD, dp(1)));

        inv.addView(invoiceLine("جمع کل کالاها", PersianFormatter.formatToman(subtotal), 0xFFEAE3D5, false));
        if (couponDisc > 0) inv.addView(invoiceLine("تخفیف کوپن (" + appliedCouponCode + ")", "− " + PersianFormatter.formatToman(couponDisc), 0xFF6EE7B7, false));
        if (walletDeduction > 0) inv.addView(invoiceLine("کسر از کیف پول طلایی", "− " + PersianFormatter.formatToman(walletDeduction), 0xFF6EE7B7, false));
        inv.addView(invoiceLine("تشریفات جعبه مخمل + زنجیره سرد", "رایگان مزون VIP", COL_GOLD_LIGHT, false));
        inv.addView(invoiceLine("هزینه ارسال", effectiveShipping == 0 ? "رایگان" : PersianFormatter.formatToman(effectiveShipping), 0xFFEAE3D5, false));
        inv.addView(invoiceLine("مبلغ نهایی قابل پرداخت", PersianFormatter.formatToman(payableTotal), COL_GOLD_LIGHT, true));
        inv.addView(invoiceLine("پاداش آنی ۵٪ کش‌بک طلایی شما", "+ " + PersianFormatter.formatToman(instantCashback), 0xFF6EE7B7, false));

        payCard.addView(inv);
        payCard.addView(space(dp(12)));

        int instCount = "zarinpal".equals(selectedPaymentMethod) ? 1 : 4;
        Button submitBtn = goldButton("تایید نهایی سفارش + صدور گواهی اصالت و کش‌بک", () -> {
            String cName = nameInput.getText().toString().trim();
            String cMob = mobileInput.getText().toString().trim();
            String cAddr = addrInput.getText().toString().trim();
            if (cName.length() < 3 || !PersianFormatter.isValidIranMobile(cMob) || cAddr.length() < 8) {
                toast("لطفاً نام، موبایل معتبر ۱۱ رقمی و نشانی دقیق را وارد نمایید.");
                return;
            }
            selectedTissueScentIdx = scentSp.getSelectedItemPosition();
            customGiftMessage = giftMsgInput.getText().toString().trim();
            db.setSetting("customer_name", cName);
            db.setSetting("customer_mobile", cMob);
            db.setSetting("customer_address", cAddr);

            try {
                Models.OrderRecord created = db.executeImperialCheckout(
                        cart, cName, cMob, cAddr,
                        shipNames[selectedShippingIndex], shipCosts[selectedShippingIndex],
                        selectedPaymentMethod, instCount, appliedCouponCode,
                        useGoldWallet, giftVipVelvetBox, giftColdChain,
                        tissueScents[selectedTissueScentIdx], customGiftMessage
                );
                cart.clear();
                appliedCouponCode = "";
                showOrderSuccessCertificateModal(created);
                customerTab = 4;
                renderCurrentScreen();
            } catch (IllegalStateException ex) {
                toast(ex.getMessage());
            }
        });
        payCard.addView(submitBtn);
        contentContainer.addView(payCard);
    }

    private void showOrderSuccessCertificateModal(Models.OrderRecord ord) {
        new AlertDialog.Builder(this)
                .setTitle("♛ سفارش تشریفاتی شما با موفقیت ثبت شد")
                .setMessage("شماره سفارش: " + ord.orderNumber + "\n" +
                        "کد گواهی اصالت دیجیتال شما: " + ord.certificateCode + "\n" +
                        "پاداش واریزشده به کیف پول طلایی: " + PersianFormatter.formatToman(ord.cashbackEarned) + "\n" +
                        "بسته‌بندی: " + (ord.vipGiftBox ? "جعبه مخمل مشکی با مهر طلایی AS" : "استاندارد") +
                        (ord.coldChain ? " + کیف زنجیره سرد" : ""))
                .setPositiveButton("مشاهده در باشگاه الماس سیاه", null)
                .show();
    }

    // =========================================================================
    // CUSTOMER TAB 4: BLACK DIAMOND VIP PRIVÉE + GOLD WALLET + SMART REPLENISHMENT
    // =========================================================================

    private void renderCustomerVipPrivee() {
        long walletBal = db.getWalletBalance();
        String pts = db.getSetting("loyalty_points", "1480");

        // Metallic Black Diamond Card
        LinearLayout vipCard = new LinearLayout(this);
        vipCard.setOrientation(LinearLayout.VERTICAL);
        vipCard.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(20), COL_GOLD, dp(2)));
        vipCard.setPadding(dp(18), dp(18), dp(18), dp(18));

        TextView tierBadge = new TextView(this);
        tierBadge.setText("♛ ASIYE BLACK DIAMOND PRIVÉE • ۵٪ تا ۷٪ کش‌بک دائمی");
        tierBadge.setTextColor(COL_GOLD_LIGHT);
        tierBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        tierBadge.setTypeface(Typeface.DEFAULT_BOLD);

        TextView memberName = new TextView(this);
        memberName.setText(db.getSetting("customer_name", "مهمان سلطنتی ASIYE"));
        memberName.setTextColor(0xFFFFFFFF);
        memberName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        memberName.setTypeface(Typeface.DEFAULT_BOLD);
        memberName.setPadding(0, dp(6), 0, dp(8));

        TextView balanceView = new TextView(this);
        balanceView.setText("موجودی کیف پول طلایی قابل خرید: " + PersianFormatter.formatToman(walletBal) +
                "\nامتیاز وفاداری باشگاه: " + PersianFormatter.toPersianDigits(pts) + " امتیاز");
        balanceView.setTextColor(0xFF6EE7B7);
        balanceView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        balanceView.setTypeface(Typeface.DEFAULT_BOLD);

        vipCard.addView(tierBadge);
        vipCard.addView(memberName);
        vipCard.addView(balanceView);
        contentContainer.addView(vipCard);
        contentContainer.addView(space(dp(12)));

        // SMART REPLENISHMENT CRM WIDGET
        LinearLayout refillCard = cardContainer();
        refillCard.setBackground(roundedShape(0xFFFFFBF2, dp(16), COL_GOLD, dp(1)));

        TextView refTitle = new TextView(this);
        refTitle.setText("⏳ یادآور هوشمند اتمام محصول (Smart Replenishment CRM)");
        refTitle.setTextColor(COL_OBSIDIAN);
        refTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        refTitle.setTypeface(Typeface.DEFAULT_BOLD);

        TextView refSub = new TextView(this);
        refSub.setText("بر اساس خرید «سرم طلایی احیاکننده ۲۴ عیار (۳۰ میل)» و دوره مصرف استاندارد ۴۵ روزه، حدود ۸۴٪ محصول شما مصرف شده و تنها ۷ روز تا پایان آن باقی است.");
        refSub.setTextColor(COL_TEXT_MUTED);
        refSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        refSub.setPadding(0, dp(4), 0, dp(8));

        // Depletion Bar
        FrameLayout barBg = new FrameLayout(this);
        barBg.setBackground(roundedShape(0xFFE6DEC9, dp(6), COL_BORDER, 0));
        barBg.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(10)));
        View barFill = new View(this);
        barFill.setBackground(roundedShape(COL_GOLD_DARK, dp(6), COL_GOLD, 0));
        FrameLayout.LayoutParams bfLp = new FrameLayout.LayoutParams(dp(240), dp(10), Gravity.RIGHT);
        barFill.setLayoutParams(bfLp);
        barBg.addView(barFill);

        Button refillBtn = goldButton("تمدید یک‌کلیکه با ۱۰٪ تخفیف وفاداری REFILL10", () -> {
            for (Models.Product p : db.getProducts(true, "همه", "", "newest")) {
                if ("AS-SRM-001".equals(p.sku)) {
                    addToCartSilent(p, p.volume, 1);
                    appliedCouponCode = "REFILL10";
                    toast("سرم طلا ۲۴ عیار + تخفیف ۱۰٪ وفاداری REFILL10 آماده پرداخت شد!");
                    customerTab = 3;
                    renderCurrentScreen();
                    break;
                }
            }
        });

        refillCard.addView(refTitle);
        refillCard.addView(refSub);
        refillCard.addView(barBg);
        refillCard.addView(space(dp(10)));
        refillCard.addView(refillBtn);
        contentContainer.addView(refillCard);
        contentContainer.addView(space(dp(12)));

        // Gold Wallet Ledger
        contentContainer.addView(sectionHeader("گردش کیف پول طلایی و پاداش‌های کش‌بک", "کلیه پاداش‌ها بلافاصله پس از ثبت سفارش شارژ می‌شوند"));
        for (Models.WalletTransactionRecord tx : db.getWalletTransactions()) {
            LinearLayout tCard = cardContainer();
            String sign = tx.amount >= 0 ? "+ " : "− ";
            TextView tv = new TextView(this);
            tv.setText(sign + PersianFormatter.formatToman(Math.abs(tx.amount)) + "  |  " + tx.description + " (" + tx.createdAtLabel + ")");
            tv.setTextColor(tx.amount >= 0 ? COL_SUCCESS : COL_WARNING);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tCard.addView(tv);
            contentContainer.addView(tCard);
            contentContainer.addView(space(dp(6)));
        }

        // Order History with Gift Concierge & Certificate Codes
        contentContainer.addView(space(dp(8)));
        contentContainer.addView(sectionHeader("سفارش‌های تشریفاتی و رهگیری مرسوله", "همراه با کد گواهی اصالت و جزئیات بسته‌بندی VIP"));
        for (Models.OrderRecord ord : db.getOrders("all")) {
            LinearLayout oCard = cardContainer();
            TextView title = new TextView(this);
            title.setText(ord.orderNumber + "  •  " + PersianFormatter.statusLabelFa(ord.status) +
                    "\nمبلغ: " + PersianFormatter.formatToman(ord.totalAmount) + "  |  " + PersianFormatter.paymentMethodLabelFa(ord.paymentMethod) +
                    "\nگواهی اصالت: " + ord.certificateCode +
                    (ord.vipGiftBox ? "\n🎁 جعبه مخمل VIP (" + ord.scentedTissue + ")" : "") +
                    (!TextUtils.isEmpty(ord.giftMessage) ? "\n✉ پیام کارت طلاکوب: «" + ord.giftMessage + "»" : ""));
            title.setTextColor(COL_TEXT_MAIN);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            title.setLineSpacing(dp(3), 1f);
            oCard.addView(title);
            contentContainer.addView(oCard);
            contentContainer.addView(space(dp(8)));
        }
    }

    // =========================================================================
    // ADMIN SUITE (DASHBOARD + CATALOG + BARCODE ORDERS + FEFO INVENTORY + FEEDS)
    // =========================================================================

    private void renderAdminDashboard() {
        Models.DashboardSnapshot snap = db.getDashboardSnapshot();
        contentContainer.addView(sectionHeader("فرماندهی اجرایی ASIYE IMPERIAL", "آمار فروش، سفارش‌های اقساطی، تشریفات هدیه و هشدار انبار FEFO"));

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(kpiCard("فروش کل دوره", PersianFormatter.formatToman(snap.salesWeek), "رشد +۲۴٪ هفتگی", COL_GOLD_DARK));
        row1.addView(spaceHorizontal(dp(8)));
        row1.addView(kpiCard("سفارش‌های فعال", PersianFormatter.toPersianDigits(snap.activeOrdersCount + " سفارش"), "نیازمند پردازش VIP", COL_SUCCESS));
        contentContainer.addView(row1);
        contentContainer.addView(space(dp(8)));

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.addView(kpiCard("تراکنش‌های اقساطی BNPL", PersianFormatter.toPersianDigits(snap.installmentOrdersCount + " پرونده"), "اسنپ‌پی / تارا / دیجی‌پی", COL_OBSIDIAN));
        row2.addView(spaceHorizontal(dp(8)));
        row2.addView(kpiCard("بسته‌بندی مخمل VIP", PersianFormatter.toPersianDigits(snap.vipGiftOrdersCount + " مرسوله"), "کانسیرژ هدیه فعال", COL_WARNING));
        contentContainer.addView(row2);
        contentContainer.addView(space(dp(12)));

        LinearLayout chartBox = cardContainer();
        TextView chartTitle = new TextView(this);
        chartTitle.setText("نمودار روند درآمد ۷ روز اخیر مزون");
        chartTitle.setTypeface(Typeface.DEFAULT_BOLD);
        chartTitle.setTextColor(COL_TEXT_MAIN);
        SalesChartView chartView = new SalesChartView(this);
        chartView.setData(snap.sevenDayRevenue, snap.sevenDayLabels);
        chartView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(185)));
        chartBox.addView(chartTitle);
        chartBox.addView(space(dp(8)));
        chartBox.addView(chartView);
        contentContainer.addView(chartBox);
    }

    private void renderAdminProducts() {
        contentContainer.addView(sectionHeader("مدیریت کاتالوگ، تصاویر استودیویی و اصالت", "افزودن، ویرایش قیمت، بچ‌کد و آرشیو امن محصولات"));
        Button addBtn = goldButton("+ ثبت محصول لوکس جدید", () -> showProductEditorDialog(null));
        contentContainer.addView(addBtn);
        contentContainer.addView(space(dp(10)));

        for (Models.Product p : db.getProducts(false, "همه", "", "newest")) {
            LinearLayout card = cardContainer();
            TextView t = new TextView(this);
            t.setText(p.nameFa + (p.published ? "" : " (آرشیو)") + "\nSKU: " + p.sku + "  •  بچ: " + p.batchCode + "  •  موجودی: " + PersianFormatter.toPersianDigits(String.valueOf(p.stock)) + "\nقیمت: " + PersianFormatter.formatToman(p.salePrice));
            t.setTextColor(COL_TEXT_MAIN);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setPadding(0, dp(8), 0, 0);
            Button edit = outlineButton("ویرایش", () -> showProductEditorDialog(p));
            edit.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            Button arch = outlineButton("آرشیو امن", () -> {
                toast(db.archiveProductSafely(p.id));
                renderCurrentScreen();
            });
            LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            aLp.setMarginStart(dp(8));
            arch.setLayoutParams(aLp);
            actions.addView(edit);
            actions.addView(arch);

            card.addView(t);
            card.addView(actions);
            contentContainer.addView(card);
            contentContainer.addView(space(dp(8)));
        }
    }

    private void showProductEditorDialog(Models.Product existing) {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        box.setPadding(dp(18), dp(12), dp(18), dp(12));
        sv.addView(box);

        EditText skuIn = createInput("کد SKU", existing != null ? existing.sku : "AS-NEW-" + (100 + new Random().nextInt(899)));
        EditText nameFaIn = createInput("نام فارسی محصول", existing != null ? existing.nameFa : "");
        EditText origIn = createInput("قیمت اصلی (تومان)", existing != null ? String.valueOf(existing.originalPrice) : "2200000");
        origIn.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText saleIn = createInput("قیمت فروش (تومان)", existing != null ? String.valueOf(existing.salePrice) : "1850000");
        saleIn.setInputType(InputType.TYPE_CLASS_NUMBER);

        String[] imgKeys = new String[]{"serum", "caviar", "lipstick", "perfume", "elixir", "giftbox"};
        Spinner imgSp = new Spinner(this);
        imgSp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"تصویر سرم طلا ۲۴ عیار", "تصویر کرم خاویار سیاه", "تصویر رژ لب مخملی", "تصویر ادوپرفیوم سلطنتی", "تصویر الکسیر طلایی مو", "تصویر کافره مخمل VIP"}));

        box.addView(skuIn); box.addView(space(dp(8)));
        box.addView(nameFaIn); box.addView(space(dp(8)));
        box.addView(origIn); box.addView(space(dp(8)));
        box.addView(saleIn); box.addView(space(dp(8)));
        box.addView(imgSp);

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "ثبت محصول جدید" : "ویرایش محصول")
                .setView(sv)
                .setPositiveButton("ذخیره", (d, w) -> {
                    try {
                        String err = db.saveOrUpdateProduct(
                                existing != null ? existing.id : null,
                                skuIn.getText().toString(), nameFaIn.getText().toString(), "ASIYE Haute Beauté",
                                "مراقبت سلولی پوست", "فرمولاسیون تخصصی مزون ASIYE", "فرانسه", "۵۰ میلی‌لیتر",
                                Long.parseLong(origIn.getText().toString().trim()),
                                Long.parseLong(saleIn.getText().toString().trim()),
                                15, true, imgKeys[imgSp.getSelectedItemPosition()]);
                        if (err != null) toast(err);
                        else { toast("ذخیره شد."); renderCurrentScreen(); }
                    } catch (Exception e) { toast("مقادیر عددی را بررسی نمایید."); }
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    private void renderAdminOrders() {
        contentContainer.addView(sectionHeader("سفارش‌ها، تشریفات هدیه و اسکن بارکد انبار",
                "بررسی سریع بارکد بسته‌بندی، پیام کارت طلاکوب و وضعیت زنجیره سرد"));

        for (Models.OrderRecord ord : db.getOrders(adminOrderStatusFilter)) {
            LinearLayout card = cardContainer();
            TextView tv = new TextView(this);
            tv.setText(ord.orderNumber + "  |  " + ord.customerName + " (" + PersianFormatter.toPersianDigits(ord.customerMobile) + ")\n" +
                    "وضعیت: " + PersianFormatter.statusLabelFa(ord.status) + "  •  مبلغ: " + PersianFormatter.formatToman(ord.totalAmount) + "\n" +
                    "تشریفات: " + (ord.vipGiftBox ? "جعبه مخمل VIP (" + ord.scentedTissue + ")" : "عادی") +
                    (ord.coldChain ? " + کیف زنجیره سرد ۱۸°C" : "") +
                    (!TextUtils.isEmpty(ord.giftMessage) ? "\nمتن کارت طلاکوب: «" + ord.giftMessage + "»" : ""));
            tv.setTextColor(COL_TEXT_MAIN);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            tv.setLineSpacing(dp(3), 1f);

            LinearLayout btnRow = new LinearLayout(this);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setPadding(0, dp(8), 0, 0);

            Button scanPackBtn = outlineButton("اسکن بارکد بسته‌بندی", () ->
                    toast("✓ کلیه اقلام سفارش " + ord.orderNumber + " با بچ‌کد FEFO و هولوگرام TTAC تایید شد."));
            scanPackBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            Button statusBtn = goldButton("تغییر وضعیت", () -> showOrderStatusDialog(ord));
            LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            sLp.setMarginStart(dp(8));
            statusBtn.setLayoutParams(sLp);

            btnRow.addView(scanPackBtn);
            btnRow.addView(statusBtn);
            card.addView(tv);
            card.addView(btnRow);
            contentContainer.addView(card);
            contentContainer.addView(space(dp(8)));
        }
    }

    private void showOrderStatusDialog(Models.OrderRecord ord) {
        String[] codes = new String[]{"paid", "preparing", "shipped", "delivered", "cancelled"};
        String[] labels = new String[]{"پرداخت‌شده", "آماده‌سازی کانسیرژ VIP", "تحویل به پیک زنجیره سرد", "تحویل نهایی", "لغو (بازگشت خودکار به انبار FEFO)"};

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(12), dp(18), dp(8));
        Spinner sp = new Spinner(this);
        sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));
        EditText trk = createInput("کد رهگیری مرسوله", ord.trackingCode);
        box.addView(sp); box.addView(space(dp(8))); box.addView(trk);

        new AlertDialog.Builder(this)
                .setTitle("وضعیت سفارش " + ord.orderNumber)
                .setView(box)
                .setPositiveButton("ثبت", (d, w) -> {
                    String err = db.updateOrderStatus(ord.id, codes[sp.getSelectedItemPosition()], trk.getText().toString());
                    toast(err != null ? err : "وضعیت سفارش به‌روز شد.");
                    renderCurrentScreen();
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    private void renderAdminInventoryFefo() {
        contentContainer.addView(sectionHeader("انبارداری هوشمند FEFO (اولین انقضا، اولین خروج)",
                "کنترل دقیق بچ‌کدها، تاریخ انقضا، زنجیره سرد و ثبت ورود/خروج اتمیک"));

        for (Models.Product p : db.getProducts(false, "همه", "", "newest")) {
            LinearLayout card = cardContainer();
            boolean low = p.stock <= p.lowStockThreshold;
            TextView tv = new TextView(this);
            tv.setText(p.nameFa + "\nبچ فعال: " + p.batchCode + "  •  انقضا: " + p.expiryLabel +
                    (p.coldChain ? "  •  ❄ زنجیره سرد" : "") +
                    "\nموجودی فعلی: " + PersianFormatter.toPersianDigits(String.valueOf(p.stock)) + (low ? " (⚠ هشدار کمبود)" : ""));
            tv.setTextColor(low ? COL_DANGER : COL_TEXT_MAIN);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);

            Button adj = outlineButton("ثبت ورود / خروج بچ انبار", () -> {
                String err = db.adjustInventory(p.id, "in", 10, "شارژ مجدد بچ استاندارد");
                toast(err != null ? err : "+۱۰ عدد به موجودی بچ " + p.batchCode + " افزوده شد.");
                renderCurrentScreen();
            });
            card.addView(tv);
            card.addView(space(dp(8)));
            card.addView(adj);
            contentContainer.addView(card);
            contentContainer.addView(space(dp(8)));
        }
    }

    private void renderAdminSettingsAndFeeds() {
        contentContainer.addView(sectionHeader("فید رسمی ترب و ایمالز + اتصال سرور مرکزی",
                "همگام‌سازی خودکار قیمت و موجودی با موتورهای جستجوی خرید ایران"));

        LinearLayout feedCard = cardContainer();
        TextView feedInfo = new TextView(this);
        feedInfo.setText("✦ وضعیت فیدهای مارکت‌پلیس:\n" +
                "• فید رسمی ترب (JSON): /api/v1/feeds/torob  [فعال ✓]\n" +
                "• فید رسمی ایمالز (XML): /api/v1/feeds/emalls  [فعال ✓]\n" +
                "• اسکیما سئو Google Rich Snippets (JSON-LD): فعال روی تمامی ۶ محصول");
        feedInfo.setTextColor(COL_SUCCESS);
        feedInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        feedInfo.setLineSpacing(dp(3), 1f);
        feedCard.addView(feedInfo);
        contentContainer.addView(feedCard);
        contentContainer.addView(space(dp(10)));

        LinearLayout apiCard = cardContainer();
        EditText urlIn = createInput("آدرس API سرور لاراول", db.getSetting("api_base_url", "https://asiyestore.ir/api/v1"));
        Button syncBtn = goldButton("تست سلامت و همگام‌سازی زنده با سرور", () -> {
            String u = urlIn.getText().toString().trim();
            db.setSetting("api_base_url", u);
            new ApiSyncClient().syncCatalogFromBackend(u, "", db, (ok, msg, count) -> {
                toast(msg);
                renderCurrentScreen();
            });
        });
        apiCard.addView(urlIn);
        apiCard.addView(space(dp(8)));
        apiCard.addView(syncBtn);
        contentContainer.addView(apiCard);
    }

    // =========================================================================
    // SHARED LUXURY UI HELPERS
    // =========================================================================

    private void addToCart(Models.Product product, String variant, int qty) {
        addToCartSilent(product, variant, qty);
        toast("«" + product.nameFa + "» به سبد تشریفاتی افزوده شد ✦");
        renderCurrentScreen();
    }

    private void addToCartSilent(Models.Product product, String variant, int qty) {
        for (Models.CartItem existing : cart) {
            if (existing.product.id == product.id && existing.selectedVariant.equals(variant)) {
                existing.quantity = Math.min(product.stock, existing.quantity + qty);
                return;
            }
        }
        Models.CartItem item = new Models.CartItem();
        item.product = product;
        item.selectedVariant = variant != null ? variant : product.volume;
        item.quantity = qty;
        cart.add(item);
    }

    private int totalCartItems() {
        int sum = 0;
        for (Models.CartItem c : cart) sum += c.quantity;
        return sum;
    }

    private long calculateCartSubtotal() {
        long sum = 0L;
        for (Models.CartItem c : cart) sum += c.lineTotal();
        return sum;
    }

    private int resolveProductImage(String key) {
        if ("caviar".equals(key)) return R.drawable.img_caviar_cream;
        if ("lipstick".equals(key)) return R.drawable.img_lipstick;
        if ("perfume".equals(key)) return R.drawable.img_perfume;
        if ("elixir".equals(key)) return R.drawable.img_hair_elixir;
        if ("giftbox".equals(key)) return R.drawable.img_vip_gift_box;
        return R.drawable.img_serum;
    }

    private LinearLayout cardContainer() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(roundedShape(COL_CARD_WHITE, dp(16), COL_BORDER, dp(1)));
        card.setElevation(dp(2));
        return card;
    }

    private View kpiCard(String label, String value, String foot, int accent) {
        LinearLayout card = cardContainer();
        card.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView l = new TextView(this); l.setText(label); l.setTextColor(COL_TEXT_MUTED); l.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        TextView v = new TextView(this); v.setText(value); v.setTextColor(accent); v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f); v.setTypeface(Typeface.DEFAULT_BOLD); v.setPadding(0, dp(4), 0, dp(2));
        TextView f = new TextView(this); f.setText(foot); f.setTextColor(COL_TEXT_MUTED); f.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        card.addView(l); card.addView(v); card.addView(f);
        return card;
    }

    private View sectionHeader(String title, String subtitle) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(2), dp(2), dp(2), dp(10));
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(COL_TEXT_MAIN);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        TextView s = new TextView(this);
        s.setText(subtitle);
        s.setTextColor(COL_TEXT_MUTED);
        s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        box.addView(t);
        box.addView(s);
        return box;
    }

    private View invoiceLine(String label, String value, int color, boolean bold) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(3), 0, dp(3));
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(color);
        l.setTextSize(TypedValue.COMPLEX_UNIT_SP, bold ? 13.5f : 11.5f);
        if (bold) l.setTypeface(Typeface.DEFAULT_BOLD);
        l.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(color);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, bold ? 14f : 12f);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(l);
        row.addView(v);
        return row;
    }

    private EditText createInput(String hint, String value) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setText(value != null ? value : "");
        et.setTextColor(COL_TEXT_MAIN);
        et.setHintTextColor(0xFF9C9488);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        et.setPadding(dp(12), dp(9), dp(12), dp(9));
        et.setBackground(roundedShape(COL_CREAM_SOFT, dp(12), COL_BORDER, dp(1)));
        return et;
    }

    private Button goldButton(String text, Runnable onClick) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(text);
        b.setTextColor(COL_OBSIDIAN);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setPadding(dp(14), dp(10), dp(14), dp(10));
        b.setBackground(roundedShape(COL_GOLD, dp(12), COL_GOLD_LIGHT, dp(1)));
        b.setOnClickListener(v -> onClick.run());
        return b;
    }

    private Button outlineButton(String text, Runnable onClick) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(text);
        b.setTextColor(COL_TEXT_MAIN);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setPadding(dp(12), dp(9), dp(12), dp(9));
        b.setBackground(roundedShape(COL_CARD_WHITE, dp(12), COL_GOLD, dp(1)));
        b.setOnClickListener(v -> onClick.run());
        return b;
    }

    private TextView miniCircleBtn(String txt, Runnable onClick) {
        TextView t = new TextView(this);
        t.setText(txt);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(COL_OBSIDIAN);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        t.setLayoutParams(new LinearLayout.LayoutParams(dp(30), dp(30)));
        t.setBackground(roundedShape(COL_CREAM_SOFT, dp(15), COL_GOLD, dp(1)));
        t.setOnClickListener(v -> onClick.run());
        return t;
    }

    private View space(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h));
        return v;
    }

    private View spaceHorizontal(int w) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(w, 1));
        return v;
    }

    private GradientDrawable roundedShape(int fillColor, int radiusPx, int strokeColor, int strokePx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(fillColor);
        gd.setCornerRadius(radiusPx);
        if (strokePx > 0) gd.setStroke(strokePx, strokeColor);
        return gd;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private int dp(float val) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics()));
    }
}
