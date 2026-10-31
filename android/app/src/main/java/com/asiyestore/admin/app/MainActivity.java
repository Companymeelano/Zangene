package com.asiyestore.admin.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.asiyestore.admin.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * ASIYE STORE — ROYAL ATELIER EDITION v5.0
 *
 * Management: سرکار خانم زنگنه (+989044200993 | @asiyeh.store)
 * Designed & Architected by: Milad Yaghoobi
 *
 * Fixes shipped in v5.0:
 *  - BiDi: every LTR token (phone, SKU, handle, latin actives) is isolated with U+2066/U+2069.
 *  - Prices: dedicated two-column row, never clipped or overlapping.
 *  - Chips: weighted single/multi rows, no mid-word clipping at screen edges.
 *  - Header: two quiet actions only (search + admin gate); noisy WhatsApp icon removed.
 *  - Inventory: compact stepper rows instead of repeated giant buttons.
 *  - Catalog admin: thumbnails + edit / publish / archive actions.
 *  - Chart: chronological left-to-right, culminates on "امروز".
 *  - New: smart search, wishlist, coupons, gift message, order tracking, invoice share,
 *         coupon builder, server sync, top-product leaderboard, stock log.
 */
public final class MainActivity extends Activity {

    // ── House constants ─────────────────────────────────────────────────────
    private static final String OWNER_PHONE_E164    = "+989044200993";
    private static final String OWNER_PHONE_DISPLAY = "+98 904 420 0993";
    private static final String OWNER_WA_NUMBER     = "989044200993";
    private static final String INSTAGRAM_URL       = "https://www.instagram.com/asiyeh.store?psln=MnoxdzRzcXcxOGtz";
    private static final String INSTAGRAM_HANDLE    = "@asiyeh.store";
    private static final String ADMIN_MOBILE_1      = "09044200993";
    private static final String ADMIN_MOBILE_2      = "09120000000";
    private static final String ADMIN_EMAIL         = "admin@asiyestore.ir";
    private static final String ADMIN_PASSWORD      = "Asiye@2026";
    private static final String APP_VERSION         = "V6.1 · ATELIER NOIR";

    // ── Palette ────────────────────────────────────────────────────────────
    private static final int COL_OBSIDIAN      = 0xFF0B0B0E;
    private static final int COL_OBSIDIAN_SOFT = 0xFF141319;
    private static final int COL_OBSIDIAN_CARD = 0xFF17161D;
    private static final int COL_GOLD          = 0xFFC8A46B;
    private static final int COL_GOLD_LIGHT    = 0xFFEBD6B0;
    private static final int COL_GOLD_DEEP     = 0xFF8E6C3A;
    private static final int COL_IVORY_BG      = 0xFFFAF8F3;
    private static final int COL_SILK_CREAM    = 0xFFF4EEE3;
    private static final int COL_WHITE         = 0xFFFFFFFF;
    private static final int COL_TEXT_MAIN     = 0xFF1B1A18;
    private static final int COL_TEXT_MUTED    = 0xFF5C554A;
    private static final int COL_TEXT_FAINT    = 0xFF857C6E;
    private static final int COL_BORDER        = 0xFFE6DDCC;
    private static final int COL_SUCCESS       = 0xFF1E6B4B;
    private static final int COL_SUCCESS_SOFT  = 0xFFEAF4EE;
    private static final int COL_DANGER        = 0xFF9E3A33;
    private static final int COL_WARN          = 0xFF9A6B2F;

    private static final int TAB_BOUTIQUE  = 0;
    private static final int TAB_ADVISOR   = 1;
    private static final int TAB_BAG       = 2;
    private static final int TAB_CONCIERGE = 3;

    private static final int ADMIN_REPORT    = 0;
    private static final int ADMIN_CATALOG   = 1;
    private static final int ADMIN_ORDERS    = 2;
    private static final int ADMIN_INVENTORY = 3;
    private static final int ADMIN_SETTINGS  = 4;

    private static final int REQ_PICK_PHOTO = 4401;

    private static final String PAY_INSTALLMENT = "installment";
    private static final String PAY_ONLINE      = "online";
    private static final String PAY_COD         = "cod";

    // ── Session state ──────────────────────────────────────────────────────
    private AsiyeDatabaseHelper db;

    private boolean adminMode = false;
    private boolean adminAuthenticated = false;
    private int customerTab = TAB_BOUTIQUE;
    private int adminTab = ADMIN_REPORT;

    private String selectedCategory = "همه";
    private int advisorConcernIdx = 0;
    private String lastCheckedSerial = "AS-GAT-150";
    private Models.Product verifiedProduct = null;
    private boolean advisorAnswered = false;

    private final List<Models.CartItem> cart = new ArrayList<>();
    private String appliedCouponCode = "";
    private long appliedCouponValue = 0L;
    private String selectedPaymentMethod = "snapp_pay";
    private boolean vipGiftPackaging = true;
    private String giftMessageDraft = "";

    private String adminOrderFilter = "all";
    private String trackingQuery = "";

    // v6.0 · installments, gateways & AI studio
    private String payMode = PAY_INSTALLMENT;
    private String selectedInstallmentId = "";
    private String selectedGateway = "zarinpal";
    private AiProductStudio.Analysis aiAnalysis;
    private AiProductStudio.Draft aiDraft;
    private String aiName = "";
    private String aiBrand = "";
    private Bitmap aiSourceBitmap;
    private String aiStudioPath = "";
    private boolean aiBusy = false;
    private boolean aiStudioOpen = false;
    private EditText aiNameInput;
    private EditText aiBrandInput;
    private EditText aiPriceInput;
    private EditText aiOldPriceInput;
    private EditText aiStockInput;
    private final List<String> settingsTextKeys = new ArrayList<>();
    private final List<EditText> settingsTextInputs = new ArrayList<>();
    private final List<String> settingsCheckKeys = new ArrayList<>();
    private final List<CheckBox> settingsCheckInputs = new ArrayList<>();

    private EditText bagNameInput;
    private EditText bagMobileInput;
    private EditText bagAddressInput;
    private EditText bagGiftInput;
    private EditText bagCouponInput;
    private EditText authSerialInput;
    private EditText trackingInput;
    private TextView couponStatusView;

    // ── Root chrome ────────────────────────────────────────────────────────
    private LinearLayout contentContainer;
    private LinearLayout bottomNavContainer;
    private TextView headerSubtitle;
    private Luxury3DIconView headerActionIcon;
    private ScrollView contentScroll;

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

        bagNameInput = makeInput("نام و نام خانوادگی تحویل‌گیرنده", InputType.TYPE_CLASS_TEXT, false);
        bagMobileInput = makeInput("شماره موبایل ۱۱ رقمی", InputType.TYPE_CLASS_PHONE, true);
        bagAddressInput = makeInput("نشانی دقیق پستی جهت ارسال", InputType.TYPE_CLASS_TEXT, false);
        bagGiftInput = makeInput("متن پیام هدیه روی کارت اختصاصی (اختیاری)", InputType.TYPE_CLASS_TEXT, false);
        bagCouponInput = makeInput("کد تخفیف اختصاصی", InputType.TYPE_CLASS_TEXT, true);
        authSerialInput = makeInput("شناسه اصالت / کد سریال کالا", InputType.TYPE_CLASS_TEXT, true);
        trackingInput = makeInput("کد سفارش خود را وارد کنید", InputType.TYPE_CLASS_TEXT, true);

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

    // ══════════════════════════════════════════════════════════════════════
    //  SHELL
    // ══════════════════════════════════════════════════════════════════════
    private View buildRootShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(COL_IVORY_BG);

        root.addView(buildHeader());

