package com.asiyestore.admin.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
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
 * ASIYE STORE — Quiet Luxury Signature Edition v4.0
 * Management: سرکار خانم زنگنه (+989044200993 | @asiyeh.store)
 * Designed & Architected by: Milad Yaghoobi
 */
public final class MainActivity extends Activity {

    // Owner & Designer Constants
    private static final String OWNER_PHONE_E164    = "+989044200993";
    private static final String OWNER_PHONE_DISPLAY = "+98 904 420 0993";
    private static final String OWNER_WA_NUMBER     = "989044200993";
    private static final String INSTAGRAM_URL       = "https://www.instagram.com/asiyeh.store?psln=MnoxdzRzcXcxOGtz";

    // Quiet-Luxury Palette (Obsidian, 24K Champagne Gold, Warm Silk Alabaster)
    private static final int COL_OBSIDIAN      = 0xFF0B0B0E;
    private static final int COL_OBSIDIAN_CARD = 0xFF16151C;
    private static final int COL_GOLD          = 0xFFC8A46B;
    private static final int COL_GOLD_LIGHT    = 0xFFE8CC9B;
    private static final int COL_GOLD_DEEP     = 0xFF96733E;
    private static final int COL_IVORY_BG      = 0xFFFAF8F4;
    private static final int COL_SILK_CREAM    = 0xFFF2ECE1;
    private static final int COL_WHITE         = 0xFFFFFFFF;
    private static final int COL_TEXT_MAIN     = 0xFF1B1A18;
    private static final int COL_TEXT_MUTED    = 0xFF6E675E;
    private static final int COL_BORDER        = 0xFFE5DCCB;
    private static final int COL_SUCCESS       = 0xFF1D6B4B;

    private AsiyeDatabaseHelper db;

    // Navigation State
    private boolean adminMode = false;
    private boolean adminAuthenticated = false;
    private int customerTab = 0; // 0=Boutique, 1=Advisor & Auth, 2=Bag, 3=Concierge & Management
    private int adminTab = 0;    // 0=Dashboard, 1=Catalog, 2=Orders, 3=Inventory

    // Boutique & Studio State
    private String selectedCategory = "همه";
    private int selectedAdvisorConcernIdx = 0;
    private String lastCheckedSerial = "AS-GAT-150";
    private Models.Product verifiedProduct = null;
    private int selectedSmartTopicIdx = 0;

    // Bag & Checkout State
    private final List<Models.CartItem> cart = new ArrayList<>();
    private String appliedCouponCode = "";
    private String selectedPaymentMethod = "snapp_pay";
    private boolean vipGiftPackaging = true;

    // Root Containers
    private LinearLayout contentContainer;
    private LinearLayout bottomNavContainer;
    private TextView headerSubtitle;
    private Luxury3DIconView headerAdminIcon;

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
        verifiedProduct = db.verifyAuthenticitySerial(lastCheckedSerial);

        List<Models.Product> initial = db.getProducts(true, "همه", "", "newest");
        if (!initial.isEmpty()) {
            Models.CartItem seed = new Models.CartItem();
            seed.product = initial.get(0);
            seed.selectedVariant = initial.get(0).volume;
            seed.quantity = 1;
            cart.add(seed);
        }

