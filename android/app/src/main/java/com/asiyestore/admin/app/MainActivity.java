package com.asiyestore.admin.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
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
    private static final String APP_VERSION         = "V7.1.7 · VIDEO REVIEW";

    // ── Palette (v6.4: one engine, two moods — ivory day & obsidian night) ──
    private static int COL_OBSIDIAN      = 0xFF0B0B0E;
    private static int COL_OBSIDIAN_SOFT = 0xFF141319;
    private static int COL_GOLD          = 0xFFC8A46B;
    private static int COL_GOLD_LIGHT    = 0xFFEBD6B0;

    // Type that lives on the deep surfaces (masthead, hero, seal, footer).
    private static int TEXT_ON_DARK       = 0xFFF6EFE3;
    private static int TEXT_ON_DARK_MUTED = 0xFFBEB6AA;
    private static int TEXT_ON_DARK_FAINT = 0xFFA79E90;

    // Stars keep their 24K warmth in every mood — a gold star is never green.
    private static int STAR_ON           = 0xFF8E6C3A;

    // v6.9.3 — the metal of the house. Every hairline, fill and foil accent is 24K gold,
    // whatever the mood; only the stone behind it changes.
    private static int COL_METAL         = 0xFFC8A46B;
    // Bright accent that stays legible on the deep surfaces (masthead, hero, seal).
    private static int ACCENT_ON_DARK    = 0xFFC8A46B;
    private static int SUCCESS_ON_DARK   = 0xFF9BD8B4;

    private static int COL_GOLD_DEEP     = 0xFF8E6C3A;
    private static int COL_IVORY_BG      = 0xFFFAF8F3;
    private static int COL_SILK_CREAM    = 0xFFF4EEE3;
    private static int COL_WHITE         = 0xFFFFFFFF;
    private static int COL_TEXT_MAIN     = 0xFF1B1A18;
    private static int COL_TEXT_MUTED    = 0xFF5C554A;
    private static int COL_TEXT_FAINT    = 0xFF786F61;
    private static int COL_BORDER        = 0xFFE6DDCC;
    private static int COL_SUCCESS       = 0xFF1E6B4B;
    private static int COL_SUCCESS_SOFT  = 0xFFEAF4EE;
    private static int COL_DANGER        = 0xFF9E3A33;
    private static int COL_WARN          = 0xFF9A6B2F;

    // Secondary surfaces that must also follow the theme.
    private static int SURF_CHIP         = 0xFFFBF4E7;
    private static int SURF_STRIP        = 0xFFFFFDF9;
    private static int SURF_SOFT         = 0xFFFFFBF2;
    private static int SURF_CHIP_LINE    = 0xFFEAD9B8;
    private static int SURF_SUCCESS_LINE = 0xFFBFDCCB;
    private static int SURF_GOLD_LINE    = 0xFFE8D6B4;
    private static int SURF_STAR_OFF     = 0xFFDFD7C8;
    private static int SURF_INACTIVE     = 0xFF9C9284;
    private static int SURF_DIVIDER      = 0x1A000000;
    private static int TEXT_BODY         = 0xFF4E4840;
    private static int TEXT_BODY_2       = 0xFF5A5244;

    /** v6.9 · three house moods: ivory day, royal emerald daylight, obsidian night. */
    private static final int THEME_IVORY  = 0;
    private static final int THEME_EMERALD = 1;
    private static final int THEME_NIGHT  = 2;
    private static final int THEME_SAPPHIRE = 3;
    private static final int THEME_ROSE = 4;
    private static final int THEME_PINK = 5;
    private static int themeMode = THEME_IVORY;
    private static boolean darkMode = false;
    private static final String PREF_THEME = "asiye_prefs";

    private static final int TAB_BOUTIQUE  = 0;
    private static final int TAB_ADVISOR   = 1;
    private static final int TAB_BAG       = 2;
    private static final int TAB_CONCIERGE = 3;
    private static final int TAB_ACCOUNT   = 4;

    private static final int ADMIN_REPORT    = 0;
    private static final int ADMIN_CATALOG   = 1;
    private static final int ADMIN_ORDERS    = 2;
    private static final int ADMIN_INVENTORY = 3;
    private static final int ADMIN_SETTINGS  = 4;

    private static final int REQ_PICK_PHOTO = 4401;
    private static final int REQ_NOTIF      = 4402;

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
    private String giftOccasion = "";
    private String profileName = "";
    private String profileMobile = "";
    private int bundleIdx = 0;
    private String skinFilter = "همه";
    private final List<Long> compareIds = new ArrayList<>();
    private boolean briefReminderOn = false;
    private int giftBudget = -1;
    private long giftSuggestionId = -1L;

    private final List<Models.CartItem> cart = new ArrayList<>();
    private String appliedCouponCode = "";
    private long appliedCouponValue = 0L;
    // v6.9 · gift cards and same-day courier
    private String appliedGiftCode = "";
    private long appliedGiftValue = 0L;
    private boolean courierSelected = false;
    private String courierCity = "تهران";
    private boolean vipGiftPackaging = true;
    private String giftMessageDraft = "";

    private String adminOrderFilter = "all";
    private String adminOrderQuery = "";
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
    private String aiSourcePath = "";
    private volatile int aiGeneration = 0;
    private boolean aiBusy = false;
    private boolean aiAnalysisRunning = false;
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
        loadTheme();
        restoreStudioDraft();
        briefReminderOn = getSharedPreferences(PREF_THEME, MODE_PRIVATE)
                .getBoolean("brief_reminder", false);
        profileName = getSharedPreferences(PREF_THEME, MODE_PRIVATE).getString("profile_name", "");
        profileMobile = getSharedPreferences(PREF_THEME, MODE_PRIVATE).getString("profile_mobile", "");
        if (briefReminderOn) {
            try { DailyBriefReceiver.schedule(this); } catch (Throwable ignored) { }
        }
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

        View shell = buildRootShell();
        setContentView(shell);
        renderCurrentScreen();
        showSplash(shell);

        // v6.8 · the autopilot: a daily routine plus one immediate pass after launch
        try { AutomationReceiver.schedule(this); } catch (Throwable ignored) { }
        try {
            if (SmsGateway.isEnabled(db)) {
                SmsGateway.flushAsync(this, db);
            }
        } catch (Throwable ignored) { }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  v6.7 · OPENING CREST — the logo announces the maison, then dissolves
    // ══════════════════════════════════════════════════════════════════════
    private void showSplash(View shell) {
        if (!(shell instanceof FrameLayout)) return;
        final FrameLayout root = (FrameLayout) shell;

        final FrameLayout veil = new FrameLayout(this);
        veil.setBackgroundColor(COL_OBSIDIAN);
        veil.setClickable(true); // swallows taps while it is on screen

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.setPadding(dp(30), 0, dp(30), 0);

        FrameLayout crest = new FrameLayout(this);
        crest.setLayoutParams(new LinearLayout.LayoutParams(dp(112), dp(112)));
        crest.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(30), lineGold(0x66), dp(1)));
        crest.setClipToOutline(true);
        ImageView mark = new ImageView(this);
        mark.setImageResource(R.drawable.img_brand_emblem);
        mark.setScaleType(ImageView.ScaleType.CENTER_CROP);
        crest.addView(mark, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        column.addView(crest);

        column.addView(space(22));

        TextView word = new TextView(this);
        word.setText("ASIYE STORE");
        word.setTextColor(COL_GOLD_LIGHT);
        word.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21f);
        word.setTypeface(Typeface.SERIF, Typeface.BOLD);
        tracking(word, 0.26f);
        word.setGravity(Gravity.CENTER);
        column.addView(word);

        column.addView(space(9));

        TextView kicker = new TextView(this);
        kicker.setText("IMPERIAL LUXURY BEAUTY MAISON");
        kicker.setTextColor(TEXT_ON_DARK_FAINT);
        kicker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.4f);
        kicker.setTypeface(Typeface.SERIF, Typeface.NORMAL);
        tracking(kicker, 0.3f);
        kicker.setGravity(Gravity.CENTER);
        column.addView(kicker);

        column.addView(space(26));

        LuxuryCanvas.GoldFoilDivider foil = new LuxuryCanvas.GoldFoilDivider(this);
        foil.onDark(true);
        column.addView(foil, new LinearLayout.LayoutParams(dp(168), dp(22)));

        column.addView(space(26));

        TextView owner = new TextView(this);
        owner.setText("مدیریت مجموعه: سرکار خانم زنگنه");
        owner.setTextColor(TEXT_ON_DARK_FAINT);
        owner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.4f);
        owner.setGravity(Gravity.CENTER);
        column.addView(owner);

        veil.addView(column, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        root.addView(veil, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // entrance: crest rises and settles
        crest.setScaleX(0.86f);
        crest.setScaleY(0.86f);
        crest.setAlpha(0f);
        crest.animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(620L).setInterpolator(new DecelerateInterpolator()).start();

        veil.postDelayed(new Runnable() {
            @Override public void run() {
                veil.animate().alpha(0f).setDuration(430L).withEndAction(new Runnable() {
                    @Override public void run() {
                        try { root.removeView(veil); } catch (Throwable ignored) { }
                    }
                }).start();
            }
        }, 1050L);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  v6.4 · DUAL PALETTE — ivory day, obsidian night
    // ══════════════════════════════════════════════════════════════════════
    private void loadTheme() {
        boolean legacyDark = getSharedPreferences(PREF_THEME, MODE_PRIVATE).getBoolean("dark_mode", false);
        boolean chosen = getSharedPreferences(PREF_THEME, MODE_PRIVATE)
                .getBoolean("theme_choice_by_user", false);
        themeMode = getSharedPreferences(PREF_THEME, MODE_PRIVATE)
                .getInt("theme_mode", legacyDark ? THEME_NIGHT : THEME_EMERALD);
        // v6.9.2 — the house daylight is royal emerald: anyone who never used the picker lands there.
        if (!chosen && !legacyDark) themeMode = THEME_EMERALD;
        darkMode = themeMode == THEME_NIGHT;
        applyPalette();
    }

    /** Metallic hairline at any alpha — always 24K, in every mood. */
    private int lineGold(int alpha) {
        return (alpha << 24) | (COL_METAL & 0x00FFFFFF);
    }

    /** Translucent deep surface — obsidian ink or sapphire, never a stray black. */
    private int scrim(int alpha) {
        return (alpha << 24) | (COL_OBSIDIAN & 0x00FFFFFF);
    }

    private void applyPalette() {
        darkMode = themeMode == THEME_NIGHT;

        if (themeMode == THEME_EMERALD) {
            // ROYAL EMERALD — mineral-green ground, forest ink, emerald set in gold.
            COL_OBSIDIAN      = 0xFF08251A;
            COL_OBSIDIAN_SOFT = 0xFF10382A;
            COL_GOLD          = 0xFF1E7A57;
            COL_GOLD_LIGHT    = 0xFFEAF4EE;
            COL_GOLD_DEEP     = 0xFF0B4832;
            COL_IVORY_BG      = 0xFFF3F8F4;
            COL_SILK_CREAM    = 0xFFE4EFE7;
            COL_WHITE         = 0xFFFFFFFF;
            COL_TEXT_MAIN     = 0xFF10241B;
            COL_TEXT_MUTED    = 0xFF435C4D;
            COL_TEXT_FAINT    = 0xFF6C8176;
            COL_BORDER        = 0xFFD7E5DA;
            COL_SUCCESS       = 0xFF14704E;
            COL_SUCCESS_SOFT  = 0xFFE7F5EE;
            COL_DANGER        = 0xFFA3332F;
            COL_WARN          = 0xFF8A6512;
            SURF_CHIP         = 0xFFEAF3EC;
            SURF_STRIP        = 0xFFFBFDFB;
            SURF_SOFT         = 0xFFF5FAF6;
            SURF_CHIP_LINE    = 0xFFCFE2D6;
            SURF_SUCCESS_LINE = 0xFFB6DCCF;
            SURF_GOLD_LINE    = 0xFFCDE4D7;
            SURF_STAR_OFF     = 0xFFD3E2D7;
            STAR_ON           = 0xFF8E6C3A;
            SURF_INACTIVE     = 0xFF8A9C90;
            SURF_DIVIDER      = 0x1A0E2A1E;
            TEXT_BODY         = 0xFF3D5145;
            TEXT_BODY_2       = 0xFF45594D;
            TEXT_ON_DARK       = 0xFFF0F6F0;
            TEXT_ON_DARK_MUTED = 0xFFB4C7BB;
            TEXT_ON_DARK_FAINT = 0xFF9AB0A1;
            COL_METAL          = 0xFFC8A46B;
            ACCENT_ON_DARK     = 0xFFD9B98A;
            SUCCESS_ON_DARK    = 0xFF9BD8B4;
        } else if (themeMode == THEME_PINK) {
            // PINK MAISON: clear petal pink, warm ivory and raspberry accents.
            COL_OBSIDIAN      = 0xFF512536;
            COL_OBSIDIAN_SOFT = 0xFF75334C;
            COL_GOLD          = 0xFFAB2850;
            COL_GOLD_LIGHT    = 0xFFFFEAF0;
            COL_GOLD_DEEP     = 0xFF84203F;
            COL_IVORY_BG      = 0xFFFFEEF3;
            COL_SILK_CREAM    = 0xFFFFDDE7;
            COL_WHITE         = 0xFFFFFBFC;
            COL_TEXT_MAIN     = 0xFF3D202A;
            COL_TEXT_MUTED    = 0xFF70505A;
            COL_TEXT_FAINT    = 0xFF805A65;
            COL_BORDER        = 0xFFF0CAD5;
            COL_SUCCESS       = 0xFF176B4C;
            COL_SUCCESS_SOFT  = 0xFFEAF5EF;
            COL_DANGER        = 0xFFA3332F;
            COL_WARN          = 0xFF805A0F;
            SURF_CHIP         = 0xFFFFE1EA;
            SURF_STRIP        = 0xFFFFF2F6;
            SURF_SOFT         = 0xFFFFF0F4;
            SURF_CHIP_LINE    = 0xFFF1C5D3;
            SURF_SUCCESS_LINE = 0xFFB6DCCF;
            SURF_GOLD_LINE    = 0xFFECCDD0;
            SURF_STAR_OFF     = 0xFFEBCDD3;
            STAR_ON           = 0xFF8E6C3A;
            SURF_INACTIVE     = 0xFFA17B84;
            SURF_DIVIDER      = 0x1A512536;
            TEXT_BODY         = 0xFF60454E;
            TEXT_BODY_2       = 0xFF6B4A55;
            TEXT_ON_DARK       = 0xFFFFF5F7;
            TEXT_ON_DARK_MUTED = 0xFFFFDCE5;
            TEXT_ON_DARK_FAINT = 0xFFF5C8D4;
            COL_METAL          = 0xFFD5A477;
            ACCENT_ON_DARK     = 0xFFFFD8B4;
            SUCCESS_ON_DARK    = 0xFFB8E5CD;
        } else if (themeMode == THEME_SAPPHIRE) {
            // Sapphire and champagne gold: light reading surfaces with deep blue framing.
            COL_OBSIDIAN      = 0xFF091D3B;
            COL_OBSIDIAN_SOFT = 0xFF14345F;
            COL_GOLD          = 0xFF19528E;
            COL_GOLD_LIGHT    = 0xFFEAF2FC;
            COL_GOLD_DEEP     = 0xFF123B69;
            COL_IVORY_BG      = 0xFFF3F7FC;
            COL_SILK_CREAM    = 0xFFE3EDF9;
            COL_WHITE         = 0xFFFFFFFF;
            COL_TEXT_MAIN     = 0xFF12253F;
            COL_TEXT_MUTED    = 0xFF425971;
            COL_TEXT_FAINT    = 0xFF586F86;
            COL_BORDER        = 0xFFD5E2F0;
            COL_SUCCESS       = 0xFF14704E;
            COL_SUCCESS_SOFT  = 0xFFE7F5EE;
            COL_DANGER        = 0xFFA3332F;
            COL_WARN          = 0xFF805A0F;
            SURF_CHIP         = 0xFFE8F0FA;
            SURF_STRIP        = 0xFFFAFCFF;
            SURF_SOFT         = 0xFFF4F8FD;
            SURF_CHIP_LINE    = 0xFFCDDEEF;
            SURF_SUCCESS_LINE = 0xFFB6DCCF;
            SURF_GOLD_LINE    = 0xFFC9DAEC;
            SURF_STAR_OFF     = 0xFFCFDAE8;
            STAR_ON           = 0xFF8E6C3A;
            SURF_INACTIVE     = 0xFF7B8FA4;
            SURF_DIVIDER      = 0x1A091D3B;
            TEXT_BODY         = 0xFF354E69;
            TEXT_BODY_2       = 0xFF425A73;
            TEXT_ON_DARK       = 0xFFF1F6FF;
            TEXT_ON_DARK_MUTED = 0xFFC4D3E5;
            TEXT_ON_DARK_FAINT = 0xFFAABFD5;
            COL_METAL          = 0xFFC8A46B;
            ACCENT_ON_DARK     = 0xFFE4C69A;
            SUCCESS_ON_DARK    = 0xFF9BD8B4;
        } else if (themeMode == THEME_ROSE) {
            // ROSE ATELIER: blush porcelain, deep berry ink and champagne metal.
            COL_OBSIDIAN      = 0xFF40232E;
            COL_OBSIDIAN_SOFT = 0xFF603746;
            COL_GOLD          = 0xFF934A60;
            COL_GOLD_LIGHT    = 0xFFFFEDF1;
            COL_GOLD_DEEP     = 0xFF713144;
            COL_IVORY_BG      = 0xFFFFF4F6;
            COL_SILK_CREAM    = 0xFFF8E5E9;
            COL_WHITE         = 0xFFFFFCFC;
            COL_TEXT_MAIN     = 0xFF38232A;
            COL_TEXT_MUTED    = 0xFF70525D;
            COL_TEXT_FAINT    = 0xFF80616C;
            COL_BORDER        = 0xFFECD5DC;
            COL_SUCCESS       = 0xFF176B4C;
            COL_SUCCESS_SOFT  = 0xFFEAF5EF;
            COL_DANGER        = 0xFFA3332F;
            COL_WARN          = 0xFF805A0F;
            SURF_CHIP         = 0xFFF9E8ED;
            SURF_STRIP        = 0xFFFFF7F8;
            SURF_SOFT         = 0xFFFFF5F7;
            SURF_CHIP_LINE    = 0xFFE8CDD5;
            SURF_SUCCESS_LINE = 0xFFB6DCCF;
            SURF_GOLD_LINE    = 0xFFE8D3CE;
            SURF_STAR_OFF     = 0xFFE4D1D4;
            STAR_ON           = 0xFF8E6C3A;
            SURF_INACTIVE     = 0xFF9B7D85;
            SURF_DIVIDER      = 0x1A40232E;
            TEXT_BODY         = 0xFF614952;
            TEXT_BODY_2       = 0xFF6A4C57;
            TEXT_ON_DARK       = 0xFFFFF4F5;
            TEXT_ON_DARK_MUTED = 0xFFE2CBD1;
            TEXT_ON_DARK_FAINT = 0xFFD2B6BF;
            COL_METAL          = 0xFFC79E7C;
            ACCENT_ON_DARK     = 0xFFF1D2B6;
            SUCCESS_ON_DARK    = 0xFFB8E5CD;
        } else if (darkMode) {
            COL_OBSIDIAN      = 0xFF0B0B0E;
            COL_OBSIDIAN_SOFT = 0xFF141319;
            COL_GOLD          = 0xFFC8A46B;
            COL_GOLD_LIGHT    = 0xFFEBD6B0;
            COL_GOLD_DEEP     = 0xFFD9B478;
            COL_IVORY_BG      = 0xFF0B0B0E;
            COL_SILK_CREAM    = 0xFF1F1D25;
            COL_WHITE         = 0xFF14131A;
            COL_TEXT_MAIN     = 0xFFF6EFE3;
            COL_TEXT_MUTED    = 0xFFBEB6AA;
            COL_TEXT_FAINT    = 0xFF978E7E;
            COL_BORDER        = 0xFF322C36;
            COL_SUCCESS       = 0xFF63C49B;
            COL_SUCCESS_SOFT  = 0xFF15302A;
            COL_DANGER        = 0xFFE58B80;
            COL_WARN          = 0xFFE0B36A;
            SURF_CHIP         = 0xFF241F27;
            SURF_STRIP        = 0xFF17161D;
            SURF_SOFT         = 0xFF1A1821;
            SURF_CHIP_LINE    = 0xFF4A3F2C;
            SURF_SUCCESS_LINE = 0xFF2C5A49;
            SURF_GOLD_LINE    = 0xFF4A3C28;
            SURF_STAR_OFF     = 0xFF3A343F;
            STAR_ON           = 0xFFD9B478;
            SURF_INACTIVE     = 0xFF6F6773;
            SURF_DIVIDER      = 0x33C8A46B;
            TEXT_BODY         = 0xFFD6CCBC;
            TEXT_BODY_2       = 0xFFC9BFAF;
            TEXT_ON_DARK       = 0xFFF6EFE3;
            TEXT_ON_DARK_MUTED = 0xFFBEB6AA;
            TEXT_ON_DARK_FAINT = 0xFFA79E90;
            COL_METAL          = 0xFFD9B478;
            ACCENT_ON_DARK     = 0xFFD9B478;
            SUCCESS_ON_DARK    = 0xFF63C49B;
        } else {
            COL_OBSIDIAN      = 0xFF0B0B0E;
            COL_OBSIDIAN_SOFT = 0xFF141319;
            COL_GOLD          = 0xFFC8A46B;
            COL_GOLD_LIGHT    = 0xFFEBD6B0;
            COL_GOLD_DEEP     = 0xFF8E6C3A;
            COL_IVORY_BG      = 0xFFFAF8F3;
            COL_SILK_CREAM    = 0xFFF4EEE3;
            COL_WHITE         = 0xFFFFFFFF;
            COL_TEXT_MAIN     = 0xFF1B1A18;
            COL_TEXT_MUTED    = 0xFF5C554A;
            COL_TEXT_FAINT    = 0xFF786F61;
            COL_BORDER        = 0xFFE6DDCC;
            COL_SUCCESS       = 0xFF1E6B4B;
            COL_SUCCESS_SOFT  = 0xFFEAF4EE;
            COL_DANGER        = 0xFF9E3A33;
            COL_WARN          = 0xFF9A6B2F;
            SURF_CHIP         = 0xFFFBF4E7;
            SURF_STRIP        = 0xFFFFFDF9;
            SURF_SOFT         = 0xFFFFFBF2;
            SURF_CHIP_LINE    = 0xFFEAD9B8;
            SURF_SUCCESS_LINE = 0xFFBFDCCB;
            SURF_GOLD_LINE    = 0xFFE8D6B4;
            SURF_STAR_OFF     = 0xFFDFD7C8;
            STAR_ON           = 0xFF8E6C3A;
            SURF_INACTIVE     = 0xFF9C9284;
            SURF_DIVIDER      = 0x1A000000;
            TEXT_BODY         = 0xFF4E4840;
            TEXT_BODY_2       = 0xFF5A5244;
            TEXT_ON_DARK       = 0xFFF6EFE3;
            TEXT_ON_DARK_MUTED = 0xFFBEB6AA;
            TEXT_ON_DARK_FAINT = 0xFFA79E90;
            COL_METAL          = 0xFFC8A46B;
            ACCENT_ON_DARK     = 0xFFC8A46B;
            SUCCESS_ON_DARK    = 0xFF9BD8B4;
        }
        // every medallion in the house follows the active metal family
        boolean emerald = themeMode == THEME_EMERALD;
        boolean sapphire = themeMode == THEME_SAPPHIRE;
        boolean rose = themeMode == THEME_ROSE;
        boolean pink = themeMode == THEME_PINK;
        Luxury3DIconView.palette = pink ? Luxury3DIconView.PALETTE_PINK : rose ? Luxury3DIconView.PALETTE_ROSE : sapphire ? Luxury3DIconView.PALETTE_SAPPHIRE
                : emerald ? Luxury3DIconView.PALETTE_EMERALD : Luxury3DIconView.PALETTE_GOLD;
        LuxuryCanvas.metal = pink ? LuxuryCanvas.METAL_PINK : rose ? LuxuryCanvas.METAL_ROSE : sapphire ? LuxuryCanvas.METAL_SAPPHIRE
                : emerald ? LuxuryCanvas.METAL_EMERALD : LuxuryCanvas.METAL_GOLD;
        SalesChartView.metal = pink ? SalesChartView.METAL_PINK : rose ? SalesChartView.METAL_ROSE : sapphire ? SalesChartView.METAL_SAPPHIRE
                : emerald ? SalesChartView.METAL_EMERALD : SalesChartView.METAL_GOLD;

        Window window = getWindow();
        if (window != null) {
            window.setStatusBarColor(COL_OBSIDIAN);
            window.setNavigationBarColor(COL_OBSIDIAN);
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(COL_IVORY_BG));
        }
    }

    /** Turns the 09:00 briefing on or off, asking for the notification grant once. */
    private void toggleDailyBrief() {
        briefReminderOn = !briefReminderOn;
        getSharedPreferences(PREF_THEME, MODE_PRIVATE).edit()
                .putBoolean("brief_reminder", briefReminderOn).apply();
        if (briefReminderOn) {
            if (android.os.Build.VERSION.SDK_INT >= 33
                    && checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, REQ_NOTIF);
            }
            try { DailyBriefReceiver.schedule(this); } catch (Throwable ignored) { }
            toast("یادآور روزانه ۹ صبح فعال شد.");
        } else {
            try { DailyBriefReceiver.cancel(this); } catch (Throwable ignored) { }
            toast("یادآور روزانه خاموش شد.");
        }
        renderCurrentScreen();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;
            toast(granted ? "یادآور صبحگاهی فعال است." : "برای یادآور صبحگاهی، اجازهٔ اعلان لازم است.");
        }
    }

    /** A direct theme selector for customers as well as administrators. */
    private void showThemePicker() {
        LinearLayout box = dialogBox();
        box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        box.addView(dialogTitle("انتخاب تم فروشگاه", "رنگ دلخواه خود را انتخاب کنید؛ انتخاب شما ذخیره می‌شود."));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        scroll.addView(themeTiles(dialog));
        int pickerHeight = Math.min(dp(350), (int) (getResources().getDisplayMetrics().heightPixels * 0.45f));
        box.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pickerHeight));
        box.addView(space(8));
        box.addView(ghostButton("بستن", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    private void applyThemeMode(int mode) {
        themeMode = mode;
        persistTheme();
    }

    private void persistTheme() {
        darkMode = themeMode == THEME_NIGHT;
        getSharedPreferences(PREF_THEME, MODE_PRIVATE).edit()
                .putInt("theme_mode", themeMode)
                .putBoolean("dark_mode", darkMode)
                .putBoolean("theme_choice_by_user", true)
                .apply();
        applyPalette();
        restyleInputs();
        setContentView(buildRootShell());
        renderCurrentScreen();
        toast("حالت " + themeLabel(themeMode) + " فعال شد.");
    }

    private String themeLabel(int mode) {
        if (mode == THEME_EMERALD) return "زمرد سلطنتی (روشن)";
        if (mode == THEME_SAPPHIRE) return "یاقوت کبود";
        if (mode == THEME_ROSE) return "رز آتلیه";
        if (mode == THEME_PINK) return "صورتی پینک مزون";
        if (mode == THEME_NIGHT) return "شب ابسیدین";
        return "روز عاجی";
    }

    /** The few inputs that survive a re-render keep their own colours, so they are repainted. */
    private void restyleInputs() {
        EditText[] inputs = {bagNameInput, bagMobileInput, bagAddressInput, bagGiftInput,
                bagCouponInput, authSerialInput, trackingInput};
        for (EditText input : inputs) {
            if (input == null) continue;
            input.setTextColor(COL_TEXT_MAIN);
            input.setTextColor(COL_TEXT_MAIN);
            input.setHintTextColor(COL_TEXT_FAINT);
            input.setBackground(roundedShape(COL_IVORY_BG, dp(13), COL_BORDER, dp(1)));
        }
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
        hairline.setBackgroundColor(lineGold(0x33));
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
        header.setElevation(dp(8));

        // Brand emblem
        FrameLayout emblemWrap = new FrameLayout(this);
        emblemWrap.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(14), lineGold(0x66), dp(1)));
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
        brand.setTypeface(Typeface.SERIF, Typeface.BOLD);   // Latin wordmark: serif, never Vazirmatn
        tracking(brand, 0.16f);
        brand.setSingleLine(true);
        brand.setEllipsize(TextUtils.TruncateAt.END);
        brand.setIncludeFontPadding(false);

        headerSubtitle = new TextView(this);
        headerSubtitle.setText("مدیریت مجموعه: سرکار خانم زنگنه");
        headerSubtitle.setTypeface(houseTypeface(false));
        headerSubtitle.setTextColor(TEXT_ON_DARK_MUTED);
        headerSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.8f);
        headerSubtitle.setIncludeFontPadding(false);
        headerSubtitle.setSingleLine(true);
        headerSubtitle.setEllipsize(TextUtils.TruncateAt.END);
        headerSubtitle.setPadding(0, dp(4), 0, dp(2));

        titleCol.addView(brand);
        titleCol.addView(headerSubtitle);
        header.addView(titleCol);

        // Quiet actions: night mode + search + admin gate
        FrameLayout themeButton = headerActionButton(darkMode ? Luxury3DIconView.ICON_SUN : Luxury3DIconView.ICON_MOON,
                new Runnable() {
            @Override public void run() { showThemePicker(); }
        });
        themeButton.setContentDescription("انتخاب تم، حالت فعلی: " + themeLabel(themeMode));
        header.addView(themeButton, actionLp(8));

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
        wrap.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(13), lineGold(0x33), dp(1)));
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
                    case TAB_ACCOUNT:   renderAccount();   break;
                    default:            renderBoutique();  break;
                }
                renderCustomerNav();
            }
        } catch (Throwable t) {
            // Defensive shell: one broken component must never blank the whole store.
            android.util.Log.e("ASIYE", "screen render failed (admin=" + adminMode + ", tab=" + adminTab + ")", t);
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
        bottomNavContainer.addView(navTab(Luxury3DIconView.ICON_CROWN, "ارتباط",
                customerTab == TAB_CONCIERGE, 0, new Runnable() {
                    @Override public void run() { switchCustomerTab(TAB_CONCIERGE); }
                }));
        bottomNavContainer.addView(navTab(Luxury3DIconView.ICON_USERS, "حساب من",
                customerTab == TAB_ACCOUNT, 0, new Runnable() {
                    @Override public void run() { switchCustomerTab(TAB_ACCOUNT); }
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
                ? roundedShape(lineGold(0x1F), dp(16), lineGold(0x3D), dp(1))
                : roundedShape(0x00000000, dp(16), 0x00000000, 0));
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
            badgeView.setTypeface(houseTypeface(true));
            badgeView.setGravity(Gravity.CENTER);
            badgeView.setBackground(roundedShape(COL_GOLD_LIGHT, dp(20), COL_OBSIDIAN, dp(1)));
            FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(dp(16), dp(16));
            blp.gravity = Gravity.TOP | Gravity.END;
            blp.setMargins(0, 0, dp(1), 0);
            iconWrap.addView(badgeView, blp);
        }

        TextView caption = new TextView(this);
        caption.setText(label);
        caption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.8f);
        caption.setSingleLine(true);
        caption.setEllipsize(TextUtils.TruncateAt.END);
        caption.setGravity(Gravity.CENTER);
        caption.setTypeface(houseTypeface(selected));
        caption.setTextColor(selected ? COL_GOLD_LIGHT : TEXT_ON_DARK_MUTED);
        caption.setPadding(0, dp(4), 0, 0);
        if (selected) caption.setTypeface(houseTypeface(true));

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
        contentContainer.addView(crestTitle(Luxury3DIconView.ICON_BOUTIQUE, "ویترین اختصاصی",
                "کلکسیون منتخب پوست، مو و زیبایی — اورجینال، با ضمانت اصالت", "LA COLLECTION"));
        contentContainer.addView(editorialDivider());

        contentContainer.addView(buildHeroCard());
        contentContainer.addView(space(10));
        contentContainer.addView(trustStrip());
        contentContainer.addView(space(20));

        // Section head + wishlist shortcut
        contentContainer.addView(crestTitle(Luxury3DIconView.ICON_DIAMOND, "کلکسیون امضایی",
                "شش کالای منتخب از پیج رسمی مجموعه"));
        contentContainer.addView(space(10));

        contentContainer.addView(categoryChips());
        contentContainer.addView(space(7));
        contentContainer.addView(skinChips());
        contentContainer.addView(space(12));

        if (!compareIds.isEmpty()) {
            contentContainer.addView(compareTray());
            contentContainer.addView(space(14));
        }

        List<Models.Product> products = new ArrayList<>();
        for (Models.Product candidate : db.getProducts(true, selectedCategory, "", "newest")) {
            if (matchesSkin(candidate)) products.add(candidate);
        }
        if (products.isEmpty()) {
            contentContainer.addView(emptyCard(Luxury3DIconView.ICON_HEART, "هنوز کالایی در منتخب‌های شما نیست",
                    "قلب بالای هر کالا را لمس کنید تا در ویترین اختصاصی شما ذخیره شود."));
        } else {
            addProductGrid(products);
        }

        contentContainer.addView(space(2));
        contentContainer.addView(foilDivider(false));
        contentContainer.addView(bundleCard());
        contentContainer.addView(space(18));

        contentContainer.addView(foilDivider(false));
        contentContainer.addView(brandStoryCard());
        contentContainer.addView(space(16));

        View recent = recentlyViewedStrip();
        if (recent != null) {
            contentContainer.addView(recent);
            contentContainer.addView(space(16));
        }

        contentContainer.addView(space(4));
        contentContainer.addView(footerNote());
    }

    private View buildHeroCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_OBSIDIAN, dp(24), lineGold(0x40), dp(1)));
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
                new int[]{scrim(0xE6), scrim(0x33), 0x00000000}));
        heroWrap.addView(scrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView heroTag = new TextView(this);
        heroTag.setText("SIGNATURE COLLECTION");
        heroTag.setTextColor(COL_GOLD_LIGHT);
        heroTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f);
        tracking(heroTag, 0.22f);
        heroTag.setTypeface(Typeface.SERIF, Typeface.BOLD);
        heroTag.setPadding(dp(9), dp(4), dp(9), dp(4));
        heroTag.setBackground(roundedShape(scrim(0xB3), dp(9), lineGold(0x4D), dp(1)));
        FrameLayout.LayoutParams tagLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tagLp.gravity = Gravity.TOP | Gravity.START;
        tagLp.setMargins(dp(14), dp(14), 0, 0);
        heroWrap.addView(heroTag, tagLp);

        // v6.3 — a 24K wax-seal crest floating over the editorial photograph.
        FrameLayout seal = iconMedallion(Luxury3DIconView.ICON_DIAMOND, 44, Luxury3DIconView.PEDESTAL_GOLD);
        ((Luxury3DIconView) seal.getChildAt(0)).enableSweep(true);
        FrameLayout.LayoutParams sealLp = new FrameLayout.LayoutParams(dp(44), dp(44));
        sealLp.gravity = Gravity.TOP | Gravity.END;
        sealLp.setMargins(0, dp(12), dp(12), 0);
        heroWrap.addView(seal, sealLp);

        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(168));
        heroLp.setMargins(dp(10), dp(10), dp(10), 0);
        card.addView(heroWrap, heroLp);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(18), dp(16), dp(18), dp(18));

        TextView eyebrow = text("اصالت تضمین‌شده • کلکسیون تخصصی", 10.5f, ACCENT_ON_DARK, false);
        tracking(eyebrow, 0.06f);

        TextView title = text("زیبایی اصیل، با انتخاب‌های علمی و ماندگار", 17.5f, TEXT_ON_DARK, true);
        title.setLineSpacing(dp(4), 1f);
        title.setPadding(0, dp(6), 0, dp(6));

        TextView bodyText = text("گلچینی از بهترین محصولات اورجینال گاتیو، لاکچری کوین، اوردینری و هیرفرند؛ همراه با مشاوره تخصصی مستقیم از مدیریت مجموعه.",
                11.5f, TEXT_ON_DARK_MUTED, false);
        bodyText.setLineSpacing(dp(4), 1f);

        body.addView(eyebrow);
        body.addView(title);
        body.addView(bodyText);

        Models.RatingSummary storeRating = db.getStoreRating();
        if (storeRating.count > 0) {
            body.addView(space(12));
            LinearLayout ratingLine = row(Gravity.CENTER_VERTICAL);
            ratingLine.addView(starRow(storeRating.average, 10.5f));
            TextView ratingCaption = text(ratingText(storeRating.average, storeRating.count)
                    + "  ·  امتیاز خریداران اسیه", 9.8f, TEXT_ON_DARK_MUTED, false);
            ratingCaption.setPadding(dp(7), 0, 0, 0);
            ratingLine.addView(ratingCaption);
            body.addView(ratingLine);
        }
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

    /**
     * v6.3 — the old one-line text strip is now a calm row of three small 3D crests.
     * Same information, dramatically more luxurious, still a single band.
     */
    private View trustStrip() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        panel.setGravity(Gravity.CENTER_VERTICAL);
        panel.setPadding(dp(6), dp(10), dp(6), dp(10));
        panel.setBackground(roundedShape(SURF_STRIP, dp(16), SURF_CHIP_LINE, dp(1)));

        int[] icons = {Luxury3DIconView.ICON_SHIELD, Luxury3DIconView.ICON_TRUCK, Luxury3DIconView.ICON_INSTALLMENT};
        String[] labels = {"ضمانت اصالت", "ارسال سریع VIP", "پرداخت ۴ قسط"};
        for (int i = 0; i < icons.length; i++) {
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(dp(2), 0, dp(2), 0);
            cell.addView(iconMedallion(icons[i], 34, Luxury3DIconView.PEDESTAL_OBSIDIAN));
            TextView label = text(labels[i], 8.8f, TEXT_BODY_2, true);
            label.setGravity(Gravity.CENTER);
            label.setSingleLine(true);
            label.setPadding(0, dp(4), 0, 0);
            cell.addView(label);
            panel.addView(cell, weight(1f));

            if (i < icons.length - 1) {
                View hair = new View(this);
                hair.setBackgroundColor(lineGold(0x24));
                panel.addView(hair, new LinearLayout.LayoutParams(dp(1), dp(30)));
            }
        }
        return panel;
    }

    private View categoryChips() {
        java.util.List<String[]> chips = new java.util.ArrayList<>();
        chips.add(new String[]{"همه", "همه"});
        chips.add(new String[]{"پوست", "مراقبت تخصصی پوست"});
        chips.add(new String[]{"میکاپ", "میکاپ لوکس"});
        chips.add(new String[]{"مو", "تقویت و درمان مو"});
        chips.add(new String[]{"منتخب‌ها", "منتخب‌های من"});
        java.util.Set<String> known = new java.util.LinkedHashSet<>();
        for (String[] c : chips) known.add(c[1]);
        for (Models.Product product : db.getProducts(true, "همه", "", "newest")) {
            String category = product.category == null ? "" : product.category.trim();
            if (!category.isEmpty() && known.add(category)) chips.add(new String[]{category, category});
        }
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        for (int i = 0; i < chips.size(); i++) {
            final String category = chips.get(i)[1];
            boolean active = selectedCategory.equals(category);
            TextView chip = new TextView(this);
            String chipLabel = chips.get(i)[0];
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
            chip.setTypeface(houseTypeface(active));
            chip.setPadding(dp(14), dp(9), dp(14), dp(9));
            chip.setBackground(active
                    ? deepCard(dp(18))
                    : roundedShape(COL_WHITE, dp(18), COL_BORDER, dp(1)));
            if (active) chip.setElevation(dp(1.6f));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    selectedCategory = category;
                    renderCurrentScreen();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.setMarginStart(dp(6));
            panel.addView(chip, lp);
        }
        android.widget.HorizontalScrollView scroll = new android.widget.HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(panel);
        return scroll;
    }

    /** True on tablets and wide foldables — the boutique then reads as a catalogue spread. */
    private boolean isWideCanvas() {
        return getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    /**
     * v6.7 · the same cards, arranged for the surface they land on:
     * a single quiet column on phones, a paired spread on tablets.
     */
    private void addProductGrid(List<Models.Product> products) {
        if (!isWideCanvas()) {
            int idx = 0;
            for (Models.Product p : products) {
                View card = buildProductCard(p);
                reveal(card, idx++);
                contentContainer.addView(card);
                contentContainer.addView(space(16));
            }
            return;
        }

        int idx = 0;
        for (int i = 0; i < products.size(); i += 2) {
            LinearLayout pair = row(Gravity.TOP);
            View left = buildProductCard(products.get(i));
            reveal(left, idx++);
            pair.addView(left, weight(1f));

            if (i + 1 < products.size()) {
                pair.addView(spaceH(14));
                View right = buildProductCard(products.get(i + 1));
                reveal(right, idx++);
                pair.addView(right, weight(1f));
            } else {
                // keep the last card from stretching across the full spread
                pair.addView(spaceH(14));
                View spacer = new View(this);
                pair.addView(spacer, weight(1f));
            }
            contentContainer.addView(pair);
            contentContainer.addView(space(16));
        }
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

        // v6.7 · a golden shimmer holds the frame for a beat, then dissolves
        final LuxuryCanvas.ShimmerSkeleton skeleton =
                new LuxuryCanvas.ShimmerSkeleton(this, false);
        imageWrap.addView(skeleton, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        skeleton.startShimmer();
        skeleton.postDelayed(new Runnable() {
            @Override public void run() { skeleton.release(); }
        }, 260L + (long) (Math.random() * 240));

        // subtle bottom scrim
        View scrim = new View(this);
        scrim.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{scrim(0x33), 0x00000000}));
        imageWrap.addView(scrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(60), Gravity.BOTTOM));

        // discount badge (top-start) — no overlap with anything
        TextView badge = new TextView(this);
        badge.setText(PersianFormatter.toPersianDigits(String.valueOf(Math.max(0, p.discountPercent))) + "٪ تخفیف");
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        badge.setTypeface(houseTypeface(true));
        badge.setTextColor(COL_GOLD_LIGHT);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(5), dp(10), dp(5));
        badge.setBackground(roundedShape(scrim(0xE6), dp(14), lineGold(0x55), dp(1)));
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
        heart.setTextColor(wish[0] ? COL_GOLD_DEEP : TEXT_ON_DARK);
        heart.setBackground(roundedShape(scrim(0xCC), dp(22), lineGold(0x66), dp(1)));
        FrameLayout.LayoutParams heartLp = new FrameLayout.LayoutParams(dp(38), dp(38));
        heartLp.gravity = Gravity.TOP | Gravity.END;
        heartLp.setMargins(0, dp(12), dp(12), 0);
        heart.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                tap();
                wish[0] = !wish[0];
                db.toggleWishlist(p.id, wish[0]);
                heart.setText(wish[0] ? "♥" : "♡");
                heart.setTextColor(wish[0] ? COL_GOLD_DEEP : TEXT_ON_DARK);
                toast(wish[0] ? "به منتخب‌های شما اضافه شد." : "از منتخب‌های شما حذف شد.");
            }
        });
        imageWrap.addView(heart, heartLp);
        card.addView(imageWrap);

        // Body
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(14), dp(16), dp(16));

        TextView brandLine = text(p.brand != null && !p.brand.trim().isEmpty() ? p.brand : p.nameEn, 9.5f, COL_GOLD_DEEP, false);
        tracking(brandLine, 0.06f);
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
        View rating = ratingStrip(p);
        if (rating != null) {
            LinearLayout ratingWrap = row(Gravity.CENTER_VERTICAL);
            ratingWrap.setPadding(0, 0, 0, dp(6));
            ratingWrap.addView(rating);
            body.addView(ratingWrap);
        }
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

    // ══════════════════════════════════════════════════════════════════════
    //  v6.2 · VERIFIED REVIEWS, ORDER TIMELINE, VIP CLUB, GIFT CONCIERGE
    // ══════════════════════════════════════════════════════════════════════

    /** Five crisp stars; the last one can render as a half-tone for 4.5 style averages. */
    private View starRow(double average, float sizeSp) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        int full = (int) Math.round(average);
        for (int i = 0; i < 5; i++) {
            TextView star = new TextView(this);
            star.setText("★");
            star.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
            star.setTextColor(i < full ? STAR_ON : SURF_STAR_OFF);
            star.setIncludeFontPadding(false);
            if (i > 0) star.setPadding(dp(1), 0, 0, 0);
            row.addView(star);
        }
        return row;
    }

    private String ratingText(double average, int count) {
        String avg = PersianFormatter.toPersianDigits(String.format(Locale.US, "%.1f", average));
        return avg + "  ·  " + PersianFormatter.toPersianDigits(String.valueOf(count)) + " نظر";
    }

    /** Compact social proof for product cards — one quiet line, never a wall of stars. */
    private View ratingStrip(Models.Product p) {
        Models.RatingSummary r = db.getProductRating(p.id);
        if (r.count == 0) return new View(this);
        LinearLayout row = row(Gravity.CENTER_VERTICAL);
        row.addView(starRow(r.average, 10f));
        TextView t = text(ratingText(r.average, r.count), 9.6f, COL_TEXT_MUTED, false);
        t.setPadding(dp(6), 0, 0, 0);
        row.addView(t);
        return row;
    }

    /** Full review section rendered inside the product sheet. */
    private View reviewsBlock(final Models.Product p) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_IVORY_BG, dp(16), COL_BORDER, dp(1)));
        card.setPadding(dp(14), dp(13), dp(14), dp(13));

        Models.RatingSummary summary = db.getProductRating(p.id);
        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        TextView title = text("نظرات تأییدشده", 12f, COL_TEXT_MAIN, true);
        head.addView(title, weight(1f));
        TextView write = text("ثبت نظر", 9.6f, COL_GOLD_DEEP, true);
        write.setPadding(dp(11), dp(6), dp(11), dp(6));
        write.setBackground(roundedShape(COL_WHITE, dp(10), COL_BORDER, dp(1)));
        write.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showReviewDialog(p); }
        });
        head.addView(write);
        card.addView(head);

        if (summary.count == 0) {
            card.addView(space(8));
            TextView empty = text("هنوز نظری برای این کالا ثبت نشده است؛ اولین تجربه را شما بنویسید.",
                    10.2f, COL_TEXT_MUTED, false);
            empty.setLineSpacing(dp(2), 1f);
            card.addView(empty);
            return card;
        }

        card.addView(space(8));
        LinearLayout summaryRow = row(Gravity.CENTER_VERTICAL);
        summaryRow.addView(starRow(summary.average, 11f));
        TextView avg = text(ratingText(summary.average, summary.count), 9.8f, COL_TEXT_MUTED, false);
        avg.setPadding(dp(7), 0, 0, 0);
        summaryRow.addView(avg);
        card.addView(summaryRow);

        for (Models.ReviewRecord r : db.getProductReviews(p.id, 2)) {
            card.addView(space(9));
            View divider = new View(this);
            divider.setBackgroundColor(SURF_DIVIDER);
            card.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
            card.addView(space(8));

            LinearLayout metaRow = row(Gravity.CENTER_VERTICAL);
            TextView author = text(r.author, 10.4f, COL_TEXT_MAIN, true);
            metaRow.addView(author, weight(1f));
            metaRow.addView(starRow(r.rating, 9f));
            card.addView(metaRow);

            TextView body = text(r.body, 10.4f, TEXT_BODY, false);
            body.setLineSpacing(dp(3), 1f);
            body.setPadding(0, dp(5), 0, 0);
            card.addView(body);

            TextView meta = text("خرید تأییدشده  ·  " + r.createdLabel, 8.8f, COL_TEXT_MUTED, false);
            meta.setPadding(0, dp(5), 0, 0);
            card.addView(meta);
        }
        return card;
    }

    /** Write-a-review sheet: five stars, a name and a short honest note. */
    private void showReviewDialog(final Models.Product p) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(18));
        box.setBackgroundColor(COL_WHITE);

        box.addView(text("تجربه‌تان از " + p.nameFa, 14f, COL_TEXT_MAIN, true));
        box.addView(space(4));
        box.addView(text("نظر شما پس از تأیید مدیریت مجموعه با نشان «خرید تأییدشده» منتشر می‌شود.",
                9.8f, COL_TEXT_MUTED, false));
        box.addView(space(14));

        final int[] chosen = {5};
        final TextView[] stars = new TextView[5];
        LinearLayout starsRow = new LinearLayout(this);
        starsRow.setOrientation(LinearLayout.HORIZONTAL);
        starsRow.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            final TextView star = new TextView(this);
            star.setText("★");
            star.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
            star.setTextColor(COL_GOLD_DEEP);
            star.setPadding(0, 0, dp(8), 0);
            star.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    chosen[0] = idx + 1;
                    for (int j = 0; j < 5; j++) {
                        stars[j].setTextColor(j <= idx ? STAR_ON : SURF_STAR_OFF);
                    }
                }
            });
            stars[i] = star;
            starsRow.addView(star);
        }
        box.addView(starsRow);
        box.addView(space(12));

        final EditText nameInput = makeInput("نام شما (مثلاً سارا م.)", InputType.TYPE_CLASS_TEXT, false);
        box.addView(nameInput);
        box.addView(space(9));

        final EditText bodyInput = makeInput("تجربه‌تان را کوتاه و صادقانه بنویسید", InputType.TYPE_TEXT_FLAG_MULTI_LINE, false);
        bodyInput.setSingleLine(false);
        bodyInput.setMinLines(3);
        bodyInput.setGravity(Gravity.TOP | Gravity.RIGHT);
        box.addView(bodyInput);
        box.addView(space(14));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ارسال نظر برای تأیید", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() {
                String name = nameInput.getText().toString().trim();
                String body = bodyInput.getText().toString().trim();
                if (name.length() < 2) {
                    toast("نام خود را وارد کنید.");
                    return;
                }
                if (body.length() < 8) {
                    toast("متن نظر را کامل‌تر بنویسید.");
                    return;
                }
                tap();
                long id = db.addReview(p.id, name, chosen[0], body);
                if (id > 0) {
                    toast("نظر شما ثبت شد و پس از تأیید نمایش داده می‌شود.");
                    dialog.dismiss();
                    renderCurrentScreen();
                } else {
                    toast("ثبت نظر ممکن نشد؛ دوباره تلاش کنید.");
                }
            }
        }));
        box.addView(space(8));
        TextView cancel = text("بستن", 11f, COL_TEXT_MUTED, true);
        cancel.setGravity(Gravity.CENTER);
        cancel.setPadding(0, dp(10), 0, dp(10));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dialog.dismiss(); }
        });
        box.addView(cancel);
        showRoundedDialog(dialog);
    }

    /** Elegant 4-step fulfilment timeline used by order tracking. */
    private View orderTimeline(String status) {
        final String[] steps = {"ثبت سفارش", "آماده‌سازی", "ارسال", "تحویل"};
        int active;
        if ("delivered".equals(status)) active = 3;
        else if ("shipped".equals(status)) active = 2;
        else if ("preparing".equals(status) || "paid".equals(status) || "confirmed".equals(status)
                || "pending_approval".equals(status)) active = 1;
        else if ("cancelled".equals(status) || "returned".equals(status)) active = -1;
        else active = 0;

        if (active < 0) {
            LinearLayout bad = row(Gravity.CENTER_VERTICAL);
            bad.setPadding(0, dp(6), 0, 0);
            TextView t = text("این سفارش " + PersianFormatter.statusLabelFa(status)
                    + " است؛ برای هماهنگی با مدیریت مجموعه تماس بگیرید.", 10f, COL_DANGER, false);
            bad.addView(t, weight(1f));
            return bad;
        }

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(10), 0, 0);

        LinearLayout track = new LinearLayout(this);
        track.setOrientation(LinearLayout.HORIZONTAL);
        track.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        for (int i = 0; i < steps.length; i++) {
            final boolean done = i <= active;
            LinearLayout step = new LinearLayout(this);
            step.setOrientation(LinearLayout.VERTICAL);
            step.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            step.setLayoutParams(slp);

            FrameLayout dot = new FrameLayout(this);
            dot.setLayoutParams(new LinearLayout.LayoutParams(dp(22), dp(22)));
            dot.setBackground(roundedShape(done ? COL_GOLD : COL_WHITE, dp(11),
                    done ? COL_GOLD : SURF_CHIP_LINE, dp(1)));
            TextView num = text(done ? "✓" : PersianFormatter.toPersianDigits(String.valueOf(i + 1)),
                    9.5f, done ? COL_OBSIDIAN : SURF_INACTIVE, true);
            num.setGravity(Gravity.CENTER);
            dot.addView(num, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            step.addView(dot);

            TextView label = text(steps[i], 8.6f, done ? COL_TEXT_MAIN : SURF_INACTIVE, done);
            label.setGravity(Gravity.CENTER);
            label.setPadding(0, dp(5), 0, 0);
            step.addView(label);
            track.addView(step);
        }
        wrap.addView(track);

        View bar = new View(this);
        bar.setBackgroundColor(lineGold(0x33));
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(2));
        barLp.setMargins(dp(26), dp(2), dp(26), 0);
        wrap.addView(bar);
        return wrap;
    }

    /** VIP club with a real tier ladder derived from lifetime spend. */
    private View vipClubCard() {
        long spend = db.getLifetimeSpend();
        long goldThreshold = 5_000_000L;
        long platinumThreshold = 15_000_000L;
        String tier; String tierEn; long next; int progress; String perk;
        if (spend >= platinumThreshold) {
            tier = "پلاتینیوم"; tierEn = "PLATINUM";
            next = 0L; progress = 100;
            perk = "مشاورهٔ اختصاصی، ارسال رایگان همیشگی و دسترسی زودتر به کالکسیون‌های محدود";
        } else if (spend >= goldThreshold) {
            tier = "طلایی"; tierEn = "GOLD";
            next = platinumThreshold - spend;
            progress = (int) (60 + 40 * ((spend - goldThreshold) / (double) (platinumThreshold - goldThreshold)));
            perk = "ارسال رایگان VIP، بسته‌بندی هدیه رایگان و ۱٪ اعتبار بازگشتی";
        } else {
            tier = "نقره‌ای"; tierEn = "SILVER";
            next = goldThreshold - spend;
            progress = (int) (60 * (spend / (double) goldThreshold));
            perk = "۱٪ اعتبار بازگشتی و دسترسی به پیشنهادهای اعضای باشگاه";
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(deepCard(dp(22)));
        card.setPadding(dp(19), dp(17), dp(19), dp(18));
        card.setElevation(dp(2));

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        FrameLayout crown = new FrameLayout(this);
        crown.setLayoutParams(new LinearLayout.LayoutParams(dp(38), dp(38)));
        Luxury3DIconView crownIcon = new Luxury3DIconView(this, Luxury3DIconView.ICON_MEDAL, true);
        crownIcon.enableSweep(true);
        crown.addView(crownIcon,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        head.addView(crown);
        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(11), 0, dp(11), 0);
        titles.addView(text("باشگاه مشتریان اسیه", 12.5f, TEXT_ON_DARK, true));
        titles.addView(text("سطح " + tier + "  ·  " + tierEn, 9.4f, ACCENT_ON_DARK, false));
        head.addView(titles, weight(1f));
        TextView amount = text(PersianFormatter.formatToman(spend), 11.5f, COL_GOLD_LIGHT, true);
        head.addView(amount);
        card.addView(head);

        card.addView(space(12));
        LinearLayout track = new LinearLayout(this);
        track.setOrientation(LinearLayout.HORIZONTAL);
        track.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        track.setBackground(pillDrawable(lineGold(0x33)));
        View fill = new View(this);
        fill.setBackground(pillDrawable(COL_METAL));
        track.addView(fill, new LinearLayout.LayoutParams(0, dp(6), Math.max(0.04f, Math.min(1f, progress / 100f))));
        View rest = new View(this);
        track.addView(rest, new LinearLayout.LayoutParams(0, dp(6), Math.max(0.0001f, 1f - Math.max(0.04f, Math.min(1f, progress / 100f)))));
        card.addView(track, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(6)));

        card.addView(space(9));
        TextView line = text(next > 0
                ? "تا سطح بعدی " + PersianFormatter.formatToman(next) + " خرید باقی مانده است"
                : "به بالاترین سطح باشگاه رسیده‌اید — سپاس از اعتماد شما",
                9.6f, TEXT_ON_DARK_FAINT, false);
        line.setLineSpacing(dp(2), 1f);
        card.addView(line);
        card.addView(space(8));
        TextView perks = text("مزایای فعلی: " + perk, 9.4f, TEXT_ON_DARK_FAINT, false);
        perks.setLineSpacing(dp(2), 1f);
        card.addView(perks);
        return card;
    }

    private GradientDrawable pillDrawable(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadius(dp(4));
        d.setColor(color);
        return d;
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
        price.setTypeface(houseTypeface(true));
        price.setTextColor(COL_TEXT_MAIN);
        price.setSingleLine(true);
        price.setIncludeFontPadding(false);
        price.setTextDirection(View.TEXT_DIRECTION_RTL);
        top.addView(price);

        if (p.discountPercent > 0) {
            TextView chip = new TextView(this);
            chip.setText(PersianFormatter.toPersianDigits(String.valueOf(p.discountPercent)) + "٪");
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
            chip.setTypeface(houseTypeface(true));
            chip.setTextColor(COL_GOLD_DEEP);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(7), dp(2), dp(7), dp(2));
            chip.setBackground(roundedShape(SURF_CHIP, dp(9), SURF_CHIP_LINE, dp(1)));
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.setMarginStart(dp(8));
            top.addView(chip, clp);
        }
        block.addView(top);

        if (p.originalPrice > p.salePrice) {
            LinearLayout oldRow = row(Gravity.CENTER_VERTICAL);
            oldRow.setPadding(0, dp(5), 0, 0);
            TextView oldLabel = text("قبلاً", 9.2f, COL_TEXT_FAINT, false);
            TextView old = text(PersianFormatter.formatToman(p.originalPrice), 9.8f, COL_TEXT_MUTED, false);
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
        strip.setBackground(roundedShape(SURF_CHIP, dp(11), SURF_CHIP_LINE, dp(1)));
        strip.setPadding(dp(9), dp(7), dp(9), dp(7));

        FrameLayout icon = new FrameLayout(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        icon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_INSTALLMENT,
                        Luxury3DIconView.PEDESTAL_IVORY),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        strip.addView(icon);

        TextView txt = text(PersianFormatter.toPersianDigits(String.valueOf(zero.months)) + " قسط بدون کارمزد  •  ماهانه "
                        + PersianFormatter.formatToman(zero.monthlyPayment),
                9.2f, COL_GOLD_DEEP, false);
        txt.setPadding(dp(6), 0, 0, 0);
        txt.setSingleLine(true);
        strip.addView(txt, weight(1f));
        return strip;
    }

    private void showEditorialProductDialog(final Models.Product p) {
        db.markProductViewed(p.id);
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

        body.addView(text(p.brand != null && !p.brand.trim().isEmpty() ? p.brand : p.nameEn, 10f, COL_GOLD_DEEP, false));
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
            inst.setBackground(roundedShape(SURF_CHIP, dp(13), SURF_CHIP_LINE, dp(1)));
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
        body.addView(space(12));
        if (p.ingredients != null && !p.ingredients.trim().isEmpty()) {
            TextView gloss = text("واژه‌نامهٔ ترکیبات: " + p.ingredients, 9.4f, COL_GOLD_DEEP, false);
            gloss.setLineSpacing(dp(2), 1f);
            gloss.setPadding(dp(11), dp(9), dp(11), dp(9));
            gloss.setBackground(roundedShape(SURF_CHIP, dp(12), SURF_CHIP_LINE, dp(1)));
            gloss.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { showIngredientGlossary(p.ingredients); }
            });
            body.addView(gloss);
            body.addView(space(10));
        }
        final boolean inCompare = compareIds.contains(p.id);
        body.addView(ghostGoldButton(inCompare ? "حذف از مقایسه" : "افزودن به مقایسهٔ کالاها",
                new Runnable() {
            @Override public void run() { toggleCompare(p); }
        }));
        body.addView(space(14));
        body.addView(reviewsBlock(p));

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
        wrap.setBackground(roundedShape(COL_IVORY_BG, dp(14), COL_BORDER, dp(1)));
        wrap.setPadding(dp(13), dp(11), dp(13), dp(11));
        TextView l = text(label, 9.5f, COL_GOLD_DEEP, false);
        tracking(l, 0.05f);
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

    private void shareProduct(final Models.Product p) {
        final String[] labels = {"کارت کالا", "اینستاگرام", "یوتیوب", "تلگرام", "واتس‌اپ"};
        final String[] messages = {"", p.instagramCaption, p.youtubeCaption, p.telegramCaption, p.whatsappCaption};
        new AlertDialog.Builder(this).setTitle("متن اشتراک‌گذاری")
                .setItems(labels, new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int which) {
                        if (which == 0) { shareProductCard(p); return; }
                        String value = messages[which];
                        if (value == null || value.trim().isEmpty()) { toast("برای این شبکه متنی ذخیره نشده است."); return; }
                        Intent intent = new Intent(Intent.ACTION_SEND); intent.setType("text/plain"); intent.putExtra(Intent.EXTRA_TEXT, value);
                        try { startActivity(Intent.createChooser(intent, "اشتراک‌گذاری متن قابل بازبینی")); }
                        catch (Exception e) { toast("اشتراک‌گذاری در این دستگاه ممکن نیست."); }
                    }
                }).setNegativeButton("بستن", null).show();
    }

    private void shareProductCard(Models.Product p) {
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

        TextView line = text("ASIYE STORE  ·  طراحی: میلاد یعقوبی  ·  " + APP_VERSION, 8.8f, COL_TEXT_FAINT, false);
        line.setGravity(Gravity.CENTER);
        tracking(line, 0.06f);
        foot.addView(line);
        return foot;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  2 · ADVISOR & AUTHENTICITY (مشاور و اصالت)
    // ══════════════════════════════════════════════════════════════════════
    private void renderAdvisor() {
        contentContainer.addView(crestTitle(Luxury3DIconView.ICON_AI, "مشاور هوشمند و اصالت کالا",
                "پیشنهاد دقیق متناسب با نیاز پوست و موی شما، با ضمانت اصالت", "BEAUTY ADVISOR"));

        // ── Advisor (single obsidian card, no nested dark boxes) ───────────
        LinearLayout advisorCard = new LinearLayout(this);
        advisorCard.setOrientation(LinearLayout.VERTICAL);
        advisorCard.setBackground(roundedShape(COL_OBSIDIAN, dp(24), lineGold(0x40), dp(1)));
        advisorCard.setPadding(dp(18), dp(18), dp(18), dp(18));

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        FrameLayout medallion = new FrameLayout(this);
        medallion.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));
        medallion.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(16), lineGold(0x33), dp(1)));
        medallion.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_ADVISOR, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout headText = new LinearLayout(this);
        headText.setOrientation(LinearLayout.VERTICAL);
        headText.setPadding(dp(12), 0, dp(12), 0);
        headText.addView(text("مشاور انتخاب تخصصی محصول", 14.5f, TEXT_ON_DARK, true));
        headText.addView(text("چهار نیاز اصلی را انتخاب کنید", 10.5f, TEXT_ON_DARK_FAINT, false));
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
            chip.setTypeface(houseTypeface(active));
            chip.setBackground(active
                    ? roundedShape(COL_GOLD_LIGHT, dp(14), COL_GOLD_LIGHT, dp(1))
                    : roundedShape(lineGold(0x1A), dp(14), lineGold(0x55), dp(1)));
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
        shield.setBackground(roundedShape(COL_OBSIDIAN, dp(14), lineGold(0x40), dp(1)));
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
            tick.setTypeface(houseTypeface(true));
            tick.setGravity(Gravity.CENTER);
            tick.setTextColor(COL_OBSIDIAN);
            tick.setBackground(roundedShape(0xFF7FD3A6, dp(20), 0, 0));
            resHead.addView(tick, new LinearLayout.LayoutParams(dp(22), dp(22)));
            TextView okTitle = text("اصالت این کالا در سامانه مجموعه تأیید شد", 12f, 0xFF8FE0B6, true);
            okTitle.setPadding(dp(8), 0, 0, 0);
            resHead.addView(okTitle, weight(1f));
            result.addView(resHead);

            result.addView(space(10));
            result.addView(darkResultLine("محصول", verifiedProduct.nameFa));
            result.addView(darkResultLine("شناسه سریال", ltr(verifiedProduct.ttacSerial)));
            result.addView(darkResultLine("شماره پارت", ltr(verifiedProduct.batchCode)));
            result.addView(darkResultLine("حجم / مدل", verifiedProduct.volume));
            result.addView(darkResultLine("تاریخ انقضا", ltr(PersianFormatter.toPersianDigits(verifiedProduct.expiryLabel))));
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
        contentContainer.addView(space(14));
        contentContainer.addView(giftFinderCard());
        contentContainer.addView(space(14));
        View cabinet = cabinetCard();
        if (cabinet != null) {
            contentContainer.addView(cabinet);
            contentContainer.addView(space(14));
        }
        contentContainer.addView(space(2));
    }

    /**
     * Gift concierge: two taps (occasion + budget) and one curated recommendation.
     * Deliberately kept to a single card so the advisor page stays calm.
     */
    private View giftFinderCard() {
        LinearLayout card = whiteCard();
        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        FrameLayout icon = new FrameLayout(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(38), dp(38)));
        icon.setBackground(roundedShape(COL_SILK_CREAM, dp(13), COL_BORDER, dp(1)));
        icon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_GIFT, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        head.addView(icon);
        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(11), 0, dp(11), 0);
        titles.addView(text("هدیه‌یاب هوشمند اسیه", 13f, COL_TEXT_MAIN, true));
        titles.addView(text("مناسبت و بودجه را بگویید؛ هدیه را ما انتخاب می‌کنیم", 9.6f, COL_TEXT_MUTED, false));
        head.addView(titles, weight(1f));
        card.addView(head);
        card.addView(space(12));

        final String[] occasions = {"تولد", "سالگرد", "قدردانی"};
        LinearLayout occRow = row(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < occasions.length; i++) {
            final String occ = occasions[i];
            boolean active = occ.equals(giftOccasion);
            TextView chip = text(occ, 10.4f, active ? COL_OBSIDIAN : COL_TEXT_MUTED, active);
            chip.setGravity(Gravity.CENTER);
            chip.setSingleLine(true);
            chip.setPadding(dp(4), dp(9), dp(4), dp(9));
            chip.setBackground(active
                    ? roundedShape(COL_GOLD_LIGHT, dp(13), COL_GOLD_LIGHT, dp(1))
                    : roundedShape(COL_IVORY_BG, dp(13), COL_BORDER, dp(1)));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    giftOccasion = occ;
                    giftSuggestionId = -1L;
                    renderCurrentScreen();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            occRow.addView(chip, lp);
        }
        card.addView(occRow);
        card.addView(space(7));

        final String[] budgets = {"تا ۱ میلیون", "۱ تا ۳ میلیون", "بالای ۳ میلیون"};
        LinearLayout budRow = row(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < budgets.length; i++) {
            final int idx = i;
            boolean active = giftBudget == i;
            TextView chip = text(budgets[i], 9.8f, active ? COL_OBSIDIAN : COL_TEXT_MUTED, active);
            chip.setGravity(Gravity.CENTER);
            chip.setSingleLine(true);
            chip.setPadding(dp(2), dp(9), dp(2), dp(9));
            chip.setBackground(active
                    ? roundedShape(COL_GOLD_LIGHT, dp(13), COL_GOLD_LIGHT, dp(1))
                    : roundedShape(COL_IVORY_BG, dp(13), COL_BORDER, dp(1)));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    giftBudget = idx;
                    giftSuggestionId = -1L;
                    renderCurrentScreen();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            budRow.addView(chip, lp);
        }
        card.addView(budRow);
        card.addView(space(11));

        card.addView(primaryButton("پیشنهاد هدیهٔ اختصاصی", Luxury3DIconView.ICON_GIFT, new Runnable() {
            @Override public void run() {
                if (giftOccasion.isEmpty()) {
                    toast("مناسبت را انتخاب کنید.");
                    return;
                }
                if (giftBudget < 0) {
                    toast("بازهٔ بودجه را انتخاب کنید.");
                    return;
                }
                Models.Product best = suggestGift(giftOccasion, giftBudget);
                if (best == null) {
                    toast("در این بازهٔ بودجه هدیه‌ای موجود نیست.");
                    return;
                }
                giftSuggestionId = best.id;
                renderCurrentScreen();
            }
        }));

        if (giftSuggestionId > 0) {
            Models.Product p = db.getProductById(giftSuggestionId);
            if (p != null) {
                card.addView(space(12));
                LinearLayout result = new LinearLayout(this);
                result.setOrientation(LinearLayout.HORIZONTAL);
                result.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                result.setGravity(Gravity.CENTER_VERTICAL);
                result.setBackground(roundedShape(SURF_CHIP, dp(14), SURF_CHIP_LINE, dp(1)));
                result.setPadding(dp(10), dp(10), dp(10), dp(10));

                FrameLayout thumb = new FrameLayout(this);
                thumb.setLayoutParams(new LinearLayout.LayoutParams(dp(58), dp(72)));
                thumb.setClipToOutline(true);
                thumb.setBackground(roundedShape(COL_SILK_CREAM, dp(10), 0, 0));
                ImageView img = new ImageView(this);
                Bitmap bmp = loadProductBitmap(p);
                if (bmp != null) img.setImageBitmap(bmp); else img.setImageResource(resolveProductImage(p.imageKey));
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                thumb.addView(img, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                result.addView(thumb);

                LinearLayout info = new LinearLayout(this);
                info.setOrientation(LinearLayout.VERTICAL);
                info.setPadding(dp(10), 0, dp(10), 0);
                TextView name = text(p.nameFa, 11.4f, COL_TEXT_MAIN, true);
                name.setMaxLines(2);
                info.addView(name);
                TextView why = text("مناسب " + giftOccasion + " · بسته‌بندی هدیهٔ رایگان", 9f, COL_GOLD_DEEP, false);
                why.setPadding(0, dp(3), 0, dp(3));
                info.addView(why);
                TextView price = text(PersianFormatter.formatToman(p.salePrice), 11f, COL_TEXT_MAIN, true);
                info.addView(price);
                result.addView(info, weight(1f));

                TextView add = text("افزودن", 9.4f, COL_OBSIDIAN, true);
                add.setGravity(Gravity.CENTER);
                add.setPadding(dp(10), dp(8), dp(10), dp(8));
                add.setBackground(roundedShape(COL_GOLD_LIGHT, dp(10), 0, 0));
                add.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Models.Product target = db.getProductById(giftSuggestionId);
                        if (target != null) addToCart(target);
                    }
                });
                result.addView(add);
                card.addView(result);
            }
        }
        return card;
    }

    /** Weighted match between the occasion and the real catalog, constrained by budget. */
    private Models.Product suggestGift(String occasion, int budgetIndex) {
        long min;
        long max;
        if (budgetIndex == 0) { min = 0L; max = 1_000_000L; }
        else if (budgetIndex == 1) { min = 1_000_000L; max = 3_000_000L; }
        else { min = 3_000_000L; max = Long.MAX_VALUE; }

        Models.Product best = null;
        int bestScore = -1;
        for (Models.Product p : db.getProducts(true, "همه", "", "newest")) {
            if (p.salePrice < min || p.salePrice > max) continue;
            int score = 0;
            String cat = p.category == null ? "" : p.category;
            if ("تولد".equals(occasion)) {
                if (cat.contains("میکاپ")) score += 3;
                if (cat.contains("پوست")) score += 2;
            } else if ("سالگرد".equals(occasion)) {
                if (cat.contains("پوست")) score += 3;
                if (cat.contains("میکاپ")) score += 1;
            } else {
                if (cat.contains("مو")) score += 3;
                if (cat.contains("پوست")) score += 2;
            }
            if (p.salePrice > 1_200_000L) score += 1;
            if (score > bestScore) {
                bestScore = score;
                best = p;
            }
        }
        return best;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  v6.5 · MERCHANT OPERATIONS — coupons, exports, backup, CRM
    // ══════════════════════════════════════════════════════════════════════

    private View operationsCard() {
        LinearLayout card = whiteCard();
        card.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_SETTINGS, "ابزارهای عملیاتی مجموعه"));
        card.addView(space(10));

        // Coupons
        card.addView(text("کوپن‌های تخفیف", 10.6f, COL_TEXT_MAIN, true));
        for (final Models.CouponRecord cp : db.getCoupons()) {
            card.addView(space(7));
            LinearLayout line = row(Gravity.CENTER_VERTICAL);
            TextView code = text(ltr(cp.code), 10f, COL_TEXT_MAIN, true);
            line.addView(code);
            String desc = cp.discountPercent > 0
                    ? PersianFormatter.toPersianDigits(String.valueOf(cp.discountPercent)) + "٪"
                    : PersianFormatter.formatToman(cp.discountAmount);
            TextView meta = text("  ·  " + desc, 9.2f, COL_TEXT_MUTED, false);
            line.addView(meta, weight(1f));
            TextView toggle = text(cp.active ? "فعال" : "خاموش", 8.8f,
                    cp.active ? COL_SUCCESS : COL_TEXT_FAINT, true);
            toggle.setPadding(dp(8), dp(4), dp(8), dp(4));
            toggle.setBackground(roundedShape(cp.active ? COL_SUCCESS_SOFT : COL_SILK_CREAM, dp(8),
                    cp.active ? SURF_SUCCESS_LINE : COL_BORDER, dp(1)));
            toggle.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    db.toggleCoupon(cp.id, !cp.active);
                    renderCurrentScreen();
                }
            });
            line.addView(toggle);
            TextView del = text("×", 13f, COL_DANGER, true);
            del.setPadding(dp(9), dp(2), dp(4), dp(2));
            del.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    db.deleteCoupon(cp.id);
                    toast("کوپن حذف شد.");
                    renderCurrentScreen();
                }
            });
            line.addView(del);
            card.addView(line);
        }
        card.addView(space(9));
        card.addView(ghostGoldButton("ساخت کوپن تازه", new Runnable() {
            @Override public void run() { showCouponBuilderDialog(); }
        }));

        // Exports
        card.addView(space(14));
        card.addView(editorialDivider());
        card.addView(space(8));
        card.addView(text("خروجی و پشتیبان", 10.6f, COL_TEXT_MAIN, true));
        card.addView(space(7));
        LinearLayout exports = row(Gravity.CENTER_VERTICAL);
        exports.addView(smallAction("سفارش‌ها (CSV)", new Runnable() {
            @Override public void run() { shareCsv("سفارش‌های ASIYE STORE", db.exportOrdersCsv()); }
        }), weight(1f));
        exports.addView(spaceH(7));
        exports.addView(smallAction("موجودی (CSV)", new Runnable() {
            @Override public void run() { shareCsv("موجودی انبار ASIYE STORE", db.exportInventoryCsv()); }
        }), weight(1f));
        card.addView(exports);
        card.addView(space(7));
        LinearLayout backupRow = row(Gravity.CENTER_VERTICAL);
        backupRow.addView(smallAction("پشتیبان‌گیری", new Runnable() {
            @Override public void run() { backupDatabase(); }
        }), weight(1f));
        backupRow.addView(spaceH(7));
        backupRow.addView(smallAction("بازیابی آخرین نسخه", new Runnable() {
            @Override public void run() { restoreDatabase(); }
        }), weight(1f));
        card.addView(backupRow);
        card.addView(space(6));
        TextView note = text("نسخهٔ پشتیبان در پوشهٔ اختصاصی برنامه ذخیره می‌شود؛ مسیر آن پس از ساخت نمایش داده می‌شود.",
                8.8f, COL_TEXT_FAINT, false);
        note.setLineSpacing(dp(2), 1f);
        card.addView(note);

        // CRM
        card.addView(space(14));
        card.addView(editorialDivider());
        card.addView(space(8));
        card.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_USERS, "مشتریان مجموعه"));
        List<Models.CustomerRecord> customers = db.getCustomers();
        if (customers.isEmpty()) {
            card.addView(space(6));
            card.addView(text("پس از ثبت اولین سفارش، پروفایل مشتریان این‌جا ساخته می‌شود.", 9.4f, COL_TEXT_MUTED, false));
        } else {
            int shown = 0;
            for (final Models.CustomerRecord c : customers) {
                if (shown >= 4) break;
                card.addView(space(8));
                LinearLayout line = row(Gravity.CENTER_VERTICAL);
                LinearLayout texts = new LinearLayout(this);
                texts.setOrientation(LinearLayout.VERTICAL);
                texts.addView(text(c.name, 10.4f, COL_TEXT_MAIN, true));
                texts.addView(text(ltr(c.mobile) + " · " + PersianFormatter.formatToman(c.totalSpend)
                        + " · " + PersianFormatter.toPersianDigits(String.valueOf(c.ordersCount)) + " سفارش",
                        8.8f, COL_TEXT_MUTED, false));
                line.addView(texts, weight(1f));
                TextView wa = text("واتس‌اپ", 8.8f, 0xFF1F5B45, true);
                wa.setPadding(dp(9), dp(5), dp(9), dp(5));
                wa.setBackground(roundedShape(COL_SUCCESS_SOFT, dp(8), SURF_SUCCESS_LINE, dp(1)));
                wa.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        openSmartWhatsApp("پیگیری وضعیت سفارش مشتری", c.name);
                    }
                });
                line.addView(wa);
                card.addView(line);
                shown++;
            }
        }
        return card;
    }

    private void shareCsv(String subject, String csv) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, subject);
        send.putExtra(Intent.EXTRA_TEXT, csv);
        startActivity(Intent.createChooser(send, "ارسال خروجی " + subject));
    }

    /** Copies the live database next to the app's private files and reports the path. */
    private void backupDatabase() {
        try {
            java.io.File dbFile = getDatabasePath("asiye_royal_v5.db");
            java.io.File dir = getExternalFilesDir(null);
            if (dir == null) dir = getFilesDir();
            java.io.File target = new java.io.File(dir, "asiye-backup-" + System.currentTimeMillis() + ".db");
            java.io.FileInputStream in = new java.io.FileInputStream(dbFile);
            java.io.FileOutputStream out = new java.io.FileOutputStream(target);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            in.close();
            out.close();
            toast("پشتیبان ساخته شد: " + target.getAbsolutePath());
            android.content.ClipboardManager cm =
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(android.content.ClipData.newPlainText("backup", target.getAbsolutePath()));
                toast("مسیر پشتیبان در کلیپ‌بورد کپی شد.");
            }
        } catch (Throwable t) {
            toast("پشتیبان‌گیری ممکن نشد: " + t.getClass().getSimpleName());
        }
    }

    private void restoreDatabase() {
        try {
            java.io.File dir = getExternalFilesDir(null);
            if (dir == null) dir = getFilesDir();
            java.io.File[] files = dir.listFiles();
            java.io.File latest = null;
            if (files != null) {
                for (java.io.File f : files) {
                    if (!f.getName().startsWith("asiye-backup-")) continue;
                    if (latest == null || f.lastModified() > latest.lastModified()) latest = f;
                }
            }
            if (latest == null) {
                toast("نسخهٔ پشتیبانی پیدا نشد.");
                return;
            }
            db.close();
            java.io.File target = getDatabasePath("asiye_royal_v5.db");
            java.io.FileInputStream in = new java.io.FileInputStream(latest);
            java.io.FileOutputStream out = new java.io.FileOutputStream(target);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            in.close();
            out.close();
            db = new AsiyeDatabaseHelper(this);
            toast("پایگاه‌داده از نسخهٔ پشتیبان بازیابی شد.");
            renderCurrentScreen();
        } catch (Throwable t) {
            toast("بازیابی ممکن نشد: " + t.getClass().getSimpleName());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  v6.8 · SMS CONTROL CENTRE
    // ══════════════════════════════════════════════════════════════════════

    private View smsCenterCard() {
        LinearLayout card = whiteCard();

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        head.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_SMS, "مرکز پیامک و اتوماسیون"), weight(1f));
        boolean on = SmsGateway.isEnabled(db);
        TextView state = text(on ? "فعال" : "خاموش", 9f,
                on ? COL_SUCCESS : COL_TEXT_MUTED, true);
        state.setPadding(dp(9), dp(4), dp(9), dp(4));
        state.setBackground(roundedShape(on ? COL_SUCCESS_SOFT : COL_IVORY_BG, dp(9),
                on ? SURF_SUCCESS_LINE : COL_BORDER, dp(1)));
        head.addView(state);
        card.addView(head);

        card.addView(space(7));
        TextView hint = text("درگاه پیامک را یک‌بار تنظیم کنید؛ از آن پس همهٔ پیام‌های سفارش، یادآورها و گزارش روزانه خودکار ارسال می‌شوند.",
                9.5f, COL_TEXT_MUTED, false);
        hint.setLineSpacing(dp(3), 1f);
        card.addView(hint);
        card.addView(space(11));

        // provider chips
        card.addView(text("درگاه ارسال", 9.4f, COL_TEXT_MUTED, false));
        card.addView(space(6));
        final String[] providers = { SmsGateway.PROVIDER_KAVENEGAR, SmsGateway.PROVIDER_MELLI,
                SmsGateway.PROVIDER_SMSIR, SmsGateway.PROVIDER_GHASEDAK, SmsGateway.PROVIDER_SANDBOX };
        final String current = db.getSetting("sms_provider", SmsGateway.PROVIDER_SANDBOX);
        android.widget.HorizontalScrollView scroll = new android.widget.HorizontalScrollView(this);
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        for (final String provider : providers) {
            boolean selected = provider.equals(current);
            TextView chip = text(SmsGateway.providerLabel(provider), 9.2f,
                    selected ? COL_OBSIDIAN : COL_TEXT_MUTED, selected);
            chip.setPadding(dp(10), dp(6), dp(10), dp(6));
            chip.setBackground(selected ? roundedShape(COL_GOLD_LIGHT, dp(11), 0, 0)
                    : roundedShape(COL_IVORY_BG, dp(11), COL_BORDER, dp(1)));
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.setMarginEnd(dp(6));
            chip.setLayoutParams(clp);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    db.setSetting("sms_provider", provider);
                    toast("درگاه روی «" + SmsGateway.providerLabel(provider) + "» تنظیم شد.");
                    renderCurrentScreen();
                }
            });
            chips.addView(chip);
        }
        scroll.addView(chips);
        card.addView(scroll);

        card.addView(space(10));
        card.addView(settingsText("sms_api_key", "کلید API (کاوه‌نگار / sms.ir / قاصدک)", "", false));
        card.addView(settingsText("sms_sender", "شماره خط ارسال", "", false));
        card.addView(settingsText("sms_username", "نام کاربری ملی‌پیامک (در صورت استفاده)", "", false));
        card.addView(settingsText("sms_password", "رمز ملی‌پیامک", "", false));
        card.addView(settingsText("sms_test_mobile", "شمارهٔ تست پیامک", "", true));

        card.addView(space(8));
        LinearLayout actions = row(Gravity.CENTER_VERTICAL);
        actions.addView(smallGoldAction("ارسال پیام تست", new Runnable() {
            @Override public void run() {
                String to = db.getSetting("sms_test_mobile", "");
                if (!SmsGateway.isValidMobile(to)) {
                    toast("ابتدا شمارهٔ تست را درست وارد کنید.");
                    return;
                }
                String body = "اسیه استور\nاین یک پیام تست است. درگاه «"
                        + SmsGateway.providerLabel(db.getSetting("sms_provider", "sandbox"))
                        + "» با موفقیت تنظیم شده است.";
                db.enqueueSms(to, "manual_test", body);
                SmsGateway.flushAsync(MainActivity.this, db);
                toast("پیام تست در صف ارسال قرار گرفت.");
                renderCurrentScreen();
            }
        }), weight(1f));
        actions.addView(spaceH(7));
        actions.addView(smallAction("ارسال صف", new Runnable() {
            @Override public void run() {
                int sent = SmsGateway.flush(MainActivity.this, db);
                toast(sent > 0 ? PersianFormatter.toPersianDigits(String.valueOf(sent)) + " پیام ارسال شد."
                        : "صف خالی است یا درگاه تنظیم نشده.");
                renderCurrentScreen();
            }
        }), weight(1f));
        card.addView(actions);

        card.addView(space(11));
        card.addView(resultLine("در صف ارسال", PersianFormatter.toPersianDigits(String.valueOf(db.countSmsPending()))));
        card.addView(resultLine("ارسال‌شده امروز", PersianFormatter.toPersianDigits(String.valueOf(db.countSmsSentToday()))));
        card.addView(resultLine("ناموفق (نیازمند بررسی)", PersianFormatter.toPersianDigits(String.valueOf(db.countSmsFailed()))));

        card.addView(space(11));
        card.addView(foilDivider(false));
        card.addView(text("اتوماسیون خودکار", 11f, COL_TEXT_MAIN, true));
        card.addView(space(8));

        final String[][] autos = {
                {"auto_reorder", "یادآور سفارش مجدد", "پیش از پایان محصول، پیام یادآوری"},
                {"auto_birthday", "هدیهٔ تولد", "کوپن اختصاصی در روز تولد مشتری"},
                {"auto_review_request", "درخواست نظر پس از تحویل", "دعوت به ثبت نظر خرید تأییدشده"},
                {"auto_owner_alert", "گزارش روزانهٔ مدیر", "خلاصهٔ سفارش‌ها و موجودی هر صبح"}
        };
        for (String[] a : autos) {
            final String key = a[0];
            final boolean enabled = db.getSettingBool(key, true);
            LinearLayout line = row(Gravity.CENTER_VERTICAL);
            line.setPadding(0, dp(7), 0, dp(7));
            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.addView(text(a[1], 10.6f, COL_TEXT_MAIN, true));
            texts.addView(text(a[2], 9f, COL_TEXT_FAINT, false));
            line.addView(texts, weight(1f));
            final TextView toggle = text(enabled ? "روشن" : "خاموش", 9f,
                    enabled ? COL_SUCCESS : COL_TEXT_MUTED, true);
            toggle.setPadding(dp(10), dp(5), dp(10), dp(5));
            toggle.setBackground(roundedShape(enabled ? COL_SUCCESS_SOFT : COL_IVORY_BG, dp(9),
                    enabled ? SURF_SUCCESS_LINE : COL_BORDER, dp(1)));
            toggle.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    boolean now = !db.getSettingBool(key, true);
                    db.setSetting(key, now ? "1" : "0");
                    toggle.setText(now ? "روشن" : "خاموش");
                    toggle.setTextColor(now ? COL_SUCCESS : COL_TEXT_MUTED);
                    toggle.setBackground(roundedShape(now ? COL_SUCCESS_SOFT : COL_IVORY_BG, dp(9),
                            now ? SURF_SUCCESS_LINE : COL_BORDER, dp(1)));
                }
            });
            line.addView(toggle);
            card.addView(line);
        }

        card.addView(space(11));
        card.addView(smallGoldAction("ویرایش قالب‌های پیام", Luxury3DIconView.ICON_EDIT, new Runnable() {
            @Override public void run() { showTemplateListDialog(); }
        }));
        card.addView(space(8));
        card.addView(text("تغییرات این بخش با لمس «ذخیرهٔ تنظیمات» در پایین صفحه ثبت می‌شود.",
                8.8f, COL_TEXT_FAINT, false));
        return card;
    }

    /** The merchant's view of every card in circulation. */
    private void showGiftCardsList() {
        final LinearLayout box = dialogBox();
        box.addView(dialogTitle("کارت‌های هدیه", "اعتبار باقی‌ماندهٔ هر کارت به‌صورت زنده"));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();

        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        box.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(360)));

        List<AsiyeDatabaseHelper.GiftCard> cards = db.getGiftCards(20);
        if (cards.isEmpty()) {
            list.addView(text("هنوز کارت هدیه‌ای ساخته نشده است.", 10f, COL_TEXT_MUTED, false));
        }
        for (AsiyeDatabaseHelper.GiftCard g : cards) {
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.VERTICAL);
            line.setPadding(dp(10), dp(8), dp(10), dp(8));
            line.setBackground(roundedShape(COL_IVORY_BG, dp(11), COL_BORDER, dp(1)));
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 0, 0, dp(6));

            LinearLayout top = row(Gravity.CENTER_VERTICAL);
            top.addView(text(ltr(g.code), 10.4f, COL_TEXT_MAIN, true), weight(1f));
            top.addView(text("active".equals(g.status) ? "فعال" : "مصرف‌شده", 8.8f,
                    "active".equals(g.status) ? COL_SUCCESS : COL_TEXT_FAINT, true));
            line.addView(top);
            line.addView(text("مبلغ " + PersianFormatter.formatToman(g.amount)
                    + " · باقی‌مانده " + PersianFormatter.formatToman(g.remaining), 9.2f, COL_TEXT_MUTED, false));
            if (g.recipientName != null && !g.recipientName.isEmpty()) {
                line.addView(text("گیرنده: " + g.recipientName, 9f, COL_TEXT_FAINT, false));
            }
            list.addView(line, llp);
        }

        box.addView(space(8));
        box.addView(ghostButton("بستن", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    private void showTemplateListDialog() {
        final LinearLayout box = dialogBox();
        box.addView(dialogTitle("قالب‌های پیام", "متن هر رویداد را می‌توانید با جای‌نگهدارها بنویسید"));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();

        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(420));
        box.addView(scroll, lp);

        for (Models.SmsTemplateRecord t : db.getTemplates()) {
            LinearLayout rowItem = new LinearLayout(this);
            rowItem.setOrientation(LinearLayout.VERTICAL);
            rowItem.setPadding(dp(11), dp(9), dp(11), dp(9));
            rowItem.setBackground(roundedShape(COL_IVORY_BG, dp(12), COL_BORDER, dp(1)));
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ilp.setMargins(0, 0, 0, dp(7));

            LinearLayout top = row(Gravity.CENTER_VERTICAL);
            top.addView(text(t.title, 10.6f, COL_TEXT_MAIN, true), weight(1f));
            top.addView(text(t.enabled ? "فعال" : "خاموش", 8.6f,
                    t.enabled ? COL_SUCCESS : COL_TEXT_FAINT, true));
            rowItem.addView(top);
            TextView preview = text(t.body.replace("\n", " "), 9f, COL_TEXT_MUTED, false);
            preview.setMaxLines(2);
            preview.setEllipsize(android.text.TextUtils.TruncateAt.END);
            preview.setPadding(0, dp(4), 0, 0);
            rowItem.addView(preview);

            final String key = t.key;
            rowItem.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    dialog.dismiss();
                    showTemplateEditor(key);
                }
            });
            list.addView(rowItem, ilp);
        }

        box.addView(space(8));
        box.addView(ghostButton("بستن", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    private void showTemplateEditor(final String key) {
        final String currentBody = db.getTemplateBody(key);
        final EditText body = makeInput("متن پیام", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, false);
        body.setText(currentBody == null ? SmsGateway.defaultTemplate(key) : currentBody);
        body.setMinLines(4);
        body.setGravity(Gravity.TOP | Gravity.START);

        final LinearLayout box = dialogBox();
        box.addView(dialogTitle(SmsGateway.defaultTitle(key),
                "جای‌نگهدارها: {name} {order} {total} {tracking} {product} {days} {code} {coupon} {store}"));
        box.addView(body);
        box.addView(space(9));
        box.addView(text("پیش‌نمایش با دادهٔ نمونه:", 9f, COL_TEXT_FAINT, false));
        box.addView(space(4));
        final TextView preview = text(SmsGateway.preview(db, key), 9.6f, COL_TEXT_MUTED, false);
        preview.setPadding(dp(10), dp(8), dp(10), dp(8));
        preview.setBackground(roundedShape(COL_IVORY_BG, dp(10), COL_BORDER, dp(1)));
        box.addView(preview);
        box.addView(space(12));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ذخیرهٔ قالب", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() {
                db.setTemplate(key, body.getText().toString().trim(), db.isTemplateEnabled(key));
                dialog.dismiss();
                toast("قالب ذخیره شد.");
            }
        }));
        box.addView(space(8));
        box.addView(ghostButton("بازگشت به پیش‌فرض", new Runnable() {
            @Override public void run() {
                body.setText(SmsGateway.defaultTemplate(key));
                preview.setText(SmsGateway.render(SmsGateway.defaultTemplate(key), SmsGateway.demoVars()));
            }
        }));
        showRoundedDialog(dialog);
    }

    private TextView smallGoldAction(String label, Runnable action) {
        TextView t = text(label, 9.6f, COL_OBSIDIAN, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(11), dp(9), dp(11), dp(9));
        t.setBackground(roundedShape(COL_GOLD_LIGHT, dp(11), 0, 0));
        t.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return t;
    }

    private View smallGoldAction(String label, int iconType, final Runnable action) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.HORIZONTAL);
        wrap.setGravity(Gravity.CENTER);
        wrap.setPadding(dp(11), dp(9), dp(11), dp(9));
        wrap.setBackground(roundedShape(COL_GOLD_LIGHT, dp(11), 0, 0));
        wrap.addView(new Luxury3DIconView(this, iconType, false),
                new LinearLayout.LayoutParams(dp(16), dp(16)));
        TextView t = text(label, 9.6f, COL_OBSIDIAN, true);
        t.setPadding(dp(6), 0, 0, 0);
        wrap.addView(t);
        wrap.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return wrap;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  v6.6 · BRAND STORY · INGREDIENT GLOSSARY · INVOICE EXPORT
    // ══════════════════════════════════════════════════════════════════════

    /** v6.8 · one glance: are the machines doing their job today? */
    private View automationStatusCard() {
        LinearLayout card = whiteCard();
        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        head.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_AI, "اتوماسیون مجموعه"), weight(1f));
        boolean smsOn = SmsGateway.isEnabled(db);
        TextView pill = text(smsOn ? "در حال کار" : "خاموش", 9f,
                smsOn ? COL_SUCCESS : COL_TEXT_MUTED, true);
        pill.setPadding(dp(9), dp(4), dp(9), dp(4));
        pill.setBackground(roundedShape(smsOn ? COL_SUCCESS_SOFT : COL_IVORY_BG, dp(9),
                smsOn ? SURF_SUCCESS_LINE : COL_BORDER, dp(1)));
        head.addView(pill);
        card.addView(head);

        card.addView(space(10));
        card.addView(resultLine("اعضای باشگاه", PersianFormatter.toPersianDigits(
                String.valueOf(db.getAccountsCount()))));
        card.addView(resultLine("پیامک در صف", PersianFormatter.toPersianDigits(
                String.valueOf(db.countSmsPending()))));
        card.addView(resultLine("ارسال‌شده امروز", PersianFormatter.toPersianDigits(
                String.valueOf(db.countSmsSentToday()))));
        card.addView(resultLine("سفارش امروز", PersianFormatter.toPersianDigits(
                String.valueOf(db.getTodayOrdersCount()))));

        card.addView(space(10));
        card.addView(text(smsOn
                        ? "یادآور سفارش مجدد، تبریک تولد، درخواست نظر و گزارش روزانه هر صبح خودکار اجرا می‌شوند."
                        : "برای فعال‌سازی، در تنظیمات «مرکز پیامک» درگاه را تنظیم کنید.",
                9.4f, COL_TEXT_MUTED, false));

        card.addView(space(9));
        card.addView(smallGoldAction("تنظیمات پیامک", Luxury3DIconView.ICON_SETTINGS, new Runnable() {
            @Override public void run() {
                aiStudioOpen = false;
                adminTab = ADMIN_SETTINGS;
                renderCurrentScreen();
            }
        }));
        return card;
    }

    /** The panel's "open" sign: it must be obvious the moment the owner signs in. */
    private View adminLiveStrip() {
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        strip.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        strip.setGravity(Gravity.CENTER_VERTICAL);
        strip.setPadding(dp(13), dp(11), dp(13), dp(11));
        strip.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(16), lineGold(0x55), dp(1)));

        FrameLayout seal = iconMedallion(Luxury3DIconView.ICON_CROWN, 34, Luxury3DIconView.PEDESTAL_GOLD);
        strip.addView(seal);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(10), 0, dp(10), 0);
        texts.addView(text("پنل مدیریت فعال است", 11.8f, TEXT_ON_DARK, true));
        texts.addView(text("خوش آمدید خانم زنگنه — همهٔ ابزارها در دسترس شماست", 9.4f, TEXT_ON_DARK_FAINT, false));
        strip.addView(texts, weight(1f));

        TextView out = text("خروج", 9.4f, COL_GOLD_LIGHT, true);
        out.setPadding(dp(10), dp(6), dp(10), dp(6));
        out.setBackground(roundedShape(scrim(0x22), dp(10), lineGold(0x66), dp(1)));
        out.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                adminMode = false;
                aiStudioOpen = false;
                customerTab = TAB_BOUTIQUE;
                headerSubtitle.setText("مدیریت مجموعه: سرکار خانم زنگنه");
                renderCurrentScreen();
                toast("از پنل مدیریت خارج شدید.");
            }
        });
        strip.addView(out);
        return strip;
    }

    /** "پشت ویترین" — the boutique's story, told in three quiet frames. */
    /** A hairline of beaten gold with a diamond at its heart. */
    private View foilDivider(boolean onDark) {
        LuxuryCanvas.GoldFoilDivider d = new LuxuryCanvas.GoldFoilDivider(this);
        d.onDark(onDark);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(22));
        lp.setMargins(dp(10), dp(4), dp(10), dp(4));
        d.setLayoutParams(lp);
        return d;
    }

    private View brandStoryCard() {
        LinearLayout card = whiteCard();
        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        FrameLayout logo = new FrameLayout(this);
        logo.setLayoutParams(new LinearLayout.LayoutParams(dp(52), dp(52)));
        logo.setClipToOutline(true);
        logo.setBackground(roundedShape(COL_OBSIDIAN, dp(16), lineGold(0x55), dp(1)));
        ImageView mark = new ImageView(this);
        mark.setImageResource(R.drawable.img_brand_emblem);
        mark.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logo.addView(mark, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        head.addView(logo);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(11), 0, dp(11), 0);
        titles.addView(text("پشت ویترین اسیه", 13f, COL_TEXT_MAIN, true));
        titles.addView(text("سه اصل ثابت در هر انتخاب", 9.4f, COL_TEXT_MUTED, false));
        head.addView(titles, weight(1f));
        card.addView(head);

        String[][] frames = {
                {"۱", "انتخاب علمی", "هر کالا با پروندهٔ ترکیبات و شناسهٔ اصالت وارد ویترین می‌شود؛ تنها فرمول‌هایی که برای پوست ایرانی آزموده شده‌اند."},
                {"۲", "بسته‌بندی آیینی", "کارتن مخملی، کاغذ کتان و مهر مُوم طلایی؛ هر سفارش مثل یک هدیه بسته‌بندی می‌شود."},
                {"۳", "مراقبت پس از خرید", "مشاورهٔ مستقیم مدیریت مجموعه و پیگیری وضعیت تا تحویل و پس از آن."}
        };
        for (String[] f : frames) {
            card.addView(space(10));
            View divider = new View(this);
            divider.setBackgroundColor(SURF_DIVIDER);
            card.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
            card.addView(space(9));

            LinearLayout line = row(Gravity.TOP);
            TextView num = text(f[0], 10f, COL_OBSIDIAN, true);
            num.setGravity(Gravity.CENTER);
            num.setBackground(roundedShape(COL_GOLD_LIGHT, dp(18), 0, 0));
            num.setLayoutParams(new LinearLayout.LayoutParams(dp(22), dp(22)));
            line.addView(num);

            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.setPadding(dp(9), 0, dp(9), 0);
            texts.addView(text(f[1], 10.8f, COL_TEXT_MAIN, true));
            TextView body = text(f[2], 9.6f, COL_TEXT_MUTED, false);
            body.setLineSpacing(dp(3), 1f);
            body.setPadding(0, dp(3), 0, 0);
            texts.addView(body);
            line.addView(texts, weight(1f));
            card.addView(line);
        }
        return card;
    }

    /** Tap an ingredient, get a two-line honest explanation. */
    private void showIngredientGlossary(String raw) {
        String ingredient = raw == null ? "" : raw.trim();
        if (ingredient.isEmpty()) return;

        String[][] known = {
                {"نیاسینامید", "ویتامین B3؛ ترمیم سد پوستی، تنظیم چربی و کاهش لک."},
                {"پپتید", "زنجیرهٔ اسید آمینه که کلاژن‌سازی را تحریک می‌کند و خطوط ریز را نرم می‌کند."},
                {"گلیکولیک", "آلفا هیدروکسی اسید؛ لایه‌برداری سطحی و شفاف‌سازی منافذ."},
                {"هیالورونیک", "جذب آب در لایهٔ سطحی؛ آبرسانی فوری و پرکردن ظاهری خطوط."},
                {"ویتامین C", "آنتی‌اکسیدان روشن‌کننده؛ یکدست‌سازی تناژ و محافظت در برابر آلودگی."},
                {"سرامید", "چربی طبیعی پوست؛ بازسازی سد محافظ و کاهش خشکی."},
                {"روغن مورینگا", "مغذی سبک برای ساقهٔ مو؛ درخشش بدون سنگینی."},
                {"درخت چای", "ضدباکتری طبیعی؛ کنترل چربی کف سر و کمک به رفع شوره."},
                {"آلوئه", "آرام‌کننده و خنک‌کننده؛ مناسب پوست‌های حساس و پس از اصلاح."},
                {"جینسنگ", "محافظت آنتی‌اکسیدانی و کمک به شادابی پوست."}
        };

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(18));
        box.setBackgroundColor(COL_WHITE);
        box.addView(text("واژه‌نامهٔ ترکیبات", 14f, COL_TEXT_MAIN, true));
        box.addView(space(4));
        box.addView(text(ingredient, 9.8f, COL_TEXT_MUTED, false));
        box.addView(space(12));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        int found = 0;
        for (String[] k : known) {
            if (ingredient.contains(k[0]) || k[0].contains(ingredient)) {
                LinearLayout line = new LinearLayout(this);
                line.setOrientation(LinearLayout.VERTICAL);
                line.setPadding(0, dp(7), 0, dp(7));
                line.addView(text(k[0], 11f, COL_GOLD_DEEP, true));
                TextView d = text(k[1], 9.8f, COL_TEXT_MUTED, false);
                d.setLineSpacing(dp(3), 1f);
                line.addView(d);
                box.addView(line);
                found++;
            }
        }
        if (found == 0) {
            TextView d = text("این ترکیب در واژه‌نامه نیست؛ برای توضیح دقیق با مشاور مجموعه در واتس‌اپ گفت‌وگو کنید.",
                    9.8f, COL_TEXT_MUTED, false);
            d.setLineSpacing(dp(3), 1f);
            box.addView(d);
        }
        box.addView(space(14));
        box.addView(primaryButton("متوجه شدم", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    /** Renders the invoice as a PNG into the Downloads folder (Android 10+). */
    private void exportInvoicePng(Models.OrderRecord o) {
        try {
            int w = 900, h = 1200;
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            android.graphics.Canvas c = new android.graphics.Canvas(bmp);
            c.drawColor(COL_OBSIDIAN);
            android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            p.setColor(COL_GOLD);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3f);
            c.drawRect(36, 36, w - 36, h - 36, p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(COL_GOLD_LIGHT);
            p.setTextSize(30f);
            p.setTextAlign(Paint.Align.CENTER);
            c.drawText("ASIYE STORE", w / 2f, 120, p);

            p.setColor(TEXT_ON_DARK);
            p.setTextSize(22f);
            c.drawText("فاکتور فروش", w / 2f, 168, p);

            p.setColor(TEXT_ON_DARK_MUTED);
            p.setTextSize(17f);
            c.drawText("شمارهٔ سفارش: " + o.orderNumber, w / 2f, 214, p);
            c.drawText("تاریخ: " + o.createdAtLabel, w / 2f, 246, p);

            float y = 310;
            p.setTextSize(17f);
            for (Models.OrderItemRecord item : o.items) {
                p.setColor(TEXT_ON_DARK);
                p.setTextAlign(Paint.Align.RIGHT);
                c.drawText(item.productName, w - 80, y, p);
                p.setColor(TEXT_ON_DARK_MUTED);
                p.setTextAlign(Paint.Align.LEFT);
                c.drawText(PersianFormatter.toPersianDigits(String.valueOf(item.quantity)) + " × "
                        + PersianFormatter.formatToman(item.unitPrice), 80, y, p);
                y += 34;
                p.setColor(lineGold(0x33));
                c.drawLine(80, y - 12, w - 80, y - 12, p);
            }

            y += 24;
            p.setColor(TEXT_ON_DARK);
            p.setTextSize(19f);
            p.setTextAlign(Paint.Align.RIGHT);
            c.drawText("جمع کل: " + PersianFormatter.formatToman(o.totalAmount), w - 80, y, p);

            p.setColor(TEXT_ON_DARK_FAINT);
            p.setTextSize(15f);
            p.setTextAlign(Paint.Align.CENTER);
            c.drawText("مدیریت مجموعه: سرکار خانم زنگنه · " + OWNER_PHONE_DISPLAY, w / 2f, h - 90, p);
            c.drawText(INSTAGRAM_HANDLE, w / 2f, h - 62, p);

            String name = "asiye-invoice-" + o.orderNumber.replace("/", "-") + ".png";
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                android.content.ContentValues cv = new android.content.ContentValues();
                cv.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name);
                cv.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png");
                cv.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download/ASIYE STORE");
                android.net.Uri uri = getContentResolver()
                        .insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                if (uri != null) {
                    java.io.OutputStream out = getContentResolver().openOutputStream(uri);
                    if (out != null) {
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.close();
                    }
                    toast("فاکتور در پوشهٔ Download/ASIYE STORE ذخیره شد.");
                    return;
                }
            }
            java.io.File dir = getExternalFilesDir(null);
            if (dir == null) dir = getFilesDir();
            java.io.File file = new java.io.File(dir, name);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            toast("فاکتور ذخیره شد: " + file.getAbsolutePath());
        } catch (Throwable t) {
            toast("ساخت تصویر فاکتور ممکن نشد.");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  v6.5 · SKIN-TYPE FILTER · PRODUCT COMPARISON
    // ══════════════════════════════════════════════════════════════════════

    /** The catalog is fixed, so suitability is derived from each SKU's real formulation. */
    private String skinTypeFor(Models.Product p) {
        String sku = p.sku == null ? "" : p.sku;
        if ("AS-GAT-150".equals(sku)) return "هر پوست";
        if ("AS-GAT-050".equals(sku)) return "خشک";
        if ("AS-LC-104".equals(sku)) return "چرب";
        if ("AS-ORD-240".equals(sku)) return "مختلط";
        if ("AS-HF-300".equals(sku)) return "حساس";
        if ("AS-MRG-500".equals(sku)) return "چرب";
        return "هر پوست";
    }

    private boolean matchesSkin(Models.Product p) {
        if ("همه".equals(skinFilter)) return true;
        String t = skinTypeFor(p);
        return t.equals(skinFilter) || "هر پوست".equals(t);
    }

    /** The house's filter chip: porcelain when idle, gold-rimmed silk when chosen. */
    private TextView luxeChip(String label, boolean active, float sizeSp) {
        TextView chip = text(label, sizeSp, active ? COL_GOLD_DEEP : COL_TEXT_MUTED, active);
        chip.setGravity(Gravity.CENTER);
        chip.setSingleLine(true);
        chip.setPadding(dp(4), dp(8), dp(4), dp(8));
        chip.setBackground(roundedShape(active ? SURF_CHIP : COL_WHITE, dp(13),
                active ? COL_METAL : COL_BORDER, dp(active ? 1.4f : 1)));
        if (active) chip.setElevation(dp(1.2f));
        return chip;
    }

    private View skinChips() {
        final String[] types = {"همه", "خشک", "چرب", "مختلط", "حساس"};
        LinearLayout row = row(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < types.length; i++) {
            final String type = types[i];
            boolean active = skinFilter.equals(type);
            TextView chip = text(type, 9.8f, active ? COL_GOLD_LIGHT : COL_TEXT_MUTED, active);
            chip.setGravity(Gravity.CENTER);
            chip.setSingleLine(true);
            chip.setPadding(dp(2), dp(8), dp(2), dp(8));
            chip.setBackground(active ? deepCard(dp(13)) : roundedShape(COL_WHITE, dp(13), COL_BORDER, dp(1)));
            if (active) chip.setElevation(dp(1.4f));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (!skinFilter.equals(type)) {
                        skinFilter = type;
                        renderCurrentScreen();
                    }
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            row.addView(chip, lp);
        }
        return row;
    }

    private View compareTray() {
        if (compareIds.isEmpty()) return new View(this);

        LinearLayout card = whiteCard();
        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        head.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_DIAMOND, "مقایسهٔ سریع"), weight(1f));
        TextView clear = text("پاک‌کردن", 9f, COL_TEXT_FAINT, true);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                compareIds.clear();
                renderCurrentScreen();
            }
        });
        head.addView(clear);
        card.addView(head);

        LinearLayout chips = row(Gravity.CENTER_VERTICAL);
        chips.setPadding(0, dp(9), 0, dp(4));
        for (long id : compareIds) {
            Models.Product p = db.getProductById(id);
            if (p == null) continue;
            TextView chip = text(p.nameFa, 9f, COL_TEXT_MAIN, false);
            chip.setSingleLine(true);
            chip.setEllipsize(android.text.TextUtils.TruncateAt.END);
            chip.setPadding(dp(8), dp(6), dp(8), dp(6));
            chip.setBackground(roundedShape(COL_SILK_CREAM, dp(9), COL_BORDER, dp(1)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            chips.addView(chip, lp);
        }
        card.addView(chips);

        if (compareIds.size() < 2) {
            card.addView(text("یک کالای دیگر را از شیت محصول به مقایسه اضافه کنید.", 9.2f, COL_TEXT_FAINT, false));
        } else {
            card.addView(primaryButton("مقایسهٔ دو کالا", Luxury3DIconView.ICON_CHART, new Runnable() {
                @Override public void run() { showCompareDialog(); }
            }));
        }
        return card;
    }

    private void toggleCompare(Models.Product p) {
        if (compareIds.contains(p.id)) {
            compareIds.remove(p.id);
            toast("از مقایسه حذف شد.");
        } else {
            if (compareIds.size() >= 2) compareIds.remove(0);
            compareIds.add(p.id);
            toast("به مقایسه اضافه شد.");
        }
        renderCurrentScreen();
    }

    private void showCompareDialog() {
        if (compareIds.size() < 2) return;
        Models.Product a = db.getProductById(compareIds.get(0));
        Models.Product b = db.getProductById(compareIds.get(1));
        if (a == null || b == null) return;

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(18), dp(18), dp(16));
        box.setBackgroundColor(COL_WHITE);
        box.addView(text("مقایسهٔ دو کالا", 14f, COL_TEXT_MAIN, true));
        box.addView(space(12));

        String[][] rows = {
                {"قیمت", PersianFormatter.formatToman(a.salePrice), PersianFormatter.formatToman(b.salePrice)},
                {"حجم / وزن", a.volume, b.volume},
                {"کشور / اصالت", a.country, b.country},
                {"مناسب برای", skinTypeFor(a), skinTypeFor(b)},
                {"موجودی", a.stock > 0 ? "موجود" : "ناموجود", b.stock > 0 ? "موجود" : "ناموجود"},
                {"دورهٔ مصرف", PersianFormatter.toPersianDigits(String.valueOf(a.replenishDays)) + " روز",
                        PersianFormatter.toPersianDigits(String.valueOf(b.replenishDays)) + " روز"}
        };

        LinearLayout header = row(Gravity.CENTER_VERTICAL);
        header.addView(text("", 9f, COL_TEXT_FAINT, false), weight(1f));
        header.addView(compareHead(a.nameFa), weight(1f));
        header.addView(compareHead(b.nameFa), weight(1f));
        box.addView(header);

        for (String[] r : rows) {
            LinearLayout line = row(Gravity.CENTER_VERTICAL);
            line.setPadding(0, dp(7), 0, dp(7));
            TextView label = text(r[0], 9.2f, COL_TEXT_MUTED, false);
            line.addView(label, weight(1f));
            line.addView(text(r[1], 9.2f, COL_TEXT_MAIN, true), weight(1f));
            line.addView(text(r[2], 9.2f, COL_TEXT_MAIN, true), weight(1f));
            box.addView(line);
            View divider = new View(this);
            divider.setBackgroundColor(SURF_DIVIDER);
            box.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(box);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(scroll).create();
        box.addView(space(10));
        box.addView(primaryButton("بستن", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    private TextView compareHead(String name) {
        TextView t = text(name, 9f, COL_GOLD_DEEP, true);
        t.setMaxLines(2);
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        return t;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  v6.5 · ACCOUNT · ORDERS · ADDRESS BOOK · CERTIFICATE · ALERTS
    // ══════════════════════════════════════════════════════════════════════

    private void renderAccount() {
        contentContainer.addView(crestTitle(Luxury3DIconView.ICON_MEDAL, "حساب من",
                "سفارش‌ها، نشانی‌ها و گواهی اصالت شما در یک نگاه", "MAISON ACCOUNT"));
        contentContainer.addView(editorialDivider());
        contentContainer.addView(space(10));

        // v6.8 · identity first: the maison greets a member, or invites a first-time visitor
        if (!AccountStore.isSignedIn(this)) {
            contentContainer.addView(welcomeCard());
            contentContainer.addView(space(12));
            contentContainer.addView(guestTrackingCard());
            contentContainer.addView(space(12));
            contentContainer.addView(accountBenefitsCard());
            contentContainer.addView(space(10));
            contentContainer.addView(footerNote());
            return;
        }

        contentContainer.addView(profileCard());
        contentContainer.addView(space(12));
        contentContainer.addView(smsPreferencesCard());
        contentContainer.addView(space(12));

        View alerts = notificationsCard();
        if (alerts != null) {
            contentContainer.addView(alerts);
            contentContainer.addView(space(12));
        }

        contentContainer.addView(ordersCard());
        contentContainer.addView(space(12));
        contentContainer.addView(addressBookCard());
        contentContainer.addView(space(12));
        contentContainer.addView(certificateCard());
        contentContainer.addView(space(6));
        contentContainer.addView(footerNote());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  v6.9 · REWARDS — club tier, gift card, same-day courier
    // ══════════════════════════════════════════════════════════════════════

    private String checkoutMobile() {
        Models.AccountRecord me = db.getAccount(AccountStore.sessionMobile(this));
        if (me != null && !me.mobile.isEmpty()) return me.mobile;
        String typed = PersianFormatter.toAsciiDigits(bagMobileInput.getText().toString()).trim();
        return typed.isEmpty() ? profileMobile : typed;
    }

    private View rewardsCard() {
        LinearLayout card = whiteCard();
        card.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_CROWN, "باشگاه، کارت هدیه و روش ارسال"));
        card.addView(space(9));

        // ── club tier ────────────────────────────────────────────────────
        int percent = db.getTierPercent(checkoutMobile());
        LinearLayout tierRow = row(Gravity.CENTER_VERTICAL);
        tierRow.setBackground(roundedShape(COL_OBSIDIAN, dp(14), lineGold(0x55), dp(1)));
        tierRow.setPadding(dp(12), dp(10), dp(12), dp(10));

        FrameLayout crest = new FrameLayout(this);
        crest.setLayoutParams(new LinearLayout.LayoutParams(dp(26), dp(26)));
        crest.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_DIAMOND, true),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tierRow.addView(crest);

        LinearLayout tierTexts = new LinearLayout(this);
        tierTexts.setOrientation(LinearLayout.VERTICAL);
        tierTexts.setPadding(dp(9), 0, dp(9), 0);
        tierTexts.addView(text("عضو " + db.getTierName(checkoutMobile()) + " باشگاه اسیه",
                10.6f, TEXT_ON_DARK, true));
        tierTexts.addView(text(percent > 0
                        ? "تخفیف خودکار " + PersianFormatter.toPersianDigits(String.valueOf(percent))
                          + "٪ روی همهٔ خریدها — بدون کد"
                        : "با اولین خرید، عضویت نقره‌ای و ۳٪ تخفیف دائمی فعال می‌شود",
                9f, TEXT_ON_DARK_MUTED, false));
        tierRow.addView(tierTexts, weight(1f));

        if (percent > 0) {
            TextView badge = text("−" + PersianFormatter.toPersianDigits(String.valueOf(percent)) + "٪",
                    11f, COL_OBSIDIAN, true);
            badge.setPadding(dp(9), dp(4), dp(9), dp(4));
            badge.setBackground(roundedShape(COL_GOLD_LIGHT, dp(9), 0, 0));
            tierRow.addView(badge);
        }
        card.addView(tierRow);

        // ── gift card ────────────────────────────────────────────────────
        if (db.getSettingBool("gift_cards_enabled", true)) {
            card.addView(space(11));
            card.addView(text("کارت هدیه", 10.4f, COL_TEXT_MAIN, true));
            card.addView(space(6));

            final EditText giftInput = makeInput("کد کارت هدیه (AS-GIFT-…)", InputType.TYPE_CLASS_TEXT, false);
            giftInput.setText(appliedGiftCode);
            card.addView(giftInput);
            card.addView(space(7));

            LinearLayout giftRow = row(Gravity.CENTER_VERTICAL);
            LinearLayout applyGift = row(Gravity.CENTER);
            applyGift.setPadding(dp(12), dp(6), dp(10), dp(6));
            applyGift.setBackground(roundedShape(COL_GOLD_LIGHT, dp(13), 0, 0));
            FrameLayout giftMedal = new FrameLayout(this);
            giftMedal.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
            giftMedal.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_GIFT, true),
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            applyGift.addView(giftMedal);
            TextView applyLabel = text(appliedGiftValue > 0 ? "برداشتن کارت" : "اعمال کارت هدیه",
                    9.6f, COL_OBSIDIAN, true);
            applyLabel.setGravity(Gravity.CENTER);
            applyLabel.setPadding(dp(7), 0, 0, 0);
            applyGift.addView(applyLabel);
            applyGift.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (appliedGiftValue > 0) {
                        appliedGiftCode = "";
                        appliedGiftValue = 0L;
                        toast("کارت هدیه برداشته شد.");
                    } else {
                        AsiyeDatabaseHelper.GiftCard g = db.getGiftCard(giftInput.getText().toString());
                        if (g == null) {
                            toast("کارت هدیه‌ای با این کد پیدا نشد.");
                            return;
                        }
                        if (!"active".equals(g.status) || g.remaining <= 0) {
                            toast("این کارت هدیه استفاده شده است.");
                            return;
                        }
                        appliedGiftCode = g.code;
                        appliedGiftValue = Math.min(g.remaining, calculateCartSubtotal());
                        toast("کارت هدیه اعمال شد.");
                    }
                    renderCurrentScreen();
                }
            });
            giftRow.addView(applyGift);

            TextView buy = text("خرید کارت هدیه", 9.6f, COL_GOLD_DEEP, true);
            buy.setGravity(Gravity.CENTER);
            buy.setPadding(dp(14), dp(9), dp(14), dp(9));
            buy.setBackground(roundedShape(COL_IVORY_BG, dp(11), COL_BORDER, dp(1)));
            buy.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { showGiftCardPurchase(); }
            });
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            blp.setMarginStart(dp(7));
            giftRow.addView(buy, blp);
            card.addView(giftRow);

            if (appliedGiftValue > 0) {
                card.addView(space(6));
                card.addView(text("کارت " + ltr(appliedGiftCode) + " — "
                                + PersianFormatter.formatToman(appliedGiftValue) + " از مبلغ کسر می‌شود.",
                        9.2f, COL_SUCCESS, false));
            }
        }

        // ── delivery method ──────────────────────────────────────────────
        if (db.isCourierEnabled()) {
            card.addView(space(12));
            LinearLayout shipHead = row(Gravity.CENTER_VERTICAL);
            FrameLayout shipMedal = new FrameLayout(this);
            shipMedal.setLayoutParams(new LinearLayout.LayoutParams(dp(21), dp(21)));
            shipMedal.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_TRUCK, true),
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            TextView shipTitle = text("روش ارسال", 10.4f, COL_TEXT_MAIN, true);
            shipTitle.setPadding(dp(7), 0, 0, 0);
            shipHead.addView(shipMedal);
            shipHead.addView(shipTitle, weight(1f));
            TextView shipEta = text(shippingEtaLabel(), 8.8f, COL_TEXT_FAINT, false);
            shipHead.addView(shipEta);
            card.addView(shipHead);
            card.addView(space(7));

            LinearLayout methods = row(Gravity.CENTER_VERTICAL);
            methods.addView(deliveryChip("پست پیشتاز VIP", "۲ تا ۴ روز کاری", !courierSelected, new Runnable() {
                @Override public void run() {
                    courierSelected = false;
                    renderCurrentScreen();
                }
            }), weight(1f));
            methods.addView(spaceH(7));
            methods.addView(deliveryChip("پیک شهری", db.getSetting("courier_eta", "۲ تا ۴ ساعت"),
                    courierSelected, new Runnable() {
                @Override public void run() {
                    courierSelected = true;
                    renderCurrentScreen();
                }
            }), weight(1f));
            card.addView(methods);

            if (courierSelected) {
                card.addView(space(8));
                card.addView(text("شهر تحویل", 9.4f, COL_TEXT_MUTED, false));
                card.addView(space(6));
                android.widget.HorizontalScrollView scroll = new android.widget.HorizontalScrollView(this);
                LinearLayout cities = new LinearLayout(this);
                cities.setOrientation(LinearLayout.HORIZONTAL);
                cities.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                for (final String city : db.getCourierCities()) {
                    boolean on = city.equals(courierCity);
                    TextView chip = text(city, 9.2f, on ? COL_OBSIDIAN : COL_TEXT_MUTED, on);
                    chip.setPadding(dp(11), dp(6), dp(11), dp(6));
                    chip.setBackground(on ? roundedShape(COL_GOLD_LIGHT, dp(10), 0, 0)
                            : roundedShape(COL_IVORY_BG, dp(10), COL_BORDER, dp(1)));
                    LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    clp.setMarginEnd(dp(6));
                    chip.setLayoutParams(clp);
                    chip.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            courierCity = city;
                            renderCurrentScreen();
                        }
                    });
                    cities.addView(chip);
                }
                scroll.addView(cities);
                card.addView(scroll);
                card.addView(space(6));
                card.addView(text("کرایهٔ پیک " + PersianFormatter.formatToman(db.getCourierFee())
                        + " — پرداخت در محل تحویل", 9.2f, COL_TEXT_FAINT, false));
            }
        }
        return card;
    }

    /** "تهران · ۲ تا ۴ ساعت" when the courier is selected, otherwise the postal promise. */
    private String shippingEtaLabel() {
        if (courierSelected && db.isCourierEnabled()) {
            return courierCity + " · " + db.getSetting("courier_eta", "۲ تا ۴ ساعت");
        }
        return "تحویل ۲ تا ۴ روز کاری";
    }

    private View deliveryChip(String label, String promise, boolean selected, final Runnable action) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(8), dp(10), dp(8), dp(10));
        box.setBackground(roundedShape(selected ? SURF_SOFT : COL_IVORY_BG, dp(13),
                selected ? COL_GOLD : COL_BORDER, dp(selected ? 2 : 1)));

        FrameLayout icon = new FrameLayout(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(24), dp(24)));
        icon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_TRUCK, selected),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        box.addView(icon);
        box.addView(space(5));

        TextView t = text(label, 10f, selected ? COL_TEXT_MAIN : COL_TEXT_MUTED, selected);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        box.addView(t);

        if (promise != null && !promise.isEmpty()) {
            TextView p = text(promise, 8.8f, selected ? COL_GOLD_DEEP : COL_TEXT_FAINT, false);
            p.setGravity(Gravity.CENTER);
            p.setSingleLine(true);
            p.setEllipsize(TextUtils.TruncateAt.END);
            p.setPadding(0, dp(2), 0, 0);
            box.addView(p);
        }

        box.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return box;
    }

    /** Buy a card for someone: amount, recipient, a short note — and an SMS to them. */
    private void showGiftCardPurchase() {
        final long[] amounts = { 1_000_000L, 2_000_000L, 5_000_000L, 10_000_000L };
        final int[] chosen = { 1 };

        final EditText recipientName = makeInput("نام گیرندهٔ هدیه", InputType.TYPE_CLASS_TEXT, false);
        final EditText recipientMobile = makeInput("موبایل گیرنده", InputType.TYPE_CLASS_PHONE, true);
        final EditText note = makeInput("یادداشت کوتاه روی کارت (اختیاری)", InputType.TYPE_CLASS_TEXT, false);

        LinearLayout box = dialogBox();
        box.addView(dialogTitle("کارت هدیهٔ اسیه", "هدیه‌ای که با یک پیامک به دست عزیزتان می‌رسد"));

        LinearLayout chips = row(Gravity.CENTER_VERTICAL);
        final TextView[] chipsView = new TextView[amounts.length];
        for (int i = 0; i < amounts.length; i++) {
            final int idx = i;
            boolean on = i == chosen[0];
            TextView chip = text(PersianFormatter.formatToman(amounts[i]), 9.2f,
                    on ? COL_OBSIDIAN : COL_TEXT_MUTED, on);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(8), dp(8), dp(8), dp(8));
            chip.setBackground(on ? roundedShape(COL_GOLD_LIGHT, dp(11), 0, 0)
                    : roundedShape(COL_IVORY_BG, dp(11), COL_BORDER, dp(1)));
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            clp.setMarginEnd(i < amounts.length - 1 ? dp(5) : 0);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    chosen[0] = idx;
                    for (int k = 0; k < chipsView.length; k++) {
                        boolean active = k == idx;
                        chipsView[k].setTextColor(active ? COL_OBSIDIAN : COL_TEXT_MUTED);
                        chipsView[k].setBackground(active ? roundedShape(COL_GOLD_LIGHT, dp(11), 0, 0)
                                : roundedShape(COL_IVORY_BG, dp(11), COL_BORDER, dp(1)));
                    }
                }
            });
            chipsView[i] = chip;
            chips.addView(chip, clp);
        }
        box.addView(chips);
        box.addView(space(10));
        box.addView(recipientName);
        box.addView(space(8));
        box.addView(recipientMobile);
        box.addView(space(8));
        box.addView(note);
        box.addView(space(12));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ساخت کارت و ارسال پیامک", Luxury3DIconView.ICON_GIFT, new Runnable() {
            @Override public void run() {
                String rn = recipientName.getText().toString().trim();
                String rm = recipientMobile.getText().toString().trim();
                if (!SmsGateway.isValidMobile(rm)) {
                    toast("شمارهٔ گیرنده معتبر نیست.");
                    return;
                }
                final AsiyeDatabaseHelper.GiftCard card = db.createGiftCard(
                        amounts[chosen[0]], profileName, checkoutMobile(), rn, rm,
                        note.getText().toString().trim());

                java.util.Map<String, String> vars = new java.util.HashMap<>();
                vars.put("name", rn.isEmpty() ? "عزیز" : rn);
                vars.put("coupon", card.code);
                vars.put("percent", PersianFormatter.toPersianDigits("0"));
                vars.put("total", PersianFormatter.formatToman(card.amount));
                db.setTemplate("gift_card_gift",
                        "{name} عزیز، {total} کارت هدیهٔ اسیه برای شما فعال شد.\n"
                                + "کد: {coupon}\n"
                                + "با این کد در سبد خرید، مبلغ از سفارش شما کسر می‌شود.\n{store} · {owner}", true);
                SmsGateway.notify(MainActivity.this, db, rm, "gift_card_gift", vars);

                dialog.dismiss();
                toast("کارت " + card.code + " ساخته شد و پیامک برای گیرنده رفت.");
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
    //  v6.8 · IDENTITY — welcome, sign-up, sign-in, recovery
    // ══════════════════════════════════════════════════════════════════════

    private View welcomeCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_OBSIDIAN, dp(24), lineGold(0x45), dp(1)));
        card.setPadding(dp(18), dp(20), dp(18), dp(18));
        card.setGravity(Gravity.CENTER_HORIZONTAL);

        FrameLayout crest = new FrameLayout(this);
        crest.setLayoutParams(new LinearLayout.LayoutParams(dp(84), dp(84)));
        crest.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(26), lineGold(0x66), dp(1)));
        crest.setClipToOutline(true);
        ImageView mark = new ImageView(this);
        mark.setImageResource(R.drawable.img_brand_emblem);
        mark.setScaleType(ImageView.ScaleType.CENTER_CROP);
        crest.addView(mark, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        card.addView(crest);
        card.addView(space(14));

        TextView t = text("به خانهٔ زیبایی اسیه خوش آمدید", 16.5f, TEXT_ON_DARK, true);
        t.setGravity(Gravity.CENTER);
        card.addView(t);
        card.addView(space(7));

        TextView sub = text("با یک حساب، سفارش‌ها، گواهی اصالت، دفترچهٔ نشانی و مشاورهٔ اختصاصی شما یک‌جا می‌مانَد — دیگر لازم نیست چیزی را دستی پیگیری کنید.",
                10.6f, TEXT_ON_DARK_MUTED, false);
        sub.setGravity(Gravity.CENTER);
        sub.setLineSpacing(dp(4), 1f);
        card.addView(sub);
        card.addView(space(16));

        card.addView(primaryButton("ساخت حساب در یک دقیقه", Luxury3DIconView.ICON_USERS, new Runnable() {
            @Override public void run() { startSignup(); }
        }));
        card.addView(space(8));
        card.addView(ghostDarkButton("قبلاً حساب دارم — ورود", new Runnable() {
            @Override public void run() { showLoginDialog(); }
        }));
        card.addView(space(10));

        TextView note = text("ورود با کد پیامکی انجام می‌شود؛ رمز شما هرگز روی سرور نگه‌داری نمی‌شود.",
                9f, COL_TEXT_FAINT, false);
        note.setGravity(Gravity.CENTER);
        card.addView(note);
        return card;
    }

    private View accountBenefitsCard() {
        LinearLayout card = whiteCard();
        card.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_CROWN, "با حساب اسیه چه چیزی به دست می‌آورید؟"));
        card.addView(space(9));
        String[][] rows = {
                {"پیگیری خودکار سفارش", "از ثبت تا تحویل، پیامک وضعیت برای شما ارسال می‌شود."},
                {"دفترچهٔ نشانی", "نشانی‌ها یک‌بار ثبت می‌شوند و در تسویه خودکار پر می‌شوند."},
                {"گواهی اصالت دیجیتال", "برای هر خرید، گواهی با سریال TTAC در حساب شما می‌ماند."},
                {"یادآور سفارش مجدد", "پیش از تمام‌شدن محصول، یادآوری هوشمند دریافت می‌کنید."},
                {"هدیهٔ تولد", "در روز تولد، کوپن اختصاصی برای شما فعال می‌شود."}
        };
        final int[] signetIcons = { Luxury3DIconView.ICON_TRUCK, Luxury3DIconView.ICON_DOC,
                Luxury3DIconView.ICON_SHIELD, Luxury3DIconView.ICON_BELL,
                Luxury3DIconView.ICON_GIFT };
        for (int ri = 0; ri < rows.length; ri++) {
            String[] r = rows[ri];
            LinearLayout line = row(Gravity.TOP);
            line.setPadding(0, dp(8), 0, dp(8));
            FrameLayout signet = new FrameLayout(this);
            signet.setLayoutParams(new LinearLayout.LayoutParams(dp(24), dp(24)));
            signet.addView(new Luxury3DIconView(this, signetIcons[ri], true),
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            LinearLayout signetBox = new LinearLayout(this);
            signetBox.setOrientation(LinearLayout.VERTICAL);
            signetBox.setPadding(0, dp(1), dp(9), 0);
            signetBox.addView(signet);
            line.addView(signetBox);
            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.addView(text(r[0], 10.8f, COL_TEXT_MAIN, true));
            TextView d = text(r[1], 9.6f, COL_TEXT_MUTED, false);
            d.setLineSpacing(dp(3), 1f);
            d.setPadding(0, dp(2), 0, 0);
            texts.addView(d);
            line.addView(texts, weight(1f));
            card.addView(line);
        }
        return card;
    }

    private View guestTrackingCard() {
        LinearLayout card = whiteCard();
        card.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_TRUCK, "پیگیری سفارش بدون حساب"));
        card.addView(space(6));
        TextView hint = text("شمارهٔ سفارش خود را وارد کنید؛ وضعیت و کد رهگیری را نشان می‌دهیم.",
                9.8f, COL_TEXT_MUTED, false);
        hint.setLineSpacing(dp(3), 1f);
        card.addView(hint);
        card.addView(space(10));

        final EditText input = makeInput("مثلاً AS-1405-0142", InputType.TYPE_CLASS_TEXT, false);
        card.addView(input);
        card.addView(space(10));

        final LinearLayout result = new LinearLayout(this);
        result.setOrientation(LinearLayout.VERTICAL);
        card.addView(result);

        card.addView(primaryButton("نمایش وضعیت سفارش", Luxury3DIconView.ICON_SEARCH, new Runnable() {
            @Override public void run() {
                result.removeAllViews();
                String q = input.getText().toString().trim();
                Models.OrderRecord o = q.isEmpty() ? null : db.getOrderByNumber(q);
                if (o == null) {
                    result.addView(text("سفارشی با این شماره پیدا نشد.", 10f, COL_DANGER, false));
                    return;
                }
                result.addView(space(6));
                result.addView(resultLine("وضعیت", PersianFormatter.statusLabelFa(o.status)));
                result.addView(resultLine("مبلغ", PersianFormatter.formatToman(o.totalAmount)));
                if (o.trackingCode != null && !o.trackingCode.isEmpty()) {
                    result.addView(resultLine("کد رهگیری", ltr(o.trackingCode)));
                }
            }
        }));
        return card;
    }

    private void startSignup() {
        final EditText mobile = makeInput("شماره موبایل ۱۱ رقمی", InputType.TYPE_CLASS_PHONE, true);
        mobile.setText(profileMobile);

        LinearLayout box = dialogBox();
        box.addView(dialogTitle("ساخت حساب اسیه", "گام ۱ از ۲ — تأیید شمارهٔ موبایل"));
        box.addView(mobile);
        box.addView(space(10));
        TextView hint = text("کد تأیید ۵ رقمی برای شما پیامک می‌شود.", 9.6f, COL_TEXT_MUTED, false);
        box.addView(hint);
        box.addView(space(14));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ارسال کد تأیید", Luxury3DIconView.ICON_SMS, new Runnable() {
            @Override public void run() {
                String m = mobile.getText().toString().trim();
                if (!SmsGateway.isValidMobile(m)) {
                    toast("شمارهٔ موبایل معتبر نیست.");
                    return;
                }
                if (db.accountExists(m)) {
                    dialog.dismiss();
                    showLoginDialog();
                    toast("این شماره قبلاً ثبت‌نام کرده است؛ وارد شوید.");
                    return;
                }
                dialog.dismiss();
                askOtpThen("signup", m, new OtpCallback() {
                    @Override public void onVerified(String verifiedMobile) {
                        showSignupDetails(verifiedMobile);
                    }
                });
            }
        }));
        box.addView(space(8));
        box.addView(ghostButton("انصراف", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    private void showSignupDetails(final String mobile) {
        final EditText name = makeInput("نام و نام خانوادگی", InputType.TYPE_CLASS_TEXT, false);
        final EditText pin = makeInput("رمز ۵ رقمی برای ورود", InputType.TYPE_CLASS_NUMBER, true);
        final EditText birthday = makeInput("تاریخ تولد (اختیاری) مثلاً ۱۳۷۰/۰۵/۱۲", InputType.TYPE_CLASS_TEXT, false);

        final String[] skins = {"نامشخص", "خشک", "چرب", "مختلط", "حساس"};
        final int[] chosen = {0};
        LinearLayout skinRow = row(Gravity.CENTER_VERTICAL);
        skinRow.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        final TextView[] chips = new TextView[skins.length];
        for (int i = 0; i < skins.length; i++) {
            final int idx = i;
            final TextView chip = text(skins[i], 9.4f, i == 0 ? COL_OBSIDIAN : COL_TEXT_MUTED, i == 0);
            chip.setPadding(dp(9), dp(5), dp(9), dp(5));
            chip.setBackground(i == 0 ? roundedShape(COL_GOLD_LIGHT, dp(11), 0, 0)
                    : roundedShape(COL_IVORY_BG, dp(11), COL_BORDER, dp(1)));
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.setMarginEnd(dp(5));
            chip.setLayoutParams(clp);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    chosen[0] = idx;
                    for (int k = 0; k < chips.length; k++) {
                        boolean on = k == idx;
                        chips[k].setTextColor(on ? COL_OBSIDIAN : COL_TEXT_MUTED);
                        chips[k].setTypeface(houseTypeface(on));
                        chips[k].setBackground(on ? roundedShape(COL_GOLD_LIGHT, dp(11), 0, 0)
                                : roundedShape(COL_IVORY_BG, dp(11), COL_BORDER, dp(1)));
                    }
                }
            });
            chips[i] = chip;
            skinRow.addView(chip);
        }

        final boolean[] optin = {true};

        LinearLayout box = dialogBox();
        box.addView(dialogTitle("تکمیل پروفایل", "گام ۲ از ۲ — شماره تأیید شد: " + SmsGateway.maskedMobile(mobile)));
        box.addView(name);
        box.addView(space(9));
        box.addView(pin);
        box.addView(space(9));
        box.addView(birthday);
        box.addView(space(11));
        box.addView(text("نوع پوست شما", 9.4f, COL_TEXT_MUTED, false));
        box.addView(space(6));
        android.widget.HorizontalScrollView skinScroll = new android.widget.HorizontalScrollView(this);
        skinScroll.addView(skinRow);
        box.addView(skinScroll);
        box.addView(space(10));

        final TextView optinChip = text("✓ دریافت پیامک‌های سفارش و یادآورها", 9.4f, COL_OBSIDIAN, true);
        optinChip.setPadding(dp(10), dp(6), dp(10), dp(6));
        optinChip.setBackground(roundedShape(COL_GOLD_LIGHT, dp(11), 0, 0));
        optinChip.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                optin[0] = !optin[0];
                optinChip.setText(optin[0] ? "✓ دریافت پیامک‌های سفارش و یادآورها"
                        : "دریافت پیامک غیرفعال — فقط اعلان درون‌برنامه‌ای");
                optinChip.setTextColor(optin[0] ? COL_OBSIDIAN : COL_TEXT_MUTED);
                optinChip.setBackground(optin[0] ? roundedShape(COL_GOLD_LIGHT, dp(11), 0, 0)
                        : roundedShape(COL_IVORY_BG, dp(11), COL_BORDER, dp(1)));
            }
        });
        box.addView(optinChip);
        box.addView(space(14));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ساخت حساب", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() {
                String error = AccountStore.register(MainActivity.this, db, mobile,
                        name.getText().toString(), pin.getText().toString(),
                        skins[chosen[0]], birthday.getText().toString().trim(), optin[0]);
                if (error != null) {
                    toast(error);
                    return;
                }
                Models.AccountRecord acc = db.getAccount(mobile);
                profileName = AccountStore.displayName(acc);
                profileMobile = SmsGateway.normalizeMobile(mobile);
                getSharedPreferences(PREF_THEME, MODE_PRIVATE).edit()
                        .putString("profile_name", profileName)
                        .putString("profile_mobile", profileMobile).apply();

                if (optin[0]) {
                    java.util.Map<String, String> v = new java.util.HashMap<>();
                    v.put("name", profileName);
                    v.put("store", "اسیه استور");
                    v.put("owner", "خانم زنگنه");
                    SmsGateway.notify(MainActivity.this, db, profileMobile, "welcome", v);
                }

                dialog.dismiss();
                toast("حساب شما ساخته شد. خوش آمدید.");
                renderCurrentScreen();
            }
        }));
        box.addView(space(8));
        box.addView(ghostButton("انصراف", new Runnable() {
            @Override public void run() { dialog.dismiss(); }
        }));
        showRoundedDialog(dialog);
    }

    private void showLoginDialog() {
        final EditText mobile = makeInput("شماره موبایل ثبت‌شده", InputType.TYPE_CLASS_PHONE, true);
        mobile.setText(profileMobile);
        final EditText pin = makeInput("رمز ۵ رقمی", InputType.TYPE_CLASS_NUMBER, true);

        LinearLayout box = dialogBox();
        box.addView(dialogTitle("ورود به حساب", "با شمارهٔ موبایل و رمز خود وارد شوید"));
        box.addView(mobile);
        box.addView(space(9));
        box.addView(pin);
        box.addView(space(12));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ورود به حساب", Luxury3DIconView.ICON_LOCK, new Runnable() {
            @Override public void run() {
                String error = AccountStore.login(MainActivity.this, db,
                        mobile.getText().toString(), pin.getText().toString());
                if (error != null) {
                    toast(error);
                    return;
                }
                syncProfileFromAccount();
                dialog.dismiss();
                toast("خوش آمدید.");
                renderCurrentScreen();
            }
        }));
        box.addView(space(8));
        box.addView(ghostButton("رمز را فراموش کرده‌ام", new Runnable() {
            @Override public void run() {
                String m = mobile.getText().toString().trim();
                if (!db.accountExists(m)) {
                    toast("ابتدا شماره را وارد کنید.");
                    return;
                }
                dialog.dismiss();
                askOtpThen("reset", m, new OtpCallback() {
                    @Override public void onVerified(String verifiedMobile) { showPinReset(verifiedMobile); }
                });
            }
        }));
        showRoundedDialog(dialog);
    }

    private void showPinReset(final String mobile) {
        final EditText pin = makeInput("رمز تازه (۵ تا ۸ رقم)", InputType.TYPE_CLASS_NUMBER, true);
        LinearLayout box = dialogBox();
        box.addView(dialogTitle("تعیین رمز تازه", "شماره " + SmsGateway.maskedMobile(mobile) + " تأیید شد"));
        box.addView(pin);
        box.addView(space(12));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ذخیرهٔ رمز", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() {
                String error = AccountStore.resetPin(db, mobile, pin.getText().toString());
                if (error != null) {
                    toast(error);
                    return;
                }
                AccountStore.signIn(MainActivity.this, mobile);
                syncProfileFromAccount();
                dialog.dismiss();
                toast("رمز تازه ثبت شد؛ وارد شدید.");
                renderCurrentScreen();
            }
        }));
        showRoundedDialog(dialog);
    }

    public interface OtpCallback {
        void onVerified(String mobile);
    }

    /** Sends a code (through the real gateway when configured) and asks for it. */
    private void askOtpThen(final String purpose, final String mobile, final OtpCallback callback) {
        final String code = db.issueOtp(mobile, purpose);

        java.util.Map<String, String> v = new java.util.HashMap<>();
        v.put("code", PersianFormatter.toPersianDigits(code));
        SmsGateway.notify(this, db, mobile, "otp", v);

        final EditText input = makeInput("کد ۵ رقمی پیامک‌شده", InputType.TYPE_CLASS_NUMBER, true);

        LinearLayout box = dialogBox();
        box.addView(dialogTitle("تأیید شماره", "کد برای " + SmsGateway.maskedMobile(mobile) + " ارسال شد"));
        box.addView(text("اگر درگاه پیامک تنظیم نشده باشد، کد در همین صفحه نمایش داده می‌شود تا ثبت‌نام متوقف نشود.",
                9.2f, COL_TEXT_FAINT, false));
        box.addView(space(8));
        box.addView(input);

        boolean sandbox = SmsGateway.PROVIDER_SANDBOX.equals(db.getSetting("sms_provider", "sandbox"));
        if (sandbox) {
            box.addView(space(8));
            TextView peek = text("کد آزمایشی: " + PersianFormatter.toPersianDigits(code), 10f, COL_GOLD_DEEP, true);
            peek.setPadding(dp(10), dp(7), dp(10), dp(7));
            peek.setBackground(roundedShape(COL_IVORY_BG, dp(10), COL_BORDER, dp(1)));
            box.addView(peek);
        }
        box.addView(space(12));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();

        final Runnable verify = new Runnable() {
            @Override public void run() {
                String error = db.verifyOtp(mobile, purpose, input.getText().toString());
                if (error != null) {
                    toast(error);
                    return;
                }
                dialog.dismiss();
                callback.onVerified(SmsGateway.normalizeMobile(mobile));
            }
        };

        box.addView(primaryButton("تأیید کد", Luxury3DIconView.ICON_SHIELD, verify));
        box.addView(space(8));
        box.addView(ghostButton("ارسال دوبارهٔ کد", new Runnable() {
            @Override public void run() {
                dialog.dismiss();
                askOtpThen(purpose, mobile, callback);
            }
        }));
        showRoundedDialog(dialog);
    }

    private LinearLayout dialogBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(18));
        box.setBackground(roundedShape(COL_WHITE, dp(22), COL_BORDER, dp(1)));
        return box;
    }

    private LinearLayout dialogTitle(String title, String subtitle) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, 0, 0, dp(14));
        box.addView(text(title, 15f, COL_TEXT_MAIN, true));
        box.addView(space(3));
        TextView s = text(subtitle, 9.8f, COL_TEXT_MUTED, false);
        s.setLineSpacing(dp(3), 1f);
        box.addView(s);
        return box;
    }

    private void syncProfileFromAccount() {
        Models.AccountRecord acc = db.getAccount(AccountStore.sessionMobile(this));
        if (acc == null) return;
        profileName = AccountStore.displayName(acc);
        profileMobile = acc.mobile;
        getSharedPreferences(PREF_THEME, MODE_PRIVATE).edit()
                .putString("profile_name", profileName)
                .putString("profile_mobile", profileMobile).apply();
    }

    /** Per-channel consent, editable any time — real transactional messaging needs it. */
    private View smsPreferencesCard() {
        final Models.AccountRecord acc = db.getAccount(AccountStore.sessionMobile(this));
        if (acc == null) return whiteCard();

        LinearLayout card = whiteCard();
        card.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_BELL, "ترجیح پیامک و اعلان‌ها"));
        card.addView(space(7));
        TextView hint = text("فقط پیام‌های مربوط به سفارش و یادآورهای انتخابی شما ارسال می‌شود.",
                9.6f, COL_TEXT_MUTED, false);
        hint.setLineSpacing(dp(3), 1f);
        card.addView(hint);
        card.addView(space(11));

        final boolean[] on = {acc.smsOptin};
        final TextView toggle = text(on[0] ? "✓ پیامک فعال است" : "پیامک خاموش است",
                10f, on[0] ? COL_SUCCESS : COL_TEXT_MUTED, true);
        toggle.setPadding(dp(12), dp(8), dp(12), dp(8));
        toggle.setGravity(Gravity.CENTER);
        toggle.setBackground(roundedShape(on[0] ? COL_SUCCESS_SOFT : COL_IVORY_BG, dp(11),
                on[0] ? SURF_SUCCESS_LINE : COL_BORDER, dp(1)));
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                on[0] = !on[0];
                db.updateAccountProfile(acc.mobile, acc.name, acc.email, acc.skinType, acc.birthday, on[0]);
                toggle.setText(on[0] ? "✓ پیامک فعال است" : "پیامک خاموش است");
                toggle.setTextColor(on[0] ? COL_SUCCESS : COL_TEXT_MUTED);
                toggle.setBackground(roundedShape(on[0] ? COL_SUCCESS_SOFT : COL_IVORY_BG, dp(11),
                        on[0] ? SURF_SUCCESS_LINE : COL_BORDER, dp(1)));
                toast(on[0] ? "از این پس پیامک‌های سفارش را دریافت می‌کنید."
                        : "پیامک‌ها خاموش شد؛ اعلان‌ها در حساب شما می‌ماند.");
            }
        });
        card.addView(toggle);
        card.addView(space(10));
        card.addView(resultLine("شمارهٔ دریافت", ltr(SmsGateway.maskedMobile(acc.mobile))));
        card.addView(resultLine("نوع پوست ثبت‌شده", acc.skinType));
        return card;
    }

    private View profileCard() {
        final Models.AccountRecord acc = db.getAccountWithStats(AccountStore.sessionMobile(this));
        final String name = AccountStore.displayName(acc);
        final String mobile = acc == null ? profileMobile : acc.mobile;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_OBSIDIAN, dp(22), lineGold(0x45), dp(1)));
        card.setPadding(dp(17), dp(16), dp(17), dp(16));

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        FrameLayout crest = iconMedallion(Luxury3DIconView.ICON_MEDAL, 44, Luxury3DIconView.PEDESTAL_GOLD);
        head.addView(crest);
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(11), 0, dp(11), 0);
        texts.addView(text(name, 13f, TEXT_ON_DARK, true));
        texts.addView(text(SmsGateway.maskedMobile(mobile), 9.6f, TEXT_ON_DARK_MUTED, false));
        head.addView(texts, weight(1f));

        TextView edit = text("ویرایش", 9.4f, COL_OBSIDIAN, true);
        edit.setPadding(dp(11), dp(7), dp(11), dp(7));
        edit.setBackground(roundedShape(COL_GOLD_LIGHT, dp(10), 0, 0));
        edit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showProfileDialog(); }
        });
        head.addView(edit);
        card.addView(head);

        card.addView(space(11));
        LinearLayout stats = row(Gravity.CENTER_VERTICAL);
        stats.addView(briefStat("مجموع خرید", PersianFormatter.formatToman(acc == null ? 0 : acc.totalSpent)), weight(1f));
        stats.addView(briefStat("سفارش‌ها", PersianFormatter.toPersianDigits(
                String.valueOf(acc == null ? 0 : acc.ordersCount)) + " مورد"), weight(1f));
        card.addView(stats);

        // tier + exit row
        card.addView(space(11));
        LinearLayout foot = row(Gravity.CENTER_VERTICAL);
        String tier = acc == null ? "عضو تازه" : vipTierLabel(acc.totalSpent);
        TextView tierChip = text(tier, 9f, COL_GOLD_LIGHT, true);
        tierChip.setPadding(dp(10), dp(5), dp(10), dp(5));
        tierChip.setBackground(roundedShape(lineGold(0x22), dp(10), lineGold(0x55), dp(1)));
        foot.addView(tierChip, weight(1f));

        TextView out = text("خروج از حساب", 9f, TEXT_ON_DARK_FAINT, false);
        out.setPadding(dp(10), dp(5), dp(10), dp(5));
        out.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AccountStore.signOut(MainActivity.this);
                toast("از حساب خارج شدید.");
                renderCurrentScreen();
            }
        });
        foot.addView(out);
        card.addView(foot);
        return card;
    }

    private String vipTierLabel(long spend) {
        if (spend >= 15_000_000L) return "عضو الماس اسیه";
        if (spend >= 5_000_000L) return "عضو طلایی اسیه";
        if (spend > 0) return "عضو نقره‌ای اسیه";
        return "عضو تازه";
    }

    private void showProfileDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(18));
        box.setBackgroundColor(COL_WHITE);
        box.addView(text("مشخصات شما", 14f, COL_TEXT_MAIN, true));
        box.addView(space(4));
        box.addView(text("این اطلاعات در تسویهٔ سفارش خودکار پر می‌شود.", 9.8f, COL_TEXT_MUTED, false));
        box.addView(space(12));

        final EditText name = makeInput("نام و نام خانوادگی", InputType.TYPE_CLASS_TEXT, false);
        name.setText(profileName);
        final EditText mobile = makeInput("شماره موبایل ۱۱ رقمی", InputType.TYPE_CLASS_PHONE, true);
        mobile.setText(profileMobile);
        box.addView(name);
        box.addView(space(9));
        box.addView(mobile);
        box.addView(space(14));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ذخیرهٔ مشخصات", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() {
                profileName = name.getText().toString().trim();
                profileMobile = PersianFormatter.toAsciiDigits(mobile.getText().toString()).trim();
                getSharedPreferences(PREF_THEME, MODE_PRIVATE).edit()
                        .putString("profile_name", profileName)
                        .putString("profile_mobile", profileMobile).apply();
                Models.AccountRecord existing = db.getAccount(AccountStore.sessionMobile(MainActivity.this));
                if (existing != null) {
                    db.updateAccountProfile(existing.mobile, profileName, existing.email,
                            existing.skinType, existing.birthday, existing.smsOptin);
                }
                dialog.dismiss();
                toast("مشخصات شما ذخیره شد.");
                renderCurrentScreen();
            }
        }));
        showRoundedDialog(dialog);
    }

    private View notificationsCard() {
        List<Models.NotificationRecord> list = db.getNotifications();
        if (list.isEmpty()) return null;
        LinearLayout card = whiteCard();
        card.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_BELL, "اعلان‌های شما"));
        int shown = 0;
        for (Models.NotificationRecord n : list) {
            if (shown >= 3) break;
            card.addView(space(9));
            LinearLayout line = row(Gravity.CENTER_VERTICAL);
            line.addView(iconMedallion(n.icon, 26, Luxury3DIconView.PEDESTAL_IVORY));
            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.setPadding(dp(9), 0, dp(9), 0);
            texts.addView(text(n.title, 10.6f, COL_TEXT_MAIN, true));
            TextView body = text(n.body, 9.4f, COL_TEXT_MUTED, false);
            body.setMaxLines(2);
            body.setEllipsize(android.text.TextUtils.TruncateAt.END);
            texts.addView(body);
            line.addView(texts, weight(1f));
            card.addView(line);
            shown++;
        }
        return card;
    }

    private View ordersCard() {
        // v6.8 · a member sees her own ledger, never the whole shop's
        Models.AccountRecord me = db.getAccount(AccountStore.sessionMobile(this));
        List<Models.OrderRecord> all = db.getOrders("all");
        String mine = SmsGateway.normalizeMobile(me == null ? profileMobile : me.mobile);
        List<Models.OrderRecord> orders = new ArrayList<>();
        for (Models.OrderRecord o : all) {
            if (mine.isEmpty() || SmsGateway.normalizeMobile(o.customerMobile).equals(mine)) orders.add(o);
        }
        LinearLayout card = whiteCard();
        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        head.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_DOC, "دفترچهٔ سفارش‌ها"), weight(1f));
        head.addView(text(PersianFormatter.toPersianDigits(String.valueOf(orders.size())) + " سفارش",
                9f, COL_TEXT_FAINT, false));
        card.addView(head);

        if (orders.isEmpty()) {
            card.addView(space(8));
            card.addView(text("هنوز سفارشی ثبت نشده است. اولین انتخاب شما این‌جا ثبت می‌شود.", 10f, COL_TEXT_MUTED, false));
            return card;
        }

        int shown = 0;
        for (final Models.OrderRecord o : orders) {
            if (shown >= 4) break;
            card.addView(space(10));
            View divider = new View(this);
            divider.setBackgroundColor(SURF_DIVIDER);
            card.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
            card.addView(space(9));

            LinearLayout line = row(Gravity.CENTER_VERTICAL);
            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.addView(text(ltr(o.orderNumber), 10.8f, COL_TEXT_MAIN, true));
            texts.addView(text(o.createdAtLabel, 9f, COL_TEXT_FAINT, false));
            line.addView(texts, weight(1f));
            line.addView(text(PersianFormatter.formatToman(o.totalAmount), 10.4f, COL_TEXT_MAIN, true));
            card.addView(line);

            card.addView(space(6));
            LinearLayout actions = row(Gravity.CENTER_VERTICAL);
            actions.addView(statusPill(o.status));
            actions.addView(spaceH(8));
            TextView inv = text("فاکتور", 9.2f, COL_GOLD_DEEP, true);
            inv.setPadding(dp(10), dp(6), dp(10), dp(6));
            inv.setBackground(roundedShape(COL_IVORY_BG, dp(9), COL_BORDER, dp(1)));
            inv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    exportInvoicePng(o);
                    return true;
                }
            });
            inv.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { shareInvoice(o); }
            });
            actions.addView(inv);
            TextView again = text("سفارش مجدد", 9.2f, COL_OBSIDIAN, true);
            again.setPadding(dp(10), dp(6), dp(10), dp(6));
            again.setBackground(roundedShape(COL_GOLD_LIGHT, dp(9), 0, 0));
            again.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { reorder(o); }
            });
            actions.addView(spaceH(8));
            actions.addView(again);
            card.addView(actions);
            shown++;
        }
        return card;
    }

    /** Re-adds every still-available line of a past order to the bag. */
    private void reorder(Models.OrderRecord order) {
        tap();
        int added = 0;
        for (Models.OrderItemRecord item : order.items) {
            Models.Product p = db.getProductById(item.productId);
            if (p != null && p.stock > 0) {
                addToCart(p);
                added++;
            }
        }
        if (added == 0) {
            toast("کالاهای این سفارش فعلاً موجود نیستند.");
            return;
        }
        customerTab = TAB_BAG;
        renderCurrentScreen();
    }

    private View addressBookCard() {
        List<Models.AddressRecord> list = db.getAddresses();
        LinearLayout card = whiteCard();
        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        head.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_GLOBE, "دفترچهٔ نشانی"), weight(1f));
        TextView add = text("+ نشانی تازه", 9.6f, COL_GOLD_DEEP, true);
        add.setPadding(dp(9), dp(5), dp(9), dp(5));
        add.setBackground(roundedShape(SURF_CHIP, dp(9), SURF_CHIP_LINE, dp(1)));
        add.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showAddressDialog(null); }
        });
        head.addView(add);
        card.addView(head);

        if (list.isEmpty()) {
            card.addView(space(8));
            card.addView(text("نشانی‌ای ذخیره نشده است؛ با یک‌بار ثبت، تسویهٔ بعدی سریع‌تر می‌شود.", 10f, COL_TEXT_MUTED, false));
            return card;
        }

        for (final Models.AddressRecord a : list) {
            card.addView(space(9));
            View divider = new View(this);
            divider.setBackgroundColor(SURF_DIVIDER);
            card.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
            card.addView(space(8));

            LinearLayout line = row(Gravity.CENTER_VERTICAL);
            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            LinearLayout titleRow = row(Gravity.CENTER_VERTICAL);
            titleRow.addView(text(a.label, 10.6f, COL_TEXT_MAIN, true));
            if (a.isDefault) {
                TextView star = text("پیش‌فرض", 8.4f, COL_GOLD_DEEP, true);
                star.setPadding(dp(7), dp(2), dp(7), dp(2));
                star.setBackground(roundedShape(SURF_CHIP, dp(7), SURF_CHIP_LINE, dp(1)));
                star.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                slp.setMargins(dp(7), 0, 0, 0);
                star.setLayoutParams(slp);
                titleRow.addView(star);
            }
            texts.addView(titleRow);
            TextView detail = text(a.name + " · " + a.address, 9.4f, COL_TEXT_MUTED, false);
            detail.setMaxLines(2);
            detail.setEllipsize(android.text.TextUtils.TruncateAt.END);
            detail.setPadding(0, dp(3), 0, 0);
            texts.addView(detail);
            line.addView(texts, weight(1f));

            TextView del = text("حذف", 9f, COL_DANGER, true);
            del.setPadding(dp(9), dp(5), dp(9), dp(5));
            del.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    db.deleteAddress(a.id);
                    toast("نشانی حذف شد.");
                    renderCurrentScreen();
                }
            });
            line.addView(del);
            card.addView(line);
        }
        return card;
    }

    private void showAddressDialog(final Models.AddressRecord editing) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(22), dp(22), dp(18));
        box.setBackgroundColor(COL_WHITE);
        box.addView(text("نشانی تازه", 14f, COL_TEXT_MAIN, true));
        box.addView(space(12));

        final EditText label = makeInput("برچسب (خانه، محل کار…)", InputType.TYPE_CLASS_TEXT, false);
        final EditText name = makeInput("نام تحویل‌گیرنده", InputType.TYPE_CLASS_TEXT, false);
        final EditText mobile = makeInput("شماره موبایل", InputType.TYPE_CLASS_PHONE, true);
        final EditText address = makeInput("نشانی کامل پستی", InputType.TYPE_CLASS_TEXT, false);
        if (editing != null) {
            label.setText(editing.label);
            name.setText(editing.name);
            mobile.setText(editing.mobile);
            address.setText(editing.address);
        } else {
            name.setText(profileName);
            mobile.setText(profileMobile);
        }
        box.addView(label); box.addView(space(8));
        box.addView(name); box.addView(space(8));
        box.addView(mobile); box.addView(space(8));
        box.addView(address); box.addView(space(14));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ذخیرهٔ نشانی", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() {
                if (address.getText().toString().trim().length() < 6) {
                    toast("نشانی را کامل‌تر وارد کنید.");
                    return;
                }
                db.addAddress(label.getText().toString(), name.getText().toString(),
                        mobile.getText().toString(), address.getText().toString(), true);
                dialog.dismiss();
                toast("نشانی ذخیره شد.");
                renderCurrentScreen();
            }
        }));
        showRoundedDialog(dialog);
    }

    /** The shareable certificate of authenticity — drawn in-app, shared as text (and image on Q+). */
    private View certificateCard() {
        LinearLayout card = whiteCard();
        card.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_SHIELD, "گواهی اصالت دیجیتال"));
        card.addView(space(5));
        card.addView(text("برای کالای تأییدشدهٔ خود یک گواهی رسمی با سریال TTAC بسازید؛ مناسب انتشار در استوری.",
                9.6f, COL_TEXT_MUTED, false));
        card.addView(space(11));

        final Models.Product target = verifiedProduct != null ? verifiedProduct : findProductBySku("AS-GAT-150");
        if (target == null) return card;
        card.addView(outlineGoldButton("ساخت گواهی برای " + target.nameFa, Luxury3DIconView.ICON_SHIELD,
                new Runnable() {
            @Override public void run() { showCertificate(target); }
        }));
        return card;
    }

    private void showCertificate(final Models.Product p) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(COL_OBSIDIAN);
        box.setPadding(dp(18), dp(18), dp(18), dp(16));

        LinearLayout frame = new LinearLayout(this);
        frame.setOrientation(LinearLayout.VERTICAL);
        frame.setGravity(Gravity.CENTER_HORIZONTAL);
        frame.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(18), COL_METAL, dp(1)));
        frame.setPadding(dp(16), dp(20), dp(16), dp(18));

        frame.addView(iconMedallion(Luxury3DIconView.ICON_SHIELD, 52, Luxury3DIconView.PEDESTAL_GOLD));
        TextView k = text("CERTIFICATE OF AUTHENTICITY", 8.4f, ACCENT_ON_DARK, false);
        tracking(k, 0.22f);
        k.setPadding(0, dp(10), 0, dp(4));
        frame.addView(k);

        TextView brand = text("ASIYE STORE", 17f, TEXT_ON_DARK, true);
        brand.setTypeface(Typeface.SERIF, Typeface.BOLD);
        tracking(brand, 0.12f);
        frame.addView(brand);

        TextView name = text(p.nameFa, 12.4f, COL_GOLD_LIGHT, true);
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, dp(10), 0, dp(10));
        frame.addView(name);

        String[][] rows = {
                {"سریال اصالت", ltr(p.ttacSerial)},
                {"شناسهٔ بچ", ltr(p.batchCode)},
                {"کد کالا", ltr(p.sku)},
                {"اعتبار", p.expiryLabel}
        };
        for (String[] r : rows) {
            LinearLayout line = row(Gravity.CENTER_VERTICAL);
            line.setPadding(0, dp(4), 0, dp(4));
            line.addView(text(r[0], 9f, TEXT_ON_DARK_MUTED, false), weight(1f));
            line.addView(text(r[1], 9.4f, TEXT_ON_DARK, true));
            frame.addView(line);
        }

        frame.addView(space(12));
        TextView seal = text("مهر مجموعه و ضمانت اصالت", 8.8f, ACCENT_ON_DARK, false);
        seal.setGravity(Gravity.CENTER);
        seal.setPadding(dp(10), dp(5), dp(10), dp(5));
        seal.setBackground(roundedShape(lineGold(0x1A), dp(9), lineGold(0x55), dp(1)));
        frame.addView(seal);

        TextView sig = text("مدیریت: سرکار خانم زنگنه · " + OWNER_PHONE_DISPLAY, 8.2f, TEXT_ON_DARK_FAINT, false);
        sig.setGravity(Gravity.CENTER);
        sig.setPadding(0, dp(8), 0, 0);
        frame.addView(sig);

        box.addView(frame);
        box.addView(space(12));

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        LinearLayout actions = row(Gravity.CENTER_VERTICAL);
        actions.addView(primaryButton("اشتراک گواهی", Luxury3DIconView.ICON_SHARE, new Runnable() {
            @Override public void run() {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_SUBJECT, "گواهی اصالت ASIYE STORE");
                send.putExtra(Intent.EXTRA_TEXT, certificateText(p));
                startActivity(Intent.createChooser(send, "ارسال گواهی اصالت"));
            }
        }), weight(1f));
        actions.addView(spaceH(8));
        TextView copy = text("کپی متن", 10.4f, COL_GOLD_LIGHT, true);
        copy.setGravity(Gravity.CENTER);
        copy.setPadding(dp(14), dp(13), dp(14), dp(13));
        copy.setBackground(roundedShape(lineGold(0x1F), dp(12), lineGold(0x66), dp(1)));
        copy.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                android.content.ClipboardManager cm =
                        (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (cm != null) cm.setPrimaryClip(android.content.ClipData.newPlainText("certificate", certificateText(p)));
                toast("متن گواهی کپی شد.");
            }
        });
        actions.addView(copy);
        box.addView(actions);
        showRoundedDialog(dialog);
    }

    private String certificateText(Models.Product p) {
        return "گواهی اصالت ASIYE STORE\n"
                + "کالا: " + p.nameFa + "\n"
                + "سریال اصالت: " + p.ttacSerial + "\n"
                + "شناسهٔ بچ: " + p.batchCode + "\n"
                + "کد کالا: " + p.sku + "\n"
                + "اعتبار: " + p.expiryLabel + "\n"
                + "مجموعهٔ تخصصی پوست، مو و زیبایی به مدیریت سرکار خانم زنگنه\n"
                + OWNER_PHONE_DISPLAY + " · " + INSTAGRAM_HANDLE;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  v6.4 · ROUTINE BUNDLE · RECENTLY VIEWED · BEAUTY CABINET · MORNING BRIEF
    // ══════════════════════════════════════════════════════════════════════

    private static final String[][] ROUTINE_BUNDLES = {
            {"AS-GAT-150", "AS-GAT-050", "روتین ضدلک و لیفتینگ گاتیو"},
            {"AS-ORD-240", "AS-MRG-500", "روتین پاک‌سازی و تقویت مو"}
    };

    /** One calm card that sells a two-step routine at 8% off — the quiet upsell. */
    private View bundleCard() {
        final String[] skus = ROUTINE_BUNDLES[bundleIdx];
        Models.Product a = findProductBySku(skus[0]);
        Models.Product b = findProductBySku(skus[1]);
        if (a == null || b == null) return new View(this);

        long full = a.salePrice + b.salePrice;
        long bundlePrice = Math.round(full * 0.92);

        LinearLayout card = whiteCard();
        card.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_BOX, "بستهٔ روتین امضایی"));

        LinearLayout chips = row(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < ROUTINE_BUNDLES.length; i++) {
            final int idx = i;
            boolean active = bundleIdx == i;
            TextView chip = text(ROUTINE_BUNDLES[i][2], 9.4f, active ? COL_OBSIDIAN : COL_TEXT_MUTED, active);
            chip.setGravity(Gravity.CENTER);
            chip.setSingleLine(true);
            chip.setPadding(dp(6), dp(9), dp(6), dp(9));
            chip.setBackground(active
                    ? roundedShape(COL_GOLD_LIGHT, dp(13), COL_GOLD_LIGHT, dp(1))
                    : roundedShape(COL_IVORY_BG, dp(13), COL_BORDER, dp(1)));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    bundleIdx = idx;
                    renderCurrentScreen();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            chips.addView(chip, lp);
        }
        card.addView(space(10));
        card.addView(chips);
        card.addView(space(11));

        card.addView(bundleLine(a));
        card.addView(space(6));
        card.addView(bundleLine(b));
        card.addView(space(11));

        LinearLayout priceRow = row(Gravity.CENTER_VERTICAL);
        LinearLayout prices = new LinearLayout(this);
        prices.setOrientation(LinearLayout.VERTICAL);
        TextView now = text(PersianFormatter.formatToman(bundlePrice), 13.5f, COL_TEXT_MAIN, true);
        TextView before = text(PersianFormatter.formatToman(full), 9.6f, COL_TEXT_FAINT, false);
        before.setPaintFlags(before.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        prices.addView(now);
        prices.addView(before);
        priceRow.addView(prices, weight(1f));

        TextView save = text("صرفه‌جویی " + PersianFormatter.formatToman(full - bundlePrice), 9.2f, COL_SUCCESS, true);
        save.setPadding(dp(9), dp(5), dp(9), dp(5));
        save.setBackground(roundedShape(COL_SUCCESS_SOFT, dp(9), SURF_SUCCESS_LINE, dp(1)));
        priceRow.addView(save);
        card.addView(priceRow);

        card.addView(space(11));
        card.addView(primaryButton("افزودن بسته به سبد", Luxury3DIconView.ICON_BAG, new Runnable() {
            @Override public void run() {
                Models.Product first = findProductBySku(skus[0]);
                Models.Product second = findProductBySku(skus[1]);
                if (first == null || second == null) return;
                addToCart(first);
                addToCart(second);
                long discount = db.evaluateCouponDiscount("BUNDLE8", calculateCartSubtotal());
                if (discount > 0) {
                    appliedCouponCode = "BUNDLE8";
                    appliedCouponValue = discount;
                    toast("بسته به سبد اضافه شد و ۸٪ تخفیف بسته اعمال گردید.");
                } else {
                    toast("بسته به سبد اضافه شد.");
                }
                customerTab = TAB_BAG;
                renderCurrentScreen();
            }
        }));
        return card;
    }

    private View bundleLine(Models.Product p) {
        LinearLayout line = row(Gravity.CENTER_VERTICAL);
        line.setPadding(0, dp(3), 0, dp(3));
        FrameLayout dot = new FrameLayout(this);
        dot.setLayoutParams(new LinearLayout.LayoutParams(dp(18), dp(18)));
        dot.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_DIAMOND, true),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout dotBox = new LinearLayout(this);
        dotBox.setOrientation(LinearLayout.VERTICAL);
        dotBox.setPadding(0, 0, dp(7), 0);
        dotBox.addView(dot);
        line.addView(dotBox);
        TextView name = text(p.nameFa, 10.6f, TEXT_BODY, false);
        name.setMaxLines(1);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        line.addView(name, weight(1f));
        line.addView(text(PersianFormatter.formatToman(p.salePrice), 9.8f, COL_TEXT_MUTED, false));
        return line;
    }

    /** A quiet shelf of the last few products the customer opened. */
    private View recentlyViewedStrip() {
        List<Models.Product> recent = db.getRecentlyViewed(5, -1L);
        if (recent.isEmpty()) return null;

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_SEARCH, "اخیراً دیده‌اید"));
        wrap.addView(space(9));

        android.widget.HorizontalScrollView scroll = new android.widget.HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout shelf = new LinearLayout(this);
        shelf.setOrientation(LinearLayout.HORIZONTAL);
        shelf.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        for (final Models.Product p : recent) {
            LinearLayout tile = new LinearLayout(this);
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setPadding(dp(7), dp(7), dp(7), dp(9));
            tile.setBackground(roundedShape(COL_WHITE, dp(15), COL_BORDER, dp(1)));
            tile.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    tap();
                    showEditorialProductDialog(p);
                }
            });

            FrameLayout thumb = new FrameLayout(this);
            thumb.setLayoutParams(new LinearLayout.LayoutParams(dp(80), dp(96)));
            thumb.setClipToOutline(true);
            thumb.setBackground(roundedShape(COL_SILK_CREAM, dp(11), 0, 0));
            ImageView img = new ImageView(this);
            Bitmap bmp = loadProductBitmap(p);
            if (bmp != null) img.setImageBitmap(bmp); else img.setImageResource(resolveProductImage(p.imageKey));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.addView(img, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            tile.addView(thumb);

            TextView name = text(p.nameFa, 8.6f, COL_TEXT_MUTED, false);
            name.setMaxLines(2);
            name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            name.setPadding(0, dp(6), 0, 0);
            name.setWidth(dp(80));
            tile.addView(name);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, dp(9), 0);
            shelf.addView(tile, lp);
        }
        scroll.addView(shelf);
        wrap.addView(scroll);
        return wrap;
    }

    /** How long a product realistically lasts, by SKU — the basis of the reorder nudge. */
    private int supplyDays(Models.Product p) {
        String sku = p.sku == null ? "" : p.sku;
        if ("AS-GAT-150".equals(sku)) return 75;
        if ("AS-GAT-050".equals(sku)) return 45;
        if ("AS-LC-104".equals(sku)) return 120;
        if ("AS-HF-300".equals(sku)) return 90;
        if ("AS-ORD-240".equals(sku)) return 70;
        if ("AS-MRG-500".equals(sku)) return 40;
        return 60;
    }

    /** The customer's own cabinet: what they own, how far along it is, when to reorder. */
    private View cabinetCard() {
        List<Long> ids = db.getPurchasedProductIds();
        if (ids.isEmpty()) return null;

        LinearLayout card = whiteCard();
        card.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_LEAF, "کابین زیبایی من"));
        card.addView(space(4));
        card.addView(text("بر پایهٔ خریدهای شما، زمان تقریبی اتمام هر کالا محاسبه می‌شود.",
                9.4f, COL_TEXT_MUTED, false));

        int shown = 0;
        for (long id : ids) {
            if (shown >= 3) break;
            Models.Product p = db.getProductById(id);
            if (p == null) continue;
            long lastMs = db.getLastPurchaseMs(id);
            if (lastMs <= 0L) continue;

            int days = supplyDays(p);
            long usedDays = Math.max(0L, (System.currentTimeMillis() - lastMs) / 86_400_000L);
            int left = (int) Math.max(0L, days - usedDays);
            float ratio = Math.min(1f, usedDays / (float) days);

            card.addView(space(11));
            View divider = new View(this);
            divider.setBackgroundColor(SURF_DIVIDER);
            card.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
            card.addView(space(10));

            LinearLayout head = row(Gravity.CENTER_VERTICAL);
            FrameLayout thumb = new FrameLayout(this);
            thumb.setLayoutParams(new LinearLayout.LayoutParams(dp(38), dp(46)));
            thumb.setClipToOutline(true);
            thumb.setBackground(roundedShape(COL_SILK_CREAM, dp(9), 0, 0));
            ImageView img = new ImageView(this);
            Bitmap bmp = loadProductBitmap(p);
            if (bmp != null) img.setImageBitmap(bmp); else img.setImageResource(resolveProductImage(p.imageKey));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.addView(img, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            head.addView(thumb);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(dp(9), 0, dp(9), 0);
            TextView name = text(p.nameFa, 10.6f, COL_TEXT_MAIN, true);
            name.setMaxLines(1);
            name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            info.addView(name);
            TextView status = text(left <= 0 ? "به پایان رسیده — زمان سفارش مجدد"
                            : (left <= 10 ? "حدود " + PersianFormatter.toPersianDigits(String.valueOf(left)) + " روز تا اتمام"
                            : "حدود " + PersianFormatter.toPersianDigits(String.valueOf(left)) + " روز دیگر کافی است"),
                    9.2f, left <= 10 ? COL_GOLD_DEEP : COL_TEXT_FAINT, left <= 10);
            status.setPadding(0, dp(3), 0, dp(4));
            info.addView(status);

            LinearLayout track = new LinearLayout(this);
            track.setOrientation(LinearLayout.HORIZONTAL);
            track.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            track.setBackground(roundedShape(lineGold(0x22), dp(3), 0, 0));
            View fill = new View(this);
            fill.setBackground(roundedShape(left <= 10 ? COL_GOLD : 0xFF8FA98F, dp(3), 0, 0));
            track.addView(fill, new LinearLayout.LayoutParams(0, dp(5), Math.max(0.02f, ratio)));
            View rest = new View(this);
            track.addView(rest, new LinearLayout.LayoutParams(0, dp(5), Math.max(0.0001f, 1f - ratio)));
            info.addView(track);
            head.addView(info, weight(1f));

            if (left <= 20) {
                TextView again = text("سفارش مجدد", 9f, COL_OBSIDIAN, true);
                again.setGravity(Gravity.CENTER);
                again.setPadding(dp(10), dp(7), dp(10), dp(7));
                again.setBackground(roundedShape(COL_GOLD_LIGHT, dp(10), 0, 0));
                final Models.Product target = p;
                again.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        tap();
                        addToCart(target);
                    }
                });
                head.addView(again);
            }
            card.addView(head);
            shown++;
        }
        return shown == 0 ? null : card;
    }

    /** Executive morning card: yesterday's money, the review queue and the stock alarm. */
    private View morningBriefCard() {
        long now = System.currentTimeMillis();
        long yRevenue = db.getRevenueBetween(now - 86_400_000L, now);
        int yOrders = db.getOrderCountBetween(now - 86_400_000L, now);
        int pending = db.getPendingReviewsCount();
        int lowStock = db.getDashboardSnapshot().lowStockCount;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_OBSIDIAN, dp(22), lineGold(0x45), dp(1)));
        card.setPadding(dp(17), dp(15), dp(17), dp(16));

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        FrameLayout medal = iconMedallion(Luxury3DIconView.ICON_SUN, 34, Luxury3DIconView.PEDESTAL_GOLD);
        head.addView(medal);
        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(10), 0, dp(10), 0);
        titles.addView(text("خلاصهٔ صبح مجموعه", 12.5f, TEXT_ON_DARK, true));
        titles.addView(text("۲۴ ساعت گذشته در یک نگاه", 9.2f, TEXT_ON_DARK_MUTED, false));
        head.addView(titles, weight(1f));
        card.addView(head);

        card.addView(space(12));
        LinearLayout stats = row(Gravity.CENTER_VERTICAL);
        stats.addView(briefStat("فروش امروز", PersianFormatter.formatToman(yRevenue)), weight(1f));
        stats.addView(briefStat("سفارش‌ها", PersianFormatter.toPersianDigits(String.valueOf(yOrders)) + " مورد"), weight(1f));
        card.addView(stats);
        card.addView(space(9));
        LinearLayout stats2 = row(Gravity.CENTER_VERTICAL);
        stats2.addView(briefStat("نظرات در انتظار", PersianFormatter.toPersianDigits(String.valueOf(pending)) + " نظر"), weight(1f));
        stats2.addView(briefStat("کالای کم‌موجود", PersianFormatter.toPersianDigits(String.valueOf(lowStock)) + " کالا"), weight(1f));
        card.addView(stats2);

        card.addView(space(12));
        TextView hint = text(briefReminderOn
                        ? "یادآور روزانه ۹ صبح فعال است."
                        : "برای دریافت یادآور روزانه ۹ صبح، این دکمه را لمس کنید.",
                9.2f, TEXT_ON_DARK_FAINT, false);
        card.addView(hint);
        card.addView(space(8));
        card.addView(ghostGoldButton(briefReminderOn ? "خاموش کردن یادآور روزانه" : "فعال‌سازی یادآور ۹ صبح",
                new Runnable() {
            @Override public void run() { toggleDailyBrief(); }
        }));
        return card;
    }

    private View briefStat(String label, String value) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(0, dp(2), 0, dp(2));
        col.addView(text(label, 8.8f, TEXT_ON_DARK_FAINT, false));
        TextView v = text(value, 12f, COL_GOLD_LIGHT, true);
        v.setSingleLine(true);
        v.setEllipsize(android.text.TextUtils.TruncateAt.END);
        col.addView(v);
        return col;
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
        tracking(eyebrow, 0.08f);
        card.addView(eyebrow);
        card.addView(space(8));

        LinearLayout body = row(Gravity.TOP);
        FrameLayout thumbWrap = new FrameLayout(this);
        thumbWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(96), dp(112)));
        thumbWrap.setClipToOutline(true);
        thumbWrap.setBackground(roundedShape(COL_SILK_CREAM, dp(14), 0, 0));
        ImageView thumb = new ImageView(this);
        Bitmap thumbBitmap = loadProductBitmap(p);
        if (thumbBitmap != null) thumb.setImageBitmap(thumbBitmap); else thumb.setImageResource(resolveProductImage(p.imageKey));
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
        TextView l = text(label, 10.5f, COL_TEXT_MUTED, false);
        l.setSingleLine(true);
        TextView v = text(value == null ? "—" : value, 11.5f, COL_TEXT_MAIN, true);
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
        contentContainer.addView(crestTitle(Luxury3DIconView.ICON_BAG, "سبد خرید اختصاصی شما",
                "پرداخت در ۴ قسط بدون کارمزد، یا ارسال هوشمند سبد در واتس‌اپ مدیریت", "YOUR SELECTION"));
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
        giftCard.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_GIFT, "بسته‌بندی هدیه و کارت دست‌نویس"));
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
        couponCard.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_PERCENT, "کد تخفیف اختصاصی"));
        couponCard.addView(space(8));
        LinearLayout couponRow = row(Gravity.CENTER_VERTICAL);
        detach(bagCouponInput);
        bagCouponInput.setText(appliedCouponCode);
        couponRow.addView(bagCouponInput, weight(1f));
        couponRow.addView(spaceH(8));
        TextView applyBtn = new TextView(this);
        applyBtn.setText(appliedCouponValue > 0 ? "حذف" : "اعمال");
        applyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        applyBtn.setTypeface(houseTypeface(true));
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

        contentContainer.addView(rewardsCard());
        contentContainer.addView(space(12));

        // ── Delivery details ─────────────────────────────────────────────
        LinearLayout infoCard = whiteCard();
        infoCard.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_USERS, "اطلاعات تحویل‌گیرنده"));
        infoCard.addView(space(8));
        // v6.8 · a signed-in member never re-types what the maison already knows
        Models.AccountRecord me = db.getAccount(AccountStore.sessionMobile(this));
        String savedName = me != null && !me.name.trim().isEmpty()
                ? me.name : db.getSetting("customer_name", "");
        String savedMobile = me != null && !me.mobile.trim().isEmpty()
                ? me.mobile : db.getSetting("customer_mobile", "");
        String savedAddress = db.getSetting("customer_address", "");
        if (savedAddress.trim().isEmpty()) {
            Models.AddressRecord def = db.getDefaultAddress();
            if (def != null) savedAddress = def.address;
        }
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
        int tierPercent = db.getTierPercent(checkoutMobile());
        long tierDiscount = Math.round(subtotal * (tierPercent / 100.0));
        long shipping;
        if (courierSelected && db.isCourierEnabled()) {
            shipping = db.getCourierFee();
        } else {
            shipping = subtotal >= db.getFreeShippingThreshold()
                    ? 0L : AsiyeDatabaseHelper.STANDARD_SHIPPING_COST;
        }
        long basePayable = Math.max(0L, subtotal - appliedCouponValue - tierDiscount - appliedGiftValue) + shipping;

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
        summary.setBackground(deepCard(dp(22)));
        summary.setElevation(dp(2));
        summary.setPadding(dp(18), dp(18), dp(18), dp(18));

        TextView summaryTitle = text("صورتحساب اختصاصی", 10f, ACCENT_ON_DARK, false);
        tracking(summaryTitle, 0.14f);
        summary.addView(summaryTitle);
        summary.addView(space(10));
        summary.addView(summaryLine("جمع کالاها", PersianFormatter.formatToman(subtotal), false));
        if (appliedCouponValue > 0) {
            summary.addView(summaryLine("تخفیف کد اختصاصی", "− " + PersianFormatter.formatToman(appliedCouponValue), true));
        }
        if (tierDiscount > 0) {
            summary.addView(summaryLine("تخفیف باشگاه " + db.getTierName(checkoutMobile())
                            + " (" + PersianFormatter.toPersianDigits(String.valueOf(tierPercent)) + "٪)",
                    "− " + PersianFormatter.formatToman(tierDiscount), true));
        }
        if (appliedGiftValue > 0) {
            summary.addView(summaryLine("کارت هدیه " + ltr(appliedGiftCode),
                    "− " + PersianFormatter.formatToman(appliedGiftValue), true));
        }
        summary.addView(summaryLine(courierSelected && db.isCourierEnabled()
                        ? "پیک شهری — " + courierCity : "هزینه ارسال VIP",
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
                    + ") پس از تکمیل سفارش به کیف پول شما اضافه می‌شود.", 9.2f, TEXT_ON_DARK_MUTED, false);
            clubText.setPadding(dp(6), 0, 0, 0);
            clubText.setLineSpacing(dp(2), 1f);
            club.addView(clubText, weight(1f));
            summary.addView(club);
        }

        View divider = new View(this);
        divider.setBackgroundColor(lineGold(0x33));
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        dLp.setMargins(0, dp(12), 0, dp(12));
        summary.addView(divider, dLp);

        LinearLayout totalRow = row(Gravity.CENTER_VERTICAL);
        totalRow.addView(text("مبلغ قابل پرداخت", 12f, TEXT_ON_DARK, true), weight(1f));
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
            instBox.setBackground(roundedShape(lineGold(0x24), dp(12), lineGold(0x66), dp(1)));
            instBox.setPadding(dp(12), dp(10), dp(12), dp(10));
            instBox.addView(text("مدت بازپرداخت · "
                            + PersianFormatter.toPersianDigits(String.valueOf(activePlan.months)) + " قسط",
                    10.5f, TEXT_ON_DARK, false), weight(1f));
            instBox.addView(text("ماهانه " + PersianFormatter.formatToman(monthly), 11.5f, COL_GOLD_LIGHT, true));
            summary.addView(instBox);
            TextView approval = text("تأیید نهایی اقساط پس از بررسی هویت توسط مدیریت مجموعه انجام می‌شود.",
                    9f, TEXT_ON_DARK_FAINT, false);
            approval.setLineSpacing(dp(2), 1f);
            approval.setPadding(0, dp(7), 0, 0);
            summary.addView(approval);
        }
        if (shipping > 0L) {
            summary.addView(space(6));
            summary.addView(text("با خرید " + PersianFormatter.formatToman(
                    db.getFreeShippingThreshold() - subtotal) + " بیشتر، ارسال رایگان می‌شود.",
                    9.5f, SURF_INACTIVE, false));
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
        Bitmap thumbBitmap = loadProductBitmap(item.product);
        if (thumbBitmap != null) thumb.setImageBitmap(thumbBitmap); else thumb.setImageResource(resolveProductImage(item.product.imageKey));
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
                13.5f, COL_TEXT_MAIN, true);
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

        TextView remove = text("حذف", 10.2f, COL_TEXT_MUTED, false);
        remove.setPadding(dp(12), dp(7), dp(12), dp(7));
        remove.setBackground(roundedShape(COL_IVORY_BG, dp(10), COL_BORDER, dp(1)));
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
        wrap.setBackground(roundedShape(SURF_SOFT, dp(22), lineGold(0x66), dp(1)));
        wrap.setElevation(dp(1.4f));
        wrap.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));
        wrap.addView(new Luxury3DIconView(this, iconType, true),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        wrap.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return wrap;
    }

    private View summaryLine(String label, String value, boolean highlight) {
        LinearLayout line = row(Gravity.CENTER_VERTICAL);
        line.setPadding(0, dp(4), 0, dp(4));
        TextView l = text(label, 11f, TEXT_ON_DARK_MUTED, false);
        TextView v = text(value, 11.5f, highlight ? SUCCESS_ON_DARK : TEXT_ON_DARK, highlight);
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
        int tierPercent = db.getTierPercent(checkoutMobile());
        long tierDiscount = Math.round(subtotal * (tierPercent / 100.0));
        boolean courier = courierSelected && db.isCourierEnabled();
        long shipping = courier ? db.getCourierFee()
                : (subtotal >= db.getFreeShippingThreshold()
                        ? 0L : AsiyeDatabaseHelper.STANDARD_SHIPPING_COST);
        String shippingLabel = courier ? "پیک شهری — " + courierCity : "پست پیشتاز VIP";
        long basePayable = Math.max(0L, subtotal - appliedCouponValue - tierDiscount - appliedGiftValue) + shipping;
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
                    vipGiftPackaging, giftMessageDraft, fee, status,
                    tierDiscount + appliedGiftValue,
                    appliedGiftValue,
                    courier ? db.getCourierFee() : -1L,
                    shippingLabel);

            // consume the gift card only once the order really exists
            if (appliedGiftValue > 0 && !appliedGiftCode.isEmpty()) {
                db.redeemGiftCard(appliedGiftCode, appliedGiftValue);
            }

            cart.clear();
            appliedCouponCode = "";
            appliedCouponValue = 0L;
            appliedGiftCode = "";
            appliedGiftValue = 0L;
            courierSelected = false;
            selectedInstallmentId = "";

            if ("pending_payment".equals(order.status)) {
                renderCurrentScreen();
                showGatewayPaymentSheet(order, plan);
            } else {
                notifyOrderPlaced(order);
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
        receipt.addView(resultLine("شماره سفارش", ltr(order.orderNumber)));
        receipt.addView(resultLine("مبلغ قابل پرداخت", PersianFormatter.formatToman(order.totalAmount)));
        receipt.addView(resultLine("پذیرنده", merchant));
        receipt.addView(resultLine("درگاه", gatewayName));
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

    /** v6.8 · the two messages every order earns: the customer's receipt and the owner's alert. */
    private void notifyOrderPlaced(final Models.OrderRecord order) {
        if (order == null) return;
        try {
            java.util.Map<String, String> vars = new java.util.HashMap<>();
            vars.put("name", order.customerName == null || order.customerName.trim().isEmpty()
                    ? "مشتری گرامی" : order.customerName);
            vars.put("order", order.orderNumber);
            vars.put("total", PersianFormatter.formatToman(order.totalAmount));
            vars.put("store", "اسیه استور");
            vars.put("owner", "خانم زنگنه");

            Models.AccountRecord acc = db.getAccount(order.customerMobile);
            if (acc == null || acc.smsOptin) {
                SmsGateway.notify(this, db, order.customerMobile, "order_placed", vars);
            }
            if (db.getSettingBool("auto_owner_alert", true)) {
                String owner = db.getSetting("owner_mobile", "09044200993");
                SmsGateway.notify(this, db, owner, "owner_alert", vars);
            }
        } catch (Throwable ignored) { }
    }

    private void showOrderSuccessDialog(final Models.OrderRecord order) {
        if (order == null) return;
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(24), dp(22), dp(20));
        box.setBackgroundColor(COL_WHITE);
        box.setGravity(Gravity.CENTER_HORIZONTAL);

        // v6.7 · the wax seal that closes the order
        final LuxuryCanvas.WaxSealView seal = new LuxuryCanvas.WaxSealView(this);
        LinearLayout.LayoutParams sealLp = new LinearLayout.LayoutParams(dp(104), dp(104));
        sealLp.setMargins(0, 0, 0, dp(2));
        box.addView(seal, sealLp);
        seal.postDelayed(new Runnable() {
            @Override public void run() { seal.stamp(); }
        }, 180L);
        box.addView(space(10));

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
        receipt.addView(resultLine("شماره سفارش", ltr(order.orderNumber)));
        receipt.addView(resultLine("مبلغ نهایی", PersianFormatter.formatToman(order.totalAmount)));
        receipt.addView(resultLine("گواهی اصالت", ltr(order.certificateCode)));
        receipt.addView(resultLine("روش پرداخت", PersianFormatter.paymentMethodLabelFa(order.paymentMethod)));
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

    /** A label/value pair that stays legible on the deep obsidian or emerald cards. */
    private View darkResultLine(String label, String value) {
        LinearLayout line = row(Gravity.CENTER_VERTICAL);
        line.setPadding(0, dp(4), 0, dp(4));
        TextView l = text(label, 10.5f, TEXT_ON_DARK_MUTED, false);
        l.setSingleLine(true);
        TextView v = text(value == null ? "—" : value, 11.3f, TEXT_ON_DARK, true);
        v.setGravity(Gravity.END);
        v.setSingleLine(true);
        v.setEllipsize(android.text.TextUtils.TruncateAt.END);
        line.addView(l);
        line.addView(v, weight(1f));
        return line;
    }

    /** The quiet text action, tuned for the deep cards. */
    private View ghostDarkButton(String label, final Runnable action) {
        TextView t = text(label, 10.8f, TEXT_ON_DARK_MUTED, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(10), dp(12), dp(10));
        t.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return t;
    }


    // ══════════════════════════════════════════════════════════════════════
    //  4 · CONCIERGE & DESIGNER (مدیریت و ارتباط)
    // ══════════════════════════════════════════════════════════════════════
    private void renderConcierge() {
        contentContainer.addView(crestTitle(Luxury3DIconView.ICON_CROWN, "مدیریت و ارتباط اختصاصی",
                "خط مستقیم مدیریت مجموعه، پیام هوشمند واتس‌اپ و اینستاگرام رسمی", "VIP CONCIERGE"));
        contentContainer.addView(space(14));

        // ── Executive card, set on a whisper of islimi lattice ────────────
        LinearLayout exec = new LinearLayout(this);
        exec.setOrientation(LinearLayout.VERTICAL);
        exec.setBackground(scrimCard(dp(24), 0xE6));
        exec.setElevation(dp(2));
        exec.setPadding(dp(18), dp(18), dp(18), dp(18));

        LinearLayout eyebrowRow = row(Gravity.CENTER_VERTICAL);
        FrameLayout crownWrap = new FrameLayout(this);
        crownWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(30), dp(30)));
        crownWrap.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_CROWN, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TextView eyebrow = text("EXECUTIVE MANAGEMENT & VIP CONCIERGE", 8.5f, ACCENT_ON_DARK, false);
        tracking(eyebrow, 0.14f);
        eyebrow.setTypeface(Typeface.SERIF, Typeface.BOLD);
        eyebrow.setPadding(dp(8), 0, 0, 0);
        eyebrowRow.addView(crownWrap);
        eyebrowRow.addView(eyebrow, weight(1f));
        exec.addView(eyebrowRow);
        exec.addView(space(12));

        exec.addView(text("مدیریت مجموعه: سرکار خانم زنگنه", 17f, TEXT_ON_DARK, true));
        exec.addView(space(6));
        TextView desc = text("برای مشاوره تخصصی انتخاب محصول، استعلام موجودی و ثبت سفارش، از هر یک از راه‌های ارتباطی زیر استفاده فرمایید. کد شناسه اپلیکیشن به صورت خودکار در پیام درج می‌شود.",
                11.3f, TEXT_ON_DARK_MUTED, false);
        desc.setLineSpacing(dp(4), 1f);
        exec.addView(desc);
        exec.addView(space(14));

        // Direct line pill
        LinearLayout pill = row(Gravity.CENTER_VERTICAL);
        pill.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(18), lineGold(0x55), dp(1)));
        pill.setPadding(dp(14), dp(11), dp(12), dp(11));

        LinearLayout pillText = new LinearLayout(this);
        pillText.setOrientation(LinearLayout.VERTICAL);
        TextView pillLabel = text("خط مستقیم مدیریت", 9.5f, TEXT_ON_DARK_FAINT, false);
        TextView pillPhone = new TextView(this);
        pillPhone.setText(OWNER_PHONE_DISPLAY);
        pillPhone.setTextDirection(View.TEXT_DIRECTION_LTR);
        pillPhone.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);
        pillPhone.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        pillPhone.setTextColor(COL_GOLD_LIGHT);
        tracking(pillPhone, 0.04f);
        pillPhone.setIncludeFontPadding(false);
        pillPhone.setPadding(0, dp(2), 0, 0);
        pillText.addView(pillLabel);
        pillText.addView(pillPhone);
        pill.addView(pillText, weight(1f));

        TextView copy = text("کپی", 10.5f, COL_GOLD_LIGHT, true);
        copy.setGravity(Gravity.CENTER);
        copy.setPadding(dp(12), dp(8), dp(12), dp(8));
        copy.setBackground(roundedShape(lineGold(0x22), dp(12), lineGold(0x55), dp(1)));
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
        innerLine.setBackgroundColor(lineGold(0x22));
        exec.addView(innerLine, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        exec.addView(space(10));

        LinearLayout hoursRow = row(Gravity.CENTER_VERTICAL);
        TextView hours = text("پاسخگویی: شنبه تا پنجشنبه — ۱۰:۰۰ تا ۲۱:۰۰", 9.6f, TEXT_ON_DARK_FAINT, false);
        hours.setSingleLine(false);
        hours.setLineSpacing(dp(2), 1f);
        hoursRow.addView(hours, weight(1f));
        exec.addView(hoursRow);
        exec.addView(space(8));

        LinearLayout contactRow = row(Gravity.CENTER_VERTICAL);
        contactRow.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(14), lineGold(0x33), dp(1)));
        contactRow.setPadding(dp(12), dp(9), dp(12), dp(9));
        TextView addContact = text("افزودن شماره مدیریت به مخاطبین", 10f, COL_GOLD_LIGHT, true);
        contactRow.addView(addContact, weight(1f));
        TextView addIcon = text("＋", 12f, COL_GOLD_LIGHT, true);
        contactRow.addView(addIcon);
        contactRow.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { addOwnerToContacts(); }
        });
        exec.addView(contactRow);
        // the lattice lives behind the card, visible only where the obsidian thins
        FrameLayout execStage = new FrameLayout(this);
        execStage.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        execStage.setClipToOutline(true);
        execStage.setBackground(roundedShape(COL_OBSIDIAN, dp(24), lineGold(0x66), dp(1)));
        LuxuryCanvas.ArabesqueBackdrop lattice = new LuxuryCanvas.ArabesqueBackdrop(this);
        lattice.ink(true).strength(20);
        execStage.addView(lattice, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        exec.setBackground(null);
        execStage.addView(exec, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        contentContainer.addView(execStage);
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

        // ── VIP club ladder (lifetime value → tier) ───────────────────────
        contentContainer.addView(vipClubCard());
        contentContainer.addView(space(14));

        // ── Order tracking ────────────────────────────────────────────────
        LinearLayout track = whiteCard();
        track.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_TRUCK, "پیگیری سفارش و گواهی اصالت"));
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
                res.addView(orderTimeline(order.status));
                res.addView(space(8));
                for (Models.OrderItemRecord it : order.items) {
                    res.addView(resultLine(it.productName + "  •  " + PersianFormatter.toPersianDigits(String.valueOf(it.quantity)) + " عدد",
                            PersianFormatter.formatToman(it.unitPrice * it.quantity)));
                }
                res.addView(space(4));
                res.addView(resultLine("مبلغ نهایی", PersianFormatter.formatToman(order.totalAmount)));
                if (order.trackingCode != null && !order.trackingCode.isEmpty()) {
                    res.addView(resultLine("کد رهگیری پست", ltr(order.trackingCode)));
                }
                track.addView(res);
            }
        }
        contentContainer.addView(track);
        contentContainer.addView(space(14));

        // ── Recent orders ─────────────────────────────────────────────────
        List<Models.OrderRecord> orders = db.getOrders("all");
        if (!orders.isEmpty()) {
            contentContainer.addView(sectionHeader(Luxury3DIconView.ICON_DOC, "سفارش‌های ثبت‌شده اخیر",
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
                card.addView(text(o.customerName + " • " + o.createdAtLabel, 10.5f, COL_TEXT_MUTED, false));
                card.addView(space(8));
                card.addView(resultLine("مبلغ کل", PersianFormatter.formatToman(o.totalAmount)));
                card.addView(space(6));
                LinearLayout orderActions = row(Gravity.CENTER_VERTICAL);
                orderActions.setPadding(0, dp(8), 0, 0);
                TextView invoice = text("اشتراک‌گذاری فاکتور", 9.6f, COL_GOLD_DEEP, true);
                invoice.setGravity(Gravity.CENTER);
                invoice.setPadding(dp(12), dp(7), dp(12), dp(7));
                invoice.setBackground(roundedShape(SURF_CHIP, dp(10), SURF_CHIP_LINE, dp(1)));
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
        TextView copyright = text("© ۱۴۰۵ ASIYE STORE  ·  تمام حقوق محفوظ است", 8.8f, TEXT_ON_DARK_FAINT, false);
        copyright.setGravity(Gravity.CENTER);
        tracking(copyright, 0.05f);
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
        tile.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(18), lineGold(0x2E), dp(1)));
        tile.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });

        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(50), dp(50)));
        iconWrap.addView(new Luxury3DIconView(this, iconType, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tile.addView(iconWrap);
        tile.addView(space(8));

        TextView t = text(title, 12f, TEXT_ON_DARK, true);
        t.setGravity(Gravity.CENTER);
        tile.addView(t);

        TextView s = new TextView(this);
        s.setText(subtitle);
        s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
        s.setTextColor(TEXT_ON_DARK_MUTED);
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
        plaque.setBackground(deepCard(dp(24)));
        plaque.setElevation(dp(2));
        plaque.setPadding(dp(20), dp(24), dp(20), dp(22));

        FrameLayout seal = new FrameLayout(this);
        seal.setLayoutParams(new LinearLayout.LayoutParams(dp(52), dp(52)));
        seal.setBackground(roundedShape(COL_OBSIDIAN_SOFT, dp(20), lineGold(0x55), dp(1)));
        seal.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_CROWN, true),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        plaque.addView(seal);
        plaque.addView(space(14));

        TextView eyebrow = text("DESIGNED & ARCHITECTED BY", 8.5f, ACCENT_ON_DARK, false);
        tracking(eyebrow, 0.3f);
        eyebrow.setTypeface(Typeface.SERIF, Typeface.BOLD);
        eyebrow.setGravity(Gravity.CENTER);
        plaque.addView(eyebrow);
        plaque.addView(space(8));

        TextView name = text("MILAD YAGHOOBI", 22f, COL_GOLD_LIGHT, true);
        name.setTypeface(Typeface.SERIF, Typeface.BOLD);
        tracking(name, 0.16f);
        name.setGravity(Gravity.CENTER);
        plaque.addView(name);
        plaque.addView(space(10));

        View line = new View(this);
        line.setBackgroundColor(lineGold(0x66));
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(dp(56), dp(1));
        lineLp.setMargins(0, 0, 0, dp(12));
        plaque.addView(line, lineLp);

        TextView fa = text("طراحی تجربه کاربری لوکس و معماری اکوسیستم دیجیتال\nبه صورت اختصاصی برای مجموعه ASIYE STORE", 11f, TEXT_ON_DARK_MUTED, false);
        fa.setGravity(Gravity.CENTER);
        fa.setLineSpacing(dp(4), 1f);
        plaque.addView(fa);
        plaque.addView(space(12));

        TextView version = text(APP_VERSION, 8.5f, TEXT_ON_DARK_FAINT, false);
        tracking(version, 0.2f);
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

        contentContainer.addView(crestTitle(Luxury3DIconView.ICON_CHART, "گزارش عملکرد مجموعه",
                "مدیریت سرکار خانم زنگنه • آمار زنده فروش و سفارش‌ها", "ATELIER DASHBOARD"));
        contentContainer.addView(space(10));
        contentContainer.addView(adminLiveStrip());
        contentContainer.addView(space(12));
        contentContainer.addView(morningBriefCard());
        contentContainer.addView(space(14));

        LinearLayout kpiRow1 = row(Gravity.CENTER_VERTICAL);
        kpiRow1.addView(kpiCard(Luxury3DIconView.ICON_CHART, "فروش ۷ روز", PersianFormatter.formatCompactMillions(snap.salesWeek),
                "+۲۱٪ نسبت به هفته گذشته", COL_SUCCESS), weight(1f));
        kpiRow1.addView(spaceH(10));
        kpiRow1.addView(kpiCard(Luxury3DIconView.ICON_TRUCK, "سفارش‌های در جریان",
                PersianFormatter.toPersianDigits(String.valueOf(snap.activeOrdersCount)) + " سفارش",
                "از " + PersianFormatter.toPersianDigits(String.valueOf(snap.totalOrdersCount)) + " سفارش کل", COL_TEXT_MUTED), weight(1f));
        contentContainer.addView(kpiRow1);
        contentContainer.addView(space(10));

        LinearLayout kpiRow2 = row(Gravity.CENTER_VERTICAL);
        kpiRow2.addView(kpiCard(Luxury3DIconView.ICON_WALLET, "میانگین سبد خرید", PersianFormatter.formatCompactMillions(snap.avgOrderValue),
                "ارزش هر سفارش", COL_TEXT_MUTED), weight(1f));
        kpiRow2.addView(spaceH(10));
        kpiRow2.addView(kpiCard(Luxury3DIconView.ICON_BOX, "هشدار موجودی",
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
        topCard.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_MEDAL, "پرفروش‌ترین کالاهای مجموعه"));
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

        contentContainer.addView(automationStatusCard());
        contentContainer.addView(space(14));

        // Coupons
        LinearLayout couponCard = whiteCard();
        LinearLayout couponHead = row(Gravity.CENTER_VERTICAL);
        couponHead.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_PERCENT, "کوپن‌های تخفیف فعال"), weight(1f));
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
                    c.active ? SURF_SUCCESS_LINE : COL_BORDER, dp(1)));
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
        syncCard.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_GLOBE, "همگام‌سازی با سرور لاراول"));
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

    /** v6.3 — KPI tile with a small 3D medallion; callers pass 0 for a text-only tile. */
    private View kpiCard(int iconType, String label, String value, String hint, int hintColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_WHITE, dp(18), COL_BORDER, dp(1)));
        card.setPadding(dp(14), dp(13), dp(14), dp(13));

        if (iconType != 0) {
            card.addView(iconMedallion(iconType, 30, Luxury3DIconView.PEDESTAL_IVORY));
            card.addView(space(7));
        }

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
        headRow.addView(crestTitle(Luxury3DIconView.ICON_TAG, "مدیریت محصولات مجموعه",
                "افزودن، ویرایش قیمت و کنترل نمایش کالاها"), weight(1f));
        contentContainer.addView(headRow);
        contentContainer.addView(space(12));

        contentContainer.addView(primaryButton("افزودن هوشمند کالا با هوش مصنوعی",
                Luxury3DIconView.ICON_AI, new Runnable() {
                    @Override public void run() {
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
        contentContainer.addView(reviewsModerationCard());
        contentContainer.addView(space(12));
        contentContainer.addView(footerNote());
    }

    /** Merchant queue for new reviews — approve to publish, delete to discard. */
    private View reviewsModerationCard() {
        List<Models.ReviewRecord> pending = db.getPendingReviews();
        LinearLayout card = whiteCard();
        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        head.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_STAR, "نظرات در انتظار تأیید"), weight(1f));
        TextView count = text(PersianFormatter.toPersianDigits(String.valueOf(pending.size())), 9.5f,
                pending.isEmpty() ? COL_SUCCESS : COL_GOLD_DEEP, true);
        count.setPadding(dp(9), dp(4), dp(9), dp(4));
        count.setBackground(roundedShape(pending.isEmpty() ? COL_SUCCESS_SOFT : SURF_CHIP, dp(9),
                pending.isEmpty() ? SURF_SUCCESS_LINE : SURF_CHIP_LINE, dp(1)));
        head.addView(count);
        card.addView(head);

        if (pending.isEmpty()) {
            card.addView(space(7));
            card.addView(text("همهٔ نظرات بررسی شده‌اند. نظر تازه‌ای در صف تأیید نیست.", 10f, COL_TEXT_MUTED, false));
            return card;
        }

        for (final Models.ReviewRecord r : pending) {
            card.addView(space(10));
            View divider = new View(this);
            divider.setBackgroundColor(SURF_DIVIDER);
            card.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
            card.addView(space(9));

            LinearLayout metaRow = row(Gravity.CENTER_VERTICAL);
            metaRow.addView(text(r.author, 10.5f, COL_TEXT_MAIN, true), weight(1f));
            metaRow.addView(starRow(r.rating, 9.5f));
            card.addView(metaRow);

            TextView body = text(r.body, 10.2f, TEXT_BODY, false);
            body.setLineSpacing(dp(2), 1f);
            body.setPadding(0, dp(4), 0, dp(7));
            card.addView(body);

            LinearLayout actions = row(Gravity.CENTER_VERTICAL);
            TextView approve = text("تأیید و انتشار", 9.4f, COL_OBSIDIAN, true);
            approve.setGravity(Gravity.CENTER);
            approve.setPadding(dp(12), dp(7), dp(12), dp(7));
            approve.setBackground(roundedShape(COL_GOLD_LIGHT, dp(10), 0, 0));
            approve.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    tap();
                    db.setReviewApproved(r.id, true);
                    toast("نظر تأیید و در صفحهٔ کالا منتشر شد.");
                    renderCurrentScreen();
                }
            });
            actions.addView(approve);
            actions.addView(spaceH(8));
            TextView reject = text("حذف", 9.4f, COL_DANGER, true);
            reject.setGravity(Gravity.CENTER);
            reject.setPadding(dp(12), dp(7), dp(12), dp(7));
            reject.setBackground(roundedShape(COL_WHITE, dp(10), COL_BORDER, dp(1)));
            reject.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    db.deleteReview(r.id);
                    toast("نظر حذف شد.");
                    renderCurrentScreen();
                }
            });
            actions.addView(reject);
            card.addView(actions);
        }
        return card;
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
            badgeRow.addView(statusChip("هوش مصنوعی", COL_GOLD_DEEP, SURF_CHIP), aiLp);
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
        t.setBackground(roundedShape(bgColor, dp(10), SURF_DIVIDER, dp(1)));
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
        contentContainer.addView(crestTitle(Luxury3DIconView.ICON_TRUCK, "مدیریت سفارش‌های مشتریان",
                "به‌روزرسانی وضعیت، ثبت کد رهگیری و ارتباط مستقیم"));
        contentContainer.addView(space(14));

        final EditText orderSearch = makeInput("جستجوی شمارهٔ سفارش، نام یا موبایل", InputType.TYPE_CLASS_TEXT, false);
        orderSearch.setText(adminOrderQuery);
        orderSearch.setSingleLine(true);
        LinearLayout searchRow = row(Gravity.CENTER_VERTICAL);
        searchRow.addView(orderSearch, weight(1f));
        searchRow.addView(spaceH(7));
        final TextView go = text("جستجو", 9.6f, COL_OBSIDIAN, true);
        go.setPadding(dp(13), dp(12), dp(13), dp(12));
        go.setBackground(roundedShape(COL_GOLD_LIGHT, dp(11), 0, 0));
        go.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                adminOrderQuery = orderSearch.getText().toString().trim();
                renderCurrentScreen();
            }
        });
        searchRow.addView(go);
        contentContainer.addView(searchRow);
        contentContainer.addView(space(10));

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
            chip.setTypeface(houseTypeface(active));
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

        List<Models.OrderRecord> orders = db.searchOrders(adminOrderQuery, adminOrderFilter);
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
            card.addView(resultLine(it.productName,
                    PersianFormatter.toPersianDigits(String.valueOf(it.quantity)) + " عدد"));
        }
        card.addView(space(6));
        card.addView(resultLine("مبلغ کل", PersianFormatter.formatToman(o.totalAmount)));
        card.addView(resultLine("تحویل", o.shippingMethod));
        if (o.trackingCode != null && !o.trackingCode.isEmpty()) {
            card.addView(resultLine("کد رهگیری", ltr(o.trackingCode)));
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

                java.util.Map<String, String> vars = new java.util.HashMap<>();
                vars.put("name", o.customerName == null ? "مشتری گرامی" : o.customerName);
                vars.put("order", o.orderNumber);
                vars.put("tracking", code);
                vars.put("store", "اسیه استور");
                vars.put("owner", "خانم زنگنه");
                boolean byCourier = o.shippingMethod != null && o.shippingMethod.contains("پیک");
                if (byCourier) {
                    vars.put("eta", db.getSetting("courier_eta", "۲ تا ۴ ساعت"));
                    SmsGateway.notify(MainActivity.this, db, o.customerMobile, "courier_ontheway", vars);
                } else {
                    SmsGateway.notify(MainActivity.this, db, o.customerMobile, "order_shipped", vars);
                }

                toast("کد رهگیری ثبت شد و پیامک ارسال برای مشتری در صف قرار گرفت.");
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
        contentContainer.addView(crestTitle(Luxury3DIconView.ICON_BOX, "کنترل موجودی انبار",
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
        logCard.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_BOX, "آخرین گردش انبار"));
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
        charge.setBackground(roundedShape(SURF_CHIP, dp(11), SURF_GOLD_LINE, dp(1)));
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
        box.addView(space(9));

        // v6.7 · the phrases customers actually look for
        android.widget.HorizontalScrollView chipScroll = new android.widget.HorizontalScrollView(this);
        chipScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        String[] quick = {"ضدلک", "ضدچروک", "تونر", "شامپو", "گراتیو", "اوردینری", "پوست خشک"};
        for (final String q : quick) {
            TextView chip = text(q, 9.6f, COL_GOLD_DEEP, true);
            chip.setPadding(dp(11), dp(6), dp(11), dp(6));
            chip.setBackground(roundedShape(COL_IVORY_BG, dp(12), COL_BORDER, dp(1)));
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.setMarginEnd(dp(6));
            chip.setLayoutParams(clp);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    query.setText(q);
                    query.setSelection(q.length());
                }
            });
            chips.addView(chip);
        }
        chipScroll.addView(chips);
        box.addView(chipScroll);
        box.addView(space(10));

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
        tap();
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
            case "generic":             return R.drawable.img_brand_emblem;
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
        head.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_CARD, "روش پرداخت و تسویه"), weight(1f));
        TextView official = text("۳ روش رسمی", 8.8f, COL_GOLD_DEEP, true);
        official.setPadding(dp(9), dp(4), dp(9), dp(4));
        official.setBackground(roundedShape(SURF_CHIP, dp(9), SURF_CHIP_LINE, dp(1)));
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
                    sheet.setBackground(roundedShape(COL_OBSIDIAN, dp(15), lineGold(0x40), dp(1)));
                    sheet.setPadding(dp(13), dp(12), dp(13), dp(12));
                    sheet.addView(text("جدول بازپرداخت", 9.5f, ACCENT_ON_DARK, false));
                    sheet.addView(space(6));
                    sheet.addView(darkResultLine("مبلغ کل خرید", PersianFormatter.formatToman(basePayable)));
                    sheet.addView(darkResultLine("پیش‌پرداخت امروز", PersianFormatter.formatToman(activePlan.downPayment)));
                    sheet.addView(darkResultLine("تعداد اقساط",
                            PersianFormatter.toPersianDigits(String.valueOf(activePlan.months)) + " قسط"));
                    sheet.addView(darkResultLine("مبلغ هر قسط", PersianFormatter.formatToman(activePlan.monthlyPayment)));
                    if (activePlan.totalFee > 0) {
                        sheet.addView(darkResultLine("کارمزد تسهیلات", PersianFormatter.formatToman(activePlan.totalFee)));
                    }
                    sheet.addView(darkResultLine("مجموع بازپرداخت", PersianFormatter.formatToman(activePlan.payableTotal)));
                    card.addView(sheet);
                    card.addView(space(7));
                    TextView approval = text("شرایط اقساط پس از ثبت سفارش توسط مدیریت مجموعه بررسی و تأیید می‌شود.",
                            9.2f, COL_TEXT_MUTED, false);
                    approval.setLineSpacing(dp(2), 1f);
                    card.addView(approval);
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
        box.setBackground(roundedShape(selected ? SURF_SOFT : COL_WHITE, dp(15),
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
        ring.setBackground(roundedShape(selected ? SURF_SOFT : COL_WHITE, dp(10),
                selected ? COL_GOLD : COL_BORDER, dp(selected ? 2 : 1)));
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
        TextView pill = text(PersianFormatter.statusLabelFa(status), 9.6f, color, true);
        pill.setGravity(Gravity.CENTER);
        pill.setSingleLine(true);
        pill.setEllipsize(TextUtils.TruncateAt.END);
        pill.setPadding(dp(11), dp(5), dp(11), dp(5));
        pill.setBackground(roundedShape((color & 0x00FFFFFF) | 0x14, dp(11),
                (color & 0x00FFFFFF) | 0x3D, dp(1)));
        return pill;
    }

    private View installmentPlanRow(final Models.InstallmentPlan plan, boolean selected) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(13), dp(11), dp(13), dp(11));
        box.setBackground(roundedShape(selected ? SURF_SOFT : COL_IVORY_BG, dp(13),
                selected ? COL_GOLD : COL_BORDER, dp(selected ? 2 : 1)));

        LinearLayout top = row(Gravity.CENTER_VERTICAL);
        if (selected) {
            FrameLayout medal = new FrameLayout(this);
            medal.setLayoutParams(new LinearLayout.LayoutParams(dp(22), dp(22)));
            medal.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_INSTALLMENT, true),
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            TextView titlePad = new TextView(this);
            top.addView(medal);
            titlePad.setLayoutParams(new LinearLayout.LayoutParams(dp(8), dp(1)));
            top.addView(titlePad);
        }
        TextView title = text(plan.titleFa, 11.5f, COL_TEXT_MAIN, selected);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        top.addView(title, weight(1f));
        TextView chip = text(plan.zeroInterest ? "بدون کارمزد" : plan.providerFa, 8.8f,
                plan.zeroInterest ? COL_SUCCESS : COL_TEXT_MUTED, true);
        chip.setPadding(dp(8), dp(3), dp(8), dp(3));
        chip.setBackground(roundedShape(plan.zeroInterest ? COL_SUCCESS_SOFT : COL_WHITE, dp(9),
                plan.zeroInterest ? SURF_SUCCESS_LINE : COL_BORDER, dp(1)));
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
        head.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_BANK, "درگاه پرداخت آنلاین"), weight(1f));
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
            line.setBackground(roundedShape(selected ? SURF_SOFT : COL_IVORY_BG, dp(12),
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
        card.addView(resultLine("مبلغ قابل پرداخت", PersianFormatter.formatToman(payable)));
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
    /** Shared two-column palette cards, used by the storefront and settings. */
    private LinearLayout themeTiles(final AlertDialog dialog) {
        final int[] modes = { THEME_IVORY, THEME_EMERALD, THEME_SAPPHIRE, THEME_ROSE, THEME_PINK, THEME_NIGHT };
        final int[] faces = { 0xFFFAF8F3, 0xFFF3F8F4, 0xFF14345F, 0xFFFFE7ED, 0xFFFFC6D7, 0xFF0B0B0E };
        final int[] inks = { 0xFF8E6C3A, 0xFF0B4832, 0xFFD9B478, 0xFF934A60, 0xFFAB2850, 0xFFD9B478 };
        final String[] names = { "روز عاجی", "زمرد سلطنتی", "یاقوت کبود", "رز آتلیه", "صورتی پینک مزون", "شب ابسیدین" };
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout paletteRow = null;

        for (int i = 0; i < modes.length; i++) {
            if (i % 2 == 0) {
                if (i > 0) grid.addView(space(8));
                paletteRow = row(Gravity.TOP);
                grid.addView(paletteRow);
            }
            final int mode = modes[i];
            boolean active = themeMode == mode;

            LinearLayout tile = new LinearLayout(this);
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER_HORIZONTAL);
            tile.setPadding(dp(6), dp(9), dp(6), dp(9));
            tile.setBackground(roundedShape(active ? SURF_SOFT : COL_IVORY_BG, dp(14),
                    active ? COL_GOLD : COL_BORDER, dp(active ? 2 : 1)));

            TextView swatch = text("", 10f, faces[i], true);
            swatch.setLayoutParams(new LinearLayout.LayoutParams(dp(52), dp(34)));
            swatch.setBackground(roundedShape(faces[i], dp(11), inks[i], dp(2)));
            tile.addView(swatch);
            tile.addView(space(6));

            TextView nm = text(names[i], 9.2f, active ? COL_TEXT_MAIN : COL_TEXT_MUTED, active);
            nm.setGravity(Gravity.CENTER);
            tile.addView(nm);

            if (active) {
                TextView now = text("فعال", 8f, COL_GOLD_DEEP, true);
                now.setGravity(Gravity.CENTER);
                tile.addView(now);
            }

            tile.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    tap();
                    if (dialog != null) dialog.dismiss();
                    applyThemeMode(mode);
                }
            });
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tlp.setMarginEnd(i % 2 == 0 ? dp(8) : 0);
            paletteRow.addView(tile, tlp);
        }

        if (modes.length % 2 != 0) paletteRow.addView(space(0), weight(1f));
        return grid;
    }

    private void renderAdminSettings() {
        settingsTextKeys.clear();
        settingsTextInputs.clear();
        settingsCheckKeys.clear();
        settingsCheckInputs.clear();

        contentContainer.addView(crestTitle(Luxury3DIconView.ICON_SETTINGS, "تنظیمات فروشگاه",
                "پرداخت آنلاین، فروش اقساطی و استودیوی هوش مصنوعی — همه در یک صفحه"));
        contentContainer.addView(space(14));

        // ── APPEARANCE (v6.4) ────────────────────────────────────────────
        LinearLayout look = whiteCard();
        look.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_SUN, "ظاهر و تجربهٔ برنامه"));
        look.addView(space(9));

        TextView lookHint = text("شش حالت طراحی‌شده برای اسیه — عاجی، زمرد، یاقوت کبود، رز آتلیه، صورتی و شب ابسیدین."
                + " کاشی‌ها، آیکن‌های سه‌بعدی و همهٔ بخش‌ها با هم تغییر می‌کنند و انتخاب شما ذخیره می‌شود.",
                9.4f, COL_TEXT_MUTED, false);
        lookHint.setLineSpacing(dp(3), 1f);
        look.addView(lookHint);
        look.addView(space(10));

        look.addView(themeTiles(null));

        look.addView(space(11));
        LinearLayout lookRow2 = row(Gravity.CENTER_VERTICAL);
        LinearLayout lookText2 = new LinearLayout(this);
        lookText2.setOrientation(LinearLayout.VERTICAL);
        lookText2.addView(text("یادآور صبحگاهی مدیریت", 11.5f, COL_TEXT_MAIN, true));
        lookText2.addView(text("هر روز ۹ صبح: فروش، نظرات در انتظار و کالاهای کم‌موجود", 9.4f, COL_TEXT_MUTED, false));
        lookRow2.addView(lookText2, weight(1f));
        TextView lookBtn2 = text(briefReminderOn ? "خاموش" : "فعال", 10f,
                briefReminderOn ? COL_DANGER : COL_OBSIDIAN, true);
        lookBtn2.setGravity(Gravity.CENTER);
        lookBtn2.setPadding(dp(16), dp(9), dp(16), dp(9));
        lookBtn2.setBackground(roundedShape(briefReminderOn ? COL_IVORY_BG : COL_GOLD_LIGHT, dp(12),
                briefReminderOn ? COL_BORDER : 0, dp(1)));
        lookBtn2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                tap();
                toggleDailyBrief();
            }
        });
        lookRow2.addView(lookBtn2);
        look.addView(lookRow2);

        contentContainer.addView(look);
        contentContainer.addView(space(14));
        // ── SMS AUTOPILOT (v6.8) ─────────────────────────────────────────
        contentContainer.addView(smsCenterCard());
        contentContainer.addView(space(14));

        // ── COURIER & GIFT CARDS (v6.9) ─────────────────────────────────
        LinearLayout courier = whiteCard();
        courier.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_TRUCK, "پیک شهری و کارت هدیه"));
        courier.addView(space(8));
        courier.addView(settingsToggle("courier_enabled", "فعال بودن پیک شهری (تحویل همان روز)"));
        courier.addView(settingsText("courier_fee", "کرایهٔ پیک (تومان)", String.valueOf(db.getCourierFee()), true));
        courier.addView(settingsText("courier_cities", "شهرهای تحت پوشش (با کاما جدا کنید)",
                db.getSetting("courier_cities", "تهران، شیراز، اصفهان، مشهد"), false));
        courier.addView(settingsText("courier_eta", "زمان تقریبی تحویل",
                db.getSetting("courier_eta", "۲ تا ۴ ساعت"), false));
        courier.addView(space(9));
        courier.addView(settingsToggle("gift_cards_enabled", "فعال بودن کارت هدیه"));
        courier.addView(space(9));
        courier.addView(resultLine("اعتبار فعال کارت‌ها", PersianFormatter.formatToman(db.totalGiftLiability())));
        courier.addView(space(8));
        courier.addView(smallGoldAction("فهرست کارت‌های هدیه", Luxury3DIconView.ICON_GIFT, new Runnable() {
            @Override public void run() { showGiftCardsList(); }
        }));
        contentContainer.addView(courier);
        contentContainer.addView(space(14));

        // ── SHIPPING (v6.6) ──────────────────────────────────────────────
        LinearLayout ship = whiteCard();
        LinearLayout shipHead = row(Gravity.CENTER_VERTICAL);
        shipHead.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_TRUCK, "مدیر حمل‌ونقل"), weight(1f));
        TextView shipHint = text("تأثیر مستقیم بر مبلغ نهایی سبد", 8.6f, COL_TEXT_FAINT, false);
        shipHead.addView(shipHint);
        ship.addView(shipHead);
        ship.addView(space(8));
        ship.addView(settingsText("ship_cost", "هزینهٔ ارسال عادی (تومان)",
                String.valueOf(db.getShippingCost()), true));
        ship.addView(settingsText("ship_free_threshold", "آستانهٔ ارسال رایگان (تومان)",
                String.valueOf(db.getFreeShippingThreshold()), true));
        ship.addView(space(6));
        TextView shipNote = text("این دو عدد در صفحهٔ سبد خرید و در محاسبهٔ نهایی سفارش استفاده می‌شوند.",
                8.8f, COL_TEXT_FAINT, false);
        shipNote.setLineSpacing(dp(2), 1f);
        ship.addView(shipNote);
        contentContainer.addView(ship);
        contentContainer.addView(space(14));

        contentContainer.addView(operationsCard());
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
        ai.addView(text("تحلیل مستقیم با سرویس‌های هوش مصنوعی انجام می‌شود. نام، برند و عکس با تأیید شما ارسال خواهد شد؛ نتایج باید بررسی شوند. کلیدهای داخل این نسخه قابل استخراج هستند.", 10f, COL_TEXT_MUTED, false));
        ai.addView(space(10));
        ai.addView(ghostGoldButton("رفتن به استودیوی هوشمند افزودن کالا", new Runnable() {
            @Override public void run() {
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
    private static final String PREF_STUDIO = "studio_draft_v1";

    private void resetAiStudio() {
        aiGeneration++;
        aiAnalysis = null; aiDraft = null; aiStudioPath = ""; aiSourcePath = "";
        aiSourceBitmap = null; aiName = ""; aiBrand = ""; aiBusy = false; aiAnalysisRunning = false;
        getSharedPreferences(PREF_STUDIO, MODE_PRIVATE).edit().clear().apply();
    }

    private void persistStudioDraft() {
        android.content.SharedPreferences.Editor editor = getSharedPreferences(PREF_STUDIO, MODE_PRIVATE).edit();
        editor.putString("name", aiName).putString("brand", aiBrand)
                .putString("source", aiSourcePath).putString("studio", aiStudioPath);
        if (aiDraft == null) editor.remove("draft");
        else {
            org.json.JSONObject obj = new org.json.JSONObject();
            try {
                for (java.lang.reflect.Field f : AiProductStudio.Draft.class.getFields()) {
                    if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) obj.put(f.getName(), f.get(aiDraft));
                }
                editor.putString("draft", obj.toString());
            } catch (Exception e) { android.util.Log.e("ASIYE", "draft save", e); }
        }
        editor.apply();
    }

    private void restoreStudioDraft() {
        android.content.SharedPreferences prefs = getSharedPreferences(PREF_STUDIO, MODE_PRIVATE);
        aiName = prefs.getString("name", ""); aiBrand = prefs.getString("brand", "");
        aiSourcePath = prefs.getString("source", ""); aiStudioPath = prefs.getString("studio", "");
        aiSourceBitmap = decodeSampled(aiSourcePath, 1200);
        if (aiSourceBitmap == null) aiSourcePath = "";
        String json = prefs.getString("draft", "");
        if (!json.isEmpty()) try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            AiProductStudio.Draft draft = new AiProductStudio.Draft();
            for (java.lang.reflect.Field f : AiProductStudio.Draft.class.getFields()) {
                if (!obj.has(f.getName()) || java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                if (f.getType() == String.class) f.set(draft, obj.optString(f.getName(), ""));
                else if (f.getType() == long.class) f.setLong(draft, obj.optLong(f.getName(), 0));
                else if (f.getType() == int.class) f.setInt(draft, obj.optInt(f.getName(), 0));
            }
            aiDraft = draft;
            aiAnalysis = AiProductStudio.analyze(aiName, aiBrand, null);
        } catch (Exception e) { android.util.Log.e("ASIYE", "draft restore", e); }
    }

    private void observeStudioInput(EditText input, final boolean name) {
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (name) aiName = s.toString(); else aiBrand = s.toString();
                persistStudioDraft();
            }
            public void afterTextChanged(Editable e) { }
        });
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
        bar.setBackground(roundedShape(COL_OBSIDIAN, dp(18), lineGold(0x4D), dp(1)));
        bar.setPadding(dp(12), dp(11), dp(12), dp(11));

        FrameLayout medal = iconMedallion(Luxury3DIconView.ICON_AI, 36, Luxury3DIconView.PEDESTAL_GOLD);
        ((Luxury3DIconView) medal.getChildAt(0)).enableSweep(true);
        bar.addView(medal);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(10), 0, dp(10), 0);
        titles.addView(text("استودیوی هوشمند محصول", 13.5f, TEXT_ON_DARK, true));
        titles.addView(text("پیش‌نویس قابل بررسی و ویرایش", 9.4f, TEXT_ON_DARK_FAINT, false));
        bar.addView(titles, weight(1f));

        TextView close = text("بستن", 10f, COL_GOLD_LIGHT, true);
        close.setPadding(dp(12), dp(8), dp(12), dp(8));
        close.setBackground(roundedShape(lineGold(0x26), dp(11), lineGold(0x59), dp(1)));
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                aiStudioOpen = false;
                renderCurrentScreen();
            }
        });
        TextView settings = text("تنظیمات", 10f, COL_GOLD_LIGHT, true);
        settings.setPadding(dp(10), dp(8), dp(10), dp(8));
        settings.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showAiServices(); } });
        bar.addView(settings);
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
            android.util.Log.e("ASIYE", "studio render failure", t);
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
        card.addView(text("نمایش استودیو با مشکل روبه‌رو شد", 13f, COL_TEXT_MAIN, true));
        card.addView(space(6));
        TextView body = text("اطلاعات ذخیره‌شده پاک نشده‌اند. دوباره تلاش کنید؛ اگر مشکل تکرار شد، گزارش خطا را برای پشتیبانی ارسال کنید.", 10.4f, COL_TEXT_MUTED, false);
        body.setLineSpacing(dp(3), 1f);
        card.addView(body);
        card.addView(space(12));
        card.addView(primaryButton("تلاش دوباره", Luxury3DIconView.ICON_AI, new Runnable() {
            @Override public void run() { renderCurrentScreen(); }
        }));
        return card;
    }

    /** Best-effort brand token from a catalog product (used by the quick-start chips). */
    private String extractBrand(Models.Product p) {
        if (p == null) return "";
        if (p.brand != null && !p.brand.trim().isEmpty()) return p.brand.trim();
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
        observeStudioInput(aiNameInput, true);
        card.addView(aiNameInput);
        card.addView(space(12));

        card.addView(text("۲ · برند محصول", 10.5f, COL_TEXT_MUTED, true));
        card.addView(space(6));
        aiBrandInput = makeInput("مثال: گاتیو / Gatieau", InputType.TYPE_CLASS_TEXT, false);
        aiBrandInput.setText(aiBrand);
        observeStudioInput(aiBrandInput, false);
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
        card.addView(space(8));
        card.addView(ghostButton("وارد کردن لینک مستقیم عکس آنلاین (HTTPS)", new Runnable() {
            @Override public void run() { showOnlineImageDialog(); }
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
                        persistStudioDraft();
                        aiAnalysis = null;
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

        contentContainer.addView(primaryButton(aiAnalysisRunning ? "در حال تحلیل؛ لطفاً صبر کنید" : "تحلیل و تکمیل مشخصات با هوش مصنوعی", Luxury3DIconView.ICON_AI, new Runnable() {
            @Override public void run() { confirmCloudAnalysis(); }
        }));
        if (aiAnalysisRunning) {
            contentContainer.addView(space(6));
            contentContainer.addView(text("در حال آماده‌سازی پیش‌نویس…", 10f, COL_TEXT_MUTED, false));
            contentContainer.addView(space(6));
            contentContainer.addView(ghostButton("لغو انتظار برای تحلیل", new Runnable() {
                @Override public void run() { cancelCloudAnalysis(); }
            }));
        }
        contentContainer.addView(space(8));
        contentContainer.addView(outlineDarkButton("پیش‌نویس محلی بدون تحلیل عکس", Luxury3DIconView.ICON_AI, new Runnable() {
            @Override public void run() {
                if (aiBusy) { toast("ابتدا تحلیل در حال اجرا را لغو کنید یا منتظر بمانید."); return; }
                aiName = aiNameInput == null ? "" : aiNameInput.getText().toString().trim();
                aiBrand = aiBrandInput == null ? "" : aiBrandInput.getText().toString().trim();
                if (aiName.length() < 3) { toast("نام محصول را کامل‌تر وارد کنید."); return; }
                aiAnalysis = AiProductStudio.analyze(aiName, aiBrand, null);
                persistStudioDraft(); renderCurrentScreen();
            }
        }));
        contentContainer.addView(space(10));
        TextView hint = text("حالت محلی عکس را شناسایی نمی‌کند و از اینترنت جست‌وجو نمی‌کند. مشخصات محصول را پیش از انتشار بررسی و تکمیل کنید.",
                9.4f, COL_TEXT_FAINT, false);
        hint.setLineSpacing(dp(3), 1f);
        contentContainer.addView(hint);
    }

    // ── STEP 2 ───────────────────────────────────────────────────────────
    private void renderAiConfirmation() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedShape(COL_OBSIDIAN, dp(22), lineGold(0x55), dp(1)));
        card.setPadding(dp(18), dp(16), dp(18), dp(16));

        LinearLayout head = row(Gravity.CENTER_VERTICAL);
        FrameLayout icon = new FrameLayout(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(38), dp(38)));
        icon.addView(new Luxury3DIconView(this, Luxury3DIconView.ICON_AI, false),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        head.addView(icon);
        head.addView(text("بررسی اطلاعات واردشده", 12.5f, TEXT_ON_DARK, true), weight(1f));
        card.addView(head);
        card.addView(space(12));

        card.addView(text(aiAnalysis.detectedTitle, 15f, TEXT_ON_DARK, true));
        card.addView(space(6));
        String brandLine = (aiAnalysis.detectedBrand == null || aiAnalysis.detectedBrand.trim().isEmpty())
                ? aiAnalysis.category : aiAnalysis.detectedBrand + "  •  " + aiAnalysis.category;
        TextView brandView = text(brandLine, 10f, ACCENT_ON_DARK, false);
        brandView.setSingleLine(true);
        brandView.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(brandView);
        card.addView(space(10));
        TextView why = text(aiAnalysis.rationaleFa, 10.3f, TEXT_ON_DARK_MUTED, false);
        why.setLineSpacing(dp(3), 1f);
        card.addView(why);
        if (aiAnalysis.photoInsightFa != null && !aiAnalysis.photoInsightFa.isEmpty()) {
            card.addView(space(10));
            card.addView(text(aiAnalysis.photoInsightFa, 9.4f, TEXT_ON_DARK_FAINT, false));
        }
        contentContainer.addView(card);
        contentContainer.addView(space(12));

        contentContainer.addView(text("آیا نام و برند برای ساخت پیش‌نویس درست است؟", 11.5f, COL_TEXT_MAIN, true));
        contentContainer.addView(space(8));
        contentContainer.addView(primaryButton("ادامه و ویرایش مشخصات", Luxury3DIconView.ICON_CHECK,
                new Runnable() {
                    @Override public void run() {
                        try {
                            aiDraft = AiProductStudio.compose(aiAnalysis, aiName, aiBrand);
                            persistStudioDraft();
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
                aiDraft = null;
                persistStudioDraft();
                renderCurrentScreen();
            }
        }));

    }

    // ── STEP 3 ───────────────────────────────────────────────────────────
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
        } else if (aiSourceBitmap != null) {
            heroImg.setImageBitmap(aiSourceBitmap);
        } else {
            heroImg.setImageResource(resolveProductImage(d.imageKey));
        }
        heroImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        hero.addView(heroImg, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TextView tag = text(!aiStudioPath.isEmpty() ? "تصویر ساختگی" : aiSourceBitmap != null ? "عکس اصلی" : "تصویر پیش‌فرض", 8.5f, COL_GOLD_LIGHT, true);
        tag.setPadding(dp(8), dp(4), dp(8), dp(4));
        tag.setBackground(roundedShape(scrim(0xCC), dp(9), lineGold(0x55), dp(1)));
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
        tracking(nameEnView, 0.04f);
        card.addView(nameEnView);
        card.addView(space(10));
        card.addView(ghostButton("انتخاب یا تغییر عکس از گالری", new Runnable() {
            @Override public void run() { pickProductPhoto(); }
        }));
        card.addView(space(8));
        card.addView(ghostButton("دریافت عکس از لینک اینترنتی", new Runnable() {
            @Override public void run() { showOnlineImageDialog(); }
        }));
        if (!aiStudioPath.isEmpty() && !aiSourcePath.isEmpty()) {
            card.addView(space(8));
            card.addView(ghostButton("استفاده از عکس اصلی", new Runnable() {
                @Override public void run() { aiStudioPath = ""; persistStudioDraft(); renderCurrentScreen(); }
            }));
        }
        contentContainer.addView(card);
        contentContainer.addView(space(12));
        LinearLayout review = whiteCard();
        review.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_AI, "راهنمای بازبینی"));
        review.addView(space(6));
        review.addView(text(d.provenance.isEmpty() ? "منبع تحلیل این پیش‌نویس ثبت نشده است؛ مشخصات و عکس را بررسی کنید." : d.provenance, 10f, COL_TEXT_MUTED, false));
        review.addView(space(5));
        review.addView(text("متن‌های پیشنهادی ممکن است محلی و کلی باشند. اطلاعات نامشخص مانند ترکیبات، وزن و کشور را از بسته‌بندی وارد کنید.", 10f, COL_TEXT_MUTED, false));
        review.addView(space(8));
        review.addView(ghostButton("تکمیل متن‌های خالی با پیشنهاد محلی", new Runnable() {
            @Override public void run() {
                AiProductStudio.fillEmptyCopy(d); persistStudioDraft(); renderCurrentScreen();
                toast("فقط متن‌های خالی تکمیل شدند؛ بازبینی کنید.");
            }
        }));
        contentContainer.addView(review);
        contentContainer.addView(space(12));

        // editable commercial fields
        LinearLayout edit = whiteCard();
        edit.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_TAG, "قیمت‌گذاری و موجودی"));
        edit.addView(space(8));
        aiPriceInput = makeInput("قیمت فروش (تومان)", InputType.TYPE_CLASS_NUMBER, true);
        aiPriceInput.setText(d.suggestedPrice > 0 ? PersianFormatter.toAsciiDigits(String.valueOf(d.suggestedPrice)) : "");
        aiOldPriceInput = makeInput("قیمت پیش از تخفیف (تومان)", InputType.TYPE_CLASS_NUMBER, true);
        aiOldPriceInput.setText(d.suggestedOldPrice > 0 ? PersianFormatter.toAsciiDigits(String.valueOf(d.suggestedOldPrice)) : "");
        aiStockInput = makeInput("موجودی انبار", InputType.TYPE_CLASS_NUMBER, true);
        aiStockInput.setText(String.valueOf(d.stock));
        observeDraftNumber(aiPriceInput, "suggestedPrice");
        observeDraftNumber(aiOldPriceInput, "suggestedOldPrice");
        observeDraftNumber(aiStockInput, "stock");
        edit.addView(text("قیمت فروش · تومان (الزامی)", 10.5f, COL_TEXT_MUTED, true));
        edit.addView(space(5)); edit.addView(aiPriceInput);
        edit.addView(space(6));
        edit.addView(ghostButton("استعلام قیمت از سرویس متصل", new Runnable() {
            @Override public void run() { showPriceLookup(); }
        }));
        edit.addView(space(10));
        edit.addView(text("قیمت پیش از تخفیف · تومان (اختیاری)", 10.5f, COL_TEXT_MUTED, true));
        edit.addView(space(5)); edit.addView(aiOldPriceInput);
        edit.addView(space(10));
        edit.addView(text("موجودی اولیهٔ پیشنهادی ۵ است؛ تعداد واقعی را بررسی کنید.", 10f, COL_TEXT_MUTED, false));
        edit.addView(space(6));
        edit.addView(text("تعداد موجود در انبار", 10.5f, COL_TEXT_MUTED, true));
        edit.addView(space(5)); edit.addView(aiStockInput);
        contentContainer.addView(edit);
        contentContainer.addView(space(12));

        contentContainer.addView(text("همهٔ مشخصات را قبل از انتشار بررسی و ویرایش کنید.", 10.5f, COL_TEXT_MUTED, false));
        contentContainer.addView(space(10));
        LinearLayout details = whiteCard();
        details.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_TAG, "مشخصات کالا"));
        details.addView(space(8));
        details.addView(aiEditableField("نام محصول", "nameFa", false));
        details.addView(aiEditableField("برند", "brand", false));
        details.addView(aiEditableField("نام لاتین (اختیاری)", "nameEn", false));
        details.addView(aiEditableField("دسته‌بندی", "category", false));
        details.addView(aiEditableField("حجم یا وزن", "volume", false));
        details.addView(aiEditableField("کشور سازنده", "country", false));
        contentContainer.addView(details); contentContainer.addView(space(12));
        LinearLayout copy = whiteCard();
        copy.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_AI, "معرفی و اطلاعات محصول"));
        copy.addView(space(8));
        copy.addView(aiEditableField("توضیح کوتاه", "description", true));
        copy.addView(aiEditableField("معرفی کامل", "longDescription", true));
        copy.addView(aiEditableField("مزیت‌ها (فقط موارد تأییدشده)", "benefits", true));
        copy.addView(aiEditableField("ترکیبات", "ingredients", true));
        copy.addView(aiEditableField("روش مصرف", "usage", true));
        contentContainer.addView(copy); contentContainer.addView(space(12));
        LinearLayout seo = whiteCard();
        seo.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_AI, "معرفی و نمایش آنلاین"));
        seo.addView(space(8));
        seo.addView(aiEditableField("کلمات کلیدی", "keywords", true));
        seo.addView(aiEditableField("عنوان سئو", "seoTitle", false));
        seo.addView(aiEditableField("توضیح سئو", "seoDescription", true));
        seo.addView(aiEditableField("کپشن اینستاگرام", "instagramCaption", true));
        seo.addView(aiEditableField("توضیح یوتیوب", "youtubeCaption", true));
        seo.addView(aiEditableField("متن تلگرام", "telegramCaption", true));
        seo.addView(aiEditableField("متن واتس‌اپ", "whatsappCaption", true));
        contentContainer.addView(seo);
        contentContainer.addView(space(16));

        LinearLayout optionalMedia = whiteCard();
        optionalMedia.addView(sectionLabelWithIcon(Luxury3DIconView.ICON_CAMERA, "مرحلهٔ پایانی · تصویرسازی اختیاری"));
        optionalMedia.addView(space(6));
        optionalMedia.addView(text("پس از بررسی مشخصات، در صورت تمایل تصویر یا ویدئوی تبلیغاتی بسازید. برای ذخیرهٔ کالا این مرحله الزامی نیست.", 10f, COL_TEXT_MUTED, false));
        optionalMedia.addView(space(8));
        optionalMedia.addView(outlineDarkButton("گزینه‌های تصویر و ویدئو", Luxury3DIconView.ICON_CAMERA, new Runnable() {
            @Override public void run() { showFinalMediaOptions(); }
        }));
        contentContainer.addView(optionalMedia);
        contentContainer.addView(space(12));

        contentContainer.addView(primaryButton("ذخیره کالا در ویترین فروشگاه", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() {
                long price = parseLong(aiPriceInput == null ? "" : aiPriceInput.getText().toString());
                long old = parseLong(aiOldPriceInput == null ? "" : aiOldPriceInput.getText().toString());
                int stock = (int) Math.min(9999L, parseLong(aiStockInput == null ? "" : aiStockInput.getText().toString()));
                if (price <= 0) {
                    toast("قیمت فروش را وارد کنید.");
                    return;
                }
                if (d.nameFa.trim().isEmpty() || d.category.trim().isEmpty()) {
                    toast("نام و دسته‌بندی محصول را وارد کنید."); return;
                }
                String savedImagePath = !aiStudioPath.isEmpty() ? aiStudioPath : aiSourcePath;
                String error = db.saveAiProduct(null, null, d.nameFa, d.nameEn, d.brand, d.category,
                        d.description, d.longDescription, d.benefits, d.ingredients, d.usage,
                        d.country, d.volume, old, price, stock, true, d.imageKey, savedImagePath,
                        d.seoTitle, d.seoDescription, d.keywords, d.instagramCaption, d.youtubeCaption, d.telegramCaption, d.whatsappCaption);
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

    private View aiEditableField(final String label, final String fieldName, boolean multiline) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.addView(text(label, 10.5f, COL_TEXT_MUTED, true));
        card.addView(space(6));
        final EditText input = makeInput(label, InputType.TYPE_CLASS_TEXT | (multiline ? InputType.TYPE_TEXT_FLAG_MULTI_LINE : 0), false);
        try { Object value = AiProductStudio.Draft.class.getField(fieldName).get(aiDraft); input.setText(value == null ? "" : value.toString()); }
        catch (Exception e) { android.util.Log.e("ASIYE", "draft field", e); }
        if (multiline) { input.setSingleLine(false); input.setMinLines(2); input.setGravity(Gravity.TOP | Gravity.START); }
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (aiDraft == null) return;
                try { AiProductStudio.Draft.class.getField(fieldName).set(aiDraft, s.toString()); persistStudioDraft(); }
                catch (Exception e) { android.util.Log.e("ASIYE", "draft edit", e); }
            }
            public void afterTextChanged(Editable e) { }
        });
        card.addView(input);
        LinearLayout wrapper = new LinearLayout(this); wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(card); wrapper.addView(space(8)); return wrapper;
    }

    private void observeDraftNumber(EditText input, final String fieldName) {
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (aiDraft == null) return;
                long value = parseLong(s.toString());
                if ("stock".equals(fieldName)) aiDraft.stock = (int) Math.min(9999L, value);
                else if ("suggestedPrice".equals(fieldName)) aiDraft.suggestedPrice = value;
                else aiDraft.suggestedOldPrice = value;
                persistStudioDraft();
            }
            public void afterTextChanged(Editable e) { }
        });
    }

    private void showPriceLookup() {
        if (aiBusy || aiDraft == null) { toast("عملیات دیگری در حال انجام است."); return; }
        new AlertDialog.Builder(this).setTitle("روش استعلام قیمت")
            .setItems(new String[]{"جست‌وجوی رایگان ترب و دیجی‌کالا", "سرویس قیمت اختصاصی"}, new android.content.DialogInterface.OnClickListener() {
                @Override public void onClick(android.content.DialogInterface d, int which) {
                    if (which == 0) new AlertDialog.Builder(MainActivity.this).setTitle("جست‌وجوی رایگان و غیررسمی")
                        .setMessage("نام و برند کالا مستقیماً برای جست‌وجو به ترب و دیجی‌کالا ارسال می‌شود. دسترسی و صحت قیمت تضمین نیست؛ قیمت ترب، کمترین قیمت نمایش‌داده‌شده است. زمان فهرست، زمان دریافت است نه لزوماً زمان به‌روزرسانی قیمت. فقط کالاهای دقیقاً مشابه را انتخاب و قیمت را در سایت بررسی کنید.")
                        .setNegativeButton("انصراف", null)
                        .setPositiveButton("جست‌وجو", new android.content.DialogInterface.OnClickListener() { @Override public void onClick(android.content.DialogInterface dialog, int w) { runPriceLookup(null); } }).show();
                    else showCustomPriceLookup();
                }
            }).setNegativeButton("بستن", null).show();
    }

    private void showCustomPriceLookup() {
        final String endpoint = getSharedPreferences("market_price", MODE_PRIVATE).getString("endpoint", "");
        new AlertDialog.Builder(this).setTitle("سرویس قیمت اختصاصی")
                .setMessage(endpoint.isEmpty() ? "نشانی سرویس سازگار تنظیم نشده است." : "نام، برند و حجم یا وزن به سرویس تنظیم‌شده ارسال می‌شود. نتیجه نیازمند بررسی شماست.")
                .setNegativeButton("بستن", null)
                .setNeutralButton("تنظیم سرویس", new android.content.DialogInterface.OnClickListener() { @Override public void onClick(android.content.DialogInterface d, int w) { configurePriceEndpoint(); } })
                .setPositiveButton(endpoint.isEmpty() ? "تنظیم" : "ارسال و استعلام", new android.content.DialogInterface.OnClickListener() { @Override public void onClick(android.content.DialogInterface d, int w) { if (endpoint.isEmpty()) configurePriceEndpoint(); else runPriceLookup(endpoint); } }).show();
    }

    private void configurePriceEndpoint() {
        LinearLayout box = dialogBox();
        box.addView(dialogTitle("سرویس قیمت", "نشانی HTTPS سرویس سازگار را وارد کنید. فقط به سرویس قابل اعتماد خود متصل شوید؛ قرارداد پاسخ در مستندات برنامه آمده است."));
        final EditText input = makeInput("https://example.com/api/prices", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI, true);
        input.setText(getSharedPreferences("market_price", MODE_PRIVATE).getString("endpoint", "")); box.addView(input); box.addView(space(8));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ذخیرهٔ نشانی", Luxury3DIconView.ICON_CHECK, new Runnable() {
            @Override public void run() {
                try { String value = MarketPrice.endpoint(input.getText().toString());
                    getSharedPreferences("market_price", MODE_PRIVATE).edit().putString("endpoint", value).apply(); dialog.dismiss(); toast("سرویس ثبت شد؛ برای استعلام تأیید کنید.");
                } catch (Exception e) { toast("نشانی HTTPS معتبر بدون رمز یا پارامتر وارد کنید."); }
            }
        }));
        box.addView(ghostButton("حذف تنظیم سرویس", new Runnable() { @Override public void run() {
            getSharedPreferences("market_price", MODE_PRIVATE).edit().remove("endpoint").apply(); dialog.dismiss(); toast("تنظیم سرویس پاک شد.");
        } }));
        box.addView(ghostButton("انصراف", new Runnable() { @Override public void run() { dialog.dismiss(); } })); showRoundedDialog(dialog);
    }

    private void runPriceLookup(final String endpoint) {
        if (aiBusy || aiDraft == null) return;
        final AiProductStudio.Draft draft = aiDraft; final String name = draft.nameFa, brand = draft.brand, volume = draft.volume;
        final long priceAtStart = draft.suggestedPrice; final int generation = aiGeneration;
        if (name.trim().isEmpty()) { toast("ابتدا نام محصول را وارد کنید."); return; }
        aiBusy = true; toast("در حال دریافت پیشنهادهای قیمت…");
        new Thread(new Runnable() { @Override public void run() {
            MarketPrice.Offer[] found = null; String failed = "";
            if (endpoint == null) {
                java.util.ArrayList<MarketPrice.Offer> all = new java.util.ArrayList<>();
                String query = (name.trim() + " " + brand.trim()).trim();
                if (query.length() > 200) query = query.substring(0, 200);
                try { java.util.Collections.addAll(all, FreeMarketPrice.torob(FreeMarketPrice.get(false, query), System.currentTimeMillis())); }
                catch (Exception ignored) { failed += "ترب "; }
                try { java.util.Collections.addAll(all, FreeMarketPrice.digikala(FreeMarketPrice.get(true, query), System.currentTimeMillis())); }
                catch (Exception ignored) { failed += "دیجی‌کالا"; }
                found = all.toArray(new MarketPrice.Offer[0]);
            } else try {
                org.json.JSONObject body = new org.json.JSONObject().put("name", name).put("brand", brand).put("volume", volume);
                String response = AiHttpTransport.post(MarketPrice.endpoint(endpoint), "Accept", "application/json", body.toString(), 256 * 1024);
                found = MarketPrice.parse(response, System.currentTimeMillis());
            } catch (Exception ignored) { failed = "سرویس اختصاصی"; }
            final MarketPrice.Offer[] offers = found; final String unavailable = failed;
            runOnUiThread(new Runnable() { @Override public void run() {
                if (generation != aiGeneration || isFinishing()) return;
                aiBusy = false;
                if (draft != aiDraft || !name.equals(draft.nameFa) || !brand.equals(draft.brand) || !volume.equals(draft.volume) || draft.suggestedPrice != priceAtStart) { toast("اطلاعات تغییر کرد؛ نتیجهٔ قیمت اعمال نشد."); return; }
                if (offers == null || offers.length == 0) { new AlertDialog.Builder(MainActivity.this).setTitle("نتیجه‌ای دریافت نشد").setMessage("سرویس ممکن است محدود یا قطع باشد، یا کالایی پیدا نشده باشد. " + (unavailable.isEmpty() ? "" : "در دسترس نبود: " + unavailable) + " قیمت را می‌توانید پس از بررسی سایت‌ها دستی وارد کنید.").setPositiveButton("بستن", null).show(); return; }
                if (!unavailable.isEmpty()) toast("برخی منابع در دسترس نبودند: " + unavailable);
                final boolean[] chosen = new boolean[offers.length]; String[] labels = new String[offers.length];
                java.text.SimpleDateFormat date = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
                for (int i=0;i<offers.length;i++) labels[i]=offers[i].source + " · " + offers[i].title + "\n" + PersianFormatter.formatToman(offers[i].toman) + " · زمان دریافت/مشاهده: " + date.format(new java.util.Date(offers[i].observedAt)) + "\n" + offers[i].url;
                new AlertDialog.Builder(MainActivity.this).setTitle("فقط پیشنهادهای دقیقاً مشابه را انتخاب کنید")
                    .setMultiChoiceItems(labels, chosen, new android.content.DialogInterface.OnMultiChoiceClickListener() { @Override public void onClick(android.content.DialogInterface d, int which, boolean checked) { chosen[which] = checked; } })
                    .setNegativeButton("انصراف", null)
                    .setNeutralButton("بررسی لینک", new android.content.DialogInterface.OnClickListener() { @Override public void onClick(android.content.DialogInterface d, int w) {
                        String[] links = new String[offers.length];
                        for (int i=0;i<offers.length;i++) links[i]=offers[i].source + " · " + offers[i].title + "\n" + offers[i].url;
                        new AlertDialog.Builder(MainActivity.this).setTitle("انتخاب لینک برای بررسی در مرورگر").setItems(links, new android.content.DialogInterface.OnClickListener() { @Override public void onClick(android.content.DialogInterface dialog, int index) {
                            try { startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(offers[index].url))); } catch (Exception e) { toast("بازکردن مرورگر ممکن نیست."); }
                        }}).setNegativeButton("بستن", null).show();
                    }})
                    .setPositiveButton("اعمال قیمت یا میانگین", new android.content.DialogInterface.OnClickListener() { @Override public void onClick(android.content.DialogInterface d, int w) {
                        if (generation != aiGeneration || draft != aiDraft || !name.equals(draft.nameFa) || !brand.equals(draft.brand) || !volume.equals(draft.volume) || draft.suggestedPrice != priceAtStart) { toast("اطلاعات تغییر کرده است؛ دوباره استعلام کنید."); return; }
                        for (int i = 0; i < offers.length; i++) if (chosen[i] && (System.currentTimeMillis() - offers[i].observedAt > 86400000L || offers[i].observedAt > System.currentTimeMillis() + 300000L)) { toast("زمان پیشنهادها منقضی شده است؛ دوباره استعلام کنید."); return; }
                        int count = 0; long single = 0;
                        for (int i = 0; i < offers.length; i++) if (chosen[i]) { count++; single = offers[i].toman; }
                        if (count == 0) { toast("حداقل یک پیشنهاد مشابه را انتخاب کنید."); return; }
                        long selectedPrice = count == 1 ? single : MarketPrice.average(offers, chosen);
                        aiPriceInput.setText(String.valueOf(selectedPrice)); toast(count == 1 ? "یک قیمت پیشنهادی اعمال شد؛ میانگین نیست." : "میانگین پیشنهادهای انتخابی اعمال شد؛ قابل ویرایش است.");
                    }}).show();
            }});
        }}, "price-lookup").start();
    }

    private void showOnlineImageDialog() {
        LinearLayout box = dialogBox();
        box.addView(dialogTitle("عکس از اینترنت", "لینک مستقیم HTTPS یک تصویر را وارد کنید؛ صفحهٔ جست‌وجو یا لینک اشتراک پذیرفته نمی‌شود."));
        final EditText url = makeInput("https://example.com/photo.jpg", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI, true);
        box.addView(url); box.addView(space(8));
        box.addView(text("تصویر از سایت مربوط دریافت می‌شود؛ از مجوز استفاده از آن اطمینان حاصل کنید.", 10f, COL_TEXT_MUTED, false));
        box.addView(space(8));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("دریافت عکس", Luxury3DIconView.ICON_CAMERA, new Runnable() {
            @Override public void run() {
                final String link = url.getText().toString().trim();
                if (!link.toLowerCase(Locale.US).startsWith("https://")) { toast("لینک مستقیم HTTPS وارد کنید."); return; }
                if (aiBusy) { toast("عملیات دیگری در حال انجام است."); return; }
                dialog.dismiss(); aiBusy = true; final int generation = aiGeneration;
                toast("در حال دریافت عکس…");
                new Thread(new Runnable() { public void run() {
                    Bitmap bitmap = null; String path = "";
                    try {
                        bitmap = StudioNetwork.downloadImage(link);
                        java.io.File f = LuxuryImageStudio.saveToPrivateStorage(MainActivity.this, bitmap);
                        if (f != null) path = f.getAbsolutePath();
                    } catch (Exception e) { android.util.Log.w("ASIYE", "online image", e); }
                      catch (OutOfMemoryError e) { android.util.Log.w("ASIYE", "online image too large", e); }
                    final Bitmap result = bitmap; final String savedPath = path;
                    runOnUiThread(new Runnable() { public void run() {
                        if (generation != aiGeneration || isFinishing()) return;
                        aiBusy = false;
                        if (result == null || savedPath.isEmpty()) { toast("دریافت عکس ناموفق بود؛ لینک مستقیم، نوع تصویر و اتصال را بررسی کنید."); return; }
                        aiSourceBitmap = result; aiSourcePath = savedPath; aiStudioPath = ""; persistStudioDraft();
                        toast("عکس آنلاین دریافت شد."); if (aiStudioOpen && adminMode) renderCurrentScreen();
                    }});
                }}).start();
            }
        }));
        showRoundedDialog(dialog);
    }

    private String aiServiceLabel(String id) {
        return AiProviderOrder.label(id);
    }

    private void showAiServices() {
        final String current = getSharedPreferences("ai_services", MODE_PRIVATE).getString("preferred", "gemini");
        LinearLayout box = dialogBox();
        box.addView(dialogTitle("سرویس‌های هوشمند", "اولویت: " + aiServiceLabel(AiProviderOrder.forPreference(current)[0]) + " · در صورت نیاز، تنها Gemini، OpenAI و Groq امتحان می‌شوند."));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(outlineDarkButton("تعویض کلید روی دستگاه", Luxury3DIconView.ICON_AI, new Runnable() {
            @Override public void run() { dialog.dismiss(); chooseCredential(); }
        }));
        box.addView(space(8));
        box.addView(outlineDarkButton("انتخاب سرویس اول", Luxury3DIconView.ICON_AI, new Runnable() {
            @Override public void run() { dialog.dismiss(); chooseAiPriority(); }
        }));
        box.addView(space(8));
        box.addView(primaryButton("آزمون همهٔ سرویس‌های متنی", Luxury3DIconView.ICON_AI, new Runnable() {
            @Override public void run() { dialog.dismiss(); startAiProbes(AiProviderOrder.IDS); }
        }));
        box.addView(space(8));
        box.addView(ghostButton("آزمون یک سرویس متنی", new Runnable() {
            @Override public void run() { dialog.dismiss(); chooseSingleProbe(); }
        }));
        box.addView(space(8));
        box.addView(ghostButton("بستن", new Runnable() { @Override public void run() { dialog.dismiss(); } }));
        showRoundedDialog(dialog);
    }

    private void chooseCredential() {
        final String[] ids = {"gemini", "openai", "groq"};
        final String[] labels = {"Gemini", "OpenAI", "Groq"};
        new AlertDialog.Builder(this).setTitle("تعویض کلید روی دستگاه")
            .setItems(labels, new android.content.DialogInterface.OnClickListener() {
                @Override public void onClick(android.content.DialogInterface d, int which) { editCredential(ids[which], labels[which]); }
            }).setNegativeButton("انصراف", null).show();
    }

    private void editCredential(final String id, final String label) {
        LinearLayout box = dialogBox();
        String status = StudioCredentials.isOverrideActive(this, id) ? "کلید محلی فعال است." : StudioCredentials.hasOverride(this, id) ? "کلید محلی قبلی حفظ شده ولی کلید جدید بسته فعال است." : "در حال استفاده از کلید داخل بسته هستید.";
        box.addView(dialogTitle(label, status + " مقدار فعلی نمایش داده نمی‌شود. کلید جایگزین فقط روی همین دستگاه رمزگذاری می‌شود؛ کلید قبلی همچنان داخل APK قابل استخراج است."));
        final EditText input = makeInput("مقدار جدید", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS, false);
        input.setSingleLine(true); input.setSaveEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= 26) input.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        box.addView(input); box.addView(space(8));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ذخیرهٔ کلید جدید", Luxury3DIconView.ICON_AI, new Runnable() {
            @Override public void run() {
                String value = input.getText().toString();
                if (value.trim().isEmpty()) { toast("مقدار جدید را وارد کنید."); return; }
                try {
                    StudioCredentials.save(MainActivity.this, id, value);
                    input.setText(""); dialog.dismiss(); toast("ذخیره شد؛ اکنون اتصال را آزمون کنید."); showAiServices();
                } catch (Exception e) { toast("ذخیره امن ممکن نشد؛ تنظیمات امنیتی دستگاه را بررسی کنید."); }
            }
        }));
        if (StudioCredentials.hasOverride(this, id)) {
            box.addView(space(8));
            final boolean localActive = StudioCredentials.isOverrideActive(this, id);
            box.addView(ghostButton(localActive ? "استفاده از کلید بسته؛ حفظ کلید محلی" : "فعال‌سازی کلید محلی ذخیره‌شده", new Runnable() {
                @Override public void run() {
                    try { StudioCredentials.setOverrideEnabled(MainActivity.this, id, !localActive); dialog.dismiss(); toast("انتخاب ذخیره شد؛ اتصال را آزمایش کنید."); showAiServices(); }
                    catch (Exception e) { toast("تغییر کلید فعال ممکن نشد."); }
                }
            }));
        }
        box.addView(space(8)); box.addView(ghostButton("انصراف", new Runnable() { @Override public void run() { dialog.dismiss(); } }));
        showRoundedDialog(dialog);
    }

    private void chooseSingleProbe() {
        final String[] ids = AiProviderOrder.IDS;
        String[] labels = new String[ids.length];
        for (int i = 0; i < ids.length; i++) labels[i] = AiProviderOrder.label(ids[i]);
        new AlertDialog.Builder(this).setTitle("آزمون یک سرویس")
            .setItems(labels, new android.content.DialogInterface.OnClickListener() {
                @Override public void onClick(android.content.DialogInterface d, int which) { startAiProbes(new String[]{ids[which]}); }
            }).setNegativeButton("انصراف", null).show();
    }

    private void chooseAiPriority() {
        final String[] ids = AiProviderOrder.IDS;
        final String[] labels = new String[ids.length];
        final String current = getSharedPreferences("ai_services", MODE_PRIVATE).getString("preferred", "gemini");
        int selected = 0;
        for (int i = 0; i < ids.length; i++) { labels[i] = AiProviderOrder.label(ids[i]) + (AiProviderOrder.supportsImage(ids[i]) ? " · متن و تصویر" : " · فقط متن"); if (ids[i].equals(current)) selected = i; }
        new AlertDialog.Builder(this).setTitle("سرویس اول برای استودیو")
                .setSingleChoiceItems(labels, selected,
                    new android.content.DialogInterface.OnClickListener() {
                        @Override public void onClick(android.content.DialogInterface dialog, int which) {
                            getSharedPreferences("ai_services", MODE_PRIVATE).edit().putString("preferred", ids[which]).apply();
                            dialog.dismiss(); showAiServices();
                        }
                    }).setNegativeButton("انصراف", null).show();
    }

    private void startAiProbes(String[] providers) {
        final LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(COL_WHITE);
        box.setPadding(dp(20), dp(12), dp(20), dp(12));
        TextView hint = text("آزمون متنی با دادهٔ نمونه؛ بدون عکس یا اطلاعات محصول. ممکن است هزینه ایجاد شود.", 11f, COL_TEXT_MUTED, false);
        box.addView(hint);
        ScrollView scroll = new ScrollView(this); scroll.addView(box);
        final AlertDialog dialog = new AlertDialog.Builder(this).setTitle("نتیجهٔ آزمون سرویس‌ها")
                .setView(scroll).setPositiveButton("بستن", null).create();
        final Context app = getApplicationContext();
        final List<Thread> jobs = new ArrayList<>();
        for (final String provider : providers) {
            box.addView(space(12));
            final TextView result = text(aiServiceLabel(provider) + " — در حال اتصال…", 11f, COL_TEXT_MAIN, false);
            result.setTextIsSelectable(true); result.setLineSpacing(dp(3), 1f); box.addView(result);
            jobs.add(new Thread(new Runnable() { @Override public void run() {
                final String value = DirectAiStudio.probe(app, provider);
                runOnUiThread(new Runnable() { @Override public void run() {
                    if (!isFinishing() && dialog.isShowing()) result.setText(aiServiceLabel(provider) + " — " + value);
                }});
            }}, "ai-probe-" + provider));
        }
        dialog.show();
        for (Thread job : jobs) job.start();
    }

    private void showFinalMediaOptions() {
        if (aiBusy || aiDraft == null) { toast("عملیات دیگری در حال انجام است."); return; }
        // Local rendering does not use an external AI provider or modify the original photo.
        generateStudioImage();
    }

    private void cancelCloudAnalysis() {
        if (!aiAnalysisRunning) return;
        aiGeneration++; aiAnalysisRunning = false; aiBusy = false;
        toast("انتظار لغو شد؛ درخواست آغازشده ممکن است همچنان هزینه داشته باشد.");
        if (aiStudioOpen && adminMode) renderCurrentScreen();
    }

    private void confirmCloudAnalysis() {
        final String name = aiName.trim(), brand = aiBrand.trim();
        if (name.length() < 3) { toast("نام محصول را کامل وارد کنید."); return; }
        if (aiBusy) { toast("عملیات دیگری در حال انجام است."); return; }
        LinearLayout box = dialogBox();
        LinearLayout heading = row(Gravity.CENTER_VERTICAL);
        heading.addView(iconMedallion(Luxury3DIconView.ICON_AI, 40, Luxury3DIconView.PEDESTAL_GOLD));
        TextView title = text("تحلیل هوشمند محصول", 15f, COL_TEXT_MAIN, true);
        title.setPadding(dp(10), 0, dp(10), 0); heading.addView(title, weight(1f)); box.addView(heading); box.addView(space(12));
        TextView privacy = text("نام، برند و عکس انتخابی به Gemini، OpenAI یا Groq ارسال می‌شود. در صورت نیاز سرویس بعدی امتحان می‌شود؛ سیاست داده و هزینهٔ هر سرویس متفاوت است. خروجی را پیش از انتشار بررسی کنید.", 11f, COL_TEXT_MUTED, false);
        privacy.setLineSpacing(dp(3), 1f); box.addView(privacy); box.addView(space(14));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ارسال و تحلیل", Luxury3DIconView.ICON_AI, new Runnable() { @Override public void run() { dialog.dismiss(); runCloudAnalysis(name, brand); } }));
        box.addView(ghostButton("انصراف", new Runnable() { @Override public void run() { dialog.dismiss(); } }));
        showRoundedDialog(dialog);
    }

    private void confirmTextOnlyAnalysis(final String name, final String brand) {
        if (aiBusy || aiDraft != null || !name.equals(aiName.trim()) || !brand.equals(aiBrand.trim())) {
            toast("ورودی تغییر کرده است؛ دوباره تحلیل را آغاز کنید."); return;
        }
        new AlertDialog.Builder(this).setTitle("تحلیل فقط نام و برند؟")
                .setMessage("عکس فعلی حفظ می‌شود ولی در این تلاش ارسال یا تحلیل نمی‌شود. نام و برند به سرویس‌های انتخابی و جایگزین ارسال می‌شوند؛ ممکن است هزینه داشته باشند. نتیجه فقط یک پیش‌نویس نیازمند بازبینی است.")
                .setNegativeButton("انصراف", null)
                .setPositiveButton("ارسال فقط متن", new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface dialog, int which) { runCloudAnalysis(name, brand, true); }
                }).show();
    }

    private void runCloudAnalysis(final String name, final String brand) {
        runCloudAnalysis(name, brand, false);
    }

    private void runCloudAnalysis(final String name, final String brand, final boolean textOnly) {
        if (aiBusy) return;
        aiBusy = true; aiAnalysisRunning = true; final int generation = aiGeneration; final Bitmap photo = textOnly ? null : aiSourceBitmap;
        final String sourceAtRequest = aiSourcePath;
        final String preferred = getSharedPreferences("ai_services", MODE_PRIVATE).getString("preferred", "gemini");
        toast("در حال تحلیل توسط سرویس…");
        if (aiStudioOpen && adminMode) renderCurrentScreen();
        new Thread(new Runnable() { public void run() {
            AiProductStudio.Draft draft = null;
            String failure = "تحلیل انجام نشد؛ اتصال اینترنت، دسترسی و اعتبار کلیدها یا سهمیهٔ سرویس‌ها را بررسی کنید.";
            try { draft = DirectAiStudio.generate(MainActivity.this, name, brand, photo, preferred, new DirectAiStudio.Cancellation() {
                @Override public boolean isCancelled() { return generation != aiGeneration; }
            }); }
            catch (DirectAiStudio.ProvidersFailedException e) { /* diagnostics remain available in settings */ }
            catch (Exception e) { android.util.Log.e("ASIYE", "direct AI failed: " + e.getClass().getSimpleName()); }
            catch (OutOfMemoryError e) { failure = "حافظه برای پردازش عکس کافی نیست؛ تصویر کوچک‌تری انتخاب کنید."; }
            final AiProductStudio.Draft result = draft;
            final String errorText = failure;
            runOnUiThread(new Runnable() { public void run() {
                if (generation != aiGeneration || isFinishing()) return;
                aiBusy = false; aiAnalysisRunning = false;
                if (result == null) {
                    if (aiStudioOpen && adminMode) {
                        renderCurrentScreen();
                        AlertDialog.Builder errorDialog = new AlertDialog.Builder(MainActivity.this).setTitle("تحلیل در دسترس نیست")
                                .setMessage(errorText + " اطلاعات واردشده حفظ شده است.")
                                .setPositiveButton("متوجه شدم", null);
                        if (photo != null && name.equals(aiName.trim()) && brand.equals(aiBrand.trim()) && sourceAtRequest.equals(aiSourcePath)) {
                            errorDialog.setNeutralButton("آزمون فقط با نام و برند", new android.content.DialogInterface.OnClickListener() {
                                @Override public void onClick(android.content.DialogInterface dialog, int which) { confirmTextOnlyAnalysis(name, brand); }
                            });
                        }
                        errorDialog.show();
                    } else toast("تحلیل انجام نشد؛ دوباره به استودیو مراجعه کنید.");
                    return;
                }
                if (!name.equals(aiName.trim()) || !brand.equals(aiBrand.trim()) || !sourceAtRequest.equals(aiSourcePath) || aiDraft != null) {
                    toast("ورودی‌ها تغییر کردند؛ پاسخ قدیمی اعمال نشد.");
                    if (aiStudioOpen && adminMode) renderCurrentScreen();
                    return;
                }
                aiAnalysis = AiProductStudio.analyze(name, brand, null); aiDraft = result;
                aiDraft.provenance = photo == null ? "تحلیل فقط بر اساس نام و برند؛ عکس بررسی نشده است." : "در درخواست اولیه عکس ارسال شده است؛ تطابق عکس و مشخصات را بررسی کنید.";
                persistStudioDraft(); toast(textOnly ? "پیش‌نویس فقط از نام و برند ساخته شد؛ عکس تحلیل نشد." : "پیش‌نویس دریافت شد؛ تمام اطلاعات را بررسی کنید.");
                if (aiStudioOpen && adminMode) {
                    renderCurrentScreen();
                    if (textOnly) new AlertDialog.Builder(MainActivity.this).setTitle("پیش‌نویس بدون تحلیل عکس")
                            .setMessage("این پیش‌نویس فقط بر اساس نام و برند ساخته شده است. عکس انتخابی نگه داشته شده اما به هوش مصنوعی ارسال و تحلیل نشده؛ تمام مشخصات را بررسی کنید.")
                            .setPositiveButton("متوجه شدم", null).show();
                }
            }});
        }}).start();
    }

    private void pickProductPhoto() {
        if (aiBusy) { toast("عملیات دیگری در حال انجام است."); return; }
        persistStudioDraft();
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
        if (requestCode != REQ_PICK_PHOTO || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        final android.net.Uri uri = data.getData();
        final int generation = aiGeneration;
        aiBusy = true;
        new Thread(new Runnable() { public void run() {
            Bitmap bitmap = null; String path = "";
            try {
                BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
                try (java.io.InputStream stream = getContentResolver().openInputStream(uri)) {
                    if (stream == null) throw new java.io.IOException("empty image stream");
                    BitmapFactory.decodeStream(stream, null, bounds);
                }
                int maxSide = Math.max(bounds.outWidth, bounds.outHeight);
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0 || maxSide > 20000) throw new java.io.IOException("invalid image dimensions");
                BitmapFactory.Options opts = new BitmapFactory.Options(); opts.inSampleSize = 1;
                while (maxSide / opts.inSampleSize > 1200) opts.inSampleSize *= 2;
                try (java.io.InputStream stream = getContentResolver().openInputStream(uri)) {
                    if (stream == null) throw new java.io.IOException("empty image stream");
                    bitmap = BitmapFactory.decodeStream(stream, null, opts);
                }
                if (bitmap != null) {
                    // Gallery/camera JPEGs often store orientation in EXIF rather than pixel order.
                    try (java.io.InputStream metadata = getContentResolver().openInputStream(uri)) {
                        if (metadata != null) {
                            android.media.ExifInterface exif = new android.media.ExifInterface(metadata);
                            int orientation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION,
                                    android.media.ExifInterface.ORIENTATION_NORMAL);
                            android.graphics.Matrix transform = new android.graphics.Matrix();
                            switch (orientation) {
                                case android.media.ExifInterface.ORIENTATION_ROTATE_90: transform.postRotate(90); break;
                                case android.media.ExifInterface.ORIENTATION_ROTATE_180: transform.postRotate(180); break;
                                case android.media.ExifInterface.ORIENTATION_ROTATE_270: transform.postRotate(270); break;
                                case android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL: transform.postScale(-1, 1); break;
                                case android.media.ExifInterface.ORIENTATION_FLIP_VERTICAL: transform.postScale(1, -1); break;
                                case android.media.ExifInterface.ORIENTATION_TRANSPOSE: transform.postRotate(90); transform.postScale(-1, 1); break;
                                case android.media.ExifInterface.ORIENTATION_TRANSVERSE: transform.postRotate(270); transform.postScale(-1, 1); break;
                                default: break;
                            }
                            if (!transform.isIdentity()) bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), transform, true);
                        }
                    } catch (Exception ignored) { /* Non-JPEG or no metadata: retain decoded pixels. */ }
                    java.io.File stored = LuxuryImageStudio.saveToPrivateStorage(MainActivity.this, bitmap);
                    if (stored != null) path = stored.getAbsolutePath();
                }
            } catch (Exception e) { android.util.Log.w("ASIYE", "gallery image", e); }
              catch (OutOfMemoryError e) { android.util.Log.w("ASIYE", "gallery image too large", e); }
            final Bitmap result = bitmap; final String storedPath = path;
            runOnUiThread(new Runnable() { public void run() {
                if (generation != aiGeneration || isFinishing()) return;
                aiBusy = false;
                if (result == null || storedPath.isEmpty()) { toast("خواندن یا ذخیره عکس ممکن نشد؛ تصویر دیگری انتخاب کنید."); return; }
                aiSourceBitmap = result; aiSourcePath = storedPath; aiStudioPath = "";
                persistStudioDraft(); toast("عکس محصول ذخیره شد.");
                if (aiStudioOpen && adminMode) renderCurrentScreen();
            }});
        }}).start();
    }

    private void showVideoMenu() {
        final String[] providers = {"kling", "piapi", "kling", "piapi"};
        new AlertDialog.Builder(this).setTitle("ویدئوی تبلیغاتی")
            .setItems(new String[]{"ثبت ویدئو · Kling", "ثبت ویدئو · PiAPI", "پیگیری آخرین ویدئو · Kling", "پیگیری آخرین ویدئو · PiAPI"},
                new android.content.DialogInterface.OnClickListener() { @Override public void onClick(android.content.DialogInterface d, int which) {
                    if (which < 2) videoPrompt(providers[which]); else checkVideo(providers[which]);
                }}).setNegativeButton("بستن", null).show();
    }

    private void videoPrompt(final String provider) {
        if (aiBusy) { toast("عملیات دیگری در حال انجام است."); return; }
        LinearLayout box = dialogBox();
        box.addView(dialogTitle("ثبت ویدئو", "درخواست هزینه‌دار است و ممکن است با محصول واقعی متفاوت باشد. ابتدا سرویس انتخابی و در صورت رد قطعی، سرویس دیگر (Kling یا PiAPI) امتحان می‌شود و شرح شما به هر دو ممکن است ارسال شود. با قطعی یا پاسخ نامعلوم تلاش بعدی متوقف می‌شود؛ قبل از تکرار پنل را بررسی کنید. هر ثبت جدید شناسهٔ قبلی همان سرویس را جایگزین می‌کند."));
        final EditText prompt = makeInput("شرح ویدئو", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, false);
        prompt.setSingleLine(false); prompt.setMinLines(3); box.addView(prompt); box.addView(space(8));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ثبت درخواست هزینه‌دار", Luxury3DIconView.ICON_AI, new Runnable() { @Override public void run() {
            final String value = prompt.getText().toString().trim();
            if (value.isEmpty() || value.length() > 1000) { toast("شرح ویدئو را بین ۱ تا ۱۰۰۰ نویسه وارد کنید."); return; }
            if (aiBusy) return; dialog.dismiss(); aiBusy = true; toast("در حال ثبت درخواست…");
            final int generation = aiGeneration;
            final Context app = getApplicationContext();
            new Thread(new Runnable() { @Override public void run() {
                String task = "", used = ""; String error = "ثبت ناموفق بود؛ پیش از تکرار پنل سرویس را بررسی کنید.";
                try {
                    CreationFallback.Result<String> outcome = CreationFallback.run(CreationFallback.preferredFirst(provider, new String[]{"kling", "piapi"}),
                            new CreationFallback.Attempt<String>() { @Override public String run(String id) throws Exception { return AiVideo.submit(app, id, value); } }, null);
                    task = outcome.value; used = outcome.provider;
                    app.getSharedPreferences("ai_video", MODE_PRIVATE).edit().putString(used, task).apply();
                } catch (CreationFallback.Failed e) {
                    error = e.uncertain ? "نتیجهٔ " + e.provider + " نامعلوم است و ممکن است درخواست ثبت یا هزینه شده باشد. سرویس بعدی خودکار اجرا نشد؛ پیش از تکرار پنل را بررسی کنید." : "سرویس‌ها درخواست را رد کردند؛ اعتبار و سهمیه را بررسی کنید.\n" + e.attempts;
                } catch (Exception ignored) { }
                final String id = task; final String message = error; final String usedProvider = used;
                runOnUiThread(new Runnable() { @Override public void run() {
                    if (generation != aiGeneration || isFinishing()) return;
                    aiBusy = false;
                    new AlertDialog.Builder(MainActivity.this).setTitle(id.isEmpty() ? "ثبت ناموفق" : "درخواست ثبت شد")
                        .setMessage(id.isEmpty() ? message : "سرویس: " + usedProvider + "\nشناسهٔ درخواست ذخیره شد. از بخش پیگیری همان سرویس وضعیت را بررسی کنید.\n" + id)
                        .setPositiveButton("بستن", null).show();
                }});
            }}, "video-submit").start();
        }})); showRoundedDialog(dialog);
    }

    private void checkVideo(final String provider) {
        if (aiBusy) { toast("عملیات دیگری در حال انجام است."); return; }
        final Context app = getApplicationContext();
        final String task = app.getSharedPreferences("ai_video", MODE_PRIVATE).getString(provider, "");
        if (task.isEmpty()) { toast("درخواستی برای این سرویس ذخیره نشده است."); return; }
        aiBusy = true; toast("در حال بررسی وضعیت…");
        final int generation = aiGeneration;
        new Thread(new Runnable() { @Override public void run() {
            String[] state = null; String error = "وضعیت دریافت نشد؛ مجوز، اتصال و سهمیه را بررسی کنید.";
            try { state = AiVideo.status(app, provider, task); }
            catch (DirectAiStudio.ProviderHttpException e) { error = "HTTP " + e.status + "؛ اتصال یا دسترسی حساب را بررسی کنید."; }
            catch (Exception ignored) { }
            final String[] result = state; final String failure = error;
            runOnUiThread(new Runnable() { @Override public void run() {
                if (generation != aiGeneration || isFinishing()) return;
                aiBusy = false;
                String videoMessage = result == null ? failure : "وضعیت: " + videoStatusLabel(result[0]) + "\nشناسه: " + task
                        + ("failed".equals(result[0]) ? "\nدرخواست ناموفق بود. پیش از ثبت دوباره، وضعیت و هزینه را در پنل سرویس بررسی کنید." : result[1].isEmpty() ? "\nلینک ویدئوی نهایی در پاسخ موجود نیست." : "\nبرای مشاهده و ذخیره، لینک را باز کنید.");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setTitle("وضعیت ویدئو")
                    .setMessage(videoMessage)
                    .setNegativeButton("بستن", null);
                if (result != null && !result[1].isEmpty()) builder.setPositiveButton("باز کردن ویدئو", new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int w) { try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result[1]))); } catch (Exception e) { toast("باز کردن ویدئو ممکن نشد."); } }
                });
                builder.show();
            }});
        }}, "video-status").start();
    }

    private String videoStatusLabel(String state) {
        if ("submitted".equals(state) || "pending".equals(state)) return "در صف";
        if ("processing".equals(state)) return "در حال ساخت";
        if ("succeeded".equals(state) || "completed".equals(state)) return "تکمیل‌شده";
        if ("failed".equals(state)) return "ناموفق";
        return "نامشخص";
    }

    private void chooseMediaProvider() {
        if (aiBusy || aiDraft == null) { toast("عملیات دیگری در حال انجام است."); return; }
        final String[] ids = {"fal", "deepai", "byteplus", "stability"};
        new AlertDialog.Builder(this).setTitle("سرویس تصویر تبلیغاتی")
            .setItems(new String[]{"fal · FLUX", "DeepAI · text2img", "BytePlus · Seedream", "Stability · Stable Image"}, new android.content.DialogInterface.OnClickListener() {
                @Override public void onClick(android.content.DialogInterface d, int which) { confirmMediaGeneration(ids[which]); }
            }).setNegativeButton("انصراف", null).show();
    }

    private void confirmMediaGeneration(final String provider) {
        LinearLayout box = dialogBox();
        box.addView(dialogTitle("تصویر تبلیغاتی", "این عکس ساختگی است و ممکن است با کالای واقعی متفاوت باشد. شرح شما ابتدا به سرویس انتخابی و در صورت رد قطعی به fal، DeepAI، BytePlus یا Stability ارسال می‌شود؛ درخواست‌ها ممکن است هزینه داشته باشند. اگر نتیجهٔ درخواست نامعلوم باشد تلاش بعدی متوقف می‌شود؛ قبل از تکرار پنل را بررسی کنید."));
        final EditText prompt = makeInput("شرح تصویر تبلیغاتی", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, false);
        prompt.setSingleLine(false); prompt.setMinLines(3);
        prompt.setText("A refined luxury product advertising background, warm studio lighting, no labels, no text, no people.");
        box.addView(prompt); box.addView(space(8));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("ارسال و ساخت تصویر", Luxury3DIconView.ICON_CAMERA, new Runnable() {
            @Override public void run() {
                final String text = prompt.getText().toString().trim();
                if (text.isEmpty() || text.length() > 2000) { toast("شرح تصویر را بین ۱ تا ۲۰۰۰ نویسه وارد کنید."); return; }
                if (aiBusy) return;
                dialog.dismiss(); aiBusy = true; final int generation = aiGeneration;
                toast("در حال ساخت تصویر تبلیغاتی…");
                new Thread(new Runnable() { @Override public void run() {
                    Bitmap bitmap = null; String used = ""; String failure = "خطا در دریافت تصویر؛ حساب، مدل و اینترنت را بررسی کنید.";
                    try {
                        CreationFallback.Result<Bitmap> outcome = CreationFallback.run(CreationFallback.preferredFirst(provider, new String[]{"fal", "deepai", "byteplus", "stability"}),
                                new CreationFallback.Attempt<Bitmap>() { @Override public Bitmap run(String id) throws Exception { return AiMedia.generateImage(getApplicationContext(), id, text); } },
                                new CreationFallback.Cancelled() { @Override public boolean isCancelled() { return generation != aiGeneration; } });
                        bitmap = outcome.value; used = outcome.provider;
                    } catch (CreationFallback.Failed e) { failure = e.uncertain ? "نتیجهٔ " + e.provider + " نامعلوم است و ممکن است هزینه شده باشد. سرویس بعدی اجرا نشد؛ پیش از تکرار پنل را بررسی کنید." : "سرویس‌ها درخواست را رد کردند؛ اعتبار و سهمیه را بررسی کنید.\n" + e.attempts; }
                      catch (Exception e) { failure = "پاسخ یا اتصال سرویس قابل استفاده نبود؛ درخواست ممکن است هزینه داشته باشد."; }
                      catch (OutOfMemoryError e) { failure = "حافظه برای تصویر کافی نبود."; }
                    final Bitmap result = bitmap; final String error = failure; final String usedProvider = used;
                    runOnUiThread(new Runnable() { @Override public void run() {
                        if (generation != aiGeneration || isFinishing()) return;
                        aiBusy = false;
                        if (result == null) { new AlertDialog.Builder(MainActivity.this).setTitle("تصویر ساخته نشد").setMessage(error).setPositiveButton("بستن", null).show(); return; }
                        if (!aiStudioOpen || !adminMode || aiDraft == null) { toast("تصویر دریافت شد اما استودیو بسته است؛ چیزی اعمال نشد."); return; }
                        toast("تصویر توسط " + usedProvider + " دریافت شد؛ آن را بررسی کنید.");
                        previewMediaImage(result, generation);
                    }});
                }}, "media-generation").start();
            }
        }));
        showRoundedDialog(dialog);
    }

    private void previewMediaImage(final Bitmap result, final int generation) {
        LinearLayout box = dialogBox();
        box.addView(dialogTitle("بازبینی تصویر ساختگی", "تصویر تولیدشده ممکن است محصول واقعی را نشان ندهد. فقط در صورت مناسب بودن از آن استفاده کنید."));
        ImageView preview = new ImageView(this); preview.setImageBitmap(result); preview.setAdjustViewBounds(true);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        box.addView(preview, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(220)));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(box).create();
        box.addView(primaryButton("استفاده از این تصویر", Luxury3DIconView.ICON_CAMERA, new Runnable() {
            @Override public void run() {
                if (generation != aiGeneration || aiDraft == null) { dialog.dismiss(); return; }
                try {
                    java.io.File f = LuxuryImageStudio.saveToPrivateStorage(MainActivity.this, result);
                    if (f == null) throw new IllegalStateException();
                    aiStudioPath = f.getAbsolutePath(); persistStudioDraft(); dialog.dismiss(); renderCurrentScreen();
                } catch (Exception e) { toast("ذخیرهٔ تصویر ممکن نشد."); }
            }
        }));
        box.addView(ghostButton("انصراف", new Runnable() { @Override public void run() { dialog.dismiss(); } }));
        showRoundedDialog(dialog);
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
        final int generation = aiGeneration;
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
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (generation != aiGeneration || isFinishing()) return;
                        aiBusy = false;
                        if (finalPath.isEmpty()) {
                            toast("ساخت تصویر ممکن نشد؛ از عکس اصلی استفاده می‌شود.");
                        } else {
                            aiStudioPath = finalPath;
                            persistStudioDraft();
                            toast("تصویر استودیویی اختصاصی ساخته شد.");
                            if (aiStudioOpen && adminMode) renderCurrentScreen();
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

    /** v6.4 · a barely-there tactile tick on every decisive touch. */
    private void tap() {
        try {
            getWindow().getDecorView().performHapticFeedback(
                    android.view.HapticFeedbackConstants.VIRTUAL_KEY);
        } catch (Throwable ignored) { }
    }

    /** v6.4 · staggered editorial entrance: cards arrive, they never blink into place. */
    private void reveal(View view, int index) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(dp(16));
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(430)
                .setStartDelay(Math.min(index, 7) * 60L)
                .setInterpolator(new android.view.animation.DecelerateInterpolator(1.6f))
                .start();
    }

    /** A standalone 3D medallion, sized in dp — used as a crest beside section titles. */
    private FrameLayout iconMedallion(int iconType, int sizeDp, int pedestal) {
        FrameLayout holder = new FrameLayout(this);
        holder.setLayoutParams(new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp)));
        holder.addView(new Luxury3DIconView(this, iconType, pedestal),
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return holder;
    }

    /**
     * v6.3 — every page now opens with a 24K crest medallion instead of a bare headline.
     * Exactly one icon per screen keeps the composition calm and editorial.
     */
    private LinearLayout crestTitle(int iconType, String title, String subtitle) {
        return crestTitle(iconType, title, subtitle, null);
    }

    /**
     * v6.7 · editorial heading: the Persian title keeps the sans face,
     * a small serif Latin kicker above it carries the couture signature.
     */
    private LinearLayout crestTitle(int iconType, String title, String subtitle, String kicker) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.HORIZONTAL);
        wrap.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        wrap.setGravity(Gravity.CENTER_VERTICAL);
        wrap.setPadding(dp(2), dp(2), dp(2), 0);

        FrameLayout crest = iconMedallion(iconType, 52, Luxury3DIconView.PEDESTAL_OBSIDIAN);
        ((Luxury3DIconView) crest.getChildAt(0)).enableSweep(true);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(12), 0, dp(12), 0);

        if (kicker != null && !kicker.isEmpty()) {
            TextView k = new TextView(this);
            k.setText(kicker);
            k.setTextColor(COL_GOLD_DEEP);
            k.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.2f);
            k.setTypeface(Typeface.SERIF, Typeface.BOLD);
            tracking(k, 0.24f);
            k.setIncludeFontPadding(false);
            k.setPadding(0, 0, 0, dp(3));
            texts.addView(k);
        }

        TextView t = text(title, 18.5f, COL_TEXT_MAIN, true);
        t.setLineSpacing(dp(2), 1f);
        TextView s = text(subtitle, 10.6f, COL_TEXT_MUTED, false);
        s.setLineSpacing(dp(3), 1f);
        s.setPadding(0, dp(5), 0, 0);

        texts.addView(t);
        texts.addView(s);

        wrap.addView(crest);
        wrap.addView(texts, weight(1f));
        return wrap;
    }

    /** Small gold hairline with a diamond in the middle — separates editorial blocks. */
    private View editorialDivider() {
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        line.setGravity(Gravity.CENTER);
        line.setPadding(0, dp(4), 0, dp(4));

        View left = new View(this);
        left.setBackgroundColor(lineGold(0x2E));
        line.addView(left, new LinearLayout.LayoutParams(0, dp(1), 1f));

        TextView gem = text("◆", 7.5f, COL_METAL, false);
        gem.setPadding(dp(8), 0, dp(8), 0);
        line.addView(gem);

        View right = new View(this);
        right.setBackgroundColor(lineGold(0x2E));
        line.addView(right, new LinearLayout.LayoutParams(0, dp(1), 1f));
        return line;
    }

    /** Card heading with a small 3D crest, so every block carries the icon language. */
    private LinearLayout sectionLabelWithIcon(int iconType, String label) {
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        line.setGravity(Gravity.CENTER_VERTICAL);

        line.addView(iconMedallion(iconType, 24, Luxury3DIconView.PEDESTAL_GOLD));

        TextView t = text(label, 12.2f, COL_TEXT_MAIN, true);
        t.setPadding(dp(9), 0, 0, 0);
        line.addView(t);
        return line;
    }

    /** Sub-section heading with a 36dp crest and a supporting line. */
    private LinearLayout sectionHeader(int iconType, String title, String subtitle) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.HORIZONTAL);
        wrap.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        wrap.setGravity(Gravity.CENTER_VERTICAL);

        wrap.addView(iconMedallion(iconType, 36, Luxury3DIconView.PEDESTAL_OBSIDIAN));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(10), 0, 0, 0);
        texts.addView(text(title, 14.5f, COL_TEXT_MAIN, true));
        texts.addView(text(subtitle, 10f, COL_TEXT_MUTED, false));
        wrap.addView(texts, weight(1f));
        return wrap;
    }

    private TextView text(String value, float sizeSp, int color, boolean bold) {
        TextView t = new TextView(this);
        String v = value == null ? "" : value;
        t.setText(v);
        // Persian needs a little more room than Latin at the same nominal size.
        if (hasPersian(v) && sizeSp < 9.6f) sizeSp = 9.6f;
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        t.setTextColor(color);
        t.setTypeface(houseTypeface(bold));
        if (hasPersian(v)) t.setLineSpacing(dp(2), 1f);
        return t;
    }

    // ── v6.9.4 · the house typeface. Vazirmatn is bundled in assets/fonts, so the
    // Persian text renders identically on every Android version, with real joins.
    private static Typeface TF_BODY;
    private static Typeface TF_BOLD;
    private static Typeface TF_MEDIUM;

    private void initTypefaces() {
        if (TF_BODY != null) return;
        try {
            TF_BODY   = Typeface.createFromAsset(getAssets(), "fonts/Vazirmatn-Regular.ttf");
            TF_MEDIUM = Typeface.createFromAsset(getAssets(), "fonts/Vazirmatn-SemiBold.ttf");
            TF_BOLD   = Typeface.createFromAsset(getAssets(), "fonts/Vazirmatn-Bold.ttf");
        } catch (Throwable t) {
            TF_BODY = Typeface.DEFAULT;
            TF_MEDIUM = Typeface.DEFAULT_BOLD;
            TF_BOLD = Typeface.DEFAULT_BOLD;
        }
    }

    /** The Persian face, with the weight the label deserves. */
    private Typeface houseTypeface(boolean bold) {
        initTypefaces();
        return bold ? TF_BOLD : TF_BODY;
    }

    /** True when the string carries Persian/Arabic letters — they never take tracking. */
    private boolean hasPersian(String v) {
        if (v == null) return false;
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if ((c >= 0x0600 && c <= 0x06FF) || (c >= 0xFB50 && c <= 0xFEFF)) return true;
        }
        return false;
    }

    /**
     * Latin wordmarks breathe with letter-spacing; Persian must never receive it —
     * tracking breaks the joins and looks cheap. Applied to every label in the app.
     */
    private void tracking(TextView t, float value) {
        if (t == null) return;
        CharSequence cs = t.getText();
        if (hasPersian(cs == null ? "" : cs.toString())) return;
        // v6.9.5 · this was calling itself — a fatal StackOverflowError on the first
        // Latin wordmark. It must apply the spacing, never recurse.
        t.setLetterSpacing(value);
    }

    private LinearLayout whiteCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(porcelainCard(dp(22)));
        card.setPadding(dp(16), dp(17), dp(16), dp(17));
        card.setElevation(dp(1.4f));
        return card;
    }

    /** A porcelain card with the house's hairline: white → silk, 1dp warm border. */
    private android.graphics.drawable.Drawable porcelainCard(int radius) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{ COL_WHITE, SURF_STRIP });
        g.setCornerRadius(radius);
        g.setStroke(dp(1), COL_BORDER);
        return g;
    }

    /** A translucent deep card for blocks that sit on a painted backdrop. */
    private android.graphics.drawable.Drawable scrimCard(int radius, int alpha) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                new int[]{ scrim(alpha), COL_OBSIDIAN });
        g.setCornerRadius(radius);
        g.setStroke(dp(1), lineGold(0x66));
        return g;
    }

    /** A deep card: obsidian ink with a fine 24K bezel, used for the signature blocks. */
    private android.graphics.drawable.Drawable deepCard(int radius) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                new int[]{ COL_OBSIDIAN_SOFT, COL_OBSIDIAN });
        g.setCornerRadius(radius);
        g.setStroke(dp(1), lineGold(0x66));
        return g;
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
        button.setBackground(roundedShape(lineGold(0x14), dp(15), lineGold(0x66), dp(1)));
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
        button.setBackground(roundedShape(COL_OBSIDIAN, dp(15), lineGold(0x55), dp(1)));
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
        t.setBackground(roundedShape(SURF_CHIP, dp(12), SURF_GOLD_LINE, dp(1)));
        t.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return t;
    }

    private View ghostGoldDarkButton(String label, final Runnable action) {
        TextView t = text(label, 10.5f, COL_GOLD_LIGHT, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(10), dp(12), dp(10));
        t.setBackground(roundedShape(lineGold(0x1F), dp(12), lineGold(0x55), dp(1)));
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
        wrap.setBackground(roundedShape(COL_OBSIDIAN, dp(16), lineGold(0x40), dp(1)));
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
        input.setHintTextColor(COL_TEXT_FAINT);
        input.setTextColor(COL_TEXT_MAIN);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        input.setInputType(inputType);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackground(roundedShape(COL_IVORY_BG, dp(13), COL_BORDER, dp(1)));
        input.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        input.setSingleLine(true);
        input.setTypeface(houseTypeface(false));
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