        View hairline = new View(this);
        hairline.setBackgroundColor(0x33C8A46B);
        root.addView(hairline, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        contentScroll = new ScrollView(this);
        contentScroll.setFillViewport(true);
        contentScroll.setClipToPadding(false);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        contentScroll.setLayoutParams(scrollLp);
        contentScroll.setPadding(0, 0, 0, dp(18));

        contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(dp(16), dp(16), dp(16), dp(26));
        contentScroll.addView(contentContainer, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(contentScroll);

        bottomNavContainer = new LinearLayout(this);
        bottomNavContainer.setOrientation(LinearLayout.HORIZONTAL);
        bottomNavContainer.setBackgroundColor(COL_OBSIDIAN);
        bottomNavContainer.setPadding(dp(6), dp(8), dp(6), dp(10));
        bottomNavContainer.setElevation(dp(14));
        root.addView(bottomNavContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return root;
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(COL_OBSIDIAN);
        header.setPadding(dp(14), dp(10), dp(14), dp(10));

        // Brand emblem
        FrameLayout emblemWrap = new FrameLayout(this);
        emblemWrap.setBackground(roundedShape(0xFF100F14, dp(14), 0x66C8A46B, dp(1)));
        emblemWrap.setClipToOutline(true);
        ImageView emblem = new ImageView(this);
        emblem.setImageResource(R.drawable.img_brand_emblem);
        emblem.setScaleType(ImageView.ScaleType.CENTER_CROP);
        emblemWrap.addView(emblem, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams emblemLp = new LinearLayout.LayoutParams(dp(42), dp(42));
        emblemLp.setMarginEnd(dp(11));
        header.addView(emblemWrap, emblemLp);

        // Brand typography
        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.setGravity(Gravity.CENTER_VERTICAL);
        titleCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView brand = new TextView(this);
        brand.setText("ASIYE STORE");
        brand.setTextColor(COL_GOLD_LIGHT);
        brand.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        brand.setTypeface(Typeface.SERIF, Typeface.BOLD);
        brand.setLetterSpacing(0.14f);
        brand.setIncludeFontPadding(false);

        headerSubtitle = new TextView(this);
        headerSubtitle.setText("مدیریت مجموعه: سرکار خانم زنگنه");
        headerSubtitle.setTextColor(0xFFBEB6AA);
        headerSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        headerSubtitle.setIncludeFontPadding(false);
        headerSubtitle.setPadding(0, dp(3), 0, 0);

        titleCol.addView(brand);
        titleCol.addView(headerSubtitle);
        header.addView(titleCol);

        // Quiet actions: search + admin gate (WhatsApp/search clutter removed)
        header.addView(headerActionButton(Luxury3DIconView.ICON_SEARCH, new Runnable() {
            @Override public void run() { openSmartSearchDialog(); }
        }), actionLp(8));

        FrameLayout gate = headerActionButton(Luxury3DIconView.ICON_LOCK, new Runnable() {
            @Override public void run() { handleAdminGate(); }
        });
        headerActionIcon = (Luxury3DIconView) gate.getTag();
        header.addView(gate);

        return header;
    }

    private LinearLayout.LayoutParams actionLp(int marginDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(42), dp(42));
        lp.setMarginEnd(dp(marginDp));
        return lp;
    }

    private FrameLayout headerActionButton(int iconType, final Runnable action) {
        FrameLayout wrap = new FrameLayout(this);
        wrap.setBackground(roundedShape(0xFF131218, dp(13), 0x33C8A46B, dp(1)));
        Luxury3DIconView icon = new Luxury3DIconView(this, iconType, false);
        wrap.addView(icon, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        wrap.setTag(icon);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(42), dp(42));
        wrap.setLayoutParams(lp);
        wrap.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return wrap;
    }

    private void handleAdminGate() {
        if (adminMode) {
            adminMode = false;
            customerTab = TAB_BOUTIQUE;
            headerSubtitle.setText("مدیریت مجموعه: سرکار خانم زنگنه");
            renderCurrentScreen();
            return;
        }
        if (adminAuthenticated) {
            adminMode = true;
            adminTab = ADMIN_REPORT;
            headerSubtitle.setText("پنل مدیریت • سرکار خانم زنگنه");
            renderCurrentScreen();
            return;
        }
        showAdminLoginDialog();
    }

    private void showAdminLoginDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(18));
        box.setBackgroundColor(COL_WHITE);

        box.addView(text("ورود به پنل مدیریت", 19f, COL_TEXT_MAIN, true));
        box.addView(space(4));
        box.addView(text("دسترسی اختصاصی سرکار خانم زنگنه — مدیر مجموعه", 11.5f, COL_TEXT_MUTED, false));
        box.addView(space(16));

        final EditText mobile = makeInput("موبایل مدیریت یا ایمیل", InputType.TYPE_CLASS_TEXT, true);
        mobile.setText(ADMIN_MOBILE_1);
        final EditText password = makeInput("رمز عبور اختصاصی", InputType.TYPE_TEXT_VARIATION_PASSWORD, false);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        box.addView(mobile);
        box.addView(space(10));
        box.addView(password);
        box.addView(space(8));
        box.addView(text("شناسه: " + ltr("09044200993") + "  •  رمز: " + ltr("Asiye@2026"), 10f, COL_TEXT_FAINT, false));
        box.addView(space(16));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ورود امن به پنل فرماندهی", Luxury3DIconView.ICON_SHIELD, new Runnable() {
            @Override public void run() {
                String id = PersianFormatter.toAsciiDigits(mobile.getText().toString()).trim();
                String pass = password.getText().toString().trim();
                boolean idOk = ADMIN_MOBILE_1.equals(id) || ADMIN_MOBILE_2.equals(id)
                        || ADMIN_EMAIL.equalsIgnoreCase(id);
                if (idOk && ADMIN_PASSWORD.equals(pass)) {
                    adminAuthenticated = true;
                    adminMode = true;
                    adminTab = ADMIN_REPORT;
                    headerSubtitle.setText("پنل مدیریت • سرکار خانم زنگنه");
                    dialog.dismiss();
                    renderCurrentScreen();
                    toast("خوش آمدید خانم زنگنه — پنل مدیریت فعال شد.");
                } else {
                    toast("شناسه یا رمز عبور مدیریت صحیح نیست.");
                }
            }
        }));
        box.addView(space(8));
        box.addView(ghostButton("انصراف", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));

        showRoundedDialog(dialog);
    }

    private void showRoundedDialog(AlertDialog dialog) {
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(roundedShape(COL_WHITE, dp(22), 0, 0));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            w.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ROUTER
    // ══════════════════════════════════════════════════════════════════════
    private void renderCurrentScreen() {
        if (headerActionIcon != null) {
            headerActionIcon.setIconState(adminMode ? Luxury3DIconView.ICON_CROWN
                    : Luxury3DIconView.ICON_LOCK, adminMode);
        }
        contentContainer.removeAllViews();
        bottomNavContainer.removeAllViews();

        try {
            if (adminMode) {
                if (aiStudioOpen) {
                    renderAiStudio();
                } else {
                    switch (adminTab) {
                        case ADMIN_CATALOG:   renderAdminCatalog();   break;
                        case ADMIN_ORDERS:    renderAdminOrders();    break;
                        case ADMIN_INVENTORY: renderAdminInventory(); break;
                        case ADMIN_SETTINGS:  renderAdminSettings();  break;
                        default:              renderAdminReport();    break;
                    }
                }
                renderAdminNav();
            } else {
                switch (customerTab) {
                    case TAB_ADVISOR:   renderAdvisor();   break;
                    case TAB_BAG:       renderBag();       break;
                    case TAB_CONCIERGE: renderConcierge(); break;
                    default:            renderBoutique();  break;
                }
                renderCustomerNav();
            }
        } catch (Throwable t) {
            // Defensive shell: one broken component must never blank the whole store.
            contentContainer.removeAllViews();
            bottomNavContainer.removeAllViews();
            contentContainer.addView(safeModeCard(t));
            if (adminMode) renderAdminNav(); else renderCustomerNav();
        }
        contentScroll.post(new Runnable() {
            @Override public void run() { contentScroll.scrollTo(0, 0); }
        });
    }

    /** Last-resort recovery UI, keeps navigation alive if a screen fails to build. */
    private View safeModeCard(Throwable t) {
        LinearLayout card = whiteCard();
        card.addView(text("بازسازی امن صفحه", 14f, COL_TEXT_MAIN, true));
        card.addView(space(6));
        TextView body = text("این صفحه به‌صورت کامل بازسازی شد تا تجربه فروشگاه متوقف نشود. "
                + "اطلاعات سبد و سفارش‌های شما دست‌نخورده باقی مانده است.", 10.6f, COL_TEXT_MUTED, false);
        body.setLineSpacing(dp(3), 1f);
        card.addView(body);
        card.addView(space(12));
        card.addView(primaryButton("بازگشت به ویترین", Luxury3DIconView.ICON_BOUTIQUE, new Runnable() {
            @Override public void run() {
                adminMode = false;
                aiStudioOpen = false;
                customerTab = TAB_BOUTIQUE;
                renderCurrentScreen();
            }
        }));
        return card;
    }

    private void switchCustomerTab(int tab) {
        if (customerTab != tab) {
            customerTab = tab;
            renderCurrentScreen();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BOTTOM NAVIGATION
    // ══════════════════════════════════════════════════════════════════════
    private void renderCustomerNav() {
        bottomNavContainer.addView(navTab(Luxury3DIconView.ICON_BOUTIQUE, "ویترین",
                customerTab == TAB_BOUTIQUE, 0, new Runnable() {
                    @Override public void run() { switchCustomerTab(TAB_BOUTIQUE); }
                }));
        bottomNavContainer.addView(navTab(Luxury3DIconView.ICON_SHIELD, "مشاور و اصالت",
                customerTab == TAB_ADVISOR, 0, new Runnable() {
                    @Override public void run() { switchCustomerTab(TAB_ADVISOR); }
                }));
        bottomNavContainer.addView(navTab(Luxury3DIconView.ICON_BAG, "سبد خرید",
                customerTab == TAB_BAG, totalCartItems(), new Runnable() {
                    @Override public void run() { switchCustomerTab(TAB_BAG); }
                }));
        bottomNavContainer.addView(navTab(Luxury3DIconView.ICON_CROWN, "مدیریت و ارتباط",
                customerTab == TAB_CONCIERGE, 0, new Runnable() {
                    @Override public void run() { switchCustomerTab(TAB_CONCIERGE); }
                }));
    }

    private void renderAdminNav() {
        bottomNavContainer.addView(navTab(Luxury3DIconView.ICON_CHART, "گزارش",
                adminTab == ADMIN_REPORT, 0, new Runnable() {
                    @Override public void run() { aiStudioOpen = false; adminTab = ADMIN_REPORT; renderCurrentScreen(); }
                }));
        bottomNavContainer.addView(navTab(Luxury3DIconView.ICON_BOUTIQUE, "محصولات",
                adminTab == ADMIN_CATALOG, 0, new Runnable() {
                    @Override public void run() { aiStudioOpen = false; adminTab = ADMIN_CATALOG; renderCurrentScreen(); }
                }));
        bottomNavContainer.addView(navTab(Luxury3DIconView.ICON_BAG, "سفارش‌ها",
                adminTab == ADMIN_ORDERS, 0, new Runnable() {
                    @Override public void run() { aiStudioOpen = false; adminTab = ADMIN_ORDERS; renderCurrentScreen(); }
                }));
        bottomNavContainer.addView(navTab(Luxury3DIconView.ICON_GIFT, "انبار",
                adminTab == ADMIN_INVENTORY, 0, new Runnable() {
                    @Override public void run() { aiStudioOpen = false; adminTab = ADMIN_INVENTORY; renderCurrentScreen(); }
                }));
        bottomNavContainer.addView(navTab(Luxury3DIconView.ICON_SETTINGS, "تنظیمات",
                adminTab == ADMIN_SETTINGS, 0, new Runnable() {
                    @Override public void run() { aiStudioOpen = false; adminTab = ADMIN_SETTINGS; renderCurrentScreen(); }
                }));
    }

    private View navTab(int iconType, String label, boolean selected, int badge, final Runnable action) {
        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(0, dp(2), 0, 0);
        tab.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tab.setBackground(selected
                ? roundedShape(0x14C8A46B, dp(16), 0x00FFFFFF, 0)
                : roundedShape(0x00000000, dp(16), 0x00FFFFFF, 0));
        tab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });

        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(34)));
        Luxury3DIconView icon = new Luxury3DIconView(this, iconType, selected);
        iconWrap.addView(icon, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (badge > 0) {
            TextView badgeView = new TextView(this);
            badgeView.setText(PersianFormatter.toPersianDigits(String.valueOf(badge)));
            badgeView.setTextColor(COL_OBSIDIAN);
            badgeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f);
            badgeView.setTypeface(Typeface.DEFAULT_BOLD);
            badgeView.setGravity(Gravity.CENTER);
            badgeView.setBackground(roundedShape(COL_GOLD_LIGHT, dp(20), 0xFF0B0B0E, dp(1)));
            FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(dp(16), dp(16));
            blp.gravity = Gravity.TOP | Gravity.END;
            blp.setMargins(0, 0, dp(1), 0);
            iconWrap.addView(badgeView, blp);
        }

        TextView caption = new TextView(this);
        caption.setText(label);
        caption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.2f);
        caption.setSingleLine(true);
        caption.setEllipsize(TextUtils.TruncateAt.END);
        caption.setGravity(Gravity.CENTER);
        caption.setTextColor(selected ? COL_GOLD_LIGHT : 0xFF948E85);
        caption.setPadding(0, dp(3), 0, 0);
        if (selected) caption.setTypeface(Typeface.DEFAULT_BOLD);

        TextView indicator = new TextView(this);
        indicator.setText(" ");
        indicator.setTextSize(TypedValue.COMPLEX_UNIT_SP, 2f);
        indicator.setTextColor(0x00000000);
        indicator.setIncludeFontPadding(false);

        tab.addView(iconWrap);
        tab.addView(caption);
        tab.addView(indicator);
        return tab;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  1 · BOUTIQUE (ویترین)
    // ══════════════════════════════════════════════════════════════════════
    private void renderBoutique() {
        contentContainer.addView(pageTitle("ویترین اختصاصی",
                "کلکسیون منتخب پوست، مو و زیبایی — اورجینال، با ضمانت اصالت"));

        contentContainer.addView(buildHeroCard());
        contentContainer.addView(space(10));
        contentContainer.addView(trustStrip());
        contentContainer.addView(space(20));

        // Section head + wishlist shortcut
        contentContainer.addView(pageTitle("کلکسیون امضایی", "شش کالای منتخب از پیج رسمی مجموعه"));
        contentContainer.addView(space(10));

        contentContainer.addView(categoryChips());
        contentContainer.addView(space(14));

        List<Models.Product> products = db.getProducts(true, selectedCategory, "", "newest");
        if (products.isEmpty()) {
            contentContainer.addView(emptyCard(Luxury3DIconView.ICON_HEART, "هنوز کالایی در منتخب‌های شما نیست",
                    "قلب بالای هر کالا را لمس کنید تا در ویترین اختصاصی شما ذخیره شود."));
        } else {
            for (Models.Product p : products) {
                contentContainer.addView(buildProductCard(p));
                contentContainer.addView(space(16));
            }
        }

        contentContainer.addView(space(4));
        contentContainer.addView(footerNote());
    }

    private View buildHeroCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_OBSIDIAN, dp(24), 0x40C8A46B, dp(1)));
        card.setClipToOutline(true);

        FrameLayout heroWrap = new FrameLayout(this);
        heroWrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(168)));
        heroWrap.setClipToOutline(true);
        heroWrap.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(18), 0, 0));
        ImageView hero = new ImageView(this);
        hero.setImageResource(R.drawable.img_hero_curated);
        hero.setScaleType(ImageView.ScaleType.CENTER_CROP);
        heroWrap.addView(hero, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // gradient scrim for legibility
        View scrim = new View(this);
        scrim.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0xE60B0B0E, 0x330B0B0E, 0x000B0B0E}));
        heroWrap.addView(scrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView heroTag = new TextView(this);
        heroTag.setText("SIGNATURE COLLECTION");
        heroTag.setTextColor(COL_GOLD_LIGHT);
        heroTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f);
        heroTag.setLetterSpacing(0.22f);
        heroTag.setTypeface(Typeface.SERIF, Typeface.BOLD);
        heroTag.setPadding(dp(9), dp(4), dp(9), dp(4));
        heroTag.setBackground(roundedShape(0xB30B0B0E, dp(9), 0x4DC8A46B, dp(1)));
        FrameLayout.LayoutParams tagLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tagLp.gravity = Gravity.TOP | Gravity.START;
        tagLp.setMargins(dp(14), dp(14), 0, 0);
        heroWrap.addView(heroTag, tagLp);

        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(168));
        heroLp.setMargins(dp(10), dp(10), dp(10), 0);
        card.addView(heroWrap, heroLp);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(18), dp(16), dp(18), dp(18));

        TextView eyebrow = text("اصالت تضمین‌شده • کلکسیون تخصصی", 10.5f, COL_GOLD, false);
        eyebrow.setLetterSpacing(0.06f);

        TextView title = text("زیبایی اصیل، با انتخاب‌های علمی و ماندگار", 17.5f, 0xFFF6EFE3, true);
        title.setLineSpacing(dp(4), 1f);
        title.setPadding(0, dp(6), 0, dp(6));

        TextView bodyText = text("گلچینی از بهترین محصولات اورجینال گاتیو، لاکچری کوین، اوردینری و هیرفرند؛ همراه با مشاوره تخصصی مستقیم از مدیریت مجموعه.",
                11.5f, 0xFFBEB6AA, false);
        bodyText.setLineSpacing(dp(4), 1f);

        body.addView(eyebrow);
        body.addView(title);
        body.addView(bodyText);
        body.addView(space(16));

        LinearLayout actions = row(Gravity.CENTER_VERTICAL);
        actions.addView(primaryButton("مشاور روتین", Luxury3DIconView.ICON_ADVISOR, new Runnable() {
            @Override public void run() { switchCustomerTab(TAB_ADVISOR); }
        }), weight(1f));
        actions.addView(spaceH(10));
        actions.addView(outlineGoldButton("ارتباط با مدیر", Luxury3DIconView.ICON_CROWN, new Runnable() {
            @Override public void run() { switchCustomerTab(TAB_CONCIERGE); }
        }), weight(1f));
        body.addView(actions);

        card.addView(body);
        return card;
    }

    private View trustStrip() {
        TextView strip = new TextView(this);
        strip.setText("ضمانت اصالت   •   ارسال سریع VIP   •   پرداخت در ۴ قسط");
        strip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.6f);
        strip.setTextColor(0xFF5A5244);
        strip.setTypeface(Typeface.DEFAULT_BOLD);
        strip.setGravity(Gravity.CENTER);
        strip.setPadding(dp(12), dp(9), dp(12), dp(9));
        strip.setBackground(roundedShape(0xFFFFFDF9, dp(14), 0xFFEFE6D6, dp(1)));
        return strip;
    }

    private View categoryChips() {
        String[][] chips = {
                {"همه", "همه"},
                {"پوست", "مراقبت تخصصی پوست"},
                {"میکاپ", "میکاپ لوکس"},
                {"مو", "تقویت و درمان مو"},
                {"منتخب‌ها", "منتخب‌های من"}
        };
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        for (int i = 0; i < chips.length; i++) {
            final String category = chips[i][1];
            boolean active = selectedCategory.equals(category);
            TextView chip = new TextView(this);
            String chipLabel = chips[i][0];
            if ("منتخب‌های من".equals(category)) {
                int wished = db.getWishlistCount();
                chipLabel = "♥ " + chipLabel + (wished > 0
                        ? " " + PersianFormatter.toPersianDigits(String.valueOf(wished)) : "");
            }
            chip.setText(chipLabel);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            chip.setSingleLine(true);
            chip.setGravity(Gravity.CENTER);
            chip.setTextColor(active ? COL_GOLD_LIGHT : COL_TEXT_MUTED);
            chip.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            chip.setPadding(dp(6), dp(9), dp(6), dp(9));
            chip.setBackground(active
                    ? roundedShape(COL_OBSIDIAN, dp(18), COL_OBSIDIAN, dp(1))
                    : roundedShape(COL_WHITE, dp(18), COL_BORDER, dp(1)));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    selectedCategory = category;
                    renderCurrentScreen();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i > 0) lp.setMarginStart(dp(6));
            panel.addView(chip, lp);
        }
        return panel;
    }

    private View buildProductCard(final Models.Product p) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_WHITE, dp(22), COL_BORDER, dp(1)));
        card.setClipToOutline(true);

        // Image block
        FrameLayout imageWrap = new FrameLayout(this);
        imageWrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(212)));

        ImageView image = new ImageView(this);
        Bitmap cardBitmap = loadProductBitmap(p);
        if (cardBitmap != null) image.setImageBitmap(cardBitmap);
        else image.setImageResource(resolveProductImage(p.imageKey));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageWrap.addView(image, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // subtle bottom scrim
        View scrim = new View(this);
        scrim.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0x330B0B0E, 0x000B0B0E}));
        imageWrap.addView(scrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(60), Gravity.BOTTOM));

        // discount badge (top-start) — no overlap with anything
        TextView badge = new TextView(this);
        badge.setText(PersianFormatter.toPersianDigits(String.valueOf(Math.max(0, p.discountPercent))) + "٪ تخفیف");
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setTextColor(COL_GOLD_LIGHT);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(5), dp(10), dp(5));
        badge.setBackground(roundedShape(0xE6100F14, dp(14), 0x55C8A46B, dp(1)));
        FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeLp.gravity = Gravity.TOP | Gravity.START;
        badgeLp.setMargins(dp(12), dp(12), 0, 0);
        imageWrap.addView(badge, badgeLp);

        // wishlist heart (top-end)
        final boolean[] wish = {p.wishlisted};
        final TextView heart = new TextView(this);
        heart.setText(wish[0] ? "♥" : "♡");
        heart.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        heart.setGravity(Gravity.CENTER);
        heart.setTextColor(wish[0] ? COL_GOLD_DEEP : 0xFFF2EDE4);
        heart.setBackground(roundedShape(0xCC14131A, dp(22), 0x66C8A46B, dp(1)));
        FrameLayout.LayoutParams heartLp = new FrameLayout.LayoutParams(dp(38), dp(38));
        heartLp.gravity = Gravity.TOP | Gravity.END;
        heartLp.setMargins(0, dp(12), dp(12), 0);
        heart.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                wish[0] = !wish[0];
                db.toggleWishlist(p.id, wish[0]);
                heart.setText(wish[0] ? "♥" : "♡");
                heart.setTextColor(wish[0] ? COL_GOLD_DEEP : 0xFFF2EDE4);
                toast(wish[0] ? "به منتخب‌های شما اضافه شد." : "از منتخب‌های شما حذف شد.");
            }
        });
        imageWrap.addView(heart, heartLp);
        card.addView(imageWrap);

        // Body
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(14), dp(16), dp(16));

        TextView brandLine = text(p.nameEn, 9.5f, COL_GOLD_DEEP, false);
        brandLine.setLetterSpacing(0.06f);
        brandLine.setSingleLine(true);
        brandLine.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView name = text(p.nameFa, 15.5f, COL_TEXT_MAIN, true);
        name.setLineSpacing(dp(3), 1f);
        name.setPadding(0, dp(4), 0, dp(4));

        TextView desc = text(p.description, 11.8f, COL_TEXT_MUTED, false);
        desc.setMaxLines(3);
        desc.setEllipsize(android.text.TextUtils.TruncateAt.END);
        desc.setLineSpacing(dp(3), 1f);

        body.addView(brandLine);
        body.addView(name);
        body.addView(desc);
        body.addView(space(10));

        // stock signal only when relevant (no clutter)
        if (p.stock <= p.lowStockThreshold) {
            TextView low = text("تنها " + PersianFormatter.toPersianDigits(String.valueOf(p.stock)) + " عدد در انبار — پیشنهاد می‌کنیم سریع‌تر تصمیم بگیرید",
                    10.5f, COL_DANGER, true);
            low.setPadding(0, 0, 0, dp(8));
            body.addView(low);
        }

        body.addView(priceRow(p));
        body.addView(space(9));
        body.addView(installmentStrip(p));
        body.addView(space(11));

        LinearLayout actions = row(Gravity.CENTER_VERTICAL);
        actions.addView(primaryButton("افزودن به سبد", Luxury3DIconView.ICON_PLUS, new Runnable() {
            @Override public void run() { addToCart(p); }
        }), weight(1f));
        actions.addView(spaceH(8));
        actions.addView(roundIconButton(Luxury3DIconView.ICON_WHATSAPP, new Runnable() {
            @Override public void run() { openSmartWhatsApp("سفارش مستقیم از ویترین", p.nameFa); }
        }));
        actions.addView(spaceH(8));
        actions.addView(roundIconButton(Luxury3DIconView.ICON_SHARE, new Runnable() {
            @Override public void run() { shareProduct(p); }
        }));
        body.addView(actions);

        card.addView(body);
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showEditorialProductDialog(p); }
        });
        return card;
    }

    /**
     * Two-tier price block. Because the strike-through price sits on its own
     * baseline line, the current price can never be clipped or overlapped,
     * even on narrow 360dp screens with 7-digit amounts.
     */
    private View priceRow(Models.Product p) {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        LinearLayout top = row(Gravity.CENTER_VERTICAL);
        TextView price = new TextView(this);
        price.setText(PersianFormatter.formatToman(p.salePrice));
        price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        price.setTypeface(Typeface.DEFAULT_BOLD);
        price.setTextColor(COL_TEXT_MAIN);
        price.setSingleLine(true);
        price.setIncludeFontPadding(false);
        price.setTextDirection(View.TEXT_DIRECTION_RTL);
        top.addView(price);

        if (p.discountPercent > 0) {
            TextView chip = new TextView(this);
            chip.setText(PersianFormatter.toPersianDigits(String.valueOf(p.discountPercent)) + "٪");
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
            chip.setTypeface(Typeface.DEFAULT_BOLD);
            chip.setTextColor(COL_GOLD_DEEP);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(7), dp(2), dp(7), dp(2));
            chip.setBackground(roundedShape(0xFFFBF4E7, dp(9), 0xFFEAD9B8, dp(1)));
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.setMarginStart(dp(8));
            top.addView(chip, clp);
        }
        block.addView(top);

        if (p.originalPrice > p.salePrice) {
            LinearLayout oldRow = row(Gravity.CENTER_VERTICAL);
            oldRow.setPadding(0, dp(5), 0, 0);
            TextView oldLabel = text("قبلاً", 9.2f, 0xFF8C8272, false);
            TextView old = text(PersianFormatter.formatToman(p.originalPrice), 9.8f, 0xFF6F6350, false);
            old.setPaintFlags(old.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            old.setPadding(dp(5), 0, 0, 0);
            TextView saved = text("· صرفه‌جویی " + PersianFormatter.formatToman(p.originalPrice - p.salePrice),
                    9.2f, COL_GOLD_DEEP, false);
            saved.setPadding(dp(6), 0, 0, 0);
            oldRow.addView(oldLabel);
            oldRow.addView(old);
            oldRow.addView(saved);
            block.addView(oldRow);
        }
        return block;
    }

    /** Compact, on-brand BNPL line under every price — its own dedicated slot. */
    private View installmentStrip(Models.Product p) {
        InstallmentEngine.Config cfg = InstallmentEngine.load(db);
        List<Models.InstallmentPlan> plans = InstallmentEngine.build(cfg, p.salePrice);
        if (plans.isEmpty()) return new View(this);

        Models.InstallmentPlan zero = plans.get(0);
        LinearLayout strip = row(Gravity.CENTER_VERTICAL);
        strip.setBackground(roundedShape(0xFFF7F1E4, dp(11), 0xFFE9DCC2, dp(1)));
        strip.setPadding(dp(9), dp(7), dp(9), dp(7));

        FrameLayout icon = new FrameLayout(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        icon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_INSTALLMENT,
                        Luxury3DIconView.PEDESTAL_IVORY),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        strip.addView(icon);

        TextView txt = text(PersianFormatter.toPersianDigits(String.valueOf(zero.months)) + " قسط بدون کارمزد  •  ماهانه "
                        + PersianFormatter.formatToman(zero.monthlyPayment),
                9.2f, 0xFF7C5F31, false);
        txt.setPadding(dp(6), 0, 0, 0);
        txt.setSingleLine(true);
        strip.addView(txt, weight(1f));
        return strip;
    }

    private void showEditorialProductDialog(final Models.Product p) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(COL_WHITE);

        FrameLayout imageWrap = new FrameLayout(this);
        imageWrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(200)));
        imageWrap.setClipToOutline(true);
        imageWrap.setBackground(roundedShape(COL_SILK_CREAM, dp(18), 0, 0));
        ImageView image = new ImageView(this);
        Bitmap dialogBitmap = loadProductBitmapLarge(p);
        if (dialogBitmap != null) image.setImageBitmap(dialogBitmap);
        else image.setImageResource(resolveProductImage(p.imageKey));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageWrap.addView(image, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(200));
        imgLp.setMargins(dp(14), dp(14), dp(14), 0);
        box.addView(imageWrap, imgLp);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(20), dp(16), dp(20), dp(20));

        body.addView(text(p.nameEn, 10f, COL_GOLD_DEEP, false));
        TextView title = text(p.nameFa, 17f, COL_TEXT_MAIN, true);
        title.setPadding(0, dp(6), 0, dp(8));
        title.setLineSpacing(dp(3), 1f);
        body.addView(title);
        body.addView(text(p.description, 12f, COL_TEXT_MUTED, false));

        if (p.benefits != null && !p.benefits.trim().isEmpty()) {
            body.addView(space(12));
            body.addView(infoBlock("مزیت‌های کلیدی این محصول", p.benefits));
        }
        if (p.longDescription != null && !p.longDescription.trim().isEmpty()) {
            body.addView(space(10));
            body.addView(infoBlock("معرفی کامل محصول", p.longDescription));
        }
        body.addView(space(14));

        body.addView(infoBlock("ترکیبات کلیدی", p.ingredients));
        body.addView(space(10));
        body.addView(infoBlock("روش مصرف", p.usageMethod));
        body.addView(space(10));

        LinearLayout meta = row(Gravity.CENTER_VERTICAL);
        meta.addView(tag(p.country), weight(1f));
        meta.addView(tag("حجم: " + p.volume), weight(1f));
        meta.addView(tag("ضمانت اصالت"), weight(1f));
        body.addView(meta);
        body.addView(space(16));

        body.addView(priceRow(p));
        body.addView(space(12));

        InstallmentEngine.Config dialogCfg = InstallmentEngine.load(db);
        List<Models.InstallmentPlan> dialogPlans = InstallmentEngine.build(dialogCfg, p.salePrice);
        if (!dialogPlans.isEmpty()) {
            Models.InstallmentPlan zeroPlan = dialogPlans.get(0);
            LinearLayout inst = row(Gravity.CENTER_VERTICAL);
            inst.setBackground(roundedShape(0xFFFBF4E7, dp(13), 0xFFEFE2C8, dp(1)));
            inst.setPadding(dp(11), dp(9), dp(11), dp(9));
            FrameLayout iIcon = new FrameLayout(this);
            iIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(22), dp(22)));
            iIcon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_INSTALLMENT,
                            Luxury3DIconView.PEDESTAL_IVORY),
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            inst.addView(iIcon);
            TextView iText = text(PersianFormatter.toPersianDigits(String.valueOf(zeroPlan.months))
                    + " قسط بدون کارمزد · ماهانه " + PersianFormatter.formatToman(zeroPlan.monthlyPayment),
                    9.6f, COL_GOLD_DEEP, true);
            iText.setPadding(dp(7), 0, 0, 0);
            inst.addView(iText, weight(1f));
            body.addView(inst);
        }
        body.addView(space(12));

        LinearLayout actions = row(Gravity.CENTER_VERTICAL);
        actions.addView(primaryButton("افزودن به سبد خرید", Luxury3DIconView.ICON_PLUS, new Runnable() {
            @Override public void run() { addToCart(p); }
        }), weight(1f));
        actions.addView(spaceH(8));
        actions.addView(outlineDarkButton("سفارش واتس‌اپ", Luxury3DIconView.ICON_WHATSAPP, new Runnable() {
            @Override public void run() { openSmartWhatsApp("سفارش مستقیم کالا", p.nameFa); }
        }), weight(1f));
        body.addView(actions);

        box.addView(body);
        scroll.addView(box);

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(scroll).create();
        showRoundedDialog(dialog);
        Window w = dialog.getWindow();
        if (w != null) {
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.86f);
            w.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.94f), height);
        }
    }

    private View infoBlock(String label, String value) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setBackground(roundedShape(COL_IVORY_BG, dp(14), 0xFFF0E9DC, dp(1)));
        wrap.setPadding(dp(13), dp(11), dp(13), dp(11));
        TextView l = text(label, 9.5f, COL_GOLD_DEEP, false);
        l.setLetterSpacing(0.05f);
        TextView v = text(value == null ? "—" : value, 11.5f, COL_TEXT_MAIN, false);
        v.setLineSpacing(dp(3), 1f);
        v.setPadding(0, dp(4), 0, 0);
        wrap.addView(l);
        wrap.addView(v);
        return wrap;
    }

    private View tag(String label) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(COL_TEXT_MUTED);
        t.setSingleLine(true);
        t.setPadding(dp(4), dp(7), dp(4), dp(7));
        t.setBackground(roundedShape(COL_SILK_CREAM, dp(10), COL_BORDER, dp(1)));
        return t;
    }

    private void shareProduct(Models.Product p) {
        String message = "✨ " + p.nameFa + "\n" +
                "قیمت ویژه: " + PersianFormatter.formatToman(p.salePrice) + "\n" +
                "کد کالا: " + ltr(p.sku) + "\n" +
                "مجموعه ASIYE STORE — مدیریت سرکار خانم زنگنه\n" +
                "سفارش و مشاوره: " + ltr(OWNER_PHONE_DISPLAY) + "\n" +
                INSTAGRAM_URL;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, message);
        try {
            startActivity(Intent.createChooser(share, "ارسال کارت کالا"));
        } catch (Exception e) {
            toast("امکان اشتراک‌گذاری در این دستگاه فعال نیست.");
        }
    }

    private View footerNote() {
        LinearLayout foot = new LinearLayout(this);
        foot.setOrientation(LinearLayout.VERTICAL);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, dp(8), 0, dp(8));

        TextView line = text("ASIYE STORE  ·  طراحی: میلاد یعقوبی  ·  " + APP_VERSION, 8.8f, 0xFF9A9186, false);
        line.setGravity(Gravity.CENTER);
        line.setLetterSpacing(0.06f);
        foot.addView(line);
        return foot;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  2 · ADVISOR & AUTHENTICITY (مشاور و اصالت)
    // ══════════════════════════════════════════════════════════════════════
    private void renderAdvisor() {
        contentContainer.addView(pageTitle("مشاور هوشمند و اصالت کالا",
                "پیشنهاد دقیق متناسب با نیاز پوست و موی شما، با ضمانت اصالت"));

        // ── Advisor (single obsidian card, no nested dark boxes) ───────────
        LinearLayout advisorCard = new LinearLayout(this);
        advisorCard.setOrientation(LinearLayout.VERTICAL);
        advisorCard.setBackground(roundedShape(COL_OBSIDIAN, dp(24), 0x40C8A46B, dp(1)));
        advisorCard.setPadding(dp(18), dp(18), dp(18), dp(18));

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        FrameLayout medallion = new FrameLayout(this);
        medallion.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));
        medallion.setBackground(roundedShape(0xFF131218, dp(16), 0x33C8A46B, dp(1)));
        medallion.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_ADVISOR, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout headText = new LinearLayout(this);
        headText.setOrientation(LinearLayout.VERTICAL);
        headText.setPadding(dp(12), 0, dp(12), 0);
        headText.addView(text("مشاور انتخاب تخصصی محصول", 14.5f, 0xFFF6EFE3, true));
        headText.addView(text("چهار نیاز اصلی را انتخاب کنید", 10.5f, 0xFF9E978C, false));
        head.addView(medallion);
        head.addView(headText, weight(1f));
        advisorCard.addView(head);
        advisorCard.addView(space(16));

        final String[] concerns = {"لک و تیرگی", "چروک و لیفتینگ", "شفاف‌سازی منافذ", "ریزش و تقویت مو"};
        LinearLayout row1 = row(Gravity.CENTER_VERTICAL);
        LinearLayout row2 = row(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < concerns.length; i++) {
            final int idx = i;
            boolean active = advisorConcernIdx == i;
            TextView chip = new TextView(this);
            chip.setText(concerns[i]);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            chip.setSingleLine(true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(4), dp(10), dp(4), dp(10));
            chip.setTextColor(active ? COL_OBSIDIAN : COL_GOLD_LIGHT);
            chip.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            chip.setBackground(active
                    ? roundedShape(COL_GOLD_LIGHT, dp(14), COL_GOLD_LIGHT, dp(1))
                    : roundedShape(0x1AC8A46B, dp(14), 0x55C8A46B, dp(1)));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    advisorConcernIdx = idx;
                    advisorAnswered = false;
                    renderCurrentScreen();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(4), 0, dp(4), 0);
            if (i < 2) row1.addView(chip, lp); else row2.addView(chip, lp);
        }
        advisorCard.addView(row1);
        advisorCard.addView(space(8));
        advisorCard.addView(row2);
        advisorCard.addView(space(16));

        advisorCard.addView(primaryButton(
                advisorAnswered ? "نمایش مجدد پیشنهاد اختصاصی" : "دریافت پیشنهاد اختصاصی",
                Luxury3DIconView.ICON_BOUTIQUE, new Runnable() {
                    @Override public void run() {
                        advisorAnswered = true;
                        renderCurrentScreen();
                    }
                }));
        contentContainer.addView(advisorCard);
        contentContainer.addView(space(14));

        // ── Recommendation (separate white editorial card) ────────────────
        if (advisorAnswered) {
            contentContainer.addView(buildRecommendationCard());
            contentContainer.addView(space(14));
        }

        // ── Authenticity ──────────────────────────────────────────────────
        LinearLayout authCard = whiteCard();
        LinearLayout authHead = row(Gravity.CENTER_VERTICAL);
        FrameLayout shield = new FrameLayout(this);
        shield.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
        shield.setBackground(roundedShape(COL_OBSIDIAN, dp(14), 0x40C8A46B, dp(1)));
        shield.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_SHIELD, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout authTitles = new LinearLayout(this);
        authTitles.setOrientation(LinearLayout.VERTICAL);
        authTitles.setPadding(dp(12), 0, dp(12), 0);
        authTitles.addView(text("بررسی شناسه اصالت کالا", 14f, COL_TEXT_MAIN, true));
        authTitles.addView(text("استعلام از سامانه رسمی مجموعه", 10.5f, COL_TEXT_MUTED, false));
        authHead.addView(shield);
        authHead.addView(authTitles, weight(1f));
        authCard.addView(authHead);
        authCard.addView(space(14));

        detach(authSerialInput);
        authSerialInput.setText(lastCheckedSerial);
        authCard.addView(authSerialInput);
        authCard.addView(space(10));

        String[] quick = {"AS-GAT-150", "AS-GAT-050", "AS-LC-104", "AS-ORD-240", "AS-HF-300"};
        LinearLayout qrow1 = row(Gravity.CENTER_VERTICAL);
        LinearLayout qrow2 = row(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < quick.length; i++) {
            final String code = quick[i];
            TextView chip = new TextView(this);
            chip.setText(ltr(code));
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
            chip.setSingleLine(true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(2), dp(9), dp(2), dp(9));
            chip.setTextColor(COL_TEXT_MUTED);
            chip.setBackground(roundedShape(COL_IVORY_BG, dp(12), COL_BORDER, dp(1)));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    lastCheckedSerial = code;
                    verifiedProduct = db.verifyAuthenticitySerial(code);
                    renderCurrentScreen();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            if (i < 3) qrow1.addView(chip, lp); else qrow2.addView(chip, lp);
        }
        if (qrow2.getChildCount() > 0) {
            authCard.addView(qrow1);
            authCard.addView(space(7));
            authCard.addView(qrow2);
        } else {
            authCard.addView(qrow1);
        }
        authCard.addView(space(12));

        authCard.addView(primaryButton("استعلام اصالت محصول", Luxury3DIconView.ICON_SHIELD, new Runnable() {
            @Override public void run() {
                String raw = authSerialInput.getText().toString();
                verifiedProduct = db.verifyAuthenticitySerial(raw);
                lastCheckedSerial = raw.trim().isEmpty() ? lastCheckedSerial : raw.trim();
                renderCurrentScreen();
                toast(verifiedProduct != null ? "کالای اورجینال تأیید شد." : "شناسه در سامانه یافت نشد.");
            }
        }));
        authCard.addView(space(12));

        if (verifiedProduct != null) {
            LinearLayout result = new LinearLayout(this);
            result.setOrientation(LinearLayout.VERTICAL);
            result.setBackground(roundedShape(COL_OBSIDIAN, dp(16), 0x552E8B63, dp(1)));
            result.setPadding(dp(14), dp(13), dp(14), dp(13));

            LinearLayout resHead = row(Gravity.CENTER_VERTICAL);
            TextView tick = new TextView(this);
            tick.setText("✓");
            tick.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            tick.setTypeface(Typeface.DEFAULT_BOLD);
            tick.setGravity(Gravity.CENTER);
            tick.setTextColor(COL_OBSIDIAN);
            tick.setBackground(roundedShape(0xFF7FD3A6, dp(20), 0, 0));
            resHead.addView(tick, new LinearLayout.LayoutParams(dp(22), dp(22)));
            TextView okTitle = text("اصالت این کالا در سامانه مجموعه تأیید شد", 12f, 0xFF8FE0B6, true);
            okTitle.setPadding(dp(8), 0, 0, 0);
            resHead.addView(okTitle, weight(1f));
            result.addView(resHead);

            result.addView(space(10));
            result.addView(resultLine("محصول", verifiedProduct.nameFa));
            result.addView(resultLine("شناسه سریال", ltr(verifiedProduct.ttacSerial)));
            result.addView(resultLine("شماره پارت", ltr(verifiedProduct.batchCode)));
            result.addView(resultLine("حجم / مدل", verifiedProduct.volume));
            result.addView(resultLine("تاریخ انقضا", ltr(PersianFormatter.toPersianDigits(verifiedProduct.expiryLabel))));
            result.addView(space(10));

            result.addView(ghostGoldDarkButton("کپی شناسه اصالت", new Runnable() {
                @Override public void run() {
                    copyToClipboard(verifiedProduct.ttacSerial);
                    toast("شناسه اصالت کپی شد.");
                }
            }));
            authCard.addView(result);
        }

        authCard.addView(space(12));
        TextView note = text("هر کالای مجموعه دارای سریال یکتا، پارت‌نامه و تاریخ انقضای ثبت‌شده است؛ در صورت مغایرت، پیش از استفاده با مدیریت تماس بگیرید.",
                10f, COL_TEXT_FAINT, false);
        note.setLineSpacing(dp(3), 1f);
        authCard.addView(note);

        contentContainer.addView(authCard);
        contentContainer.addView(space(16));
    }

    private View buildRecommendationCard() {
        String[] skus = {"AS-GAT-150", "AS-GAT-050", "AS-ORD-240", "AS-HF-300"};
        String[] reasons = {
                "برای رفع لک، تیرگی و یکدست‌سازی تناژ پوست؛ ترکیب ضدلک Hentowhite AF با سرامید و ویتامین C بهترین گزینه کلکسیون است.",
                "برای خطوط ریز، افتادگی و بازگرداندن قوام پوست؛ کمپلکس پپتیدی گاتیو کلاژن‌سازی طبیعی را تحریک می‌کند.",
                "برای منافذ باز، کدری و بافت ناهموار؛ لایه‌برداری ملایم شبانه گلیکولیک اسید ۷٪ اوردینری.",
                "برای ریزش و نازکی تارها؛ تونیک گیاهی هیرفرند فولیکول را تغذیه و رویش مجدد را تحریک می‌کند."
        };
        Models.Product match = null;
        for (String sku : skus) {
            if (advisorConcernIdx >= 0 && advisorConcernIdx < skus.length
                    && skus[advisorConcernIdx].equals(sku)) {
                match = findProductBySku(sku);
            }
        }
        if (match == null) {
            List<Models.Product> all = db.getProducts(true, "همه", "", "newest");
            if (!all.isEmpty()) match = all.get(0);
        }
        if (match == null) {
            return emptyCard(Luxury3DIconView.ICON_ADVISOR, "پیشنهادی یافت نشد",
                    "لطفاً روی دریافت پیشنهاد اختصاصی دوباره بزنید.");
        }

        final Models.Product p = match;
        LinearLayout card = whiteCard();

        TextView eyebrow = text("پیشنهاد اختصاصی مدیریت", 9.5f, COL_GOLD_DEEP, false);
        eyebrow.setLetterSpacing(0.08f);
        card.addView(eyebrow);
        card.addView(space(8));

        LinearLayout body = row(Gravity.TOP);
        FrameLayout thumbWrap = new FrameLayout(this);
        thumbWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(96), dp(112)));
        thumbWrap.setClipToOutline(true);
        thumbWrap.setBackground(roundedShape(COL_SILK_CREAM, dp(14), 0, 0));
        ImageView thumb = new ImageView(this);
        thumb.setImageResource(resolveProductImage(p.imageKey));
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumbWrap.addView(thumb, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(12), 0, dp(12), 0);
        TextView name = text(p.nameFa, 13.5f, COL_TEXT_MAIN, true);
        name.setLineSpacing(dp(2), 1f);
        TextView reason = text(reasons[Math.max(0, Math.min(reasons.length - 1, advisorConcernIdx))],
                11f, COL_TEXT_MUTED, false);
        reason.setLineSpacing(dp(3), 1f);
        reason.setPadding(0, dp(6), 0, dp(8));
        TextView price = text(PersianFormatter.formatToman(p.salePrice), 14f, COL_TEXT_MAIN, true);
        info.addView(name);
        info.addView(reason);
        info.addView(price);

        body.addView(thumbWrap);
        body.addView(info, weight(1f));
        card.addView(body);
        card.addView(space(14));
        card.addView(primaryButton("افزودن مستقیم به سبد خرید", Luxury3DIconView.ICON_PLUS, new Runnable() {
            @Override public void run() { addToCart(p); }
        }));
        return card;
    }

    private Models.Product findProductBySku(String sku) {
        List<Models.Product> all = db.getProducts(false, "همه", "", "newest");
        for (Models.Product p : all) {
            if (p.sku != null && p.sku.equalsIgnoreCase(sku)) return p;
        }
        return null;
    }

    private View resultLine(String label, String value) {
        LinearLayout line = row(Gravity.CENTER_VERTICAL);
        line.setPadding(0, dp(3), 0, dp(3));
        TextView l = text(label, 10.5f, 0xFF8E877C, false);
        l.setSingleLine(true);
        TextView v = text(value == null ? "—" : value, 11.5f, 0xFFF0E7D8, false);
        v.setGravity(Gravity.END);
        v.setSingleLine(true);
        v.setEllipsize(android.text.TextUtils.TruncateAt.END);
        line.addView(l);
        line.addView(v, weight(1f));
        return line;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  3 · BAG & CHECKOUT (سبد خرید)
    // ══════════════════════════════════════════════════════════════════════
    private void renderBag() {
        contentContainer.addView(pageTitle("سبد خرید اختصاصی شما",
                "پرداخت در ۴ قسط بدون کارمزد، یا ارسال هوشمند سبد در واتس‌اپ مدیریت"));
        contentContainer.addView(space(14));

        if (cart.isEmpty()) {
            contentContainer.addView(emptyCard(Luxury3DIconView.ICON_BAG, "سبد خرید شما خالی است",
                    "کلکسیون امضایی مجموعه در ویترین آماده انتخاب شماست."));
            contentContainer.addView(space(12));
            contentContainer.addView(primaryButton("رفتن به ویترین مجموعه", Luxury3DIconView.ICON_BOUTIQUE, new Runnable() {
                @Override public void run() { switchCustomerTab(TAB_BOUTIQUE); }
            }));
            return;
        }

        for (final Models.CartItem item : cart) {
            contentContainer.addView(bagItemCard(item));
            contentContainer.addView(space(12));
        }

        // ── Gift & packaging ─────────────────────────────────────────────
        LinearLayout giftCard = whiteCard();
        giftCard.addView(sectionLabel("بسته‌بندی هدیه و کارت دست‌نویس"));
        giftCard.addView(space(8));

        LinearLayout giftRow = row(Gravity.CENTER_VERTICAL);
        final CheckBox giftCheck = new CheckBox(this);
        giftCheck.setChecked(vipGiftPackaging);
        giftCheck.setButtonTintList(ColorStateList.valueOf(COL_GOLD_DEEP));
        giftCheck.setText("بسته‌بندی هدیه مجموعه ASIYE (رایگان)");
        giftCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        giftCheck.setTextColor(COL_TEXT_MAIN);
        giftCheck.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                vipGiftPackaging = isChecked;
            }
        });
        giftRow.addView(giftCheck, weight(1f));
        giftCard.addView(giftRow);
        giftCard.addView(space(10));
        detach(bagGiftInput);
        bagGiftInput.setText(giftMessageDraft);
        giftCard.addView(bagGiftInput);
        contentContainer.addView(giftCard);
        contentContainer.addView(space(12));

        // ── Coupon ────────────────────────────────────────────────────────
        LinearLayout couponCard = whiteCard();
        couponCard.addView(sectionLabel("کد تخفیف اختصاصی"));
        couponCard.addView(space(8));
        LinearLayout couponRow = row(Gravity.CENTER_VERTICAL);
        detach(bagCouponInput);
        bagCouponInput.setText(appliedCouponCode);
        couponRow.addView(bagCouponInput, weight(1f));
        couponRow.addView(spaceH(8));
        TextView applyBtn = new TextView(this);
        applyBtn.setText(appliedCouponValue > 0 ? "حذف" : "اعمال");
        applyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        applyBtn.setTypeface(Typeface.DEFAULT_BOLD);
        applyBtn.setGravity(Gravity.CENTER);
        applyBtn.setTextColor(COL_OBSIDIAN);
        applyBtn.setPadding(dp(18), dp(13), dp(18), dp(13));
        applyBtn.setBackground(roundedShape(COL_GOLD_LIGHT, dp(13), 0, 0));
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (appliedCouponValue > 0) {
                    appliedCouponCode = "";
                    appliedCouponValue = 0L;
                    bagCouponInput.setText("");
                    toast("کد تخفیف برداشته شد.");
                } else {
                    String code = bagCouponInput.getText().toString().trim();
                    long result = db.evaluateCouponDiscount(code, calculateCartSubtotal());
                    if (result > 0) {
                        appliedCouponCode = code.toUpperCase(Locale.US);
                        appliedCouponValue = result;
                        toast("کد تخفیف اعمال شد.");
                    } else if (result == -1L) {
                        toast("کد تخفیف معتبر نیست.");
                    } else if (result == -2L) {
                        toast("حداقل مبلغ سبد برای این کد رعایت نشده است.");
                    } else {
                        toast("کد تخفیف را وارد کنید.");
                    }
                }
                renderCurrentScreen();
            }
        });
        couponRow.addView(applyBtn);
        couponCard.addView(couponRow);
        couponCard.addView(space(8));
        couponStatusView = text(appliedCouponValue > 0
                        ? "کد " + ltr(appliedCouponCode) + " اعمال شد — " + PersianFormatter.formatToman(appliedCouponValue) + " تخفیف"
                        : "کدهای فعال: " + ltr("ASIYE20") + " ، " + ltr("VIPGOLD"),
                10f, appliedCouponValue > 0 ? COL_SUCCESS : COL_TEXT_FAINT, appliedCouponValue > 0);
        couponCard.addView(couponStatusView);
        contentContainer.addView(couponCard);
        contentContainer.addView(space(12));

        // ── Delivery details ─────────────────────────────────────────────
        LinearLayout infoCard = whiteCard();
        infoCard.addView(sectionLabel("اطلاعات تحویل‌گیرنده"));
        infoCard.addView(space(8));
        String savedName = db.getSetting("customer_name", "");
        String savedMobile = db.getSetting("customer_mobile", "");
        String savedAddress = db.getSetting("customer_address", "");
        if (bagNameInput.getText().toString().trim().isEmpty() && !savedName.isEmpty()) {
            bagNameInput.setText(savedName);
        }
        if (bagMobileInput.getText().toString().trim().isEmpty() && !savedMobile.isEmpty()) {
            bagMobileInput.setText(savedMobile);
        }
        if (bagAddressInput.getText().toString().trim().isEmpty() && !savedAddress.isEmpty()) {
            bagAddressInput.setText(savedAddress);
        }
        detach(bagNameInput);
        detach(bagMobileInput);
        detach(bagAddressInput);
        infoCard.addView(bagNameInput);
        infoCard.addView(space(9));
        infoCard.addView(bagMobileInput);
        infoCard.addView(space(9));
        infoCard.addView(bagAddressInput);
        contentContainer.addView(infoCard);
        contentContainer.addView(space(12));

        // ══ Checkout economics ═══════════════════════════════════════════
        long subtotal = calculateCartSubtotal();
        long shipping = subtotal >= AsiyeDatabaseHelper.FREE_SHIPPING_THRESHOLD
                ? 0L : AsiyeDatabaseHelper.STANDARD_SHIPPING_COST;
        long basePayable = Math.max(0L, subtotal - appliedCouponValue) + shipping;

        InstallmentEngine.Config instCfg = InstallmentEngine.load(db);
        List<Models.InstallmentPlan> plans = InstallmentEngine.build(instCfg, basePayable);
        if (plans.isEmpty() && PAY_INSTALLMENT.equals(payMode)) {
            payMode = PAY_ONLINE;
        }
        if (!plans.isEmpty() && selectedInstallmentId.isEmpty()) {
            selectedInstallmentId = plans.get(0).id;
        }
        Models.InstallmentPlan activePlan = PAY_INSTALLMENT.equals(payMode)
                ? InstallmentEngine.find(plans, selectedInstallmentId) : null;
        if (activePlan == null && PAY_INSTALLMENT.equals(payMode) && !plans.isEmpty()) {
            activePlan = plans.get(0);
            selectedInstallmentId = activePlan.id;
        }
        if (activePlan == null) selectedInstallmentId = "";

        long financingFee = activePlan == null ? 0L : activePlan.totalFee;
        long payable = basePayable + financingFee;
        long monthly = activePlan == null ? Math.round(basePayable / 4.0) : activePlan.monthlyPayment;
        long cashback = Math.round(basePayable * 0.01);

        // ══ Payment mode selector ════════════════════════════════════════
        contentContainer.addView(buildPaymentModePanel(basePayable, plans, instCfg, activePlan));
        contentContainer.addView(space(12));

        // ══ Online gateway panel (only when online mode is selected) ═════
        if (PAY_ONLINE.equals(payMode)) {
            contentContainer.addView(buildGatewayPanel(payable));
            contentContainer.addView(space(12));
        }

        LinearLayout summary = new LinearLayout(this);
        summary.setOrientation(LinearLayout.VERTICAL);
        summary.setBackground(roundedShape(COL_OBSIDIAN, dp(22), 0x40C8A46B, dp(1)));
        summary.setPadding(dp(18), dp(18), dp(18), dp(18));

        summary.addView(text("صورتحساب اختصاصی", 10f, COL_GOLD, false));
        summary.addView(space(10));
        summary.addView(summaryLine("جمع کالاها", PersianFormatter.formatToman(subtotal), false));
        if (appliedCouponValue > 0) {
            summary.addView(summaryLine("تخفیف کد اختصاصی", "− " + PersianFormatter.formatToman(appliedCouponValue), true));
        }
        summary.addView(summaryLine("هزینه ارسال VIP",
                shipping == 0L ? "رایگان" : PersianFormatter.formatToman(shipping), shipping == 0L));
        if (cashback > 0L) {
            LinearLayout club = row(Gravity.CENTER_VERTICAL);
            club.setPadding(0, dp(8), 0, 0);
            FrameLayout clubIcon = new FrameLayout(this);
            clubIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(18), dp(18)));
            clubIcon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_GIFT, false),
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            club.addView(clubIcon);
            TextView clubText = text("۱٪ از این خرید (" + PersianFormatter.formatToman(cashback)
                    + ") پس از تکمیل سفارش به کیف پول شما اضافه می‌شود.", 9.2f, 0xFFB9AF9F, false);
            clubText.setPadding(dp(6), 0, 0, 0);
            clubText.setLineSpacing(dp(2), 1f);
            club.addView(clubText, weight(1f));
            summary.addView(club);
        }

        View divider = new View(this);
        divider.setBackgroundColor(0x33C8A46B);
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        dLp.setMargins(0, dp(12), 0, dp(12));
        summary.addView(divider, dLp);

        LinearLayout totalRow = row(Gravity.CENTER_VERTICAL);
        totalRow.addView(text("مبلغ قابل پرداخت", 12f, 0xFFCFC6B8, true), weight(1f));
        TextView total = text(PersianFormatter.formatToman(payable), 17f, COL_GOLD_LIGHT, true);
        totalRow.addView(total);
        summary.addView(totalRow);

        if (activePlan != null) {
            if (activePlan.downPayment > 0L) {
                summary.addView(space(2));
                summary.addView(summaryLine("پیش‌پرداخت اولیه",
                        PersianFormatter.formatToman(activePlan.downPayment), false));
            }
            if (financingFee > 0) {
                summary.addView(summaryLine("کارمزد اقساط ("
                        + PersianFormatter.toPersianDigits(String.valueOf(activePlan.months)) + " قسط)",
                        PersianFormatter.formatToman(financingFee), false));
            }
            summary.addView(space(7));
            LinearLayout instBox = row(Gravity.CENTER_VERTICAL);
            instBox.setBackground(roundedShape(0x24C8A46B, dp(12), 0x66C8A46B, dp(1)));
            instBox.setPadding(dp(12), dp(10), dp(12), dp(10));
            instBox.addView(text("مدت بازپرداخت · "
                            + PersianFormatter.toPersianDigits(String.valueOf(activePlan.months)) + " قسط",
                    10.5f, 0xFFE6DCCB, false), weight(1f));
            instBox.addView(text("ماهانه " + PersianFormatter.formatToman(monthly), 11.5f, COL_GOLD_LIGHT, true));
            summary.addView(instBox);
            TextView approval = text("تأیید نهایی اقساط پس از بررسی هویت توسط مدیریت مجموعه انجام می‌شود.",
                    9f, 0xFFA79E90, false);
            approval.setLineSpacing(dp(2), 1f);
            approval.setPadding(0, dp(7), 0, 0);
            summary.addView(approval);
        }
        if (shipping > 0L) {
            summary.addView(space(6));
            summary.addView(text("با خرید " + PersianFormatter.formatToman(
                    AsiyeDatabaseHelper.FREE_SHIPPING_THRESHOLD - subtotal) + " بیشتر، ارسال رایگان می‌شود.",
                    9.5f, 0xFF9A9186, false));
        }
        contentContainer.addView(summary);
        contentContainer.addView(space(14));

        final String ctaLabel = PAY_INSTALLMENT.equals(payMode)
                ? "ثبت سفارش اقساطی و ارسال برای تأیید"
                : (PAY_COD.equals(payMode) ? "ثبت سفارش (پرداخت در محل)" : "پرداخت امن آنلاین و ثبت سفارش");
        final int ctaIcon = PAY_INSTALLMENT.equals(payMode) ? Luxury3DIconView.ICON_INSTALLMENT
                : (PAY_COD.equals(payMode) ? Luxury3DIconView.ICON_WALLET : Luxury3DIconView.ICON_CARD);
        contentContainer.addView(primaryButton(ctaLabel, ctaIcon, new Runnable() {
            @Override public void run() { submitOrder(); }
        }));
        contentContainer.addView(space(10));
        contentContainer.addView(outlineDarkButton("ارسال هوشمند همین سبد در واتس‌اپ مدیریت",
                Luxury3DIconView.ICON_WHATSAPP, new Runnable() {
                    @Override public void run() { openSmartWhatsApp("ارسال سبد خرید کامل", null); }
                }));
        contentContainer.addView(space(10));
        contentContainer.addView(footerNote());
    }

    private View bagItemCard(final Models.CartItem item) {
        LinearLayout card = whiteCard();

        LinearLayout top = row(Gravity.TOP);
        FrameLayout thumbWrap = new FrameLayout(this);
        thumbWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(72), dp(86)));
        thumbWrap.setClipToOutline(true);
        thumbWrap.setBackground(roundedShape(COL_SILK_CREAM, dp(12), 0, 0));
        ImageView thumb = new ImageView(this);
        thumb.setImageResource(resolveProductImage(item.product.imageKey));
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumbWrap.addView(thumb, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(12), 0, dp(12), 0);
        TextView name = text(item.product.nameFa, 12.5f, COL_TEXT_MAIN, true);
        name.setMaxLines(2);
        name.setLineSpacing(dp(2), 1f);
        TextView volume = text(item.selectedVariant == null ? item.product.volume : item.selectedVariant,
                10f, COL_TEXT_FAINT, false);
        volume.setPadding(0, dp(4), 0, dp(6));
        TextView unit = text("هر عدد " + PersianFormatter.formatToman(item.product.salePrice),
                10.8f, COL_GOLD_DEEP, true);
        info.addView(name);
        info.addView(volume);
        info.addView(unit);

        top.addView(thumbWrap);
        top.addView(info, weight(1f));
        card.addView(top);
        card.addView(space(12));

        LinearLayout bottom = row(Gravity.CENTER_VERTICAL);
        LinearLayout stepper = row(Gravity.CENTER_VERTICAL);
        stepper.addView(stepperButton(Luxury3DIconView.ICON_MINUS, new Runnable() {
            @Override public void run() {
                if (item.quantity > 1) {
                    item.quantity--;
                } else {
                    cart.remove(item);
                }
                renderCurrentScreen();
            }
        }));
        TextView qty = text(PersianFormatter.toPersianDigits(String.valueOf(item.quantity)),
                13f, COL_TEXT_MAIN, true);
        qty.setGravity(Gravity.CENTER);
        qty.setMinWidth(dp(34));
        stepper.addView(qty);
        stepper.addView(stepperButton(Luxury3DIconView.ICON_PLUS, new Runnable() {
            @Override public void run() {
                if (item.quantity < item.product.stock) {
                    item.quantity++;
                    renderCurrentScreen();
                } else {
                    toast("بیشتر از موجودی انبار قابل افزودن نیست.");
                }
            }
        }));
        bottom.addView(stepper, weight(1f));

        TextView remove = text("حذف", 10.2f, 0xFF8A7D6B, false);
        remove.setPadding(dp(12), dp(7), dp(12), dp(7));
        remove.setBackground(roundedShape(0xFFFAF6EE, dp(10), 0xFFE9E0D0, dp(1)));
        remove.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                cart.remove(item);
                renderCurrentScreen();
            }
        });
        TextView lineTotal = text(PersianFormatter.formatToman(item.lineTotal()), 13.5f, COL_TEXT_MAIN, true);
        lineTotal.setPadding(0, 0, dp(10), 0);
        bottom.addView(remove);
        bottom.addView(lineTotal);
        card.addView(bottom);
        return card;
    }

    private View stepperButton(int iconType, final Runnable action) {
        FrameLayout wrap = new FrameLayout(this);
        wrap.setBackground(roundedShape(COL_IVORY_BG, dp(20), COL_BORDER, dp(1)));
        wrap.addView(new Luxury3DIconView(this, iconType, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        wrap.setLayoutParams(new LinearLayout.LayoutParams(dp(32), dp(32)));
        wrap.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return wrap;
    }

    private View paymentOption(final String code, String title, String sub) {
        boolean selected = selectedPaymentMethod.equals(code);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(roundedShape(selected ? 0xFFFFFBF3 : COL_WHITE, dp(14),
                selected ? COL_GOLD : COL_BORDER, dp(selected ? 2 : 1)));
        box.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        TextView dot = new TextView(this);
        dot.setText(selected ? "●" : "○");
        dot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        dot.setTextColor(selected ? COL_GOLD_DEEP : COL_TEXT_FAINT);
        dot.setGravity(Gravity.CENTER);
        head.addView(dot, new LinearLayout.LayoutParams(dp(20), ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView t = text(title, 12f, COL_TEXT_MAIN, selected);
        t.setPadding(dp(6), 0, 0, 0);
        head.addView(t, weight(1f));
        box.addView(head);

        TextView s = text(sub, 10f, COL_TEXT_MUTED, false);
        s.setPadding(dp(26), dp(4), 0, 0);
        box.addView(s);

        box.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                selectedPaymentMethod = code;
                renderCurrentScreen();
            }
        });
        return box;
    }

    private View summaryLine(String label, String value, boolean highlight) {
        LinearLayout line = row(Gravity.CENTER_VERTICAL);
        line.setPadding(0, dp(4), 0, dp(4));
        TextView l = text(label, 11f, 0xFFA9A196, false);
        TextView v = text(value, 11.5f, highlight ? 0xFF8FE0B6 : 0xFFF0E7D8, highlight);
        line.addView(l, weight(1f));
        line.addView(v);
        return line;
    }

    private void submitOrder() {
        String name = bagNameInput.getText().toString().trim();
        String mobile = PersianFormatter.toAsciiDigits(bagMobileInput.getText().toString());
        String address = bagAddressInput.getText().toString().trim();
        giftMessageDraft = bagGiftInput.getText().toString().trim();

        if (cart.isEmpty()) {
            toast("سبد خرید خالی است.");
            return;
        }
        if (name.length() < 3) {
            toast("نام و نام خانوادگی تحویل‌گیرنده را کامل وارد کنید.");
            return;
        }
        if (!PersianFormatter.isValidIranMobile(mobile)) {
            toast("شماره موبایل باید ۱۱ رقمی و با ۰۹ شروع شود.");
            return;
        }
        if (address.length() < 10) {
            toast("نشانی پستی را کامل‌تر وارد کنید.");
            return;
        }

        long subtotal = calculateCartSubtotal();
        long shipping = subtotal >= AsiyeDatabaseHelper.FREE_SHIPPING_THRESHOLD
                ? 0L : AsiyeDatabaseHelper.STANDARD_SHIPPING_COST;
        long basePayable = Math.max(0L, subtotal - appliedCouponValue) + shipping;
        InstallmentEngine.Config instCfg = InstallmentEngine.load(db);
        List<Models.InstallmentPlan> plans = InstallmentEngine.build(instCfg, basePayable);
        Models.InstallmentPlan plan = PAY_INSTALLMENT.equals(payMode)
                ? InstallmentEngine.find(plans, selectedInstallmentId) : null;
        if (PAY_INSTALLMENT.equals(payMode) && plan == null) {
            toast("طرح اقساطی معتبری انتخاب نشده است.");
            return;
        }

        db.setSetting("customer_name", name);
        db.setSetting("customer_mobile", mobile);
        db.setSetting("customer_address", address);

        String paymentLabel;
        String status;
        int installments;
        long fee;
        if (plan != null) {
            paymentLabel = "اقساطی — " + plan.providerFa + " (" + PersianFormatter.toPersianDigits(String.valueOf(plan.months)) + " قسط)";
            status = "pending_approval";
            installments = plan.months;
            fee = plan.totalFee;
        } else if (PAY_COD.equals(payMode)) {
            paymentLabel = "پرداخت در محل (پس‌کرایه)";
            status = "paid";
            installments = 1;
            fee = 0L;
        } else {
            Models.PaymentGatewayRecord gw = findGateway(selectedGateway);
            paymentLabel = "پرداخت آنلاین — " + (gw == null ? "درگاه زرین‌پال" : gw.titleFa);
            status = "pending_payment";
            installments = 1;
            fee = 0L;
        }

        try {
            Models.OrderRecord order = db.executeCheckoutAdvanced(cart, name, mobile, address,
                    paymentLabel, installments,
                    appliedCouponValue > 0 ? appliedCouponCode : "",
                    vipGiftPackaging, giftMessageDraft, fee, status);
            cart.clear();
            appliedCouponCode = "";
            appliedCouponValue = 0L;
            selectedInstallmentId = "";

            if ("pending_payment".equals(order.status)) {
                renderCurrentScreen();
                showGatewayPaymentSheet(order, plan);
            } else {
                showOrderSuccessDialog(order);
                renderCurrentScreen();
            }
        } catch (IllegalStateException e) {
            toast(e.getMessage());
        } catch (Exception e) {
            toast("ثبت سفارش با خطا مواجه شد؛ دوباره تلاش کنید.");
        }
    }

    private Models.PaymentGatewayRecord findGateway(String code) {
        for (Models.PaymentGatewayRecord g : db.getPaymentGateways()) {
            if (g.code.equals(code)) return g;
        }
        return null;
    }

    /** Simulated-but-realistic secure gateway hand-off sheet. */
    private void showGatewayPaymentSheet(final Models.OrderRecord order, Models.InstallmentPlan plan) {
        Models.PaymentGatewayRecord gw = findGateway(selectedGateway);
        String gatewayName = gw == null ? "درگاه امن زرین‌پال" : gw.titleFa;
        String merchant = db.getSetting("pay_merchant_id", "ASIYE-STORE-ZP");
        boolean sandbox = db.getSettingBool("pay_sandbox", true);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(18));
        box.setBackgroundColor(COL_WHITE);

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));
        iconWrap.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_CARD, Luxury3DIconView.PEDESTAL_GOLD),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        head.addView(iconWrap);
        LinearLayout headText = new LinearLayout(this);
        headText.setOrientation(LinearLayout.VERTICAL);
        headText.setPadding(dp(10), 0, dp(10), 0);
        headText.addView(text(gatewayName, 13.5f, COL_TEXT_MAIN, true));
        headText.addView(text(sandbox ? "حالت آزمایشی درگاه (Sandbox)" : "اتصال زنده به درگاه بانکی", 9.5f,
                sandbox ? COL_WARN : COL_SUCCESS, false));
        head.addView(headText, weight(1f));
        box.addView(head);
        box.addView(space(14));

        LinearLayout receipt = new LinearLayout(this);
        receipt.setOrientation(LinearLayout.VERTICAL);
        receipt.setBackground(roundedShape(COL_IVORY_BG, dp(14), COL_BORDER, dp(1)));
        receipt.setPadding(dp(14), dp(12), dp(14), dp(12));
        receipt.addView(resultDarkLine("شماره سفارش", ltr(order.orderNumber)));
        receipt.addView(resultDarkLine("مبلغ قابل پرداخت", PersianFormatter.formatToman(order.totalAmount)));
        receipt.addView(resultDarkLine("پذیرنده", merchant));
        receipt.addView(resultDarkLine("درگاه", gatewayName));
        box.addView(receipt);
        box.addView(space(6));
        TextView note = text("پس از تأیید، فاکتور رسمی و کد رهگیری برای شما و مدیریت مجموعه ارسال می‌شود.",
                9.8f, COL_TEXT_FAINT, false);
        note.setLineSpacing(dp(3), 1f);
        box.addView(note);
        box.addView(space(14));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("پرداخت مبلغ و بازگشت به فروشگاه", Luxury3DIconView.ICON_SHIELD, new Runnable() {
            @Override public void run() {
                db.updateOrderStatus(order.id, "paid", "");
                dialog.dismiss();
                toast("پرداخت با موفقیت انجام شد. سفارش شما ثبت نهایی گردید.");
                renderCurrentScreen();
                Models.OrderRecord refreshed = db.getOrderById(order.id);
                showOrderSuccessDialog(refreshed);
            }
        }));
        box.addView(space(8));
        box.addView(ghostButton("انصراف از پرداخت (سفارش در انتظار می‌ماند)", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    private void showOrderSuccessDialog(final Models.OrderRecord order) {
        if (order == null) return;
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(24), dp(22), dp(20));
        box.setBackgroundColor(COL_WHITE);
        box.setGravity(Gravity.CENTER_HORIZONTAL);

        FrameLayout seal = new FrameLayout(this);
        seal.setLayoutParams(new LinearLayout.LayoutParams(dp(64), dp(64)));
        seal.setBackground(roundedShape(COL_OBSIDIAN, dp(24), 0x66C8A46B, dp(1)));
        seal.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_CROWN, true),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        box.addView(seal);
        box.addView(space(14));

        TextView title = text("سفارش شما با موفقیت ثبت شد", 17f, COL_TEXT_MAIN, true);
        title.setGravity(Gravity.CENTER);
        box.addView(title);
        box.addView(space(6));
        TextView sub = text("سرکار خانم زنگنه از ثبت سفارش شما مطلع شد", 11f, COL_TEXT_MUTED, false);
        sub.setGravity(Gravity.CENTER);
        box.addView(sub);
        box.addView(space(16));

        LinearLayout receipt = new LinearLayout(this);
        receipt.setOrientation(LinearLayout.VERTICAL);
        receipt.setBackground(roundedShape(COL_IVORY_BG, dp(16), COL_BORDER, dp(1)));
        receipt.setPadding(dp(14), dp(12), dp(14), dp(12));
        receipt.addView(resultDarkLine("شماره سفارش", ltr(order.orderNumber)));
        receipt.addView(resultDarkLine("مبلغ نهایی", PersianFormatter.formatToman(order.totalAmount)));
        receipt.addView(resultDarkLine("گواهی اصالت", ltr(order.certificateCode)));
        receipt.addView(resultDarkLine("روش پرداخت", PersianFormatter.paymentMethodLabelFa(order.paymentMethod)));
        box.addView(receipt, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        box.addView(space(16));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ارسال رسید در واتس‌اپ مدیریت", Luxury3DIconView.ICON_WHATSAPP, new Runnable() {
            @Override public void run() {
                openSmartWhatsApp("ارسال رسید سفارش " + order.orderNumber, null);
                dialog.dismiss();
            }
        }));
        box.addView(space(8));
        box.addView(ghostButton("مشاهده ویترین", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    private View resultDarkLine(String label, String value) {
        LinearLayout line = row(Gravity.CENTER_VERTICAL);
        line.setPadding(0, dp(4), 0, dp(4));
        TextView l = text(label, 10.5f, COL_TEXT_MUTED, false);
        TextView v = text(value, 11.5f, COL_TEXT_MAIN, true);
        v.setGravity(Gravity.END);
        line.addView(l, weight(1f));
        line.addView(v);
        return line;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  4 · CONCIERGE & DESIGNER (مدیریت و ارتباط)
    // ══════════════════════════════════════════════════════════════════════
    private void renderConcierge() {
        contentContainer.addView(pageTitle("مدیریت و ارتباط اختصاصی",
                "خط مستقیم مدیریت مجموعه، پیام هوشمند واتس‌اپ و اینستاگرام رسمی"));
        contentContainer.addView(space(14));

        // ── Executive card ────────────────────────────────────────────────
        LinearLayout exec = new LinearLayout(this);
        exec.setOrientation(LinearLayout.VERTICAL);
        exec.setBackground(roundedShape(COL_OBSIDIAN, dp(24), 0x66C8A46B, dp(1)));
        exec.setPadding(dp(18), dp(18), dp(18), dp(18));

        LinearLayout eyebrowRow = row(Gravity.CENTER_VERTICAL);
        FrameLayout crownWrap = new FrameLayout(this);
        crownWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(30), dp(30)));
        crownWrap.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_CROWN, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TextView eyebrow = text("EXECUTIVE MANAGEMENT & VIP CONCIERGE", 8.5f, COL_GOLD, false);
        eyebrow.setLetterSpacing(0.14f);
        eyebrow.setTypeface(Typeface.SERIF, Typeface.BOLD);
        eyebrow.setPadding(dp(8), 0, 0, 0);
        eyebrowRow.addView(crownWrap);
        eyebrowRow.addView(eyebrow, weight(1f));
        exec.addView(eyebrowRow);
        exec.addView(space(12));

        exec.addView(text("مدیریت مجموعه: سرکار خانم زنگنه", 17f, 0xFFF6EFE3, true));
        exec.addView(space(6));
        TextView desc = text("برای مشاوره تخصصی انتخاب محصول، استعلام موجودی و ثبت سفارش، از هر یک از راه‌های ارتباطی زیر استفاده فرمایید. کد شناسه اپلیکیشن به صورت خودکار در پیام درج می‌شود.",
                11.3f, 0xFFBEB6AA, false);
        desc.setLineSpacing(dp(4), 1f);
        exec.addView(desc);
        exec.addView(space(14));

        // Direct line pill
        LinearLayout pill = row(Gravity.CENTER_VERTICAL);
        pill.setBackground(roundedShape(0xFF131218, dp(18), 0x55C8A46B, dp(1)));
        pill.setPadding(dp(14), dp(11), dp(12), dp(11));

        LinearLayout pillText = new LinearLayout(this);
        pillText.setOrientation(LinearLayout.VERTICAL);
        TextView pillLabel = text("خط مستقیم مدیریت", 9.5f, 0xFFB3A99B, false);
        TextView pillPhone = new TextView(this);
        pillPhone.setText(OWNER_PHONE_DISPLAY);
        pillPhone.setTextDirection(View.TEXT_DIRECTION_LTR);
        pillPhone.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);
        pillPhone.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        pillPhone.setTextColor(COL_GOLD_LIGHT);
        pillPhone.setLetterSpacing(0.04f);
        pillPhone.setIncludeFontPadding(false);
        pillPhone.setPadding(0, dp(2), 0, 0);
        pillText.addView(pillLabel);
        pillText.addView(pillPhone);
        pill.addView(pillText, weight(1f));

        TextView copy = text("کپی", 10.5f, COL_GOLD_LIGHT, true);
        copy.setGravity(Gravity.CENTER);
        copy.setPadding(dp(12), dp(8), dp(12), dp(8));
        copy.setBackground(roundedShape(0x22C8A46B, dp(12), 0x55C8A46B, dp(1)));
        copy.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                copyToClipboard(OWNER_PHONE_E164);
                toast("شماره مدیریت کپی شد.");
            }
        });
        pill.addView(copy);
        exec.addView(pill);
        exec.addView(space(14));

        // 2×2 contact tiles
        LinearLayout tileRow1 = row(Gravity.CENTER_VERTICAL);
        tileRow1.addView(contactTile(Luxury3DIconView.ICON_PHONE, "تماس تلفنی", OWNER_PHONE_DISPLAY, new Runnable() {
            @Override public void run() { dialOwner(); }
        }), weightedTile(0));
        tileRow1.addView(spaceH(10));
        tileRow1.addView(contactTile(Luxury3DIconView.ICON_SMS, "ارسال پیامک", "متن هوشمند آماده ارسال", new Runnable() {
            @Override public void run() { smsOwner(); }
        }), weightedTile(1));
        exec.addView(tileRow1);
        exec.addView(space(10));

        LinearLayout tileRow2 = row(Gravity.CENTER_VERTICAL);
        tileRow2.addView(contactTile(Luxury3DIconView.ICON_WHATSAPP, "واتس‌اپ هوشمند", "با شناسه رسمی برنامه", new Runnable() {
            @Override public void run() { openSmartWhatsApp("مشاوره اختصاصی محصول", null); }
        }), weightedTile(0));
        tileRow2.addView(spaceH(10));
        tileRow2.addView(contactTile(Luxury3DIconView.ICON_INSTAGRAM, "اینستاگرام رسمی", INSTAGRAM_HANDLE, new Runnable() {
            @Override public void run() { openInstagramPage(); }
        }), weightedTile(1));
        exec.addView(tileRow2);
        exec.addView(space(14));

        View innerLine = new View(this);
        innerLine.setBackgroundColor(0x22C8A46B);
        exec.addView(innerLine, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        exec.addView(space(10));

        LinearLayout hoursRow = row(Gravity.CENTER_VERTICAL);
        TextView hours = text("پاسخگویی: شنبه تا پنجشنبه — ۱۰:۰۰ تا ۲۱:۰۰", 9.6f, 0xFFBDB3A4, false);
        hours.setSingleLine(false);
        hours.setLineSpacing(dp(2), 1f);
        hoursRow.addView(hours, weight(1f));
        exec.addView(hoursRow);
        exec.addView(space(8));

        LinearLayout contactRow = row(Gravity.CENTER_VERTICAL);
        contactRow.setBackground(roundedShape(0xFF131218, dp(14), 0x33C8A46B, dp(1)));
        contactRow.setPadding(dp(12), dp(9), dp(12), dp(9));
        TextView addContact = text("افزودن شماره مدیریت به مخاطبین", 10f, COL_GOLD_LIGHT, true);
        contactRow.addView(addContact, weight(1f));
        TextView addIcon = text("＋", 12f, COL_GOLD_LIGHT, true);
        contactRow.addView(addIcon);
        contactRow.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { addOwnerToContacts(); }
        });
        exec.addView(contactRow);
        contentContainer.addView(exec);
        contentContainer.addView(space(14));

        // ── Instagram highlights (never clipped) ──────────────────────────
        LinearLayout ig = whiteCard();
        LinearLayout igHead = row(Gravity.CENTER_VERTICAL);
        TextView igTitle = text("هایلایت‌های منتخب پیج رسمی", 12.5f, COL_TEXT_MAIN, true);
        TextView igHandle = new TextView(this);
        igHandle.setText(INSTAGRAM_HANDLE);
        igHandle.setTextDirection(View.TEXT_DIRECTION_LTR);
        igHandle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        igHandle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        igHandle.setTextColor(COL_GOLD_DEEP);
        igHead.addView(igTitle, weight(1f));
        igHead.addView(igHandle);
        ig.addView(igHead);
        ig.addView(space(10));

        String[][] highlights = {
                {"✧", "گاتیو و لاکچری"},
                {"✧", "اوردینری اصل"},
                {"✧", "مکمل و تونیک مو"},
                {"✧", "خرید مشتری"},
                {"✧", "رضایت مشتریان"}
        };
        LinearLayout hRow1 = row(Gravity.CENTER_VERTICAL);
        LinearLayout hRow2 = row(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < highlights.length; i++) {
            TextView chip = new TextView(this);
            chip.setText(highlights[i][1]);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.3f);
            chip.setSingleLine(true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(3), dp(10), dp(3), dp(10));
            chip.setTextColor(COL_TEXT_MUTED);
            chip.setBackground(roundedShape(COL_IVORY_BG, dp(12), COL_BORDER, dp(1)));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { openInstagramPage(); }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            if (i < 3) hRow1.addView(chip, lp); else hRow2.addView(chip, lp);
        }
        ig.addView(hRow1);
        ig.addView(space(7));
        ig.addView(hRow2);
        ig.addView(space(12));
        ig.addView(ghostGoldButton("مشاهده پیج رسمی در اینستاگرام", new Runnable() {
            @Override public void run() { openInstagramPage(); }
        }));
        contentContainer.addView(ig);
        contentContainer.addView(space(14));

        // ── Order tracking ────────────────────────────────────────────────
        LinearLayout track = whiteCard();
        track.addView(sectionLabel("پیگیری سفارش و گواهی اصالت"));
        track.addView(space(8));
        LinearLayout trackRow = row(Gravity.CENTER_VERTICAL);
        detach(trackingInput);
        trackingInput.setText(trackingQuery);
        trackRow.addView(trackingInput, weight(1f));
        trackRow.addView(spaceH(8));
        TextView findBtn = text("پیگیری", 12f, COL_OBSIDIAN, true);
        findBtn.setGravity(Gravity.CENTER);
        findBtn.setPadding(dp(18), dp(13), dp(18), dp(13));
        findBtn.setBackground(roundedShape(COL_GOLD_LIGHT, dp(13), 0, 0));
        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                trackingQuery = trackingInput.getText().toString().trim();
                renderCurrentScreen();
            }
        });
        trackRow.addView(findBtn);
        track.addView(trackRow);

        if (!trackingQuery.isEmpty()) {
            Models.OrderRecord order = db.getOrderByNumber(trackingQuery);
            track.addView(space(12));
            if (order == null) {
                TextView nf = text("سفارشی با این کد یافت نشد. لطفاً کد را از پیام واتس‌اپ مدیریت بررسی کنید.",
                        10.8f, COL_DANGER, false);
                nf.setLineSpacing(dp(3), 1f);
                track.addView(nf);
            } else {
                LinearLayout res = new LinearLayout(this);
                res.setOrientation(LinearLayout.VERTICAL);
                res.setBackground(roundedShape(COL_IVORY_BG, dp(14), COL_BORDER, dp(1)));
                res.setPadding(dp(13), dp(12), dp(13), dp(12));
                LinearLayout resHead = row(Gravity.CENTER_VERTICAL);
                resHead.addView(text(ltr(order.orderNumber), 12f, COL_TEXT_MAIN, true), weight(1f));
                resHead.addView(statusPill(order.status));
                res.addView(resHead);
                res.addView(space(8));
                for (Models.OrderItemRecord it : order.items) {
                    res.addView(resultDarkLine(it.productName + "  •  " + PersianFormatter.toPersianDigits(String.valueOf(it.quantity)) + " عدد",
                            PersianFormatter.formatToman(it.unitPrice * it.quantity)));
                }
                res.addView(space(4));
                res.addView(resultDarkLine("مبلغ نهایی", PersianFormatter.formatToman(order.totalAmount)));
                if (order.trackingCode != null && !order.trackingCode.isEmpty()) {
                    res.addView(resultDarkLine("کد رهگیری پست", ltr(order.trackingCode)));
                }
                track.addView(res);
            }
        }
        contentContainer.addView(track);
        contentContainer.addView(space(14));

        // ── Recent orders ─────────────────────────────────────────────────
        List<Models.OrderRecord> orders = db.getOrders("all");
        if (!orders.isEmpty()) {
            contentContainer.addView(sectionHeader("سفارش‌های ثبت‌شده اخیر",
                    "وضعیت پرداخت و رهگیری مرسوله‌ها"));
            contentContainer.addView(space(10));
            int shown = 0;
            for (final Models.OrderRecord o : orders) {
                if (shown >= 2) break;
                LinearLayout card = whiteCard();
                LinearLayout top = row(Gravity.CENTER_VERTICAL);
                TextView num = text(ltr(o.orderNumber), 11.5f, COL_TEXT_MAIN, true);
                num.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                top.addView(num, weight(1f));
                top.addView(statusPill(o.status));
                card.addView(top);
                card.addView(space(6));
                card.addView(text(o.customerName + " • " + o.createdAtLabel, 10.5f, COL_TEXT_FAINT, false));
                card.addView(space(8));
                card.addView(resultDarkLine("مبلغ کل", PersianFormatter.formatToman(o.totalAmount)));
                card.addView(space(6));
                LinearLayout orderActions = row(Gravity.CENTER_VERTICAL);
                orderActions.setPadding(0, dp(8), 0, 0);
                TextView invoice = text("اشتراک‌گذاری فاکتور", 9.6f, COL_GOLD_DEEP, true);
                invoice.setGravity(Gravity.CENTER);
                invoice.setPadding(dp(12), dp(7), dp(12), dp(7));
                invoice.setBackground(roundedShape(0xFFFBF4E7, dp(10), 0xFFEAD9B8, dp(1)));
                invoice.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { shareInvoice(o); }
                });
                orderActions.addView(invoice);
                orderActions.addView(spaceH(8));
                TextView trackBtn = text("پیگیری مرسوله", 9.8f, COL_TEXT_MUTED, true);
                trackBtn.setGravity(Gravity.CENTER);
                trackBtn.setPadding(dp(12), dp(7), dp(12), dp(7));
                trackBtn.setBackground(roundedShape(COL_WHITE, dp(10), COL_BORDER, dp(1)));
                trackBtn.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        trackingQuery = o.orderNumber;
                        customerTab = TAB_CONCIERGE;
                        renderCurrentScreen();
                    }
                });
                orderActions.addView(trackBtn);
                card.addView(orderActions);
                contentContainer.addView(card);
                contentContainer.addView(space(10));
                shown++;
            }
        }

        // ── Designer plaque ───────────────────────────────────────────────
        contentContainer.addView(space(6));
        contentContainer.addView(designerPlaque());
        contentContainer.addView(space(10));
        TextView copyright = text("© ۱۴۰۵ ASIYE STORE  ·  تمام حقوق محفوظ است", 8.8f, 0xFFA79E90, false);
        copyright.setGravity(Gravity.CENTER);
        copyright.setLetterSpacing(0.05f);
        contentContainer.addView(copyright);
        contentContainer.addView(space(12));
    }

    private LinearLayout.LayoutParams weightedTile(int index) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        return lp;
    }

    private View contactTile(final int iconType, String title, String subtitle, final Runnable action) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(10), dp(16), dp(10), dp(16));
        tile.setBackground(roundedShape(0xFF131218, dp(18), 0x2EC8A46B, dp(1)));
        tile.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });

        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(50), dp(50)));
        iconWrap.addView(new Luxury3DIconView(this, iconType, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tile.addView(iconWrap);
        tile.addView(space(8));

        TextView t = text(title, 12f, 0xFFF6EFE3, true);
        t.setGravity(Gravity.CENTER);
        tile.addView(t);

        TextView s = new TextView(this);
        s.setText(subtitle);
        s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
        s.setTextColor(0xFFB0A697);
        s.setGravity(Gravity.CENTER);
        s.setSingleLine(true);
        s.setEllipsize(android.text.TextUtils.TruncateAt.END);
        s.setPadding(0, dp(3), 0, 0);
        if (subtitle.startsWith("+") || subtitle.startsWith("@") || subtitle.matches(".*[A-Za-z].*")) {
            s.setTextDirection(View.TEXT_DIRECTION_LTR);
            s.setTypeface(Typeface.MONOSPACE);
        }
        tile.addView(s);
        return tile;
    }

    private View designerPlaque() {
        LinearLayout plaque = new LinearLayout(this);
        plaque.setOrientation(LinearLayout.VERTICAL);
        plaque.setGravity(Gravity.CENTER_HORIZONTAL);
        plaque.setBackground(roundedShape(COL_OBSIDIAN, dp(24), 0x66C8A46B, dp(1)));
        plaque.setPadding(dp(20), dp(24), dp(20), dp(22));

        FrameLayout seal = new FrameLayout(this);
        seal.setLayoutParams(new LinearLayout.LayoutParams(dp(52), dp(52)));
        seal.setBackground(roundedShape(0xFF131218, dp(20), 0x55C8A46B, dp(1)));
        seal.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_CROWN, true),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        plaque.addView(seal);
        plaque.addView(space(14));

        TextView eyebrow = text("DESIGNED & ARCHITECTED BY", 8.5f, COL_GOLD, false);
        eyebrow.setLetterSpacing(0.3f);
        eyebrow.setTypeface(Typeface.SERIF, Typeface.BOLD);
        eyebrow.setGravity(Gravity.CENTER);
        plaque.addView(eyebrow);
        plaque.addView(space(8));

        TextView name = text("MILAD YAGHOOBI", 22f, COL_GOLD_LIGHT, true);
        name.setTypeface(Typeface.SERIF, Typeface.BOLD);
        name.setLetterSpacing(0.16f);
        name.setGravity(Gravity.CENTER);
        plaque.addView(name);
        plaque.addView(space(10));

        View line = new View(this);
        line.setBackgroundColor(0x66C8A46B);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(dp(56), dp(1));
        lineLp.setMargins(0, 0, 0, dp(12));
        plaque.addView(line, lineLp);

        TextView fa = text("طراحی تجربه کاربری لوکس و معماری اکوسیستم دیجیتال\nبه صورت اختصاصی برای مجموعه ASIYE STORE", 11f, 0xFFBEB6AA, false);
        fa.setGravity(Gravity.CENTER);
        fa.setLineSpacing(dp(4), 1f);
        plaque.addView(fa);
        plaque.addView(space(12));

        TextView version = text(APP_VERSION, 8.5f, 0xFF7E776C, false);
        version.setLetterSpacing(0.2f);
        version.setGravity(Gravity.CENTER);
        plaque.addView(version);
        return plaque;
    }

    private void shareInvoice(Models.OrderRecord o) {
        StringBuilder sb = new StringBuilder();
        sb.append("🧾 فاکتور رسمی ASIYE STORE\n");
        sb.append("شماره سفارش: ").append(o.orderNumber).append("\n");
        sb.append("مشتری: ").append(o.customerName).append("\n");
        sb.append("تاریخ: ").append(o.createdAtLabel).append("\n");
        sb.append("———————————————\n");
        for (Models.OrderItemRecord it : o.items) {
            sb.append("• ").append(it.productName).append(" × ").append(it.quantity)
                    .append(" = ").append(PersianFormatter.formatToman(it.unitPrice * it.quantity)).append("\n");
        }
        sb.append("———————————————\n");
        sb.append("مبلغ نهایی: ").append(PersianFormatter.formatToman(o.totalAmount)).append("\n");
        sb.append("وضعیت: ").append(PersianFormatter.statusLabelFa(o.status)).append("\n");
        sb.append("گواهی اصالت: ").append(o.certificateCode).append("\n");
        sb.append("مدیریت مجموعه: سرکار خانم زنگنه — ").append(OWNER_PHONE_DISPLAY);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, sb.toString());
        try {
            startActivity(Intent.createChooser(share, "ارسال فاکتور"));
        } catch (Exception e) {
            toast("امکان ارسال فاکتور در این دستگاه فعال نیست.");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADMIN · REPORT
    // ══════════════════════════════════════════════════════════════════════
    private void renderAdminReport() {
        final Models.DashboardSnapshot snap = db.getDashboardSnapshot();

        contentContainer.addView(pageTitle("گزارش عملکرد مجموعه",
                "مدیریت سرکار خانم زنگنه • آمار زنده فروش و سفارش‌ها"));
        contentContainer.addView(space(14));

        LinearLayout kpiRow1 = row(Gravity.CENTER_VERTICAL);
        kpiRow1.addView(kpiCard("فروش ۷ روز", PersianFormatter.formatCompactMillions(snap.salesWeek),
                "+۲۱٪ نسبت به هفته گذشته", COL_SUCCESS), weight(1f));
        kpiRow1.addView(spaceH(10));
        kpiRow1.addView(kpiCard("سفارش‌های در جریان",
                PersianFormatter.toPersianDigits(String.valueOf(snap.activeOrdersCount)) + " سفارش",
                "از " + PersianFormatter.toPersianDigits(String.valueOf(snap.totalOrdersCount)) + " سفارش کل", COL_TEXT_MUTED), weight(1f));
        contentContainer.addView(kpiRow1);
        contentContainer.addView(space(10));

        LinearLayout kpiRow2 = row(Gravity.CENTER_VERTICAL);
        kpiRow2.addView(kpiCard("میانگین سبد خرید", PersianFormatter.formatCompactMillions(snap.avgOrderValue),
                "ارزش هر سفارش", COL_TEXT_MUTED), weight(1f));
        kpiRow2.addView(spaceH(10));
        kpiRow2.addView(kpiCard("هشدار موجودی",
                PersianFormatter.toPersianDigits(String.valueOf(snap.lowStockCount)) + " کالا",
                snap.lowStockCount > 0 ? "نیاز به شارژ فوری" : "موجودی سالم است",
                snap.lowStockCount > 0 ? COL_DANGER : COL_SUCCESS), weight(1f));
        contentContainer.addView(kpiRow2);
        contentContainer.addView(space(14));

        // Chart
        LinearLayout chartCard = whiteCard();
        LinearLayout chartHead = row(Gravity.CENTER_VERTICAL);
        chartHead.addView(text("روند فروش هفت روز اخیر", 12.5f, COL_TEXT_MAIN, true), weight(1f));
        TextView legend = text("درآمد (میلیون تومان)", 9f, COL_TEXT_FAINT, false);
        chartHead.addView(legend);
        chartCard.addView(chartHead);
        chartCard.addView(space(6));
        SalesChartView chart = new SalesChartView(this);
        chart.setData(snap.sevenDayRevenue, snap.sevenDayLabels);
        LinearLayout.LayoutParams chartLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(196));
        chart.setLayoutParams(chartLp);
        chartCard.addView(chart);
        chartCard.addView(space(4));
        TextView chartFoot = text("ترتیب زمانی از شش روز پیش تا امروز — روند صعودی فروش مجموعه", 9.5f, COL_TEXT_FAINT, false);
        chartFoot.setGravity(Gravity.CENTER);
        chartCard.addView(chartFoot);
        contentContainer.addView(chartCard);
        contentContainer.addView(space(14));

        // Top products
        LinearLayout topCard = whiteCard();
        topCard.addView(sectionLabel("پرفروش‌ترین کالاهای مجموعه"));
        topCard.addView(space(10));
        List<Models.TopProductRecord> top = db.getTopProducts(3);
        if (top.isEmpty()) {
            topCard.addView(text("پس از ثبت اولین سفارش، تحلیل پرفروش‌ها اینجا نمایش داده می‌شود.", 10.5f, COL_TEXT_FAINT, false));
        } else {
            int rank = 1;
            for (Models.TopProductRecord t : top) {
                LinearLayout line = row(Gravity.CENTER_VERTICAL);
                line.setPadding(0, dp(7), 0, dp(7));
                TextView badge = text(PersianFormatter.toPersianDigits(String.valueOf(rank)), 10f, COL_OBSIDIAN, true);
                badge.setGravity(Gravity.CENTER);
                badge.setBackground(roundedShape(COL_GOLD_LIGHT, dp(20), 0, 0));
                line.addView(badge, new LinearLayout.LayoutParams(dp(22), dp(22)));
                TextView nm = text(t.productName, 11f, COL_TEXT_MAIN, false);
                nm.setMaxLines(1);
                nm.setEllipsize(android.text.TextUtils.TruncateAt.END);
                nm.setPadding(dp(8), 0, dp(8), 0);
                line.addView(nm, weight(1f));
                line.addView(text(PersianFormatter.toPersianDigits(String.valueOf(t.unitsSold)) + " عدد",
                        10f, COL_TEXT_MUTED, false));
                topCard.addView(line);
                rank++;
            }
        }
        contentContainer.addView(topCard);
        contentContainer.addView(space(14));

        // Coupons
        LinearLayout couponCard = whiteCard();
        LinearLayout couponHead = row(Gravity.CENTER_VERTICAL);
        couponHead.addView(sectionLabel("کوپن‌های تخفیف فعال"), weight(1f));
        TextView add = text("+ کوپن جدید", 10.5f, COL_GOLD_DEEP, true);
        add.setPadding(dp(8), dp(4), dp(8), dp(4));
        add.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showCouponBuilderDialog(); }
        });
        couponHead.addView(add);
        couponCard.addView(couponHead);
        couponCard.addView(space(10));
        for (final Models.CouponRecord c : db.getCoupons()) {
            LinearLayout line = row(Gravity.CENTER_VERTICAL);
            line.setPadding(0, dp(6), 0, dp(6));
            TextView code = text(ltr(c.code), 11.5f, COL_TEXT_MAIN, true);
            code.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            line.addView(code, weight(1f));
            String desc = c.discountPercent > 0
                    ? PersianFormatter.toPersianDigits(String.valueOf(c.discountPercent)) + "٪ تخفیف"
                    : PersianFormatter.formatToman(c.discountAmount) + " تخفیف";
            line.addView(text(desc, 10f, COL_TEXT_MUTED, false));
            TextView toggle = text(c.active ? "فعال" : "غیرفعال", 9f,
                    c.active ? COL_SUCCESS : COL_TEXT_FAINT, true);
            toggle.setPadding(dp(9), dp(4), dp(9), dp(4));
            toggle.setBackground(roundedShape(c.active ? COL_SUCCESS_SOFT : COL_IVORY_BG, dp(10),
                    c.active ? 0xFFBFDCCB : COL_BORDER, dp(1)));
            final boolean target = !c.active;
            toggle.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    db.toggleCouponActive(c.id, target);
                    renderCurrentScreen();
                }
            });
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tlp.setMarginStart(dp(8));
            line.addView(toggle, tlp);
            couponCard.addView(line);
        }
        contentContainer.addView(couponCard);
        contentContainer.addView(space(14));

        // Server sync
        LinearLayout syncCard = whiteCard();
        syncCard.addView(sectionLabel("همگام‌سازی با سرور لاراول"));
        syncCard.addView(space(4));
        syncCard.addView(text("کاتالوگ و قیمت‌ها را از API رسمی مجموعه به‌روزرسانی کنید.", 10.3f, COL_TEXT_FAINT, false));
        syncCard.addView(space(8));
        final EditText urlInput = makeInput("آدرس سرور (مثال: https://asiyestore.ir/api/v1)", InputType.TYPE_TEXT_VARIATION_URI, true);
        urlInput.setText(db.getSetting("api_base_url", "https://asiyestore.ir/api/v1"));
        syncCard.addView(urlInput);
        syncCard.addView(space(10));
        syncCard.addView(primaryButton("همگام‌سازی اکنون", Luxury3DIconView.ICON_SHARE, new Runnable() {
            @Override public void run() {
                String url = urlInput.getText().toString().trim();
                db.setSetting("api_base_url", url);
                toast("در حال همگام‌سازی با سرور…");
                new ApiSyncClient().syncCatalogFromBackend(url, "", db, new ApiSyncClient.SyncCallback() {
                    @Override public void onComplete(boolean success, String message, int syncedCount) {
                        toast(message);
                        renderCurrentScreen();
                    }
                });
            }
        }));
        contentContainer.addView(syncCard);
        contentContainer.addView(space(14));

        contentContainer.addView(outlineDarkButton("خروجی گزارش مدیریتی (اشتراک‌گذاری)",
                Luxury3DIconView.ICON_CHART, new Runnable() {
                    @Override public void run() { shareManagementReport(); }
                }));
        contentContainer.addView(space(12));
        contentContainer.addView(footerNote());
    }

    private View kpiCard(String label, String value, String hint, int hintColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_WHITE, dp(18), COL_BORDER, dp(1)));
        card.setPadding(dp(14), dp(13), dp(14), dp(13));

        TextView l = text(label, 9.8f, COL_TEXT_MUTED, false);
        TextView v = text(value, 15.5f, COL_TEXT_MAIN, true);
        v.setPadding(0, dp(6), 0, dp(4));
        v.setSingleLine(true);
        v.setEllipsize(android.text.TextUtils.TruncateAt.END);
        TextView h = text(hint, 8.8f, hintColor, false);
        h.setSingleLine(true);

        card.addView(l);
        card.addView(v);
        card.addView(h);
        return card;
    }

    private void shareManagementReport() {
        Models.DashboardSnapshot s = db.getDashboardSnapshot();
        StringBuilder sb = new StringBuilder();
        sb.append("📊 گزارش مدیریتی ASIYE STORE\n");
        sb.append("مدیریت: سرکار خانم زنگنه\n");
        sb.append("———————————————\n");
        sb.append("فروش ۷ روز: ").append(PersianFormatter.formatToman(s.salesWeek)).append("\n");
        sb.append("کل فروش ثبت‌شده: ").append(PersianFormatter.formatToman(s.totalRevenue)).append("\n");
        sb.append("میانگین سبد: ").append(PersianFormatter.formatToman(s.avgOrderValue)).append("\n");
        sb.append("سفارش‌ها: ").append(s.totalOrdersCount).append(" (در جریان: ").append(s.activeOrdersCount).append(")\n");
        sb.append("کالاهای کم‌موجود: ").append(s.lowStockCount).append("\n");
        sb.append("———————————————\n");
        for (Models.TopProductRecord t : db.getTopProducts(3)) {
            sb.append("• ").append(t.productName).append(" — ").append(t.unitsSold).append(" عدد\n");
        }
        sb.append("\nطراحی اکوسیستم: Milad Yaghoobi");
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, sb.toString());
        try {
            startActivity(Intent.createChooser(share, "ارسال گزارش"));
        } catch (Exception e) {
            toast("امکان ارسال گزارش فعال نیست.");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADMIN · CATALOG
    // ══════════════════════════════════════════════════════════════════════
    private void renderAdminCatalog() {
        LinearLayout headRow = row(Gravity.CENTER_VERTICAL);
        headRow.addView(pageTitle("مدیریت محصولات مجموعه",
                "افزودن، ویرایش قیمت و کنترل نمایش کالاها"), weight(1f));
        contentContainer.addView(headRow);
        contentContainer.addView(space(12));

        contentContainer.addView(primaryButton("افزودن هوشمند کالا با هوش مصنوعی",
                Luxury3DIconView.ICON_AI, new Runnable() {
                    @Override public void run() {
                        resetAiStudio();
                        aiStudioOpen = true;
                        adminTab = ADMIN_CATALOG;
                        renderCurrentScreen();
                    }
                }));
        contentContainer.addView(space(8));
        contentContainer.addView(outlineDarkButton("افزودن دستی کالا (فرم کامل)",
                Luxury3DIconView.ICON_PLUS, new Runnable() {
                    @Override public void run() { showProductEditorDialog(null); }
                }));
        contentContainer.addView(space(14));

        List<Models.Product> products = db.getProducts(false, "همه", "", "newest");
        for (final Models.Product p : products) {
            contentContainer.addView(adminProductRow(p));
            contentContainer.addView(space(10));
        }
        contentContainer.addView(space(6));
        contentContainer.addView(footerNote());
    }

    private View adminProductRow(final Models.Product p) {
        LinearLayout card = whiteCard();

        LinearLayout top = row(Gravity.TOP);
        FrameLayout thumbWrap = new FrameLayout(this);
        thumbWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(56), dp(70)));
        thumbWrap.setClipToOutline(true);
        thumbWrap.setBackground(roundedShape(COL_SILK_CREAM, dp(11), 0, 0));
        ImageView thumb = new ImageView(this);
        Bitmap aiBitmap = loadProductBitmap(p);
        if (aiBitmap != null) thumb.setImageBitmap(aiBitmap);
        else thumb.setImageResource(resolveProductImage(p.imageKey));
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumbWrap.addView(thumb, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(11), 0, 0, 0);
        TextView name = text(p.nameFa, 11.5f, COL_TEXT_MAIN, true);
        name.setMaxLines(2);
        name.setLineSpacing(dp(2), 1f);
        TextView meta = text(ltr(p.sku) + "   •   " + PersianFormatter.formatToman(p.salePrice),
                9.5f, COL_TEXT_FAINT, false);
        meta.setPadding(0, dp(4), 0, 0);
        LinearLayout badgeRow = row(Gravity.CENTER_VERTICAL);
        badgeRow.setPadding(0, dp(6), 0, 0);
        badgeRow.addView(statusChip(p.published ? "منتشرشده" : "آرشیو",
                p.published ? COL_SUCCESS : COL_TEXT_FAINT,
                p.published ? COL_SUCCESS_SOFT : COL_IVORY_BG));
        LinearLayout.LayoutParams gapLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gapLp.setMarginStart(dp(6));
        badgeRow.addView(statusChip("موجودی " + PersianFormatter.toPersianDigits(String.valueOf(p.stock)),
                p.stock <= p.lowStockThreshold ? COL_DANGER : COL_TEXT_MUTED,
                p.stock <= p.lowStockThreshold ? 0xFFFDF1EF : COL_IVORY_BG), gapLp);
        if (p.aiGenerated) {
            LinearLayout.LayoutParams aiLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            aiLp.setMarginStart(dp(6));
            badgeRow.addView(statusChip("هوش مصنوعی", COL_GOLD_DEEP, 0xFFFBF3E4), aiLp);
        }
        info.addView(name);
        info.addView(meta);
        info.addView(badgeRow);

        top.addView(thumbWrap);
        top.addView(info, weight(1f));
        card.addView(top);

        card.addView(space(11));
        LinearLayout actions = row(Gravity.CENTER_VERTICAL);
        actions.addView(smallAction("ویرایش", new Runnable() {
            @Override public void run() { showProductEditorDialog(p); }
        }), weight(1f));
        actions.addView(spaceH(5));
        actions.addView(smallAction(p.published ? "توقف" : "انتشار", new Runnable() {
            @Override public void run() {
                db.setProductPublished(p.id, !p.published);
                renderCurrentScreen();
            }
        }), weight(1f));
        actions.addView(spaceH(5));
        actions.addView(smallAction("موجودی", new Runnable() {
            @Override public void run() { showExactStockDialog(p); }
        }), weight(1f));
        actions.addView(spaceH(5));
        actions.addView(smallDangerAction("حذف", new Runnable() {
            @Override public void run() {
                String msg = db.deleteProduct(p.id);
                toast(msg);
                renderCurrentScreen();
            }
        }));
        card.addView(actions);
        return card;
    }

    private TextView statusChip(String label, int textColor, int bgColor) {
        TextView t = text(label, 8.8f, textColor, true);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(true);
        t.setPadding(dp(8), dp(4), dp(8), dp(4));
        t.setBackground(roundedShape(bgColor, dp(10), 0x22000000, dp(1)));
        return t;
    }

    private View smallAction(String label, final Runnable action) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.8f);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(true);
        t.setTextColor(COL_TEXT_MAIN);
        t.setPadding(dp(4), dp(9), dp(4), dp(9));
        t.setBackground(roundedShape(COL_IVORY_BG, dp(11), COL_BORDER, dp(1)));
        t.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return t;
    }

    private View smallDangerAction(String label, final Runnable action) {
        TextView t = (TextView) smallAction(label, action);
        t.setTextColor(COL_DANGER);
        t.setBackground(roundedShape(0xFFFDF3F1, dp(11), 0xFFEFD8D3, dp(1)));
        return t;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADMIN · ORDERS
    // ══════════════════════════════════════════════════════════════════════
    private void renderAdminOrders() {
        contentContainer.addView(pageTitle("مدیریت سفارش‌های مشتریان",
                "به‌روزرسانی وضعیت، ثبت کد رهگیری و ارتباط مستقیم"));
        contentContainer.addView(space(14));

        String[][] filters = {
                {"همه", "all"}, {"در آماده‌سازی", "preparing"},
                {"ارسال‌شده", "shipped"}, {"تحویل‌شده", "delivered"}
        };
        LinearLayout filterRow = row(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < filters.length; i++) {
            final String key = filters[i][1];
            boolean active = adminOrderFilter.equals(key);
            TextView chip = new TextView(this);
            chip.setText(filters[i][0]);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.8f);
            chip.setSingleLine(true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(2), dp(9), dp(2), dp(9));
            chip.setTextColor(active ? COL_GOLD_LIGHT : COL_TEXT_MUTED);
            chip.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            chip.setBackground(active
                    ? roundedShape(COL_OBSIDIAN, dp(14), COL_OBSIDIAN, dp(1))
                    : roundedShape(COL_WHITE, dp(14), COL_BORDER, dp(1)));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    adminOrderFilter = key;
                    renderCurrentScreen();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i > 0) lp.setMarginStart(dp(6));
            filterRow.addView(chip, lp);
        }
        contentContainer.addView(filterRow);
        contentContainer.addView(space(14));

        List<Models.OrderRecord> orders = db.getOrders(adminOrderFilter);
        if (orders.isEmpty()) {
            contentContainer.addView(emptyCard(Luxury3DIconView.ICON_BAG, "سفارشی در این وضعیت وجود ندارد",
                    "با انتخاب «همه» می‌توانید تمام سفارش‌های ثبت‌شده را ببینید."));
        } else {
            for (final Models.OrderRecord o : orders) {
                contentContainer.addView(adminOrderCard(o));
                contentContainer.addView(space(12));
            }
        }
        contentContainer.addView(footerNote());
    }

    private View adminOrderCard(final Models.OrderRecord o) {
        LinearLayout card = whiteCard();

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        LinearLayout who = new LinearLayout(this);
        who.setOrientation(LinearLayout.VERTICAL);
        who.addView(text(o.customerName, 12.5f, COL_TEXT_MAIN, true));
        TextView meta = text(ltr(o.orderNumber) + "  •  " + o.createdAtLabel, 9.5f, COL_TEXT_FAINT, false);
        meta.setPadding(0, dp(3), 0, 0);
        who.addView(meta);
        head.addView(who, weight(1f));

        head.addView(statusPill(o.status));
        card.addView(head);

        card.addView(space(10));
        for (Models.OrderItemRecord it : o.items) {
            card.addView(resultDarkLine(it.productName,
                    PersianFormatter.toPersianDigits(String.valueOf(it.quantity)) + " عدد"));
        }
        card.addView(space(6));
        card.addView(resultDarkLine("مبلغ کل", PersianFormatter.formatToman(o.totalAmount)));
        if (o.trackingCode != null && !o.trackingCode.isEmpty()) {
            card.addView(resultDarkLine("کد رهگیری", ltr(o.trackingCode)));
        }
        card.addView(resultDarkLine("تحویل", o.shippingMethod));
        if (o.trackingCode != null && !o.trackingCode.isEmpty()) {
            card.addView(resultDarkLine("کد رهگیری", ltr(o.trackingCode)));
        }
        card.addView(space(10));

        LinearLayout actions = row(Gravity.CENTER_VERTICAL);
        actions.addView(smallAction("پیگیری تلفنی", new Runnable() {
            @Override public void run() { dialNumber(o.customerMobile); }
        }), weight(1f));
        actions.addView(spaceH(6));
        actions.addView(smallAction("ثبت کد رهگیری", new Runnable() {
            @Override public void run() { showTrackingDialog(o); }
        }), weight(1f));
        if (!"delivered".equals(o.status) && !"cancelled".equals(o.status)) {
            actions.addView(spaceH(6));
            actions.addView(smallAction("مرحله بعد", new Runnable() {
                @Override public void run() {
                    String next = nextStatus(o.status);
                    db.updateOrderStatus(o.id, next, null);
                    toast("وضعیت سفارش: " + PersianFormatter.statusLabelFa(next));
                    renderCurrentScreen();
                }
            }), weight(1f));
        }
        card.addView(actions);
        return card;
    }

    private String nextStatus(String status) {
        if ("pending_payment".equals(status)) return "paid";
        if ("paid".equals(status)) return "confirmed";
        if ("confirmed".equals(status)) return "preparing";
        if ("preparing".equals(status)) return "shipped";
        if ("shipped".equals(status)) return "delivered";
        return "delivered";
    }

    private void showTrackingDialog(final Models.OrderRecord o) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(18));
        box.setBackgroundColor(COL_WHITE);
        box.addView(text("ثبت کد رهگیری مرسوله", 16f, COL_TEXT_MAIN, true));
        box.addView(space(4));
        box.addView(text("سفارش " + ltr(o.orderNumber) + " — " + o.customerName, 10.5f, COL_TEXT_MUTED, false));
        box.addView(space(14));
        final EditText input = makeInput("کد ۲۴ رقمی رهگیری پست", InputType.TYPE_CLASS_NUMBER, true);
        input.setText(o.trackingCode == null ? "" : o.trackingCode);
        box.addView(input);
        box.addView(space(14));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ثبت و ارسال وضعیت", Luxury3DIconView.ICON_SHIELD, new Runnable() {
            @Override public void run() {
                String code = PersianFormatter.toAsciiDigits(input.getText().toString()).trim();
                if (code.length() < 8) {
                    toast("کد رهگیری معتبر نیست.");
                    return;
                }
                db.updateOrderStatus(o.id, "shipped", code);
                dialog.dismiss();
                toast("کد رهگیری ثبت و وضعیت به «ارسال‌شده» تغییر یافت.");
                renderCurrentScreen();
            }
        }));
        box.addView(space(8));
        box.addView(ghostButton("انصراف", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADMIN · INVENTORY
    // ══════════════════════════════════════════════════════════════════════
    private void renderAdminInventory() {
        contentContainer.addView(pageTitle("کنترل موجودی انبار",
                "شارژ سریع کالاهای اورجینال با ثبت خودکار در دفتر انبار"));
        contentContainer.addView(space(12));

        contentContainer.addView(primaryButton("شارژ گروهی کالاهای کم‌موجود (+۵)",
                Luxury3DIconView.ICON_GIFT, new Runnable() {
                    @Override public void run() {
                        int charged = 0;
                        for (Models.Product p : db.getProducts(true, "همه", "", "newest")) {
                            if (p.stock <= p.lowStockThreshold) {
                                db.adjustInventory(p.id, "in", 5, "شارژ گروهی کالاهای کم‌موجود");
                                charged++;
                            }
                        }
                        toast(charged == 0 ? "همه کالاها موجودی کافی دارند." : "موجودی " + charged + " کالا شارژ شد.");
                        renderCurrentScreen();
                    }
                }));
        contentContainer.addView(space(14));

        List<Models.Product> products = db.getProducts(false, "همه", "", "newest");
        for (final Models.Product p : products) {
            contentContainer.addView(inventoryRow(p));
            contentContainer.addView(space(9));
        }

        contentContainer.addView(space(6));
        LinearLayout logCard = whiteCard();
        logCard.addView(sectionLabel("آخرین گردش انبار"));
        logCard.addView(space(8));
        List<Models.StockMovementRecord> moves = db.getRecentMovements(6);
        if (moves.isEmpty()) {
            logCard.addView(text("هنوز گردشی در انبار ثبت نشده است.", 10.5f, COL_TEXT_FAINT, false));
        } else {
            for (Models.StockMovementRecord m : moves) {
                LinearLayout line = row(Gravity.CENTER_VERTICAL);
                line.setPadding(0, dp(5), 0, dp(5));
                TextView sign = text((m.deltaQuantity >= 0 ? "+" : "−") +
                        PersianFormatter.toPersianDigits(String.valueOf(Math.abs(m.deltaQuantity))),
                        10.5f, m.deltaQuantity >= 0 ? COL_SUCCESS : COL_DANGER, true);
                sign.setMinWidth(dp(34));
                TextView name = text(m.productName, 10.5f, COL_TEXT_MAIN, false);
                name.setMaxLines(1);
                name.setEllipsize(android.text.TextUtils.TruncateAt.END);
                name.setPadding(dp(6), 0, dp(6), 0);
                TextView when = text(m.createdAtLabel, 9f, COL_TEXT_FAINT, false);
                line.addView(sign);
                line.addView(name, weight(1f));
                line.addView(when);
                logCard.addView(line);
            }
        }
        contentContainer.addView(logCard);
        contentContainer.addView(space(12));
        contentContainer.addView(footerNote());
    }

    private View inventoryRow(final Models.Product p) {
        LinearLayout card = whiteCard();

        LinearLayout top = row(Gravity.CENTER_VERTICAL);

        FrameLayout thumbWrap = new FrameLayout(this);
        thumbWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(46), dp(56)));
        thumbWrap.setClipToOutline(true);
        thumbWrap.setBackground(roundedShape(COL_SILK_CREAM, dp(10), 0, 0));
        ImageView thumb = new ImageView(this);
        Bitmap aiBitmap = loadProductBitmap(p);
        if (aiBitmap != null) thumb.setImageBitmap(aiBitmap);
        else thumb.setImageResource(resolveProductImage(p.imageKey));
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumbWrap.addView(thumb, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(10), 0, dp(10), 0);
        TextView name = text(p.nameFa, 11f, COL_TEXT_MAIN, true);
        name.setMaxLines(2);
        name.setLineSpacing(dp(2), 1f);
        TextView sku = text(ltr(p.sku) + "  •  انقضا " + p.expiryLabel, 9f, COL_TEXT_FAINT, false);
        sku.setPadding(0, dp(3), 0, 0);
        info.addView(name);
        info.addView(sku);

        top.addView(thumbWrap);
        top.addView(info, weight(1f));

        int stockColor = p.stock <= 0 ? COL_DANGER
                : (p.stock <= p.lowStockThreshold ? COL_WARN : COL_SUCCESS);
        TextView stock = text(PersianFormatter.toPersianDigits(String.valueOf(p.stock)), 11f, COL_WHITE, true);
        stock.setGravity(Gravity.CENTER);
        stock.setMinWidth(dp(34));
        stock.setPadding(dp(8), dp(6), dp(8), dp(6));
        stock.setBackground(roundedShape(stockColor, dp(12), 0, 0));
        top.addView(stock);
        card.addView(top);

        card.addView(space(10));

        // Grouped stepper: [+]  value  [−]  + exact-set button — compact, no giant rows.
        LinearLayout bottom = row(Gravity.CENTER_VERTICAL);
        LinearLayout stepper = row(Gravity.CENTER_VERTICAL);
        stepper.setBackground(roundedShape(COL_IVORY_BG, dp(14), COL_BORDER, dp(1)));
        stepper.setPadding(dp(4), dp(4), dp(4), dp(4));
        stepper.addView(stepperIconButton(Luxury3DIconView.ICON_MINUS, new Runnable() {
            @Override public void run() {
                db.adjustInventory(p.id, "out", 1, "کسر دستی موجودی توسط مدیریت");
                renderCurrentScreen();
            }
        }));
        TextView qty = text(PersianFormatter.toPersianDigits(String.valueOf(p.stock)), 11.5f, COL_TEXT_MAIN, true);
        qty.setGravity(Gravity.CENTER);
        qty.setMinWidth(dp(30));
        stepper.addView(qty);
        stepper.addView(stepperIconButton(Luxury3DIconView.ICON_PLUS, new Runnable() {
            @Override public void run() {
                db.adjustInventory(p.id, "in", 1, "شارژ موجودی توسط مدیریت");
                renderCurrentScreen();
            }
        }));
        bottom.addView(stepper);

        TextView exact = text("تنظیم دقیق", 9.5f, COL_TEXT_MUTED, false);
        exact.setGravity(Gravity.CENTER);
        exact.setPadding(dp(10), dp(8), dp(10), dp(8));
        exact.setBackground(roundedShape(COL_WHITE, dp(11), COL_BORDER, dp(1)));
        exact.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showExactStockDialog(p); }
        });
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        elp.setMarginStart(dp(8));
        bottom.addView(exact);

        TextView charge = text("+۵ سریع", 9.5f, COL_GOLD_DEEP, true);
        charge.setGravity(Gravity.CENTER);
        charge.setPadding(dp(10), dp(8), dp(10), dp(8));
        charge.setBackground(roundedShape(0xFFFBF4E7, dp(11), 0xFFE8D6B4, dp(1)));
        charge.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                db.adjustInventory(p.id, "in", 5, "شارژ سریع ۵ عددی مدیریت");
                renderCurrentScreen();
            }
        });
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.setMarginStart(dp(6));
        bottom.addView(charge);

        card.addView(bottom);
        return card;
    }

    private View stepperIconButton(int iconType, final Runnable action) {
        FrameLayout wrap = new FrameLayout(this);
        wrap.setBackground(roundedShape(COL_WHITE, dp(16), COL_BORDER, dp(1)));
        wrap.setLayoutParams(new LinearLayout.LayoutParams(dp(30), dp(30)));
        wrap.addView(new Luxury3DIconView(this, iconType, Luxury3DIconView.PEDESTAL_IVORY),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        wrap.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return wrap;
    }

    private void showExactStockDialog(final Models.Product p) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(18));
        box.setBackgroundColor(COL_WHITE);
        box.addView(text("تنظیم دقیق موجودی انبار", 16f, COL_TEXT_MAIN, true));
        box.addView(space(4));
        box.addView(text(p.nameFa, 10.5f, COL_TEXT_MUTED, false));
        box.addView(space(14));
        final EditText input = makeInput("موجودی دقیق (عدد)", InputType.TYPE_CLASS_NUMBER, true);
        input.setText(PersianFormatter.toAsciiDigits(String.valueOf(p.stock)));
        box.addView(input);
        box.addView(space(14));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ثبت موجودی", Luxury3DIconView.ICON_GIFT, new Runnable() {
            @Override public void run() {
                String raw = PersianFormatter.toAsciiDigits(input.getText().toString()).trim();
                if (raw.isEmpty()) {
                    toast("عدد موجودی را وارد کنید.");
                    return;
                }
                int value = Integer.parseInt(raw);
                db.setExactInventory(p.id, value, "انبارگردانی دقیق مدیریت");
                dialog.dismiss();
                toast("موجودی به " + PersianFormatter.toPersianDigits(String.valueOf(value)) + " عدد به‌روزرسانی شد.");
                renderCurrentScreen();
            }
        }));
        box.addView(space(8));
        box.addView(ghostButton("انصراف", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADMIN · PRODUCT EDITOR & COUPON BUILDER
    // ══════════════════════════════════════════════════════════════════════
    private void showProductEditorDialog(final Models.Product existing) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(18));
        box.setBackgroundColor(COL_WHITE);

        box.addView(text(existing == null ? "افزودن کالای جدید" : "ویرایش کالای ویترین", 16.5f, COL_TEXT_MAIN, true));
        box.addView(space(4));
        box.addView(text("اطلاعات ویترین و قیمت‌گذاری کالای اورجینال", 10.2f, COL_TEXT_MUTED, false));
        box.addView(space(14));

        final EditText name = makeInput("نام فارسی کالا", InputType.TYPE_CLASS_TEXT, false);
        final EditText sku = makeInput("کد کالا (مثال: AS-GAT-150)", InputType.TYPE_CLASS_TEXT, true);
        final EditText brand = makeInput("برند / زیرعنوان", InputType.TYPE_CLASS_TEXT, false);
        final EditText desc = makeInput("توضیح تخصصی کوتاه", InputType.TYPE_CLASS_TEXT, false);
        final EditText volume = makeInput("حجم یا شماره رنگ", InputType.TYPE_CLASS_TEXT, false);
        final EditText orig = makeInput("قیمت پیش از تخفیف (تومان)", InputType.TYPE_CLASS_NUMBER, true);
        final EditText sale = makeInput("قیمت فروش ویژه (تومان)", InputType.TYPE_CLASS_NUMBER, true);
        final EditText stock = makeInput("موجودی انبار (عدد)", InputType.TYPE_CLASS_NUMBER, true);

        if (existing != null) {
            name.setText(existing.nameFa);
            sku.setText(existing.sku);
            brand.setText(existing.nameEn);
            desc.setText(existing.description);
            volume.setText(existing.volume);
            orig.setText(PersianFormatter.toAsciiDigits(String.valueOf(existing.originalPrice)));
            sale.setText(PersianFormatter.toAsciiDigits(String.valueOf(existing.salePrice)));
            stock.setText(PersianFormatter.toAsciiDigits(String.valueOf(existing.stock)));
        } else {
            stock.setText("12");
        }

        EditText[] fields = {name, sku, brand, desc, volume, orig, sale, stock};
        for (int i = 0; i < fields.length; i++) {
            box.addView(fields[i]);
            box.addView(space(8));
        }

        final CheckBox publish = new CheckBox(this);
        publish.setChecked(existing == null || existing.published);
        publish.setText("نمایش فوری در ویترین فروشگاه");
        publish.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        publish.setTextColor(COL_TEXT_MAIN);
        publish.setButtonTintList(ColorStateList.valueOf(COL_GOLD_DEEP));
        box.addView(publish);
        box.addView(space(10));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(scroll).create();
        scroll.addView(box);

        box.addView(primaryButton(existing == null ? "ثبت کالا در ویترین" : "ذخیره تغییرات", Luxury3DIconView.ICON_BOUTIQUE, new Runnable() {
            @Override public void run() {
                long origPrice = parseLong(orig.getText().toString());
                long salePrice = parseLong(sale.getText().toString());
                int stockValue = (int) parseLong(stock.getText().toString());
                String error = db.saveOrUpdateProduct(
                        existing == null ? null : existing.id,
                        sku.getText().toString(),
                        name.getText().toString(),
                        brand.getText().toString(),
                        existing == null ? "مراقبت تخصصی پوست" : existing.category,
                        desc.getText().toString(),
                        existing == null ? "اورجینال" : existing.country,
                        volume.getText().toString(),
                        origPrice, salePrice, stockValue, publish.isChecked(),
                        existing == null ? "gatieau_whitening" : existing.imageKey);
                if (error == null) {
                    dialog.dismiss();
                    toast(existing == null ? "کالای جدید به ویترین اضافه شد." : "تغییرات کالا ذخیره شد.");
                    renderCurrentScreen();
                } else {
                    toast(error);
                }
            }
        }));
        box.addView(space(8));
        box.addView(ghostButton("انصراف", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));

        showRoundedDialog(dialog);
        Window w = dialog.getWindow();
        if (w != null) {
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.88f);
            w.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.94f), height);
        }
    }

    private void showCouponBuilderDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(18));
        box.setBackgroundColor(COL_WHITE);
        box.addView(text("ساخت کوپن تخفیف اختصاصی", 16.5f, COL_TEXT_MAIN, true));
        box.addView(space(4));
        box.addView(text("برای مشتریان وفادار مجموعه، کد اختصاصی بسازید.", 10.2f, COL_TEXT_MUTED, false));
        box.addView(space(14));

        final EditText code = makeInput("کد تخفیف (مثال: ASIYEVIP)", InputType.TYPE_CLASS_TEXT, true);
        final EditText percent = makeInput("درصد تخفیف (۰ تا ۹۰)", InputType.TYPE_CLASS_NUMBER, true);
        final EditText amount = makeInput("یا مبلغ ثابت تخفیف (تومان)", InputType.TYPE_CLASS_NUMBER, true);
        final EditText min = makeInput("حداقل مبلغ سبد (تومان)", InputType.TYPE_CLASS_NUMBER, true);
        final EditText max = makeInput("سقف تخفیف (تومان)", InputType.TYPE_CLASS_NUMBER, true);
        EditText[] fields = {code, percent, amount, min, max};
        for (int i = 0; i < fields.length; i++) {
            box.addView(fields[i]);
            box.addView(space(8));
        }

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ثبت کوپن", Luxury3DIconView.ICON_GIFT, new Runnable() {
            @Override public void run() {
                String error = db.createOrUpdateCoupon(code.getText().toString(),
                        (int) parseLong(percent.getText().toString()),
                        parseLong(amount.getText().toString()),
                        parseLong(min.getText().toString()),
                        parseLong(max.getText().toString()));
                if (error == null) {
                    dialog.dismiss();
                    toast("کوپن تخفیف ثبت شد.");
                    renderCurrentScreen();
                } else {
                    toast(error);
                }
            }
        }));
        box.addView(space(8));
        box.addView(ghostButton("انصراف", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    private long parseLong(String raw) {
        String clean = PersianFormatter.toAsciiDigits(raw).replace(",", "").replace("،", "").trim();
        if (clean.isEmpty()) return 0L;
        try {
            return Math.abs(Long.parseLong(clean));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SMART SEARCH
    // ══════════════════════════════════════════════════════════════════════
    private void openSmartSearchDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(20), dp(20), dp(16));
        box.setBackgroundColor(COL_WHITE);

        box.addView(text("جستجوی هوشمند ویترین", 16.5f, COL_TEXT_MAIN, true));
        box.addView(space(4));
        box.addView(text("نام کالا، برند، کد سریال یا نیاز پوستی خود را بنویسید.", 10.2f, COL_TEXT_MUTED, false));
        box.addView(space(12));

        final EditText query = makeInput("جستجو در کلکسیون امضایی…", InputType.TYPE_CLASS_TEXT, false);
        box.addView(query);
        box.addView(space(12));

        final LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);

        final ScrollView resultsScroll = new ScrollView(this);
        resultsScroll.addView(results);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(340));
        box.addView(resultsScroll, scrollLp);

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();

        final Runnable refresh = new Runnable() {
            @Override public void run() {
                results.removeAllViews();
                List<Models.Product> found = db.getProducts(true, "همه", query.getText().toString(), "newest");
                if (found.isEmpty()) {
                    results.addView(text("کالایی با این عبارت یافت نشد.", 11f, COL_TEXT_FAINT, false));
                    return;
                }
                for (final Models.Product p : found) {
                    LinearLayout line = new LinearLayout(MainActivity.this);
                    line.setOrientation(LinearLayout.VERTICAL);
                    line.setBackground(roundedShape(COL_IVORY_BG, dp(13), COL_BORDER, dp(1)));
                    line.setPadding(dp(10), dp(9), dp(10), dp(9));

                    LinearLayout rowTop = row(Gravity.CENTER_VERTICAL);
                    FrameLayout thumbWrap = new FrameLayout(MainActivity.this);
                    thumbWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(48)));
                    thumbWrap.setClipToOutline(true);
                    thumbWrap.setBackground(roundedShape(COL_SILK_CREAM, dp(9), 0, 0));
                    ImageView thumb = new ImageView(MainActivity.this);
                    Bitmap aiBmp = loadProductBitmap(p);
                    if (aiBmp != null) thumb.setImageBitmap(aiBmp);
                    else thumb.setImageResource(resolveProductImage(p.imageKey));
                    thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    thumbWrap.addView(thumb, new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    LinearLayout info = new LinearLayout(MainActivity.this);
                    info.setOrientation(LinearLayout.VERTICAL);
                    info.setPadding(dp(10), 0, dp(10), 0);
                    TextView nm = text(p.nameFa, 10.8f, COL_TEXT_MAIN, true);
                    nm.setMaxLines(2);
                    nm.setLineSpacing(dp(2), 1f);
                    TextView pr = text(PersianFormatter.formatToman(p.salePrice), 10f, COL_GOLD_DEEP, true);
                    pr.setPadding(0, dp(3), 0, 0);
                    info.addView(nm);
                    info.addView(pr);

                    rowTop.addView(thumbWrap);
                    rowTop.addView(info, weight(1f));
                    line.addView(rowTop);

                    TextView addBtn = text("＋ افزودن به سبد خرید", 9.5f, COL_OBSIDIAN, true);
                    addBtn.setGravity(Gravity.CENTER);
                    addBtn.setPadding(dp(8), dp(7), dp(8), dp(7));
                    addBtn.setBackground(roundedShape(COL_GOLD_LIGHT, dp(11), 0, 0));
                    LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    alp.setMargins(0, dp(8), 0, 0);
                    line.addView(addBtn, alp);

                    line.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            addToCart(p);
                            dialog.dismiss();
                        }
                    });
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, 0, dp(7));
                    results.addView(line, lp);
                }
            }
        };
        refresh.run();

        query.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refresh.run(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        box.addView(ghostButton("بستن", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  INBOX ACTIONS · CALL / SMS / WHATSAPP / INSTAGRAM
    // ══════════════════════════════════════════════════════════════════════
    private void dialOwner() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + OWNER_PHONE_E164)));
    }

    private void dialNumber(String raw) {
        String clean = PersianFormatter.toAsciiDigits(raw).replace(" ", "");
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clean)));
        } catch (Exception e) {
            toast("امکان تماس در این دستگاه فعال نیست.");
        }
    }

    private void smsOwner() {
        String message = "سلام و درود خدمت سرکار خانم زنگنه 🌹\n"
                + "اینجانب از طریق اپلیکیشن رسمی ASIYE STORE پیام می‌دهم.\n"
                + "شناسه پورتال: " + appRef() + "\n"
                + "موضوع: مشاوره و سفارش محصول\n"
                + "لطفاً راهنمایی فرمایید.";
        try {
            Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + OWNER_PHONE_E164));
            sms.putExtra("sms_body", message);
            startActivity(sms);
        } catch (Exception e) {
            Intent sms = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + OWNER_PHONE_E164));
            sms.putExtra("sms_body", message);
            startActivity(sms);
        }
    }

    private void openSmartWhatsApp(String topic, String productName) {
        StringBuilder sb = new StringBuilder();
        sb.append("سلام و درود خدمت سرکار خانم زنگنه 🌹").append("\n");
        sb.append("این پیام از طریق پلتفرم رسمی ASIYE STORE (اپلیکیشن اندروید) ارسال می‌شود.").append("\n");
        sb.append("شناسه پورتال: ").append(appRef()).append("\n");
        sb.append("موضوع: ").append(topic).append("\n");
        if (productName != null && !productName.isEmpty()) {
            sb.append("محصول: ").append(productName).append("\n");
        }
        if (!cart.isEmpty()) {
            sb.append("سبد خرید فعلی:").append("\n");
            for (Models.CartItem item : cart) {
                sb.append("• ").append(item.product.nameFa).append(" × ").append(item.quantity)
                        .append(" (").append(item.product.volume).append(")").append("\n");
            }
            sb.append("جمع سبد: ").append(PersianFormatter.formatToman(calculateCartSubtotal())).append("\n");
        }
        sb.append("لطفاً راهنمایی بفرمایید. سپاس از شما.");
        String encoded;
        try {
            encoded = java.net.URLEncoder.encode(sb.toString(), "UTF-8");
        } catch (Exception e) {
            encoded = sb.toString();
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://wa.me/" + OWNER_WA_NUMBER + "?text=" + encoded)));
        } catch (Exception e) {
            toast("برای استفاده از واتس‌اپ، نصب بودن آن الزامی است.");
        }
    }

    private String appRef() {
        if (!cart.isEmpty()) {
            int code = Math.abs((cart.get(0).product.sku + cart.size()).hashCode()) % 9000 + 1000;
            return "AS-APP-" + code;
        }
        Random random = new Random();
        return "AS-APP-" + (1000 + random.nextInt(8999));
    }

    private void openInstagramPage() {
        try {
            Intent app = new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL));
            app.setPackage("com.instagram.android");
            startActivity(app);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL)));
            } catch (Exception ignored) {
                toast("مرورگری برای باز کردن اینستاگرام یافت نشد.");
            }
        }
    }

    private void addOwnerToContacts() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, "سرکار خانم زنگنه — ASIYE STORE");
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, OWNER_PHONE_E164);
        try {
            startActivity(intent);
        } catch (Exception e) {
            copyToClipboard(OWNER_PHONE_E164);
            toast("شماره مدیریت کپی شد؛ امکان افزودن خودکار مخاطب نیست.");
        }
    }

    private void copyToClipboard(String value) {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            manager.setPrimaryClip(ClipData.newPlainText("ASIYE STORE", value));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CART
    // ══════════════════════════════════════════════════════════════════════
    private void addToCart(Models.Product p) {
        if (p.stock <= 0) {
            toast("این کالا فعلاً موجود نیست؛ با مدیریت تماس بگیرید.");
            return;
        }
        for (Models.CartItem item : cart) {
            if (item.product.id == p.id) {
                if (item.quantity < p.stock) {
                    item.quantity++;
                    toast("تعداد در سبد افزایش یافت (" + PersianFormatter.toPersianDigits(String.valueOf(item.quantity)) + " عدد).");
                } else {
                    toast("بیشتر از موجودی انبار قابل افزودن نیست.");
                }
                return;
            }
        }
        Models.CartItem item = new Models.CartItem();
        item.product = p;
        item.selectedVariant = p.volume;
        item.quantity = 1;
        cart.add(item);
        toast("«" + p.nameFa + "» به سبد اختصاصی شما اضافه شد.");
    }

    private int totalCartItems() {
        int total = 0;
        for (Models.CartItem item : cart) total += item.quantity;
        return total;
    }

    private long calculateCartSubtotal() {
        long total = 0L;
        for (Models.CartItem item : cart) total += item.lineTotal();
        return total;
    }

    private int resolveProductImage(String key) {
        if (key == null) return R.drawable.img_gatieau_whitening;
        switch (key) {
            case "gatieau_antiwrinkle": return R.drawable.img_gatieau_antiwrinkle;
            case "luxury_coin":         return R.drawable.img_luxury_coin;
            case "hair_friend":         return R.drawable.img_hair_friend;
            case "ordinary_toner":      return R.drawable.img_ordinary_toner;
            case "moringa_teatree":     return R.drawable.img_moringa_teatree;
            default:                    return R.drawable.img_gatieau_whitening;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CHECKOUT · PAYMENT MODES / INSTALLMENTS / GATEWAYS  (v6.0)
    // ══════════════════════════════════════════════════════════════════════
    private View buildPaymentModePanel(long basePayable, List<Models.InstallmentPlan> plans,
                                       InstallmentEngine.Config cfg, Models.InstallmentPlan activePlan) {
        LinearLayout card = whiteCard();

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        head.addView(sectionLabel("روش پرداخت و تسویه"), weight(1f));
        TextView official = text("۳ روش رسمی", 8.8f, COL_GOLD_DEEP, true);
        official.setPadding(dp(9), dp(4), dp(9), dp(4));
        official.setBackground(roundedShape(0xFFFBF4E7, dp(9), 0xFFEAD9B8, dp(1)));
        head.addView(official);
        card.addView(head);
        card.addView(space(10));

        card.addView(paymentModeRow(PAY_INSTALLMENT, Luxury3DIconView.ICON_INSTALLMENT,
                "خرید اقساطی (BNPL)",
                plans.isEmpty()
                        ? "برای مبلغ فعلی سبد قابل استفاده نیست"
                        : cfg.providerLabel + " • تا " + PersianFormatter.toPersianDigits(String.valueOf(plans.get(plans.size() - 1).months)) + " قسط",
                plans.isEmpty()));
        card.addView(space(8));
        card.addView(paymentModeRow(PAY_ONLINE, Luxury3DIconView.ICON_CARD,
                "پرداخت امن آنلاین",
                "زرین‌پال • شاپرک • آیدی‌پی و سایر درگاه‌ها", false));
        card.addView(space(8));
        card.addView(paymentModeRow(PAY_COD, Luxury3DIconView.ICON_WALLET,
                "پرداخت در محل مرسوله",
                db.getSettingBool("pay_cod_enabled", true) ? "پرداخت نقدی هنگام تحویل" : "غیرفعال توسط مدیریت",
                !db.getSettingBool("pay_cod_enabled", true)));

        if (PAY_INSTALLMENT.equals(payMode)) {
            card.addView(space(14));
            if (plans.isEmpty()) {
                LinearLayout warn = new LinearLayout(this);
                warn.setOrientation(LinearLayout.VERTICAL);
                warn.setBackground(roundedShape(0xFFFDF7EC, dp(13), 0xFFEBD9B6, dp(1)));
                warn.setPadding(dp(12), dp(11), dp(12), dp(11));
                warn.addView(text("شرایط فروش اقساطی برای این سبد", 10.8f, COL_WARN, true));
                warn.addView(text("حداقل مبلغ خرید اقساطی " + PersianFormatter.formatToman(cfg.minAmount)
                        + " است. با افزودن کالای دیگر به سبد، گزینه‌های اقساط فعال می‌شود.",
                        9.8f, COL_TEXT_MUTED, false));
                card.addView(warn);
            } else {
                card.addView(text("طرح اقساطی خود را انتخاب کنید", 10.8f, COL_TEXT_MUTED, true));
                card.addView(space(8));
                for (Models.InstallmentPlan plan : plans) {
                    card.addView(installmentPlanRow(plan, plan.id.equals(selectedInstallmentId)));
                    card.addView(space(7));
                }
                if (activePlan != null) {
                    card.addView(space(4));
                    LinearLayout sheet = new LinearLayout(this);
                    sheet.setOrientation(LinearLayout.VERTICAL);
                    sheet.setBackground(roundedShape(COL_OBSIDIAN, dp(15), 0x40C8A46B, dp(1)));
                    sheet.setPadding(dp(13), dp(12), dp(13), dp(12));
                    sheet.addView(text("جدول بازپرداخت", 9.5f, COL_GOLD, false));
                    sheet.addView(space(6));
                    sheet.addView(resultLine("مبلغ کل خرید", PersianFormatter.formatToman(basePayable)));
                    sheet.addView(resultLine("پیش‌پرداخت امروز", PersianFormatter.formatToman(activePlan.downPayment)));
                    sheet.addView(resultLine("تعداد اقساط",
                            PersianFormatter.toPersianDigits(String.valueOf(activePlan.months)) + " قسط"));
                    sheet.addView(resultLine("مبلغ هر قسط", PersianFormatter.formatToman(activePlan.monthlyPayment)));
                    if (activePlan.totalFee > 0) {
                        sheet.addView(resultLine("کارمزد تسهیلات", PersianFormatter.formatToman(activePlan.totalFee)));
                    }
                    sheet.addView(resultLine("مجموع بازپرداخت", PersianFormatter.formatToman(activePlan.payableTotal)));
                    card.addView(sheet);
                }
            }
        } else if (PAY_COD.equals(payMode)) {
            card.addView(space(12));
            card.addView(text("این سفارش بدون پرداخت آنلاین ثبت می‌شود؛ مبلغ در زمان تحویل مرسوله به پیک یا مأمور پست پرداخت می‌گردد.",
                    10f, COL_TEXT_MUTED, false));
        }

        card.addView(space(12));
        if (adminMode) {
            TextView manage = text("مدیریت درگاه‌ها و شرایط اقساط", 9.6f, COL_GOLD_DEEP, true);
            manage.setPadding(0, dp(4), 0, dp(2));
            manage.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!adminAuthenticated) {
                    showAdminLoginDialog();
                } else {
                    adminMode = true;
                    aiStudioOpen = false;
                    adminTab = ADMIN_SETTINGS;
                    renderCurrentScreen();
                }
            }
            });
            card.addView(manage);
        } else {
            TextView note = text("شرایط اقساط پس از ثبت سفارش توسط مدیریت مجموعه بررسی و تأیید می‌شود.",
                    9.4f, COL_TEXT_FAINT, false);
            note.setLineSpacing(dp(2), 1f);
            card.addView(note);
        }
        return card;
    }

    private View paymentModeRow(final String mode, int iconType, String title, String subtitle, boolean disabled) {
        boolean selected = payMode.equals(mode);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        box.setPadding(dp(12), dp(11), dp(12), dp(11));
        box.setBackground(roundedShape(selected ? 0xFFFFFBF2 : COL_WHITE, dp(15),
                selected ? COL_GOLD : COL_BORDER, dp(selected ? 2 : 1)));

        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));
        iconWrap.addView(new Luxury3DIconView(this, iconType,
                        selected ? Luxury3DIconView.PEDESTAL_GOLD : Luxury3DIconView.PEDESTAL_OBSIDIAN),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        box.addView(iconWrap);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setPadding(dp(10), 0, dp(8), 0);
        TextView t = text(title, 11.8f, disabled ? COL_TEXT_FAINT : COL_TEXT_MAIN, selected);
        TextView s = text(subtitle, 9.2f, COL_TEXT_FAINT, false);
        s.setPadding(0, dp(3), 0, 0);
        textCol.addView(t);
        textCol.addView(s);
        box.addView(textCol, weight(1f));

        box.addView(radioMark(selected));

        if (!disabled) {
            box.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    payMode = mode;
                    renderCurrentScreen();
                }
            });
        }
        return box;
    }

    /** Crisp 2-layer radio control — replaces the faint ● / ○ glyphs. */
    private View radioMark(boolean selected) {
        FrameLayout ring = new FrameLayout(this);
        ring.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        ring.setBackground(roundedShape(selected ? 0xFFFFFBF2 : COL_WHITE, dp(10),
                selected ? COL_GOLD : 0xFFDCD3C2, dp(selected ? 2 : 1)));
        if (selected) {
            View dot = new View(this);
            dot.setBackground(roundedShape(COL_GOLD_DEEP, dp(5), 0, 0));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(9), dp(9));
            lp.gravity = Gravity.CENTER;
            ring.addView(dot, lp);
        }
        return ring;
    }

    /** On-palette status pill: soft tinted bed + saturated label (no purple/blue). */
    private TextView statusPill(String status) {
        int color = PersianFormatter.statusColor(status);
        TextView pill = text(PersianFormatter.statusLabelFa(status), 9.3f, color, true);
        pill.setGravity(Gravity.CENTER);
        pill.setSingleLine(true);
        pill.setPadding(dp(10), dp(5), dp(10), dp(5));
        pill.setBackground(roundedShape((color & 0x00FFFFFF) | 0x1A000000, dp(11), (color & 0x00FFFFFF) | 0x33000000, dp(1)));
        return pill;
    }

    private View installmentPlanRow(final Models.InstallmentPlan plan, boolean selected) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(13), dp(11), dp(13), dp(11));
        box.setBackground(roundedShape(selected ? 0xFFFFFBF2 : COL_IVORY_BG, dp(13),
                selected ? COL_GOLD : COL_BORDER, dp(selected ? 2 : 1)));

        LinearLayout top = row(Gravity.CENTER_VERTICAL);
        TextView title = text(plan.titleFa, 11.5f, COL_TEXT_MAIN, selected);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        top.addView(title, weight(1f));
        TextView chip = text(plan.zeroInterest ? "بدون کارمزد" : plan.providerFa, 8.8f,
                plan.zeroInterest ? COL_SUCCESS : COL_TEXT_MUTED, true);
        chip.setPadding(dp(8), dp(3), dp(8), dp(3));
        chip.setBackground(roundedShape(plan.zeroInterest ? COL_SUCCESS_SOFT : COL_WHITE, dp(9),
                plan.zeroInterest ? 0xFFBFDCCB : COL_BORDER, dp(1)));
        top.addView(chip);
        box.addView(top);

        LinearLayout bottom = row(Gravity.CENTER_VERTICAL);
        bottom.setPadding(0, dp(7), 0, 0);
        TextView monthly = text(PersianFormatter.formatToman(plan.monthlyPayment) + " در ماه",
                11f, COL_GOLD_DEEP, true);
        monthly.setSingleLine(true);
        monthly.setEllipsize(TextUtils.TruncateAt.END);
        bottom.addView(monthly, weight(1f));
        TextView fee = text(plan.totalFee > 0
                ? "کارمزد " + PersianFormatter.formatToman(plan.totalFee)
                : "بدون هزینه اضافه", 9f, COL_TEXT_FAINT, false);
        fee.setSingleLine(true);
        bottom.addView(fee);
        box.addView(bottom);

        box.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                selectedInstallmentId = plan.id;
                payMode = PAY_INSTALLMENT;
                renderCurrentScreen();
            }
        });
        return box;
    }

    private View buildGatewayPanel(long payable) {
        LinearLayout card = whiteCard();
        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        head.addView(sectionLabel("درگاه پرداخت آنلاین"), weight(1f));
        head.addView(text(db.getSettingBool("pay_sandbox", true) ? "Sandbox" : "Live", 8.8f,
                db.getSettingBool("pay_sandbox", true) ? COL_WARN : COL_SUCCESS, true));
        card.addView(head);
        card.addView(space(8));

        List<Models.PaymentGatewayRecord> gateways = db.getPaymentGateways();
        int enabledCount = 0;
        boolean selectedExists = false;
        for (Models.PaymentGatewayRecord g : gateways) {
            if (!g.enabled) continue;
            enabledCount++;
            if (g.code.equals(selectedGateway)) selectedExists = true;
        }
        if (enabledCount == 0) {
            card.addView(text("هیچ درگاهی توسط مدیریت فعال نشده است. لطفاً از بخش تنظیمات پرداخت، درگاه مورد نظر را فعال کنید.",
                    10f, COL_DANGER, false));
            return card;
        }
        if (!selectedExists) {
            for (Models.PaymentGatewayRecord g : gateways) {
                if (g.enabled) {
                    selectedGateway = g.code;
                    break;
                }
            }
        }

        for (Models.PaymentGatewayRecord g : gateways) {
            if (!g.enabled) continue;
            final String code = g.code;
            boolean selected = code.equals(selectedGateway);
            LinearLayout line = row(Gravity.CENTER_VERTICAL);
            line.setPadding(dp(11), dp(10), dp(11), dp(10));
            line.setBackground(roundedShape(selected ? 0xFFFFFBF2 : COL_IVORY_BG, dp(12),
                    selected ? COL_GOLD : COL_BORDER, dp(selected ? 2 : 1)));

            TextView radio = text(selected ? "●" : "○", 12f, selected ? COL_GOLD_DEEP : COL_TEXT_FAINT, true);
            line.addView(radio);

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setPadding(dp(8), 0, 0, 0);
            col.addView(text(g.titleFa, 10.8f, COL_TEXT_MAIN, selected));
            col.addView(text(g.subtitleFa, 8.8f, COL_TEXT_FAINT, false));
            line.addView(col, weight(1f));

            line.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    selectedGateway = code;
                    renderCurrentScreen();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(7));
            card.addView(line, lp);
        }

        card.addView(space(4));
        card.addView(resultDarkLine("مبلغ قابل پرداخت", PersianFormatter.formatToman(payable)));
        if (db.getSettingBool("pay_card_to_card", true)) {
            card.addView(space(8));
            LinearLayout c2c = new LinearLayout(this);
            c2c.setOrientation(LinearLayout.VERTICAL);
            c2c.setBackground(roundedShape(COL_IVORY_BG, dp(12), COL_BORDER, dp(1)));
            c2c.setPadding(dp(12), dp(10), dp(12), dp(10));
            c2c.addView(text("کارت‌به‌کارت (پس از تأیید مدیریت)", 9.6f, COL_TEXT_MUTED, true));
            c2c.addView(text(ltr(db.getSetting("pay_card_number", "6037-9975-0000-0000"))
                    + "  —  " + db.getSetting("pay_card_holder", "سرکار خانم زنگنه"), 10f, COL_TEXT_MAIN, false));
            c2c.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    copyToClipboard(db.getSetting("pay_card_number", "6037-9975-0000-0000"));
                    toast("شماره کارت کپی شد.");
                }
            });
            card.addView(c2c);
        }
        return card;
    }


    // ══════════════════════════════════════════════════════════════════════
    //  ADMIN · SETTINGS (online payment · installments · AI studio)
    // ══════════════════════════════════════════════════════════════════════
    private void renderAdminSettings() {
        settingsTextKeys.clear();
        settingsTextInputs.clear();
        settingsCheckKeys.clear();
        settingsCheckInputs.clear();

        contentContainer.addView(pageTitle("تنظیمات فروشگاه",
                "پرداخت آنلاین، فروش اقساطی و استودیوی هوش مصنوعی — همه در یک صفحه"));
        contentContainer.addView(space(14));

        // ── ONLINE PAYMENT ───────────────────────────────────────────────
        LinearLayout pay = whiteCard();
        LinearLayout payHead = row(Gravity.CENTER_VERTICAL);
        FrameLayout payIcon = new FrameLayout(this);
        payIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));
        payIcon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_CARD, Luxury3DIconView.PEDESTAL_GOLD),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        payHead.addView(payIcon);
        LinearLayout payTitles = new LinearLayout(this);
        payTitles.setOrientation(LinearLayout.VERTICAL);
        payTitles.setPadding(dp(10), 0, dp(10), 0);
        payTitles.addView(text("درگاه پرداخت آنلاین", 13.5f, COL_TEXT_MAIN, true));
        payTitles.addView(text("فعال‌سازی درگاه‌ها و تنظیمات پذیرندگی", 9.5f, COL_TEXT_MUTED, false));
        payHead.addView(payTitles, weight(1f));
        pay.addView(payHead);
        pay.addView(space(12));

        pay.addView(settingsToggle("pay_online_enabled", "پرداخت آنلاین فعال باشد"));
        pay.addView(settingsToggle("pay_gateway_zarinpal", "درگاه زرین‌پال"));
        pay.addView(settingsToggle("pay_gateway_shaparak", "درگاه مستقیم شاپرک"));
        pay.addView(settingsToggle("pay_gateway_idpay", "درگاه آیدی‌پی (کارمزد ۱٪)"));
        pay.addView(settingsToggle("pay_gateway_nextpay", "درگاه نکست‌پی"));
        pay.addView(settingsToggle("pay_cod_enabled", "پرداخت در محل (پس‌کرایه)"));
        pay.addView(settingsToggle("pay_card_to_card", "نمایش کارت‌به‌کارت در سبد خرید"));
        pay.addView(settingsToggle("pay_sandbox", "حالت آزمایشی (Sandbox)"));
        pay.addView(space(8));

        pay.addView(settingsText("pay_merchant_id", "شناسه پذیرنده (Merchant ID)", "ASIYE-STORE-ZP", false));
        pay.addView(settingsText("pay_callback_url", "آدرس بازگشت تراکنش (Callback)", "https://asiyestore.ir/api/v1/payment/callback", false));
        pay.addView(settingsText("pay_card_number", "شماره کارت جهت کارت‌به‌کارت", "6037-9975-0000-0000", false));
        pay.addView(settingsText("pay_card_holder", "نام صاحب کارت", "سرکار خانم زنگنه", false));
        pay.addView(settingsText("pay_min_amount", "حداقل مبلغ پرداخت (تومان)", "50000", true));
        pay.addView(settingsText("pay_description", "توضیح تراکنش در صورت‌حساب", "خرید از مجموعه ASIYE STORE", false));
        contentContainer.addView(pay);
        contentContainer.addView(space(14));

        // ── INSTALLMENTS ─────────────────────────────────────────────────
        LinearLayout inst = whiteCard();
        LinearLayout instHead = row(Gravity.CENTER_VERTICAL);
        FrameLayout instIcon = new FrameLayout(this);
        instIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));
        instIcon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_INSTALLMENT, Luxury3DIconView.PEDESTAL_GOLD),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        instHead.addView(instIcon);
        LinearLayout instTitles = new LinearLayout(this);
        instTitles.setOrientation(LinearLayout.VERTICAL);
        instTitles.setPadding(dp(10), 0, dp(10), 0);
        instTitles.addView(text("فروش اقساطی (BNPL)", 13.5f, COL_TEXT_MAIN, true));
        instTitles.addView(text("تعیین شرایط، سقف و کارمزد تسهیلات", 9.5f, COL_TEXT_MUTED, false));
        instHead.addView(instTitles, weight(1f));
        inst.addView(instHead);
        inst.addView(space(12));

        inst.addView(settingsToggle("inst_enabled", "فروش اقساطی فعال باشد"));
        inst.addView(settingsToggle("inst_verify_kyc", "احراز هویت مشتری الزامی باشد"));
        inst.addView(settingsToggle("inst_auto_approve", "تأیید خودکار سفارش‌های اقساطی"));
        inst.addView(space(8));
        inst.addView(settingsText("inst_zero_months", "تعداد اقساط بدون کارمزد", "4", true));
        inst.addView(settingsText("inst_extended_months", "اقساط کارمزددار (با کاما جدا کنید)", "6,12", false));
        inst.addView(settingsText("inst_interest_percent", "نرخ کارمزد سالانه (٪)", "18", true));
        inst.addView(settingsText("inst_min_amount", "حداقل مبلغ خرید اقساطی (تومان)", "1000000", true));
        inst.addView(settingsText("inst_max_amount", "حداکثر مبلغ خرید اقساطی (تومان)", "20000000", true));
        inst.addView(settingsText("inst_down_percent", "درصد پیش‌پرداخت اولیه (٪)", "0", true));
        inst.addView(space(6));

        inst.addView(text("ارائه‌دهنده اعتبار", 10.5f, COL_TEXT_MUTED, true));
        inst.addView(space(6));
        LinearLayout provRow1 = row(Gravity.CENTER_VERTICAL);
        LinearLayout provRow2 = row(Gravity.CENTER_VERTICAL);
        String[][] providers = {
                {"snapp_pay", "اسنپ‌پی"}, {"tara", "تارا"}, {"digipay", "دیجی‌پی"},
                {"azkivam", "ازکی‌وام"}, {"bank", "کارت اعتباری بانکی"}
        };
        String currentProvider = db.getSetting("inst_provider", "snapp_pay");
        for (int i = 0; i < providers.length; i++) {
            final String code = providers[i][0];
            boolean active = currentProvider.equals(code);
            TextView chip = text(providers[i][1], 10f, active ? COL_OBSIDIAN : COL_TEXT_MUTED, active);
            chip.setGravity(Gravity.CENTER);
            chip.setSingleLine(true);
            chip.setPadding(dp(4), dp(9), dp(4), dp(9));
            chip.setBackground(active
                    ? roundedShape(COL_GOLD_LIGHT, dp(12), COL_GOLD_LIGHT, dp(1))
                    : roundedShape(COL_IVORY_BG, dp(12), COL_BORDER, dp(1)));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    db.setSetting("inst_provider", code);
                    renderCurrentScreen();
                    toast("ارائه‌دهنده اعتبار تغییر کرد.");
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), dp(6));
            if (i < 3) provRow1.addView(chip, lp); else provRow2.addView(chip, lp);
        }
        inst.addView(provRow1);
        inst.addView(provRow2);
        contentContainer.addView(inst);
        contentContainer.addView(space(14));

        // ── AI STUDIO ────────────────────────────────────────────────────
        LinearLayout ai = whiteCard();
        LinearLayout aiHead = row(Gravity.CENTER_VERTICAL);
        FrameLayout aiIcon = new FrameLayout(this);
        aiIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));
        aiIcon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_AI, Luxury3DIconView.PEDESTAL_GOLD),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        aiHead.addView(aiIcon);
        LinearLayout aiTitles = new LinearLayout(this);
        aiTitles.setOrientation(LinearLayout.VERTICAL);
        aiTitles.setPadding(dp(10), 0, dp(10), 0);
        aiTitles.addView(text("استودیوی هوش مصنوعی", 13.5f, COL_TEXT_MAIN, true));
        aiTitles.addView(text("کپشن، سئو و تصویر استودیویی خودکار", 9.5f, COL_TEXT_MUTED, false));
        aiHead.addView(aiTitles, weight(1f));
        ai.addView(aiHead);
        ai.addView(space(12));
        ai.addView(settingsToggle("ai_auto_caption", "ساخت خودکار کپشن و سئو پس از تشخیص محصول"));
        ai.addView(settingsText("ai_tone", "لحن نوشتار", "لوکس و تخصصی", false));
        ai.addView(settingsText("ai_instagram_handle", "شناسه اینستاگرام در کپشن", "asiyeh.store", false));
        ai.addView(settingsText("ai_seo_city", "شهر برای سئوی محلی", "تهران", false));
        ai.addView(settingsText("ai_endpoint", "آدرس سرویس ابری هوش مصنوعی (اختیاری)", "خالی = پردازش کاملاً آفلاین", false));
        ai.addView(space(10));
        ai.addView(ghostGoldButton("رفتن به استودیوی هوشمند افزودن کالا", new Runnable() {
            @Override public void run() {
                resetAiStudio();
                aiStudioOpen = true;
                adminTab = ADMIN_CATALOG;
                renderCurrentScreen();
            }
        }));
        contentContainer.addView(ai);
        contentContainer.addView(space(16));

        contentContainer.addView(primaryButton("ذخیره تمام تنظیمات", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() {
                persistSettings();
                toast("تنظیمات فروشگاه با موفقیت ذخیره شد.");
                renderCurrentScreen();
            }
        }));
        contentContainer.addView(space(10));
        contentContainer.addView(outlineDarkButton("بازگشت به گزارش فروش", Luxury3DIconView.ICON_CHART, new Runnable() {
            @Override public void run() {
                adminTab = ADMIN_REPORT;
                renderCurrentScreen();
            }
        }));
        contentContainer.addView(space(12));
        contentContainer.addView(footerNote());
    }

    private View settingsToggle(String key, String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        row.setPadding(0, dp(5), 0, dp(5));

        CheckBox box = new CheckBox(this);
        box.setChecked(db.getSettingBool(key, "1".equals(defaultFor(key))));
        box.setButtonTintList(ColorStateList.valueOf(COL_GOLD_DEEP));
        row.addView(box, new LinearLayout.LayoutParams(dp(34), dp(34)));

        TextView t = text(label, 10.8f, COL_TEXT_MAIN, false);
        t.setPadding(dp(4), 0, 0, 0);
        row.addView(t, weight(1f));

        settingsCheckKeys.add(key);
        settingsCheckInputs.add(box);
        return row;
    }

    private String defaultFor(String key) {
        if (key.startsWith("pay_gateway_idpay") || key.startsWith("pay_gateway_nextpay")) return "0";
        return "1";
    }

    private View settingsText(String key, String label, String hint, boolean numeric) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(6), 0, dp(2));

        TextView l = text(label, 9.6f, COL_TEXT_MUTED, false);
        wrap.addView(l);

        EditText input = makeInput(hint, numeric ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT, true);
        String stored = db.getSetting(key, "");
        input.setText(stored);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(5), 0, dp(2));
        wrap.addView(input, lp);

        settingsTextKeys.add(key);
        settingsTextInputs.add(input);
        return wrap;
    }

    private void persistSettings() {
        for (int i = 0; i < settingsTextKeys.size(); i++) {
            String value = settingsTextInputs.get(i).getText().toString().trim();
            db.setSetting(settingsTextKeys.get(i), value);
        }
        for (int i = 0; i < settingsCheckKeys.size(); i++) {
            db.setSetting(settingsCheckKeys.get(i), settingsCheckInputs.get(i).isChecked() ? "1" : "0");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADMIN · AI PRODUCT STUDIO  (3 inputs → full luxury listing)
    // ══════════════════════════════════════════════════════════════════════
    private void resetAiStudio() {
        aiAnalysis = null;
        aiDraft = null;
        aiStudioPath = "";
        aiBusy = false;
    }

    /**
     * v6.1 · Studio shell hardened.
     *
     * All wizard steps are built into a detached stage first and attached only
     * when complete, so a partial failure can never leave the merchant staring at
     * an empty screen (the previous "only the back button" report).
     */
    private void renderAiStudio() {
        final LinearLayout stage = new LinearLayout(this);
        stage.setOrientation(LinearLayout.VERTICAL);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        bar.setBackground(roundedShape(COL_OBSIDIAN, dp(18), 0x4DC8A46B, dp(1)));
        bar.setPadding(dp(12), dp(11), dp(12), dp(11));

        FrameLayout medal = new FrameLayout(this);
        medal.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));
        medal.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_AI, Luxury3DIconView.PEDESTAL_GOLD),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        bar.addView(medal);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(10), 0, dp(10), 0);
        titles.addView(text("استودیوی هوشمند محصول", 13.5f, 0xFFF6EFE3, true));
        titles.addView(text("نام، برند و یک عکس؛ بقیه مسیر خودکار است", 9.4f, 0xFFB8AE9F, false));
        bar.addView(titles, weight(1f));

        TextView close = text("بستن", 10f, COL_GOLD_LIGHT, true);
        close.setPadding(dp(12), dp(8), dp(12), dp(8));
        close.setBackground(roundedShape(0x26C8A46B, dp(11), 0x59C8A46B, dp(1)));
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                aiStudioOpen = false;
                renderCurrentScreen();
            }
        });
        bar.addView(close);
        stage.addView(bar);

        LinearLayout saved = contentContainer;
        contentContainer = stage;
        try {
            contentContainer.addView(space(12));
            contentContainer.addView(aiStepStrip());
            contentContainer.addView(space(14));
            if (aiDraft != null) {
                renderAiDraft();
            } else if (aiAnalysis != null) {
                renderAiConfirmation();
            } else {
                renderAiInputs();
            }
        } catch (Throwable t) {
            stage.removeAllViews();
            stage.addView(aiBarFallback(bar));
            stage.addView(aiFallbackCard());
        }
        contentContainer = saved;

        // Hard guarantee: the studio can never collapse to just its top bar.
        if (stage.getChildCount() < 3) {
            stage.addView(aiFallbackCard());
        }
        saved.addView(stage);
        saved.addView(space(12));

        final Runnable toTop = new Runnable() {
            @Override public void run() {
                if (contentScroll != null) contentScroll.scrollTo(0, 0);
            }
        };
        if (contentScroll != null) {
            contentScroll.post(toTop);
            contentScroll.postDelayed(toTop, 90L);
        }
    }

    /** Re-attachable copy of the studio bar used by the recovery path. */
    private View aiBarFallback(LinearLayout original) {
        LinearLayout holder = new LinearLayout(this);
        holder.setOrientation(LinearLayout.VERTICAL);
        if (original.getParent() instanceof ViewGroup) {
            ((ViewGroup) original.getParent()).removeView(original);
        }
        holder.addView(original);
        return holder;
    }

    /** Never a blank screen: explains the recovery and offers a clean restart. */
    private View aiFallbackCard() {
        LinearLayout card = whiteCard();
        card.addView(text("استودیو به‌صورت ایمن بازنشانی شد", 13f, COL_TEXT_MAIN, true));
        card.addView(space(6));
        TextView body = text("برای جلوگیری از نمایش ناقص، حافظه استودیو پاک شد. دوباره نام و برند محصول را وارد کنید؛"
                + " اگر مشکل تکرار شد، عکس محصول را با حجم کمتر انتخاب کنید.", 10.4f, COL_TEXT_MUTED, false);
        body.setLineSpacing(dp(3), 1f);
        card.addView(body);
        card.addView(space(12));
        card.addView(primaryButton("شروع دوباره استودیو", Luxury3DIconView.ICON_AI, new Runnable() {
            @Override public void run() {
                resetAiStudio();
                renderCurrentScreen();
            }
        }));
        return card;
    }

    /** Best-effort brand token from a catalog product (used by the quick-start chips). */
    private String extractBrand(Models.Product p) {
        if (p == null) return "";
        if (p.nameEn != null && p.nameEn.contains(" ")) {
            String[] parts = p.nameEn.trim().split("\\s+");
            if (parts.length > 0) return parts[0];
        }
        return p.nameEn == null ? "" : p.nameEn;
    }

    private View aiStepStrip() {
        int step = aiDraft != null ? 3 : (aiAnalysis != null ? 2 : 1);
        String[] labels = {"اطلاعات پایه", "تشخیص محصول", "تولید کامل"};
        LinearLayout strip = row(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < labels.length; i++) {
            boolean done = (i + 1) < step;
            boolean active = (i + 1) == step;
            TextView chip = text((done ? "✓ " : PersianFormatter.toPersianDigits(String.valueOf(i + 1)) + " · ") + labels[i],
                    9.4f, active ? COL_GOLD_LIGHT : (done ? COL_OBSIDIAN : COL_TEXT_FAINT), active || done);
            chip.setGravity(Gravity.CENTER);
            chip.setSingleLine(true);
            chip.setPadding(dp(3), dp(8), dp(3), dp(8));
            chip.setBackground(active
                    ? roundedShape(COL_OBSIDIAN, dp(12), COL_OBSIDIAN, dp(1))
                    : roundedShape(done ? COL_GOLD_LIGHT : COL_WHITE, dp(12),
                    done ? COL_GOLD_LIGHT : COL_BORDER, dp(1)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            strip.addView(chip, lp);
        }
        return strip;
    }

    // ── STEP 1 ───────────────────────────────────────────────────────────
    private void renderAiInputs() {
        LinearLayout card = whiteCard();

        card.addView(text("۱ · نام محصول", 10.5f, COL_TEXT_MUTED, true));
        card.addView(space(6));
        aiNameInput = makeInput("مثال: کرم روشن‌کننده گاتیو", InputType.TYPE_CLASS_TEXT, false);
        aiNameInput.setText(aiName);
        card.addView(aiNameInput);
        card.addView(space(12));

        card.addView(text("۲ · برند محصول", 10.5f, COL_TEXT_MUTED, true));
        card.addView(space(6));
        aiBrandInput = makeInput("مثال: گاتیو / Gatieau", InputType.TYPE_CLASS_TEXT, false);
        aiBrandInput.setText(aiBrand);
        card.addView(aiBrandInput);
        card.addView(space(12));

        card.addView(text("۳ · عکس محصول (اختیاری اما توصیه‌شده)", 10.5f, COL_TEXT_MUTED, true));
        card.addView(space(6));

        if (aiSourceBitmap != null) {
            FrameLayout preview = new FrameLayout(this);
            preview.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(150)));
            preview.setClipToOutline(true);
            preview.setBackground(roundedShape(COL_SILK_CREAM, dp(14), 0, 0));
            ImageView iv = new ImageView(this);
            iv.setImageBitmap(aiSourceBitmap);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            preview.addView(iv, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            card.addView(preview);
            card.addView(space(8));
        }

        card.addView(outlineDarkButton(aiSourceBitmap == null ? "انتخاب عکس از گالری" : "تغییر عکس محصول",
                Luxury3DIconView.ICON_CAMERA, new Runnable() {
                    @Override public void run() { pickProductPhoto(); }
                }));
        contentContainer.addView(card);
        contentContainer.addView(space(14));

        // Quick start from the live catalog — one tap fills the two fields.
        List<Models.Product> quickProducts = db.getProducts(true, "همه", "", "newest");
        if (!quickProducts.isEmpty()) {
            LinearLayout quickWrap = whiteCard();
            quickWrap.addView(text("شروع سریع از کالاهای موجود (اختیاری)", 9.8f, COL_GOLD_DEEP, true));
            quickWrap.addView(space(8));
            LinearLayout qRow1 = row(Gravity.CENTER_VERTICAL);
            LinearLayout qRow2 = row(Gravity.CENTER_VERTICAL);
            int quickIdx = 0;
            for (final Models.Product qp : quickProducts) {
                if (quickIdx >= 4) break;
                TextView chip = text(qp.nameFa.length() > 22 ? qp.nameFa.substring(0, 21) + "…" : qp.nameFa,
                        9.4f, COL_TEXT_MUTED, false);
                chip.setSingleLine(true);
                chip.setGravity(Gravity.CENTER);
                chip.setPadding(dp(4), dp(9), dp(4), dp(9));
                chip.setBackground(roundedShape(COL_IVORY_BG, dp(12), COL_BORDER, dp(1)));
                chip.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        aiName = qp.nameFa;
                        aiBrand = extractBrand(qp);
                        aiAnalysis = AiProductStudio.analyze(aiName, aiBrand, aiSourceBitmap);
                        renderCurrentScreen();
                    }
                });
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                clp.setMargins(dp(3), 0, dp(3), 0);
                if (quickIdx < 2) qRow1.addView(chip, clp); else qRow2.addView(chip, clp);
                quickIdx++;
            }
            quickWrap.addView(qRow1);
            if (quickIdx > 2) {
                quickWrap.addView(space(6));
                quickWrap.addView(qRow2);
            }
            contentContainer.addView(quickWrap);
            contentContainer.addView(space(12));
        }

        contentContainer.addView(primaryButton("تحلیل هوشمند و تشخیص محصول", Luxury3DIconView.ICON_AI, new Runnable() {
            @Override public void run() {
                aiName = aiNameInput == null ? "" : aiNameInput.getText().toString().trim();
                aiBrand = aiBrandInput == null ? "" : aiBrandInput.getText().toString().trim();
                if (aiName.length() < 3) {
                    toast("نام محصول را کامل‌تر وارد کنید.");
                    return;
                }
                try {
                    aiAnalysis = AiProductStudio.analyze(aiName, aiBrand, aiSourceBitmap);
                } catch (Throwable t) {
                    aiSourceBitmap = null;
                    try {
                        aiAnalysis = AiProductStudio.analyze(aiName, aiBrand, null);
                    } catch (Throwable ignored) {
                        aiAnalysis = null;
                    }
                    toast("عکس قابل پردازش نبود؛ تحلیل بدون عکس انجام شد.");
                }
                if (aiAnalysis == null) {
                    toast("تحلیل انجام نشد؛ دوباره تلاش کنید.");
                    return;
                }
                renderCurrentScreen();
            }
        }));
        contentContainer.addView(space(10));
        TextView hint = text("پردازش روی گوشی و بدون نیاز به اینترنت انجام می‌شود؛ هیچ اطلاعاتی از فروشگاه شما خارج نمی‌گردد.",
                9.4f, COL_TEXT_FAINT, false);
        hint.setLineSpacing(dp(3), 1f);
        contentContainer.addView(hint);
    }

    // ── STEP 2 ───────────────────────────────────────────────────────────
    private void renderAiConfirmation() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_OBSIDIAN, dp(22), 0x55C8A46B, dp(1)));
        card.setPadding(dp(18), dp(16), dp(18), dp(16));

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        FrameLayout icon = new FrameLayout(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(38), dp(38)));
        icon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_AI, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        head.addView(icon);
        head.addView(text("تشخیص هوش مصنوعی", 12.5f, 0xFFF6EFE3, true), weight(1f));
        TextView conf = text(PersianFormatter.toPersianDigits(String.valueOf(aiAnalysis.confidence)) + "٪ اطمینان",
                9.5f, COL_GOLD_LIGHT, true);
        head.addView(conf);
        card.addView(head);
        card.addView(space(12));

        // confidence bar
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        bar.setBackgroundColor(0x22C8A46B);
        View fill = new View(this);
        fill.setBackgroundColor(COL_GOLD);
        bar.addView(fill, new LinearLayout.LayoutParams(0, dp(4), Math.max(0.12f, aiAnalysis.confidence / 100f)));
        View rest = new View(this);
        bar.addView(rest, new LinearLayout.LayoutParams(0, dp(4), Math.max(0.02f, 1f - aiAnalysis.confidence / 100f)));
        card.addView(bar);
        card.addView(space(14));

        card.addView(text(aiAnalysis.detectedTitle, 15f, 0xFFF6EFE3, true));
        card.addView(space(6));
        String brandLine = (aiAnalysis.detectedBrand == null || aiAnalysis.detectedBrand.trim().isEmpty())
                ? aiAnalysis.category : aiAnalysis.detectedBrand + "  •  " + aiAnalysis.category;
        TextView brandView = text(brandLine, 10f, COL_GOLD, false);
        brandView.setSingleLine(true);
        brandView.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(brandView);
        card.addView(space(10));
        TextView why = text(aiAnalysis.rationaleFa, 10.3f, 0xFFBEB6AA, false);
        why.setLineSpacing(dp(3), 1f);
        card.addView(why);
        if (aiAnalysis.photoInsightFa != null && !aiAnalysis.photoInsightFa.isEmpty()) {
            card.addView(space(10));
            card.addView(text(aiAnalysis.photoInsightFa, 9.4f, 0xFF9A9186, false));
        }
        contentContainer.addView(card);
        contentContainer.addView(space(12));

        contentContainer.addView(valuationCard(aiAnalysis));
        contentContainer.addView(space(14));

        contentContainer.addView(text("آیا این محصول را درست تشخیص داده‌ام؟", 11.5f, COL_TEXT_MAIN, true));
        contentContainer.addView(space(8));
        contentContainer.addView(primaryButton("بله، درست است — همه بخش‌ها را بساز", Luxury3DIconView.ICON_CHECK,
                new Runnable() {
                    @Override public void run() {
                        try {
                            aiDraft = AiProductStudio.compose(aiAnalysis, aiName, aiBrand);
                        } catch (Throwable t) {
                            toast("ساخت متن ممکن نشد؛ ورودی‌ها را ساده‌تر کنید.");
                            return;
                        }
                        renderCurrentScreen();
                    }
                }));
        contentContainer.addView(space(8));
        contentContainer.addView(ghostButton("خیر، ورودی‌ها را اصلاح می‌کنم", new Runnable() {
            @Override public void run() {
                aiAnalysis = null;
                renderCurrentScreen();
            }
        }));

        if (!aiAnalysis.alternativesFa.isEmpty()) {
            contentContainer.addView(space(12));
            contentContainer.addView(text("یا یکی از این گزینه‌ها را انتخاب کنید:", 10f, COL_TEXT_MUTED, false));
            contentContainer.addView(space(6));
            for (final String alt : aiAnalysis.alternativesFa) {
                TextView chip = text(alt, 10f, COL_TEXT_MAIN, false);
                chip.setPadding(dp(10), dp(9), dp(10), dp(9));
                chip.setBackground(roundedShape(COL_WHITE, dp(12), COL_BORDER, dp(1)));
                chip.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        aiAnalysis.detectedTitle = alt;
                        try {
                            aiDraft = AiProductStudio.compose(aiAnalysis, aiName, aiBrand);
                        } catch (Throwable t) {
                            toast("ساخت متن ممکن نشد.");
                            return;
                        }
                        renderCurrentScreen();
                    }
                });
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, dp(6));
                contentContainer.addView(chip, lp);
            }
        }
    }

    // ── STEP 3 ───────────────────────────────────────────────────────────
    /** Self-proposed: quotes the AI price against the real catalog so the merchant never mis-prices. */
    private View valuationCard(AiProductStudio.Analysis analysis) {
        List<Models.Product> all = db.getProducts(true, "همه", "", "newest");
        long sum = 0;
        int count = 0;
        long min = Long.MAX_VALUE;
        long max = 0;
        for (Models.Product p : all) {
            if (p.salePrice <= 0) continue;
            if (p.category != null && analysis.category != null
                    && !p.category.equals(analysis.category) && count > 0) continue;
            sum += p.salePrice;
            min = Math.min(min, p.salePrice);
            max = Math.max(max, p.salePrice);
            count++;
        }
        if (count == 0) {
            for (Models.Product p : all) {
                if (p.salePrice <= 0) continue;
                sum += p.salePrice;
                min = Math.min(min, p.salePrice);
                max = Math.max(max, p.salePrice);
                count++;
            }
        }
        if (count == 0) return new View(this);

        final long average = sum / count;
        long suggested = analysis.suggestedPrice;
        String positionFa;
        int tint;
        if (suggested < average * 0.85f) {
            positionFa = "اقتصادی‌تر از میانگین مجموعه";
            tint = 0xFF3E7A57;
        } else if (suggested > average * 1.15f) {
            positionFa = "پریمیوم‌تر از میانگین مجموعه";
            tint = 0xFF8A6A2E;
        } else {
            positionFa = "هم‌تراز با میانگین مجموعه";
            tint = 0xFF3E7A57;
        }

        LinearLayout card = whiteCard();
        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        FrameLayout icon = new FrameLayout(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(22), dp(22)));
        icon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_CHART, Luxury3DIconView.PEDESTAL_IVORY),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        head.addView(icon);
        TextView title = text("ارزش‌گذاری سریع", 11.5f, COL_TEXT_MAIN, true);
        title.setPadding(dp(7), 0, 0, 0);
        head.addView(title, weight(1f));
        head.addView(statusChip(positionFa, tint, 0xFFF3EFE5));
        card.addView(head);
        card.addView(space(11));

        LinearLayout stats = row(Gravity.CENTER_VERTICAL);
        stats.addView(statCell("قیمت پیشنهادی", PersianFormatter.formatToman(suggested), COL_TEXT_MAIN), weight(1f));
        stats.addView(statCell("میانگین کاتالوگ", PersianFormatter.formatToman(average), COL_TEXT_MUTED), weight(1f));
        card.addView(stats);
        card.addView(space(10));
        card.addView(text("بازه کالاهای موجود: " + PersianFormatter.formatToman(min) + " تا " + PersianFormatter.formatToman(max)
                + "  •  در " + PersianFormatter.toPersianDigits(String.valueOf(count)) + " کالای منتشرشده",
                9.2f, COL_TEXT_MUTED, false));
        return card;
    }

    private View statCell(String caption, String value, int valueColor) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.addView(text(caption, 9f, COL_TEXT_MUTED, false));
        cell.addView(space(3));
        cell.addView(text(value, 12f, valueColor, true));
        return cell;
    }

    private void renderAiDraft() {
        final AiProductStudio.Draft d = aiDraft;

        LinearLayout card = whiteCard();
        FrameLayout hero = new FrameLayout(this);
        hero.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(190)));
        hero.setClipToOutline(true);
        hero.setBackground(roundedShape(COL_OBSIDIAN, dp(16), 0, 0));
        ImageView heroImg = new ImageView(this);
        if (aiStudioPath != null && !aiStudioPath.isEmpty()) {
            Bitmap bmp = decodeSampled(aiStudioPath, 900);
            if (bmp != null) heroImg.setImageBitmap(bmp);
            else heroImg.setImageResource(resolveProductImage(d.imageKey));
        } else {
            heroImg.setImageResource(resolveProductImage(d.imageKey));
        }
        heroImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        hero.addView(heroImg, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TextView tag = text(aiStudioPath.isEmpty() ? "PREVIEW" : "AI STUDIO", 8.5f, COL_GOLD_LIGHT, true);
        tag.setPadding(dp(8), dp(4), dp(8), dp(4));
        tag.setBackground(roundedShape(0xCC0B0B0E, dp(9), 0x55C8A46B, dp(1)));
        FrameLayout.LayoutParams tagLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tagLp.gravity = Gravity.TOP | Gravity.START;
        tagLp.setMargins(dp(10), dp(10), 0, 0);
        hero.addView(tag, tagLp);
        card.addView(hero);
        card.addView(space(12));
        card.addView(text(d.nameFa, 14.5f, COL_TEXT_MAIN, true));
        card.addView(space(4));
        TextView nameEnView = text(d.nameEn, 9.5f, COL_GOLD_DEEP, false);
        nameEnView.setSingleLine(true);
        nameEnView.setEllipsize(TextUtils.TruncateAt.END);
        nameEnView.setLetterSpacing(0.04f);
        card.addView(nameEnView);
        card.addView(space(10));
        card.addView(outlineDarkButton(aiStudioPath.isEmpty() ? "ساخت تصویر استودیویی اختصاصی" : "بازسازی تصویر استودیویی",
                Luxury3DIconView.ICON_CAMERA, new Runnable() {
                    @Override public void run() { generateStudioImage(); }
                }));
        contentContainer.addView(card);
        contentContainer.addView(space(12));

        // editable commercial fields
        LinearLayout edit = whiteCard();
        edit.addView(sectionLabel("قیمت‌گذاری و موجودی"));
        edit.addView(space(8));
        aiPriceInput = makeInput("قیمت فروش (تومان)", InputType.TYPE_CLASS_NUMBER, true);
        aiPriceInput.setText(PersianFormatter.toAsciiDigits(String.valueOf(d.suggestedPrice)));
        aiOldPriceInput = makeInput("قیمت پیش از تخفیف (تومان)", InputType.TYPE_CLASS_NUMBER, true);
        aiOldPriceInput.setText(PersianFormatter.toAsciiDigits(String.valueOf(d.suggestedOldPrice)));
        aiStockInput = makeInput("موجودی انبار", InputType.TYPE_CLASS_NUMBER, true);
        aiStockInput.setText("12");
        edit.addView(aiPriceInput);
        edit.addView(space(8));
        edit.addView(aiOldPriceInput);
        edit.addView(space(8));
        edit.addView(aiStockInput);
        contentContainer.addView(edit);
        contentContainer.addView(space(12));

        contentContainer.addView(aiCopyBlock("توضیح کوتاه ویترین", d.description, true));
        contentContainer.addView(space(10));
        contentContainer.addView(aiCopyBlock("معرفی کامل و ترغیب‌کننده", d.longDescription, false));
        contentContainer.addView(space(10));
        contentContainer.addView(aiCopyBlock("مزیت‌های کلیدی", d.benefits, false));
        contentContainer.addView(space(10));
        contentContainer.addView(aiCopyBlock("ترکیبات و روش مصرف", d.ingredients + "\n\n" + d.usage, false));
        contentContainer.addView(space(10));
        contentContainer.addView(aiCopyBlock("کلمات کلیدی سئو", d.keywords, false));
        contentContainer.addView(space(10));
        contentContainer.addView(aiCopyBlock("عنوان سئو (SEO Title)", d.seoTitle, false));
        contentContainer.addView(space(10));
        contentContainer.addView(aiCopyBlock("توضیح متا (SEO Description)", d.seoDescription, false));
        contentContainer.addView(space(10));
        contentContainer.addView(aiCopyBlock("کپشن آماده اینستاگرام", d.instagramCaption, false));
        contentContainer.addView(space(16));

        contentContainer.addView(primaryButton("ذخیره کالا در ویترین فروشگاه", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() {
                long price = parseLong(aiPriceInput == null ? "" : aiPriceInput.getText().toString());
                long old = parseLong(aiOldPriceInput == null ? "" : aiOldPriceInput.getText().toString());
                int stock = (int) Math.min(9999L, parseLong(aiStockInput == null ? "" : aiStockInput.getText().toString()));
                if (price <= 0) {
                    toast("قیمت فروش را وارد کنید.");
                    return;
                }
                String error = db.saveAiProduct(null, null, d.nameFa, d.nameEn, d.category,
                        d.description, d.longDescription, d.benefits, d.ingredients, d.usage,
                        d.country, d.volume, old, price, stock, true, d.imageKey, aiStudioPath,
                        d.seoTitle, d.seoDescription, d.keywords, d.instagramCaption);
                if (error == null) {
                    toast("کالا با موفقیت به ویترین اضافه شد.");
                    resetAiStudio();
                    aiStudioOpen = false;
                    adminTab = ADMIN_CATALOG;
                    renderCurrentScreen();
                } else {
                    toast(error);
                }
            }
        }));
        contentContainer.addView(space(8));
        contentContainer.addView(ghostButton("شروع دوباره با محصول دیگر", new Runnable() {
            @Override public void run() {
                resetAiStudio();
                renderCurrentScreen();
            }
        }));
    }

    private View aiCopyBlock(final String title, final String body, boolean highlight) {
        LinearLayout card = whiteCard();
        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        head.addView(text(title, 10.5f, highlight ? COL_GOLD_DEEP : COL_TEXT_MUTED, true), weight(1f));
        TextView copy = text("کپی", 9f, COL_GOLD_DEEP, true);
        copy.setPadding(dp(8), dp(3), dp(8), dp(3));
        copy.setBackground(roundedShape(0xFFFBF4E7, dp(9), 0xFFE8D6B4, dp(1)));
        copy.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                copyToClipboard(body);
                toast("متن کپی شد.");
            }
        });
        head.addView(copy);
        card.addView(head);
        card.addView(space(8));
        TextView content = text(body, highlight ? 11.5f : 10.5f,
                highlight ? COL_TEXT_MAIN : 0xFF4A463F, false);
        content.setLineSpacing(dp(4), 1f);
        card.addView(content);
        return card;
    }

    private void pickProductPhoto() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "انتخاب عکس محصول"), REQ_PICK_PHOTO);
        } catch (Exception e) {
            toast("دسترسی به گالری ممکن نشد.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                java.io.InputStream s1 = getContentResolver().openInputStream(data.getData());
                BitmapFactory.decodeStream(s1, null, bounds);
                if (s1 != null) s1.close();

                int sample = 1;
                int maxSide = Math.max(bounds.outWidth, bounds.outHeight);
                while (maxSide / sample > 1400) sample *= 2;

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = sample;
                java.io.InputStream s2 = getContentResolver().openInputStream(data.getData());
                Bitmap bmp = BitmapFactory.decodeStream(s2, null, opts);
                if (s2 != null) s2.close();

                if (bmp != null) {
                    aiSourceBitmap = bmp;
                    aiStudioPath = "";
                    toast("عکس محصول بارگذاری شد.");
                    renderCurrentScreen();
                } else {
                    toast("خواندن تصویر ممکن نشد.");
                }
            } catch (Exception e) {
                toast("خواندن تصویر از گالری ممکن نشد.");
            }
        }
    }

    private void generateStudioImage() {
        if (aiSourceBitmap == null) {
            toast("ابتدا عکس محصول را از گالری انتخاب کنید.");
            return;
        }
        if (aiBusy) return;
        if (aiDraft != null) {
            long editedPrice = parseLong(aiPriceInput == null ? "" : aiPriceInput.getText().toString());
            long editedOld = parseLong(aiOldPriceInput == null ? "" : aiOldPriceInput.getText().toString());
            if (editedPrice > 0) aiDraft.suggestedPrice = editedPrice;
            if (editedOld > 0) aiDraft.suggestedOldPrice = editedOld;
        }
        aiBusy = true;
        toast("در حال ساخت تصویر استودیویی ابسیدین–طلایی…");
        final Bitmap src = aiSourceBitmap;
        final String brandLine = aiBrand == null || aiBrand.trim().isEmpty() ? "ASIYE STORE" : aiBrand.trim();
        final String productLine = aiAnalysis == null ? "" : aiAnalysis.formFactorFa;
        new Thread(new Runnable() {
            @Override public void run() {
                Bitmap rendered = null;
                String path = "";
                try {
                    rendered = LuxuryImageStudio.render(src, brandLine, productLine);
                    java.io.File file = LuxuryImageStudio.saveToPrivateStorage(MainActivity.this, rendered);
                    if (file != null) path = file.getAbsolutePath();
                } catch (Throwable ignored) { }
                final String finalPath = path;
                aiBusy = false;
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (finalPath.isEmpty()) {
                            toast("ساخت تصویر ممکن نشد؛ از عکس اصلی استفاده می‌شود.");
                        } else {
                            aiStudioPath = finalPath;
                            toast("تصویر استودیویی اختصاصی ساخته شد.");
                            renderCurrentScreen();
                        }
                    }
                });
            }
        }).start();
    }

    /** Memory-safe decode: AI studio renders are 1080², so we downsample for list rows. */
    private Bitmap loadProductBitmap(Models.Product p) {
        if (p == null || p.imagePath == null || p.imagePath.trim().isEmpty()) return null;
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(p.imagePath, bounds);
            int sample = 1;
            int maxSide = Math.max(bounds.outWidth, bounds.outHeight);
            while (maxSide / sample > 640) sample *= 2;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeFile(p.imagePath, opts);
        } catch (Throwable e) {
            return null;
        }
    }

    /** Generic sampled decode so 1080² studio renders never blow up the heap on the UI thread. */
    private Bitmap decodeSampled(String path, int maxSideWanted) {
        if (path == null || path.trim().isEmpty()) return null;
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bounds);
            int sample = 1;
            int maxSide = Math.max(bounds.outWidth, bounds.outHeight);
            while (maxSide / sample > maxSideWanted) sample *= 2;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            return BitmapFactory.decodeFile(path, opts);
        } catch (Throwable e) {
            return null;
        }
    }

    /** Higher-resolution decode used by the immersive product page. */
    private Bitmap loadProductBitmapLarge(Models.Product p) {
        if (p == null || p.imagePath == null || p.imagePath.trim().isEmpty()) return null;
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(p.imagePath, bounds);
            int sample = 1;
            int maxSide = Math.max(bounds.outWidth, bounds.outHeight);
            while (maxSide / sample > 1100) sample *= 2;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            return BitmapFactory.decodeFile(p.imagePath, opts);
        } catch (Throwable e) {
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UI PRIMITIVES
    // ══════════════════════════════════════════════════════════════════════
    private static String ltr(String value) {
        return "\u2066" + value + "\u2069";
    }

    private LinearLayout pageTitle(String title, String subtitle) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(2), dp(2), dp(2), 0);

        TextView t = text(title, 20f, COL_TEXT_MAIN, true);
        t.setLineSpacing(dp(2), 1f);
        TextView s = text(subtitle, 10.8f, COL_TEXT_MUTED, false);
        s.setLineSpacing(dp(3), 1f);
        s.setPadding(0, dp(5), 0, 0);

        wrap.addView(t);
        wrap.addView(s);
        return wrap;
    }

    private LinearLayout sectionHeader(String title, String subtitle) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(text(title, 15f, COL_TEXT_MAIN, true));
        wrap.addView(text(subtitle, 10.2f, COL_TEXT_MUTED, false));
        return wrap;
    }

    private TextView sectionLabel(String label) {
        TextView t = text(label, 12.5f, COL_TEXT_MAIN, true);
        return t;
    }

    private TextView text(String value, float sizeSp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value == null ? "" : value);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private LinearLayout whiteCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_WHITE, dp(20), COL_BORDER, dp(1)));
        card.setPadding(dp(15), dp(15), dp(15), dp(15));
        return card;
    }

    private View emptyCard(int iconType, String title, String subtitle) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setBackground(roundedShape(COL_WHITE, dp(20), COL_BORDER, dp(1)));
        card.setPadding(dp(20), dp(26), dp(20), dp(26));

        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(54), dp(54)));
        iconWrap.addView(new Luxury3DIconView(this, iconType, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        card.addView(iconWrap);
        card.addView(space(12));

        TextView t = text(title, 13.5f, COL_TEXT_MAIN, true);
        t.setGravity(Gravity.CENTER);
        TextView s = text(subtitle, 10.5f, COL_TEXT_MUTED, false);
        s.setGravity(Gravity.CENTER);
        s.setPadding(0, dp(5), 0, 0);
        s.setLineSpacing(dp(3), 1f);
        card.addView(t);
        card.addView(s);
        return card;
    }

    private LinearLayout row(int gravity) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(gravity);
        r.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return r;
    }

    private LinearLayout.LayoutParams weight(float value) {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, value);
    }

    private View space(int heightDp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(heightDp)));
        return v;
    }

    private View spaceH(int widthDp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(widthDp), dp(1)));
        return v;
    }

    private void detach(View view) {
        if (view == null) return;
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) parent.removeView(view);
    }

    private View primaryButton(String label, int iconType, final Runnable action) {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);
        button.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        button.setBackground(roundedShape(COL_GOLD_LIGHT, dp(15), 0x33FFFFFF, dp(1)));
        button.setPadding(dp(12), dp(11), dp(12), dp(11));
        button.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(26), dp(26)));
        iconWrap.addView(new Luxury3DIconView(this, iconType, true),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView t = text(label, 11.5f, COL_OBSIDIAN, true);
        t.setGravity(Gravity.CENTER);
        t.setMaxLines(2);
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        t.setPadding(dp(7), 0, 0, 0);

        button.addView(iconWrap);
        button.addView(t, weight(1f));
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return button;
    }

    private View outlineGoldButton(String label, int iconType, final Runnable action) {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);
        button.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        button.setBackground(roundedShape(0x14C8A46B, dp(15), 0x66C8A46B, dp(1)));
        button.setPadding(dp(10), dp(11), dp(10), dp(11));
        button.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(24), dp(24)));
        iconWrap.addView(new Luxury3DIconView(this, iconType, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView t = text(label, 11f, COL_GOLD_LIGHT, true);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(true);
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        t.setPadding(dp(6), 0, 0, 0);

        button.addView(iconWrap);
        button.addView(t, weight(1f));
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return button;
    }

    private View outlineDarkButton(String label, int iconType, final Runnable action) {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);
        button.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        button.setBackground(roundedShape(COL_OBSIDIAN, dp(15), 0x55C8A46B, dp(1)));
        button.setPadding(dp(12), dp(12), dp(12), dp(12));
        button.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(28), dp(28)));
        iconWrap.addView(new Luxury3DIconView(this, iconType, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView t = text(label, 11.8f, COL_GOLD_LIGHT, true);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(true);
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        t.setPadding(dp(7), 0, 0, 0);

        button.addView(iconWrap);
        button.addView(t, weight(1f));
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return button;
    }

    private View ghostGoldButton(String label, final Runnable action) {
        TextView t = text(label, 10.5f, COL_GOLD_DEEP, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(10), dp(12), dp(10));
        t.setBackground(roundedShape(0xFFFBF4E7, dp(12), 0xFFE8D6B4, dp(1)));
        t.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return t;
    }

    private View ghostGoldDarkButton(String label, final Runnable action) {
        TextView t = text(label, 10.5f, COL_GOLD_LIGHT, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(10), dp(12), dp(10));
        t.setBackground(roundedShape(0x1FC8A46B, dp(12), 0x55C8A46B, dp(1)));
        t.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return t;
    }

    private View ghostButton(String label, final Runnable action) {
        TextView t = text(label, 11f, COL_TEXT_MUTED, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(10), dp(12), dp(10));
        t.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return t;
    }

    private View roundIconButton(int iconType, final Runnable action) {
        FrameLayout wrap = new FrameLayout(this);
        wrap.setBackground(roundedShape(COL_OBSIDIAN, dp(16), 0x40C8A46B, dp(1)));
        wrap.setLayoutParams(new LinearLayout.LayoutParams(dp(46), dp(46)));
        wrap.addView(new Luxury3DIconView(this, iconType, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        wrap.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return wrap;
    }

    private EditText makeInput(String hint, int inputType, boolean ltrText) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(0xFFA9A196);
        input.setTextColor(COL_TEXT_MAIN);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        input.setInputType(inputType);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackground(roundedShape(COL_IVORY_BG, dp(13), COL_BORDER, dp(1)));
        input.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        input.setSingleLine(true);
        input.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        if (ltrText) {
            input.setTextDirection(View.TEXT_DIRECTION_LTR);
        }
        return input;
    }

    private GradientDrawable roundedShape(int fillColor, int radiusPx, int strokeColor, int strokePx) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(fillColor);
        shape.setCornerRadius(radiusPx);
        if (strokePx > 0) shape.setStroke(strokePx, strokeColor);
        return shape;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }
}