        setContentView(buildRootShell());
        renderCurrentScreen();
    }

    private View buildRootShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(COL_IVORY_BG);

        // Breathable Quiet-Luxury Header (Zero Clutter)
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(COL_OBSIDIAN);
        header.setPadding(dp(16), dp(12), dp(16), dp(12));
        header.setElevation(dp(6));

        // Brand Emblem + Crisp Gold Title
        ImageView emblem = new ImageView(this);
        emblem.setImageResource(R.drawable.img_brand_emblem);
        emblem.setScaleType(ImageView.ScaleType.CENTER_CROP);
        emblem.setBackground(roundedShape(COL_OBSIDIAN, dp(11), COL_GOLD, dp(1)));
        emblem.setPadding(dp(2), dp(2), dp(2), dp(2));
        LinearLayout.LayoutParams emLp = new LinearLayout.LayoutParams(dp(42), dp(42));
        emLp.setMarginEnd(dp(11));
        emblem.setLayoutParams(emLp);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView brandName = new TextView(this);
        brandName.setText("ASIYE STORE");
        brandName.setTextColor(COL_GOLD_LIGHT);
        brandName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);
        brandName.setTypeface(Typeface.SERIF, Typeface.BOLD);
        brandName.setLetterSpacing(0.12f);

        headerSubtitle = new TextView(this);
        headerSubtitle.setText("مدیریت مجموعه: سرکار خانم زنگنه");
        headerSubtitle.setTextColor(0xFFD6CFC3);
        headerSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);

        titleCol.addView(brandName);
        titleCol.addView(headerSubtitle);

        // Sleek 3D WhatsApp Direct Trigger
        Luxury3DIconView quickWa = new Luxury3DIconView(this, Luxury3DIconView.ICON_WHATSAPP, false);
        LinearLayout.LayoutParams waLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        waLp.setMarginEnd(dp(8));
        quickWa.setLayoutParams(waLp);
        quickWa.setOnClickListener(v -> openSmartWhatsApp("مشاوره تخصصی و ثبت سفارش", ""));

        // Sleek 3D Executive Admin Key Trigger
        headerAdminIcon = new Luxury3DIconView(this, Luxury3DIconView.ICON_KEY_ADMIN, false);
        headerAdminIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
        headerAdminIcon.setOnClickListener(v -> handleModeToggle());

        header.addView(emblem);
        header.addView(titleCol);
        header.addView(quickWa);
        header.addView(headerAdminIcon);

        View goldLine = new View(this);
        goldLine.setBackgroundColor(0x66C8A46B);
        goldLine.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(dp(16), dp(16), dp(16), dp(28));
        scrollView.addView(contentContainer);

        bottomNavContainer = new LinearLayout(this);
        bottomNavContainer.setOrientation(LinearLayout.HORIZONTAL);
        bottomNavContainer.setBackgroundColor(COL_OBSIDIAN);
        bottomNavContainer.setPadding(dp(8), dp(8), dp(8), dp(10));
        bottomNavContainer.setElevation(dp(12));

        root.addView(header);
        root.addView(goldLine);
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

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        box.setPadding(dp(20), dp(14), dp(20), dp(8));

        TextView hint = new TextView(this);
        hint.setText("ورود اختصاصی مدیریت مجموعه (سرکار خانم زنگنه)");
        hint.setTextColor(COL_TEXT_MUTED);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        box.addView(hint);

        EditText userField = createInput("شماره موبایل مدیریت", "09044200993");
        EditText passField = createInput("رمز عبور امنیتی", "Asiye@2026");
        passField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        box.addView(space(dp(10)));
        box.addView(userField);
        box.addView(space(dp(8)));
        box.addView(passField);

        new AlertDialog.Builder(this)
                .setTitle("پنل مدیریت ASIYE STORE")
                .setView(box)
                .setPositiveButton("ورود", (d, w) -> {
                    String u = PersianFormatter.toAsciiDigits(userField.getText().toString().trim());
                    String p = passField.getText().toString().trim();
                    if (("09044200993".equals(u) || "09120000000".equals(u) || "admin@asiyestore.ir".equalsIgnoreCase(u))
                            && "Asiye@2026".equals(p)) {
                        adminAuthenticated = true;
                        adminMode = true;
                        toast("خوش آمدید سرکار خانم زنگنه گرامی");
                        renderCurrentScreen();
                    } else {
                        toast("شماره یا رمز عبور صحیح نیست.");
                    }
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    private void renderCurrentScreen() {
        contentContainer.removeAllViews();
        headerAdminIcon.setIconState(Luxury3DIconView.ICON_KEY_ADMIN, adminMode);

        if (!adminMode) {
            headerSubtitle.setText("مدیریت مجموعه: سرکار خانم زنگنه");
            renderCustomerBottomNav();
            if (customerTab == 0) renderBoutiqueScreen();
            else if (customerTab == 1) renderAdvisorAndAuthScreen();
            else if (customerTab == 2) renderBagScreen();
            else renderExecutiveConciergeAndDesignerScreen();
        } else {
            headerSubtitle.setText("پنل فرماندهی مدیریت • نمای ادمین فعال است");
            renderAdminBottomNav();
            if (adminTab == 0) renderAdminDashboard();
            else if (adminTab == 1) renderAdminProducts();
            else if (adminTab == 2) renderAdminOrders();
            else renderAdminInventory();
        }
    }

    private void renderCustomerBottomNav() {
        bottomNavContainer.removeAllViews();
        int qty = totalCartItems();
        String bagTitle = qty > 0
                ? "سبد خرید (" + PersianFormatter.toPersianDigits(String.valueOf(qty)) + ")"
                : "سبد خرید";

        String[] titles = new String[]{"ویترین", "مشاور و اصالت", bagTitle, "مدیریت و ارتباط"};
        int[] icons = new int[]{
                Luxury3DIconView.ICON_BOUTIQUE,
                Luxury3DIconView.ICON_ADVISOR,
                Luxury3DIconView.ICON_BAG,
                Luxury3DIconView.ICON_CONCIERGE
        };

        for (int i = 0; i < titles.length; i++) {
            final int idx = i;
            bottomNavContainer.addView(build3DNavTab(icons[i], titles[i], customerTab == i, () -> {
                customerTab = idx;
                renderCurrentScreen();
            }));
        }
    }

    private void renderAdminBottomNav() {
        bottomNavContainer.removeAllViews();
        String[] titles = new String[]{"گزارش فروش", "محصولات", "سفارش‌ها", "موجودی انبار"};
        int[] icons = new int[]{
                Luxury3DIconView.ICON_CROWN,
                Luxury3DIconView.ICON_BOUTIQUE,
                Luxury3DIconView.ICON_BAG,
                Luxury3DIconView.ICON_SHIELD
        };

        for (int i = 0; i < titles.length; i++) {
            final int idx = i;
            bottomNavContainer.addView(build3DNavTab(icons[i], titles[i], adminTab == i, () -> {
                adminTab = idx;
                renderCurrentScreen();
            }));
        }
    }

    private View build3DNavTab(int iconType, String title, boolean selected, Runnable onClick) {
        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        tab.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tab.setPadding(dp(4), dp(4), dp(4), dp(4));

        Luxury3DIconView icon = new Luxury3DIconView(this, iconType, selected);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(34)));

        TextView lbl = new TextView(this);
        lbl.setText(title);
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lbl.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        lbl.setTextColor(selected ? COL_GOLD_LIGHT : 0xFFA49C8E);
        lbl.setGravity(Gravity.CENTER);
        lbl.setSingleLine(true);
        lbl.setPadding(0, dp(3), 0, 0);

        tab.addView(icon);
        tab.addView(lbl);
        tab.setOnClickListener(v -> onClick.run());
        return tab;
    }

    // =========================================================================
    // TAB 0: QUIET-LUXURY BOUTIQUE (@asiyeh.store CURATED COLLECTION)
    // =========================================================================

    private void renderBoutiqueScreen() {
        // Serene Editorial Hero Card
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(22), COL_GOLD, dp(1)));
        hero.setPadding(dp(14), dp(14), dp(14), dp(18));

        ImageView heroImg = new ImageView(this);
        heroImg.setImageResource(R.drawable.img_hero_curated);
        heroImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        heroImg.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(158)));
        hero.addView(heroImg);

        TextView eyebrow = new TextView(this);
        eyebrow.setText("اصالت تضمین‌شده • کلکسیون تخصصی پوست و مو");
        eyebrow.setTextColor(COL_GOLD_LIGHT);
        eyebrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        eyebrow.setPadding(0, dp(12), 0, dp(2));

        TextView heroTitle = new TextView(this);
        heroTitle.setText("زیبایی اصیل، با انتخاب‌های علمی و ماندگار");
        heroTitle.setTextColor(COL_WHITE);
        heroTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17.5f);
        heroTitle.setTypeface(Typeface.DEFAULT_BOLD);

        TextView heroSub = new TextView(this);
        heroSub.setText("گلچینی از برترین محصولات اورجینال گاتیو، لاکچری کوین، اوردینری و هیرفرند با مشاوره مستقیم مدیریت");
        heroSub.setTextColor(0xFFD2C9BC);
        heroSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        heroSub.setPadding(0, dp(4), 0, dp(14));

        LinearLayout heroActions = new LinearLayout(this);
        heroActions.setOrientation(LinearLayout.HORIZONTAL);

        View advisorBtn = buttonWith3DIcon("مشاور روتین پوست", Luxury3DIconView.ICON_ADVISOR, true, () -> {
            customerTab = 1;
            renderCurrentScreen();
        });
        advisorBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.15f));

        View directBtn = buttonWith3DIcon("ارتباط با مدیریت", Luxury3DIconView.ICON_CONCIERGE, false, () -> {
            customerTab = 3;
            renderCurrentScreen();
        });
        LinearLayout.LayoutParams dbLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        dbLp.setMarginStart(dp(8));
        directBtn.setLayoutParams(dbLp);

        heroActions.addView(advisorBtn);
        heroActions.addView(directBtn);

        hero.addView(eyebrow);
        hero.addView(heroTitle);
        hero.addView(heroSub);
        hero.addView(heroActions);
        contentContainer.addView(hero);
        contentContainer.addView(space(dp(16)));

        // Calm Category Filter Pills
        HorizontalScrollView catScroll = new HorizontalScrollView(this);
        catScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout catRow = new LinearLayout(this);
        catRow.setOrientation(LinearLayout.HORIZONTAL);

        String[] cats = new String[]{"همه", "مراقبت تخصصی پوست", "میکاپ لوکس", "تقویت و درمان مو"};
        for (String cat : cats) {
            boolean active = cat.equals(selectedCategory);
            TextView pill = new TextView(this);
            pill.setText(cat);
            pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            pill.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            pill.setTextColor(active ? COL_GOLD_LIGHT : COL_TEXT_MUTED);
            pill.setPadding(dp(16), dp(8), dp(16), dp(8));
            pill.setBackground(roundedShape(active ? COL_OBSIDIAN : COL_WHITE, dp(20), active ? COL_GOLD : COL_BORDER, dp(1)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            pill.setLayoutParams(lp);
            pill.setOnClickListener(v -> {
                selectedCategory = cat;
                renderCurrentScreen();
            });
            catRow.addView(pill);
        }
        catScroll.addView(catRow);
        contentContainer.addView(catScroll);
        contentContainer.addView(space(dp(16)));

        // Spacious Gallery Product Cards
        List<Models.Product> products = db.getProducts(true, selectedCategory, "", "newest");
        for (Models.Product p : products) {
            contentContainer.addView(buildGalleryProductCard(p));
            contentContainer.addView(space(dp(16)));
        }
    }

    private View buildGalleryProductCard(Models.Product p) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_WHITE, dp(22), COL_BORDER, dp(1)));
        card.setElevation(dp(3));

        // Clean Studio Media Frame
        FrameLayout media = new FrameLayout(this);
        ImageView img = new ImageView(this);
        img.setImageResource(resolveProductImage(p.imageKey));
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(210)));
        img.setOnClickListener(v -> showEditorialProductDialog(p));
        media.addView(img);

        if (p.discountPercent > 0) {
            TextView badge = new TextView(this);
            badge.setText(PersianFormatter.toPersianDigits(p.discountPercent + "٪ تخفیف ویژه"));
            badge.setTextColor(COL_GOLD_LIGHT);
            badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            badge.setTypeface(Typeface.DEFAULT_BOLD);
            badge.setPadding(dp(11), dp(5), dp(11), dp(5));
            badge.setBackground(roundedShape(0xDD0B0B0E, dp(16), COL_GOLD, dp(1)));
            FrameLayout.LayoutParams bLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.RIGHT);
            bLp.setMargins(dp(12), dp(12), dp(12), dp(12));
            badge.setLayoutParams(bLp);
            media.addView(badge);
        }

        // 3D Sculpted Wishlist Medallion
        Luxury3DIconView wish3d = new Luxury3DIconView(this, Luxury3DIconView.ICON_HEART, p.wishlisted);
        FrameLayout.LayoutParams wLp = new FrameLayout.LayoutParams(dp(38), dp(38), Gravity.TOP | Gravity.LEFT);
        wLp.setMargins(dp(12), dp(12), dp(12), dp(12));
        wish3d.setLayoutParams(wLp);
        wish3d.setOnClickListener(v -> {
            db.toggleWishlist(p.id, !p.wishlisted);
            renderCurrentScreen();
        });
        media.addView(wish3d);
        card.addView(media);

        // Airy Typography Body
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(18), dp(16), dp(18), dp(18));

        TextView brandLine = new TextView(this);
        brandLine.setText(p.nameEn + "  •  " + p.volume);
        brandLine.setTextColor(COL_GOLD_DEEP);
        brandLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        brandLine.setTypeface(Typeface.DEFAULT_BOLD);

        TextView title = new TextView(this);
        title.setText(p.nameFa);
        title.setTextColor(COL_TEXT_MAIN);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.5f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(4), 0, dp(6));
        title.setOnClickListener(v -> showEditorialProductDialog(p));

        TextView summary = new TextView(this);
        summary.setText(p.description);
        summary.setTextColor(COL_TEXT_MUTED);
        summary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        summary.setLineSpacing(dp(3), 1f);
        summary.setMaxLines(3);

        View divider = new View(this);
        divider.setBackgroundColor(0x44C8A46B);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        divLp.setMargins(0, dp(14), 0, dp(12));
        divider.setLayoutParams(divLp);

        // Non-Colliding Two-Side Price Row (Right = Current Price, Left = Muted Strike-Through Price)
        LinearLayout priceRow = new LinearLayout(this);
        priceRow.setOrientation(LinearLayout.HORIZONTAL);
        priceRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView currentPrice = new TextView(this);
        currentPrice.setText(PersianFormatter.formatToman(p.salePrice));
        currentPrice.setTextColor(COL_OBSIDIAN);
        currentPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        currentPrice.setTypeface(Typeface.DEFAULT_BOLD);
        currentPrice.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        priceRow.addView(currentPrice);

        if (p.originalPrice > p.salePrice) {
            TextView oldPrice = new TextView(this);
            oldPrice.setText(PersianFormatter.formatToman(p.originalPrice));
            oldPrice.setPaintFlags(oldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            oldPrice.setTextColor(0xFF9A9286);
            oldPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            priceRow.addView(oldPrice);
        }

        // Action Row with Real 3D Sculpted Icons
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(14), 0, 0);

        View addBtn = buttonWith3DIcon("افزودن به سبد", Luxury3DIconView.ICON_PLUS, true, () -> addToCart(p));
        addBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.25f));

        View waBtn = buttonWith3DIcon("سفارش واتس‌اپ", Luxury3DIconView.ICON_WHATSAPP, false, () ->
                openSmartWhatsApp("مشاوره و ثبت سفارش مستقیم محصول", p.nameFa));
        LinearLayout.LayoutParams wBtnLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        wBtnLp.setMarginStart(dp(8));
        waBtn.setLayoutParams(wBtnLp);

        actions.addView(addBtn);
        actions.addView(waBtn);

        body.addView(brandLine);
        body.addView(title);
        body.addView(summary);
        body.addView(divider);
        body.addView(priceRow);
        body.addView(actions);
        card.addView(body);
        return card;
    }

    private void showEditorialProductDialog(Models.Product p) {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        box.setPadding(dp(20), dp(14), dp(20), dp(10));
        sv.addView(box);

        TextView details = new TextView(this);
        details.setText(p.description + "\n\n" +
                "✦ ترکیبات موثر: " + p.ingredients + "\n\n" +
                "✦ روش مصرف تخصصی: " + p.usageMethod + "\n\n" +
                "✦ حجم / نسخه: " + p.volume + "  •  " + p.country + "\n" +
                "✦ کد اصالت کالا: " + p.sku);
        details.setTextColor(COL_TEXT_MAIN);
        details.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        details.setLineSpacing(dp(3), 1f);
        box.addView(details);

        new AlertDialog.Builder(this)
                .setTitle(p.nameFa)
                .setView(sv)
                .setPositiveButton("افزودن به سبد خرید", (d, w) -> addToCart(p))
                .setNeutralButton("مشاوره واتس‌اپ با مدیریت", (d, w) ->
                        openSmartWhatsApp("مشاوره تخصصی محصول", p.nameFa))
                .setNegativeButton("بستن", null)
                .show();
    }

    // =========================================================================
    // TAB 1: SERENE SKIN/HAIR ROUTINE ADVISOR & AUTHENTICITY CHECK
    // =========================================================================

    private void renderAdvisorAndAuthScreen() {
        contentContainer.addView(sectionHeader("مشاور هوشمند روتین و اصالت کالا",
                "پیشنهاد دقیق محصول متناسب با نیاز پوست و موی شما و بررسی ضمانت اصالت"));

        // Card 1: Intelligent Product Advisor
        LinearLayout advisorCard = new LinearLayout(this);
        advisorCard.setOrientation(LinearLayout.VERTICAL);
        advisorCard.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(22), COL_GOLD, dp(1)));
        advisorCard.setPadding(dp(18), dp(18), dp(18), dp(18));

        LinearLayout headRow = new LinearLayout(this);
        headRow.setOrientation(LinearLayout.HORIZONTAL);
        headRow.setGravity(Gravity.CENTER_VERTICAL);
        Luxury3DIconView advIcon = new Luxury3DIconView(this, Luxury3DIconView.ICON_ADVISOR, false);
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        icLp.setMarginEnd(dp(10));
        advIcon.setLayoutParams(icLp);

        TextView advTitle = new TextView(this);
        advTitle.setText("مشاور انتخاب تخصصی محصول");
        advTitle.setTextColor(COL_GOLD_LIGHT);
        advTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);
        advTitle.setTypeface(Typeface.DEFAULT_BOLD);

        headRow.addView(advIcon);
        headRow.addView(advTitle);
        advisorCard.addView(headRow);
        advisorCard.addView(space(dp(12)));

        String[] concerns = new String[]{
                "رفع تیرگی، لک‌های پوستی و یکدست‌سازی رنگ پوست",
                "کاهش خطوط صورت، چروک و لیفتینگ پوست",
                "پوشش مخملی، ضدآب و محافظت روزانه پوست",
                "توقف ریزش مو، تقویت پیاز مو و رویش مجدد",
                "پاکسازی منافذ باز، شفافیت پوست و رفع جای جوش",
                "تنظیم چربی کف سر، رفع شوره و تغذیه ساقه مو"
        };
        String[] targetSkus = new String[]{"AS-GAT-150", "AS-GAT-050", "AS-LC-104", "AS-HF-300", "AS-ORD-240", "AS-MRG-500"};

        Spinner concSp = new Spinner(this);
        concSp.setBackground(roundedShape(COL_SILK_CREAM, dp(14), COL_GOLD, dp(1)));
        concSp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, concerns));
        concSp.setSelection(selectedAdvisorConcernIdx);
        advisorCard.addView(concSp);
        advisorCard.addView(space(dp(10)));

        View updateBtn = buttonWith3DIcon("نمایش بهترین پیشنهاد برای من", Luxury3DIconView.ICON_BOUTIQUE, false, () -> {
            selectedAdvisorConcernIdx = concSp.getSelectedItemPosition();
            renderCurrentScreen();
        });
        advisorCard.addView(updateBtn);
        advisorCard.addView(space(dp(14)));

        Models.Product recommended = db.verifyAuthenticitySerial(targetSkus[selectedAdvisorConcernIdx]);
        if (recommended != null) {
            LinearLayout recBox = new LinearLayout(this);
            recBox.setOrientation(LinearLayout.VERTICAL);
            recBox.setPadding(dp(14), dp(14), dp(14), dp(14));
            recBox.setBackground(roundedShape(0x16FFFFFF, dp(16), 0x66C8A46B, dp(1)));

            TextView rTitle = new TextView(this);
            rTitle.setText("پیشنهاد اختصاصی: " + recommended.nameFa);
            rTitle.setTextColor(COL_GOLD_LIGHT);
            rTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            rTitle.setTypeface(Typeface.DEFAULT_BOLD);

            TextView rDesc = new TextView(this);
            rDesc.setText(recommended.description + "\nبهای ویژه: " + PersianFormatter.formatToman(recommended.salePrice));
            rDesc.setTextColor(0xFFEDE6D8);
            rDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            rDesc.setLineSpacing(dp(3), 1f);
            rDesc.setPadding(0, dp(6), 0, dp(12));

            final Models.Product finalRec = recommended;
            View recAdd = buttonWith3DIcon("افزودن مستقیم به سبد خرید", Luxury3DIconView.ICON_PLUS, true, () -> addToCart(finalRec));

            recBox.addView(rTitle);
            recBox.addView(rDesc);
            recBox.addView(recAdd);
            advisorCard.addView(recBox);
        }

        contentContainer.addView(advisorCard);
        contentContainer.addView(space(dp(18)));

        // Card 2: Clean Authenticity Verifier (Zero BiDi Glitches)
        LinearLayout authCard = sleekWhiteCard();
        LinearLayout authHead = new LinearLayout(this);
        authHead.setOrientation(LinearLayout.HORIZONTAL);
        authHead.setGravity(Gravity.CENTER_VERTICAL);
        Luxury3DIconView shIcon = new Luxury3DIconView(this, Luxury3DIconView.ICON_SHIELD, false);
        LinearLayout.LayoutParams shLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        shLp.setMarginEnd(dp(10));
        shIcon.setLayoutParams(shLp);

        TextView shTitle = new TextView(this);
        shTitle.setText("بررسی شناسه اصالت کالا");
        shTitle.setTextColor(COL_TEXT_MAIN);
        shTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        shTitle.setTypeface(Typeface.DEFAULT_BOLD);
        authHead.addView(shIcon);
        authHead.addView(shTitle);
        authCard.addView(authHead);
        authCard.addView(space(dp(12)));

        EditText codeIn = createInput("کد شناسه کالا (مثلاً AS-GAT-150)", lastCheckedSerial);
        codeIn.setTextDirection(View.TEXT_DIRECTION_LTR);
        authCard.addView(codeIn);

        HorizontalScrollView chipScroll = new HorizontalScrollView(this);
        chipScroll.setHorizontalScrollBarEnabled(false);
        chipScroll.setPadding(0, dp(8), 0, dp(10));
        LinearLayout chipRow = new LinearLayout(this);
        chipRow.setOrientation(LinearLayout.HORIZONTAL);
        String[] sampleIds = new String[]{"AS-GAT-150", "AS-GAT-050", "AS-LC-104", "AS-HF-300", "AS-ORD-240"};
        for (String sid : sampleIds) {
            TextView c = new TextView(this);
            c.setText(sid);
            c.setTextDirection(View.TEXT_DIRECTION_LTR);
            c.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            c.setTextColor(COL_OBSIDIAN);
            c.setPadding(dp(12), dp(6), dp(12), dp(6));
            c.setBackground(roundedShape(COL_SILK_CREAM, dp(14), COL_GOLD, dp(1)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(6));
            c.setLayoutParams(lp);
            c.setOnClickListener(v -> {
                lastCheckedSerial = sid;
                verifiedProduct = db.verifyAuthenticitySerial(sid);
                renderCurrentScreen();
            });
            chipRow.addView(c);
        }
        chipScroll.addView(chipRow);
        authCard.addView(chipScroll);

        View checkBtn = buttonWith3DIcon("استعلام اصالت محصول", Luxury3DIconView.ICON_SHIELD, true, () -> {
            lastCheckedSerial = codeIn.getText().toString().trim();
            verifiedProduct = db.verifyAuthenticitySerial(lastCheckedSerial);
            renderCurrentScreen();
        });
        authCard.addView(checkBtn);

        if (verifiedProduct != null) {
            authCard.addView(space(dp(12)));
            LinearLayout res = new LinearLayout(this);
            res.setOrientation(LinearLayout.VERTICAL);
            res.setPadding(dp(14), dp(12), dp(14), dp(12));
            res.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(14), COL_GOLD, dp(1)));

            TextView ok = new TextView(this);
            ok.setText("اصالت کالای اورجینال در سامانه مجموعه تایید شد");
            ok.setTextColor(0xFF86EFAC);
            ok.setTypeface(Typeface.DEFAULT_BOLD);
            ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);

            TextView info = new TextView(this);
            info.setText("محصول: " + verifiedProduct.nameFa +
                    "\nحجم / مدل: " + verifiedProduct.volume +
                    "\nتاریخ انقضا: " + verifiedProduct.expiryLabel);
            info.setTextColor(0xFFF2ECE1);
            info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            info.setLineSpacing(dp(3), 1f);
            info.setPadding(0, dp(4), 0, 0);

            res.addView(ok);
            res.addView(info);
            authCard.addView(res);
        }

        contentContainer.addView(authCard);
    }

    // =========================================================================
    // TAB 2: LUXURY SHOPPING BAG & SMART CHECKOUT
    // =========================================================================

    private void renderBagScreen() {
        contentContainer.addView(sectionHeader("سبد خرید اختصاصی شما",
                "امکان تکمیل آنلاین، خرید ۴ قسط بدون کارمزد یا ارسال هوشمند سبد در واتس‌اپ مدیریت"));

        if (cart.isEmpty()) {
            LinearLayout empty = sleekWhiteCard();
            empty.setGravity(Gravity.CENTER);
            TextView msg = new TextView(this);
            msg.setText("سبد خرید شما در حال حاضر خالی است.");
            msg.setTextColor(COL_TEXT_MUTED);
            msg.setGravity(Gravity.CENTER);
            msg.setPadding(0, dp(16), 0, dp(16));
            View back = buttonWith3DIcon("مشاهده محصولات منتخب", Luxury3DIconView.ICON_BOUTIQUE, true, () -> {
                customerTab = 0;
                renderCurrentScreen();
            });
            empty.addView(msg);
            empty.addView(back);
            contentContainer.addView(empty);
            return;
        }

        for (int i = 0; i < cart.size(); i++) {
            final int idx = i;
            Models.CartItem item = cart.get(i);
            LinearLayout row = sleekWhiteCard();
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            ImageView thumb = new ImageView(this);
            thumb.setImageResource(resolveProductImage(item.product.imageKey));
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(dp(64), dp(64));
            tLp.setMarginEnd(dp(12));
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

            TextView pr = new TextView(this);
            pr.setText(PersianFormatter.formatToman(item.lineTotal()));
            pr.setTextColor(COL_GOLD_DEEP);
            pr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            pr.setTypeface(Typeface.DEFAULT_BOLD);

            info.addView(name);
            info.addView(pr);
            row.addView(info);

            LinearLayout qtyCtrl = new LinearLayout(this);
            qtyCtrl.setOrientation(LinearLayout.HORIZONTAL);
            qtyCtrl.setGravity(Gravity.CENTER_VERTICAL);
            qtyCtrl.addView(qtyCircle("+", () -> {
                if (item.quantity < item.product.stock) {
                    item.quantity++;
                    renderCurrentScreen();
                }
            }));
            TextView qTxt = new TextView(this);
            qTxt.setText(PersianFormatter.toPersianDigits(String.valueOf(item.quantity)));
            qTxt.setTypeface(Typeface.DEFAULT_BOLD);
            qTxt.setTextColor(COL_TEXT_MAIN);
            qTxt.setPadding(dp(10), 0, dp(10), 0);
            qtyCtrl.addView(qTxt);
            qtyCtrl.addView(qtyCircle("−", () -> {
                if (item.quantity > 1) item.quantity--;
                else cart.remove(idx);
                renderCurrentScreen();
            }));
            row.addView(qtyCtrl);

            contentContainer.addView(row);
            contentContainer.addView(space(dp(10)));
        }

        long subtotal = calculateCartSubtotal();
        long disc = appliedCouponCode.isEmpty() ? 0L : Math.max(0L, db.evaluateCouponDiscount(appliedCouponCode, subtotal));
        long afterDisc = Math.max(0L, subtotal - disc);
        long shipping = afterDisc >= 2_500_000L ? 0L : 65_000L;
        long total = afterDisc + shipping;

        LinearLayout formCard = sleekWhiteCard();
        EditText nameIn = createInput("نام و نام خانوادگی تحویل‌گیرنده", db.getSetting("customer_name", ""));
        EditText mobIn = createInput("شماره موبایل (۱۱ رقمی)", db.getSetting("customer_mobile", ""));
        EditText addrIn = createInput("نشانی دقیق پستی جهت ارسال", db.getSetting("customer_address", ""));

        CheckBox giftChk = new CheckBox(this);
        giftChk.setText("ارسال در بسته‌بندی شیک هدیه مجموعه ASIYE (رایگان)");
        giftChk.setTextColor(COL_TEXT_MAIN);
        giftChk.setChecked(vipGiftPackaging);
        giftChk.setOnCheckedChangeListener((b, c) -> vipGiftPackaging = c);

        String[][] payModes = new String[][]{
                {"snapp_pay", "پرداخت اعتباری ۴ قسط بدون کارمزد (اسنپ‌پی / تارا)"},
                {"zarinpal", "پرداخت آنلاین نقدی (کلیه کارت‌های عضو شتاب)"}
        };

        formCard.addView(nameIn);
        formCard.addView(space(dp(8)));
        formCard.addView(mobIn);
        formCard.addView(space(dp(8)));
        formCard.addView(addrIn);
        formCard.addView(space(dp(8)));
        formCard.addView(giftChk);
        formCard.addView(space(dp(10)));

        for (String[] pm : payModes) {
            boolean sel = pm[0].equals(selectedPaymentMethod);
            TextView opt = new TextView(this);
            opt.setText((sel ? "◉  " : "○  ") + pm[1]);
            opt.setTextColor(COL_TEXT_MAIN);
            opt.setTypeface(sel ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            opt.setPadding(dp(12), dp(10), dp(12), dp(10));
            opt.setBackground(roundedShape(sel ? 0x2EC8A46B : COL_SILK_CREAM, dp(12), sel ? COL_GOLD : COL_BORDER, dp(1)));
            opt.setOnClickListener(v -> {
                selectedPaymentMethod = pm[0];
                renderCurrentScreen();
            });
            formCard.addView(opt);
            formCard.addView(space(dp(6)));
        }

        // Clean Obsidian Summary Box
        LinearLayout summaryBox = new LinearLayout(this);
        summaryBox.setOrientation(LinearLayout.VERTICAL);
        summaryBox.setPadding(dp(16), dp(14), dp(16), dp(14));
        summaryBox.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(16), COL_GOLD, dp(1)));

        TextView totalTv = new TextView(this);
        totalTv.setText("مبلغ قابل پرداخت: " + PersianFormatter.formatToman(total) +
                (shipping == 0 ? "\nارسال ویژه: رایگان" : "\nهزینه پست پیشتاز: " + PersianFormatter.formatToman(shipping)) +
                ("snapp_pay".equals(selectedPaymentMethod) ? "\nمبلغ هر قسط ماهانه: " + PersianFormatter.formatToman(Math.round(total / 4.0)) : ""));
        totalTv.setTextColor(COL_GOLD_LIGHT);
        totalTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        totalTv.setTypeface(Typeface.DEFAULT_BOLD);
        totalTv.setLineSpacing(dp(4), 1f);
        summaryBox.addView(totalTv);

        formCard.addView(space(dp(6)));
        formCard.addView(summaryBox);
        formCard.addView(space(dp(14)));

        View confirmOnline = buttonWith3DIcon("ثبت نهایی سفارش در اپلیکیشن", Luxury3DIconView.ICON_SHIELD, true, () -> {
            String cName = nameIn.getText().toString().trim();
            String cMob = mobIn.getText().toString().trim();
            String cAddr = addrIn.getText().toString().trim();
            if (cName.length() < 3 || !PersianFormatter.isValidIranMobile(cMob) || cAddr.length() < 8) {
                toast("لطفاً نام، شماره موبایل معتبر و نشانی را کامل وارد فرمایید.");
                return;
            }
            db.setSetting("customer_name", cName);
            db.setSetting("customer_mobile", cMob);
            db.setSetting("customer_address", cAddr);
            try {
                Models.OrderRecord ord = db.executeCheckout(cart, cName, cMob, cAddr,
                        selectedPaymentMethod, "snapp_pay".equals(selectedPaymentMethod) ? 4 : 1,
                        appliedCouponCode, vipGiftPackaging);
                cart.clear();
                toast("سفارش " + ord.orderNumber + " با موفقیت ثبت شد.");
                customerTab = 3;
                renderCurrentScreen();
            } catch (Exception ex) {
                toast(ex.getMessage());
            }
        });
        formCard.addView(confirmOnline);
        formCard.addView(space(dp(8)));

        View sendWaOrder = buttonWith3DIcon("ارسال هوشمند همین سبد در واتس‌اپ مدیریت", Luxury3DIconView.ICON_WHATSAPP, false, () ->
                openSmartWhatsApp("تکمیل و ثبت سفارش اقلام سبد خرید اپلیکیشن", ""));
        formCard.addView(sendWaOrder);

        contentContainer.addView(formCard);
    }

    // =========================================================================
    // TAB 3: EXECUTIVE MANAGEMENT (سرکار خانم زنگنه) + @asiyeh.store + MILAD YAGHOOBI
    // =========================================================================

    private void renderExecutiveConciergeAndDesignerScreen() {
        // 1. Executive Management & Direct Connection Sanctuary
        LinearLayout sanctuary = new LinearLayout(this);
        sanctuary.setOrientation(LinearLayout.VERTICAL);
        sanctuary.setBackground(roundedShape(COL_OBSIDIAN_CARD, dp(24), COL_GOLD, dp(1)));
        sanctuary.setPadding(dp(20), dp(20), dp(20), dp(20));

        TextView roleKicker = new TextView(this);
        roleKicker.setText("EXECUTIVE MANAGEMENT & VIP CONCIERGE");
        roleKicker.setTextColor(COL_GOLD);
        roleKicker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        roleKicker.setLetterSpacing(0.12f);

        TextView ownerTitle = new TextView(this);
        ownerTitle.setText("مدیریت مجموعه: سرکار خانم زنگنه");
        ownerTitle.setTextColor(COL_WHITE);
        ownerTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        ownerTitle.setTypeface(Typeface.DEFAULT_BOLD);
        ownerTitle.setPadding(0, dp(4), 0, dp(4));

        TextView ownerBio = new TextView(this);
        ownerBio.setText("جهت مشاوره تخصصی انتخاب محصولات پوست و مو، استعلام موجودی و ثبت سفارش مستقیم، از درگاه‌های ارتباطی زیر استفاده فرمایید. در پیام واتس‌اپ، کد مرجع هوشمند برنامه به صورت خودکار درج می‌گردد.");
        ownerBio.setTextColor(0xFFD6CFC3);
        ownerBio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        ownerBio.setLineSpacing(dp(3), 1f);

        // LTR-Safe Luxury Phone Pill
        TextView phonePill = new TextView(this);
        phonePill.setText("خط مستقیم مدیریت:   " + OWNER_PHONE_DISPLAY);
        phonePill.setTextDirection(View.TEXT_DIRECTION_RTL);
        phonePill.setTextColor(COL_GOLD_LIGHT);
        phonePill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        phonePill.setTypeface(Typeface.DEFAULT_BOLD);
        phonePill.setPadding(dp(14), dp(8), dp(14), dp(8));
        phonePill.setBackground(roundedShape(0x18FFFFFF, dp(18), 0x66C8A46B, dp(1)));
        LinearLayout.LayoutParams ppLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ppLp.setMargins(0, dp(12), 0, dp(14));
        phonePill.setLayoutParams(ppLp);

        // Smart Topic Spinner for WhatsApp & SMS
        String[] smartTopics = new String[]{
                "مشاوره تخصصی انتخاب محصول مناسب پوست و مو",
                "ثبت سفارش مستقیم محصولات گاتیو / لاکچری کوین / اوردینری",
                "استعلام موجودی، قیمت روز و هماهنگی ارسال فوری"
        };
        Spinner topicSp = new Spinner(this);
        topicSp.setBackground(roundedShape(COL_SILK_CREAM, dp(14), COL_GOLD, dp(1)));
        topicSp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, smartTopics));
        topicSp.setSelection(selectedSmartTopicIdx);

        sanctuary.addView(roleKicker);
        sanctuary.addView(ownerTitle);
        sanctuary.addView(ownerBio);
        sanctuary.addView(phonePill);
        sanctuary.addView(topicSp);
        sanctuary.addView(space(dp(16)));

        // 2x2 Grid of 3D Obsidian-Crystal & 24K Gold Medallions
        LinearLayout gridRow1 = new LinearLayout(this);
        gridRow1.setOrientation(LinearLayout.HORIZONTAL);

        View callMedallion = buildContactMedallionCard(
                Luxury3DIconView.ICON_PHONE,
                "تماس تلفنی",
                OWNER_PHONE_DISPLAY,
                () -> openDialer()
        );
        callMedallion.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        View smsMedallion = buildContactMedallionCard(
                Luxury3DIconView.ICON_SMS,
                "ارسال پیامک",
                "متن هوشمند آماده",
                () -> {
                    selectedSmartTopicIdx = topicSp.getSelectedItemPosition();
                    openSmartSms(smartTopics[selectedSmartTopicIdx]);
                }
        );
        LinearLayout.LayoutParams smLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        smLp.setMarginStart(dp(10));
        smsMedallion.setLayoutParams(smLp);

        gridRow1.addView(callMedallion);
        gridRow1.addView(smsMedallion);
        sanctuary.addView(gridRow1);
        sanctuary.addView(space(dp(10)));

        LinearLayout gridRow2 = new LinearLayout(this);
        gridRow2.setOrientation(LinearLayout.HORIZONTAL);

        View waMedallion = buildContactMedallionCard(
                Luxury3DIconView.ICON_WHATSAPP,
                "واتس‌اپ هوشمند",
                "با شناسه رسمی برنامه",
                () -> {
                    selectedSmartTopicIdx = topicSp.getSelectedItemPosition();
                    openSmartWhatsApp(smartTopics[selectedSmartTopicIdx], "");
                }
        );
        waMedallion.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        View igMedallion = buildContactMedallionCard(
                Luxury3DIconView.ICON_INSTAGRAM,
                "اینستاگرام رسمی",
                "@asiyeh.store",
                () -> openInstagramPage()
        );
        LinearLayout.LayoutParams igLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        igLp.setMarginStart(dp(10));
        igMedallion.setLayoutParams(igLp);

        gridRow2.addView(waMedallion);
        gridRow2.addView(igMedallion);
        sanctuary.addView(gridRow2);

        // Subtle, Non-Cluttered Instagram Highlights Strip inside Sanctuary
        View hair = new View(this);
        hair.setBackgroundColor(0x38C8A46B);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        hLp.setMargins(0, dp(16), 0, dp(12));
        hair.setLayoutParams(hLp);
        sanctuary.addView(hair);

        TextView igCap = new TextView(this);
        igCap.setText("هایلایت‌های منتخب پیج رسمی @asiyeh.store:");
        igCap.setTextColor(COL_GOLD_LIGHT);
        igCap.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        igCap.setTypeface(Typeface.DEFAULT_BOLD);
        sanctuary.addView(igCap);
        sanctuary.addView(space(dp(8)));

        HorizontalScrollView hlScroll = new HorizontalScrollView(this);
        hlScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout hlRow = new LinearLayout(this);
        hlRow.setOrientation(LinearLayout.HORIZONTAL);
        String[] highlights = new String[]{"گاتیو و لاکچری", "اوردینری اصل", "مکمل و تونیک مو", "رضایت مشتریان"};
        for (String hl : highlights) {
            TextView chip = new TextView(this);
            chip.setText("✦ " + hl);
            chip.setTextColor(0xFFF2ECE1);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            chip.setPadding(dp(12), dp(6), dp(12), dp(6));
            chip.setBackground(roundedShape(0x16FFFFFF, dp(16), 0x55C8A46B, dp(1)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(6));
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> openInstagramPage());
            hlRow.addView(chip);
        }
        hlScroll.addView(hlRow);
        sanctuary.addView(hlScroll);

        contentContainer.addView(sanctuary);
        contentContainer.addView(space(dp(18)));

        // 2. Clean Recent Orders Section
        List<Models.OrderRecord> orders = db.getOrders("all");
        if (!orders.isEmpty()) {
            contentContainer.addView(sectionHeader("سفارش‌های ثبت‌شده اخیر", "وضعیت پردازش و رهگیری مرسوله‌ها"));
            for (Models.OrderRecord ord : orders) {
                LinearLayout oCard = sleekWhiteCard();
                TextView tv = new TextView(this);
                tv.setText("شماره سفارش: " + ord.orderNumber + "  •  " + PersianFormatter.statusLabelFa(ord.status) +
                        "\nمبلغ کل: " + PersianFormatter.formatToman(ord.totalAmount));
                tv.setTextColor(COL_TEXT_MAIN);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
                tv.setLineSpacing(dp(3), 1f);
                oCard.addView(tv);
                contentContainer.addView(oCard);
                contentContainer.addView(space(dp(10)));
            }
        }

        // 3. Exquisite Museum-Grade Designer Plaque — Milad Yaghoobi
        contentContainer.addView(space(dp(8)));
        LinearLayout designerPlaque = new LinearLayout(this);
        designerPlaque.setOrientation(LinearLayout.VERTICAL);
        designerPlaque.setGravity(Gravity.CENTER_HORIZONTAL);
        designerPlaque.setBackground(roundedShape(COL_OBSIDIAN, dp(22), COL_GOLD, dp(1)));
        designerPlaque.setPadding(dp(22), dp(22), dp(22), dp(22));

        Luxury3DIconView crest = new Luxury3DIconView(this, Luxury3DIconView.ICON_CROWN, false);
        crest.setLayoutParams(new LinearLayout.LayoutParams(dp(42), dp(42)));

        TextView dKicker = new TextView(this);
        dKicker.setText("DESIGNED & ARCHITECTED BY");
        dKicker.setTextColor(0xFFA89E8E);
        dKicker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
        dKicker.setLetterSpacing(0.22f);
        dKicker.setGravity(Gravity.CENTER);
        dKicker.setPadding(0, dp(10), 0, dp(3));

        TextView dName = new TextView(this);
        dName.setText("MILAD YAGHOOBI");
        dName.setTextColor(COL_GOLD_LIGHT);
        dName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.5f);
        dName.setTypeface(Typeface.SERIF, Typeface.BOLD);
        dName.setLetterSpacing(0.18f);
        dName.setGravity(Gravity.CENTER);

        View tinyGoldRule = new View(this);
        tinyGoldRule.setBackgroundColor(0x66C8A46B);
        LinearLayout.LayoutParams grLp = new LinearLayout.LayoutParams(dp(64), dp(1));
        grLp.setMargins(0, dp(8), 0, dp(8));
        tinyGoldRule.setLayoutParams(grLp);

        TextView dSub = new TextView(this);
        dSub.setText("طراحی تجربه کاربری لوکس و معماری اکوسیستم دیجیتال\nخلق‌شده به صورت اختصاصی برای مجموعه ASIYE STORE");
        dSub.setTextColor(0xFFC9C1B3);
        dSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        dSub.setGravity(Gravity.CENTER);
        dSub.setLineSpacing(dp(3), 1f);

        designerPlaque.addView(crest);
        designerPlaque.addView(dKicker);
        designerPlaque.addView(dName);
        designerPlaque.addView(tinyGoldRule);
        designerPlaque.addView(dSub);
        contentContainer.addView(designerPlaque);
    }

    private View buildContactMedallionCard(int iconType, String title, String subtitleLtr, Runnable onClick) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(dp(12), dp(14), dp(12), dp(14));
        box.setBackground(roundedShape(0x14FFFFFF, dp(18), 0x66C8A46B, dp(1)));

        Luxury3DIconView icon = new Luxury3DIconView(this, iconType, false);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(COL_GOLD_LIGHT);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(8), 0, dp(2));

        TextView s = new TextView(this);
        s.setText(subtitleLtr);
        s.setTextColor(0xFFCBC3B5);
        s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        s.setGravity(Gravity.CENTER);

        box.addView(icon);
        box.addView(t);
        box.addView(s);
        box.setOnClickListener(v -> onClick.run());
        return box;
    }

    // =========================================================================
    // DIRECT TELEPHONY, SMART SMS, TRACEABLE WHATSAPP & INSTAGRAM INTENTS
    // =========================================================================

    private void openDialer() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + OWNER_PHONE_E164));
            startActivity(intent);
        } catch (Exception e) {
            toast("شماره تماس مستقیم: " + OWNER_PHONE_DISPLAY);
        }
    }

    private void openSmartSms(String topic) {
        try {
            String body = "سلام و عرض ادب خدمت سرکار خانم زنگنه گرامی؛\n" +
                    "این پیامک از طریق اپلیکیشن رسمی ASIYE STORE جهت «" + topic + "» ارسال شده است.";
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + OWNER_PHONE_E164));
            intent.putExtra("sms_body", body);
            startActivity(intent);
        } catch (Exception e) {
            toast("خطا در باز کردن پیام‌رسان تلفن همراه");
        }
    }

    private void openSmartWhatsApp(String topic, String specificProduct) {
        String refCode = "AS-APP-" + (1000 + new Random().nextInt(8999));
        StringBuilder sb = new StringBuilder();
        sb.append("سلام و عرض ادب خدمت سرکار خانم زنگنه گرامی 🌹\n");
        sb.append("این پیام به صورت هوشمند از طریق «اپلیکیشن و وب‌سایت رسمی ASIYE STORE» ارسال شده است.\n");
        sb.append("🔹 کد مرجع پورتال: ").append(refCode).append("\n");
        sb.append("🔹 موضوع درخواست: ").append(topic).append("\n");

        if (specificProduct != null && !specificProduct.isEmpty()) {
            sb.append("🔹 محصول انتخابی: ").append(specificProduct).append("\n");
        } else if (!cart.isEmpty()) {
            sb.append("🔹 اقلام موجود در سبد خرید من:\n");
            for (Models.CartItem c : cart) {
                sb.append("   • ").append(c.product.nameFa)
                  .append(" (").append(PersianFormatter.toPersianDigits(String.valueOf(c.quantity))).append(" عدد)\n");
            }
        }
        sb.append("ممنون می‌شوم جهت راهنمایی و هماهنگی، پاسخ بفرمایید ✨");

        String url = "https://wa.me/" + OWNER_WA_NUMBER + "?text=" + Uri.encode(sb.toString());
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            toast("لطفاً نصب بودن واتس‌اپ یا مرورگر را بررسی فرمایید.");
        }
    }

    private void openInstagramPage() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL)));
        } catch (Exception e) {
            toast("پیج رسمی اینستاگرام: @asiyeh.store");
        }
    }

    // =========================================================================
    // EXECUTIVE ADMIN SUITE (CLEAN & STREAMLINED)
    // =========================================================================

    private void renderAdminDashboard() {
        Models.DashboardSnapshot snap = db.getDashboardSnapshot();
        contentContainer.addView(sectionHeader("گزارش عملکرد مجموعه ASIYE STORE",
                "مدیریت سرکار خانم زنگنه • آمار فروش و سفارش‌ها"));

        LinearLayout kpiCard = sleekWhiteCard();
        TextView kpiTv = new TextView(this);
        kpiTv.setText("فروش ۷ روز اخیر: " + PersianFormatter.formatToman(snap.salesWeek) +
                "\nسفارش‌های فعال در جریان: " + PersianFormatter.toPersianDigits(snap.activeOrdersCount + " سفارش"));
        kpiTv.setTextColor(COL_TEXT_MAIN);
        kpiTv.setTypeface(Typeface.DEFAULT_BOLD);
        kpiTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        kpiTv.setLineSpacing(dp(4), 1f);
        kpiCard.addView(kpiTv);
        contentContainer.addView(kpiCard);
        contentContainer.addView(space(dp(12)));

        LinearLayout chartBox = sleekWhiteCard();
        SalesChartView chartView = new SalesChartView(this);
        chartView.setData(snap.sevenDayRevenue, snap.sevenDayLabels);
        chartView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(190)));
        chartBox.addView(chartView);
        contentContainer.addView(chartBox);
    }

    private void renderAdminProducts() {
        contentContainer.addView(sectionHeader("مدیریت محصولات مجموعه", "افزودن یا ویرایش قیمت محصولات پیج"));
        for (Models.Product p : db.getProducts(false, "همه", "", "newest")) {
            LinearLayout card = sleekWhiteCard();
            TextView tv = new TextView(this);
            tv.setText(p.nameFa + "\nقیمت فروش: " + PersianFormatter.formatToman(p.salePrice) +
                    "  •  موجودی: " + PersianFormatter.toPersianDigits(String.valueOf(p.stock)));
            tv.setTextColor(COL_TEXT_MAIN);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            card.addView(tv);
            contentContainer.addView(card);
            contentContainer.addView(space(dp(8)));
        }
    }

    private void renderAdminOrders() {
        contentContainer.addView(sectionHeader("مدیریت سفارش‌های مشتریان", "به‌روزرسانی وضعیت ارسال مرسوله‌ها"));
        for (Models.OrderRecord ord : db.getOrders("all")) {
            LinearLayout card = sleekWhiteCard();
            TextView tv = new TextView(this);
            tv.setText(ord.orderNumber + "  •  " + ord.customerName + "\n" +
                    "وضعیت: " + PersianFormatter.statusLabelFa(ord.status) + "  •  " + PersianFormatter.formatToman(ord.totalAmount));
            tv.setTextColor(COL_TEXT_MAIN);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            card.addView(tv);
            contentContainer.addView(card);
            contentContainer.addView(space(dp(8)));
        }
    }

    private void renderAdminInventory() {
        contentContainer.addView(sectionHeader("کنترل موجودی انبار", "شارژ سریع موجودی کالاهای اورجینال"));
        for (Models.Product p : db.getProducts(false, "همه", "", "newest")) {
            LinearLayout card = sleekWhiteCard();
            TextView tv = new TextView(this);
            tv.setText(p.nameFa + " — موجودی: " + PersianFormatter.toPersianDigits(p.stock + " عدد"));
            tv.setTextColor(COL_TEXT_MAIN);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setPadding(0, 0, 0, dp(8));

            View plusStock = buttonWith3DIcon("افزایش موجودی (+۵ عدد)", Luxury3DIconView.ICON_PLUS, false, () -> {
                db.adjustInventory(p.id, "in", 5, "شارژ موجودی");
                toast("موجودی «" + p.nameFa + "» افزایش یافت.");
                renderCurrentScreen();
            });
            card.addView(tv);
            card.addView(plusStock);
            contentContainer.addView(card);
            contentContainer.addView(space(dp(10)));
        }
    }

    // =========================================================================
    // REFINED QUIET-LUXURY UI BUILDERS
    // =========================================================================

    private View buttonWith3DIcon(String label, int iconType, boolean goldBackground, Runnable onClick) {
        LinearLayout btn = new LinearLayout(this);
        btn.setOrientation(LinearLayout.HORIZONTAL);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(12), dp(9), dp(14), dp(9));
        btn.setBackground(roundedShape(
                goldBackground ? COL_GOLD : COL_OBSIDIAN_CARD,
                dp(15),
                goldBackground ? COL_GOLD_LIGHT : COL_GOLD,
                dp(1)
        ));

        Luxury3DIconView icon = new Luxury3DIconView(this, iconType, false);
        LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(dp(26), dp(26));
        iLp.setMarginEnd(dp(8));
        icon.setLayoutParams(iLp);

        TextView txt = new TextView(this);
        txt.setText(label);
        txt.setTextColor(goldBackground ? COL_OBSIDIAN : COL_WHITE);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        txt.setTypeface(Typeface.DEFAULT_BOLD);
        txt.setSingleLine(true);

        btn.addView(icon);
        btn.addView(txt);
        btn.setOnClickListener(v -> onClick.run());
        return btn;
    }

    private void addToCart(Models.Product p) {
        for (Models.CartItem ex : cart) {
            if (ex.product.id == p.id) {
                ex.quantity = Math.min(p.stock, ex.quantity + 1);
                toast("«" + p.nameFa + "» به سبد خرید افزوده شد");
                renderCurrentScreen();
                return;
            }
        }
        Models.CartItem item = new Models.CartItem();
        item.product = p;
        item.selectedVariant = p.volume;
        item.quantity = 1;
        cart.add(item);
        toast("«" + p.nameFa + "» به سبد خرید افزوده شد");
        renderCurrentScreen();
    }

    private int totalCartItems() {
        int s = 0;
        for (Models.CartItem c : cart) s += c.quantity;
        return s;
    }

    private long calculateCartSubtotal() {
        long s = 0L;
        for (Models.CartItem c : cart) s += c.lineTotal();
        return s;
    }

    private int resolveProductImage(String key) {
        if ("gatieau_antiwrinkle".equals(key)) return R.drawable.img_gatieau_antiwrinkle;
        if ("luxury_coin".equals(key)) return R.drawable.img_luxury_coin;
        if ("hair_friend".equals(key)) return R.drawable.img_hair_friend;
        if ("ordinary_toner".equals(key)) return R.drawable.img_ordinary_toner;
        if ("moringa_teatree".equals(key)) return R.drawable.img_moringa_teatree;
        return R.drawable.img_gatieau_whitening;
    }

    private LinearLayout sleekWhiteCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(roundedShape(COL_WHITE, dp(18), COL_BORDER, dp(1)));
        card.setElevation(dp(2));
        return card;
    }

    private View sectionHeader(String title, String sub) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(2), dp(2), dp(2), dp(12));
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(COL_TEXT_MAIN);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.5f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        TextView s = new TextView(this);
        s.setText(sub);
        s.setTextColor(COL_TEXT_MUTED);
        s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        box.addView(t);
        box.addView(s);
        return box;
    }

    private EditText createInput(String hint, String value) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setText(value != null ? value : "");
        et.setTextColor(COL_TEXT_MAIN);
        et.setHintTextColor(0xFF9C9488);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        et.setPadding(dp(14), dp(10), dp(14), dp(10));
        et.setBackground(roundedShape(COL_SILK_CREAM, dp(14), COL_BORDER, dp(1)));
        return et;
    }

    private TextView qtyCircle(String txt, Runnable onClick) {
        TextView t = new TextView(this);
        t.setText(txt);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(COL_OBSIDIAN);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        t.setLayoutParams(new LinearLayout.LayoutParams(dp(30), dp(30)));
        t.setBackground(roundedShape(COL_SILK_CREAM, dp(15), COL_GOLD, dp(1)));
        t.setOnClickListener(v -> onClick.run());
        return t;
    }

    private View space(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h));
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
