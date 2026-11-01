package com.asiyestore.admin.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.animation.ValueAnimator;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Hardware-Accelerated 3D Obsidian-Crystal & 24K Champagne-Gold SVG-Path Icon Renderer (v5.0 Royal).
 * Renders multi-layered 3D squircle medallions with ground shadow, metallic gold beveled rims,
 * curved crystal specular reflection, jewel inner aura, and precision vector glyphs.
 */
public final class Luxury3DIconView extends View {

    public static final int ICON_BOUTIQUE  = 1;
    public static final int ICON_ADVISOR   = 2;
    public static final int ICON_BAG       = 3;
    public static final int ICON_CONCIERGE = 4;
    public static final int ICON_PHONE     = 5;
    public static final int ICON_SMS       = 6;
    public static final int ICON_WHATSAPP  = 7;
    public static final int ICON_INSTAGRAM = 8;
    public static final int ICON_SHIELD    = 9;
    public static final int ICON_PLUS      = 10;
    public static final int ICON_HEART     = 11;
    public static final int ICON_KEY_ADMIN = 12;
    public static final int ICON_CROWN     = 13;
    public static final int ICON_SEARCH    = 14;
    public static final int ICON_EDIT      = 15;
    public static final int ICON_TRASH     = 16;
    public static final int ICON_GIFT      = 17;
    public static final int ICON_SHARE     = 18;
    public static final int ICON_MINUS     = 19;
    public static final int ICON_CHART     = 20;
    public static final int ICON_LOCK      = 21;
    public static final int ICON_CARD      = 22;
    public static final int ICON_WALLET    = 23;
    public static final int ICON_INSTALLMENT = 24;
    public static final int ICON_AI        = 25;
    public static final int ICON_CAMERA    = 26;
    public static final int ICON_SETTINGS  = 27;
    public static final int ICON_CHECK     = 28;
    public static final int ICON_BANK      = 29;
    public static final int ICON_STAR      = 30;
    public static final int ICON_TRUCK     = 31;
    public static final int ICON_MEDAL     = 32;
    public static final int ICON_PERCENT   = 33;
    public static final int ICON_BOX       = 34;
    public static final int ICON_USERS     = 35;
    public static final int ICON_LEAF      = 36;
    public static final int ICON_DIAMOND   = 37;
    public static final int ICON_TAG       = 38;
    public static final int ICON_BELL      = 39;
    public static final int ICON_GLOBE     = 40;
    public static final int ICON_DOC       = 41;
    public static final int ICON_IMAGE     = 42;
    public static final int ICON_MOON      = 43;
    public static final int ICON_SUN       = 44;

    public static final int PEDESTAL_OBSIDIAN = 0;
    public static final int PEDESTAL_GOLD     = 1;
    public static final int PEDESTAL_IVORY    = 2;

    /** v6.9.2 · the family of every medallion: 24K champagne, or emerald set in gold. */
    public static final int PALETTE_GOLD    = 0;
    public static final int PALETTE_EMERALD = 1;
    public static final int PALETTE_SAPPHIRE = 2;
    public static int palette = PALETTE_GOLD;

    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bodyPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint auraPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sheenPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glyphStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glyphFill   = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF box = new RectF();
    private final RectF shadowBox = new RectF();
    private final Path sheenPath = new Path();
    private final Path glyphPath = new Path();

    private int iconType;
    private boolean goldPedestal;
    private int pedestalStyle = PEDESTAL_OBSIDIAN;

    /** v6.3 — a slow 24K light sweep that makes the crest feel like polished bullion. */
    private final Paint sweepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint facetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint keylinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator sweepAnim;
    private float sweepPhase;
    private boolean sweepEnabled;

    public Luxury3DIconView(Context context, int iconType, boolean goldPedestal) {
        super(context);
        this.iconType = iconType;
        this.goldPedestal = goldPedestal;
        this.pedestalStyle = goldPedestal ? PEDESTAL_GOLD : PEDESTAL_OBSIDIAN;

        rimPaint.setStyle(Paint.Style.STROKE);
        glyphStroke.setStyle(Paint.Style.STROKE);
        glyphStroke.setStrokeCap(Paint.Cap.ROUND);
        glyphStroke.setStrokeJoin(Paint.Join.ROUND);
        glyphFill.setStyle(Paint.Style.FILL);
        keylinePaint.setStyle(Paint.Style.STROKE);
    }

    public Luxury3DIconView(Context context, int iconType, int pedestalStyle) {
        super(context);
        this.iconType = iconType;
        this.pedestalStyle = pedestalStyle;
        this.goldPedestal = (pedestalStyle == PEDESTAL_GOLD);

        rimPaint.setStyle(Paint.Style.STROKE);
        glyphStroke.setStyle(Paint.Style.STROKE);
        glyphStroke.setStrokeCap(Paint.Cap.ROUND);
        glyphStroke.setStrokeJoin(Paint.Join.ROUND);
        glyphFill.setStyle(Paint.Style.FILL);
        keylinePaint.setStyle(Paint.Style.STROKE);
    }

    public void setIconState(int iconType, boolean goldPedestal) {
        this.iconType = iconType;
        this.goldPedestal = goldPedestal;
        this.pedestalStyle = goldPedestal ? PEDESTAL_GOLD : PEDESTAL_OBSIDIAN;
        invalidate();
    }

    /** Turns on the slow gold sweep. Only hero crests use it, to keep the UI calm. */
    public void enableSweep(boolean enabled) {
        this.sweepEnabled = enabled;
        if (enabled) {
            if (sweepAnim == null) {
                sweepAnim = ValueAnimator.ofFloat(0f, 1f);
                sweepAnim.setDuration(4200L);
                sweepAnim.setRepeatCount(ValueAnimator.INFINITE);
                sweepAnim.setInterpolator(new LinearInterpolator());
                sweepAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override public void onAnimationUpdate(ValueAnimator animation) {
                        sweepPhase = (float) animation.getAnimatedValue();
                        invalidate();
                    }
                });
            }
            if (isAttachedToWindow() && !sweepAnim.isRunning()) sweepAnim.start();
        } else if (sweepAnim != null && sweepAnim.isRunning()) {
            sweepAnim.cancel();
            invalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (sweepEnabled && sweepAnim != null && !sweepAnim.isRunning()) sweepAnim.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (sweepAnim != null && sweepAnim.isRunning()) sweepAnim.cancel();
        super.onDetachedFromWindow();
    }

    public void setPedestal(int pedestalStyle) {
        this.pedestalStyle = pedestalStyle;
        this.goldPedestal = (pedestalStyle == PEDESTAL_GOLD);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float s = Math.min(w, h);
        float pad = s * 0.085f;
        float radius = s * 0.29f;
        final boolean emerald = palette == PALETTE_EMERALD;
        final boolean sapphire = palette == PALETTE_SAPPHIRE;

        // 1. Soft 3D Ground Shadow
        shadowBox.set(pad + s * 0.02f, pad + s * 0.055f, w - pad - s * 0.02f, h - pad + s * 0.045f);
        shadowPaint.setColor(pedestalStyle == PEDESTAL_IVORY ? 0x24050507 : 0x54050507);
        canvas.drawRoundRect(shadowBox, radius, radius, shadowPaint);

        // 2. 3D Obsidian-Crystal or 24K Gold Bullion Pedestal
        box.set(pad, pad, w - pad, h - pad);
        if (pedestalStyle == PEDESTAL_GOLD) {
            bodyPaint.setShader(new RadialGradient(
                    w * 0.32f, h * 0.25f, s * 0.78f,
                    new int[]{0xFFFFF8E6, 0xFFDCBC85, 0xFF9A7440},
                    new float[]{0f, 0.58f, 1f},
                    Shader.TileMode.CLAMP
            ));
        } else if (pedestalStyle == PEDESTAL_IVORY) {
            bodyPaint.setShader(new RadialGradient(
                    w * 0.32f, h * 0.24f, s * 0.82f,
                    sapphire ? new int[]{0xFFFFFFFF, 0xFFF3F8FF, 0xFFD9E8F8} : emerald ? new int[]{0xFFFFFFFF, 0xFFF3FAF5, 0xFFD9EBDF}
                            : new int[]{0xFFFFFFFF, 0xFFF7F0E3, 0xFFE6D9C2},
                    new float[]{0f, 0.55f, 1f},
                    Shader.TileMode.CLAMP
            ));
        } else {
            bodyPaint.setShader(new RadialGradient(
                    w * 0.32f, h * 0.24f, s * 0.78f,
                    sapphire ? new int[]{0xFF477EB4, 0xFF153963, 0xFF07172F} : emerald ? new int[]{0xFF35886A, 0xFF0F3A29, 0xFF04140D}
                            : new int[]{0xFF34303E, 0xFF15141C, 0xFF08080B},
                    new float[]{0f, 0.55f, 1f},
                    Shader.TileMode.CLAMP
            ));
        }
        canvas.drawRoundRect(box, radius, radius, bodyPaint);

        // 3. Subtle Jewel Inner Aura for Contact & Social Icons
        if (!goldPedestal) {
            int auraCol = 0;
            if (iconType == ICON_WHATSAPP) auraCol = 0x3A2E7A5C;
            else if (iconType == ICON_INSTAGRAM) auraCol = 0x3AC9A28E;
            else if (iconType == ICON_PHONE) auraCol = 0x36C8A46B;
            else if (iconType == ICON_GIFT) auraCol = 0x40C8A46B;
            else if (iconType == ICON_STAR) auraCol = 0x3CD8B269;
            else if (iconType == ICON_MEDAL) auraCol = 0x36D9AE6A;
            else if (iconType == ICON_DIAMOND) auraCol = 0x3A7FA8C8;
            else if (iconType == ICON_LEAF) auraCol = 0x342E7A50;
            else if (iconType == ICON_TRUCK) auraCol = 0x3A8A7A5E;
            else if (iconType == ICON_BELL) auraCol = 0x34B08A50;
            if (auraCol != 0) {
                auraPaint.setShader(new RadialGradient(
                        w * 0.5f, h * 0.54f, s * 0.38f,
                        auraCol, 0x0008080B, Shader.TileMode.CLAMP
                ));
                canvas.drawCircle(w * 0.5f, h * 0.54f, s * 0.36f, auraPaint);
            }
        }

        // 4. Top-Left Curved Crystal Specular Highlight
        sheenPath.reset();
        sheenPath.moveTo(box.left + radius * 0.5f, box.top + s * 0.02f);
        sheenPath.lineTo(box.right - radius * 0.5f, box.top + s * 0.02f);
        sheenPath.quadTo(box.right - s * 0.02f, box.top + s * 0.16f, box.left + s * 0.02f, box.top + s * 0.38f);
        sheenPath.close();
        sheenPaint.setShader(new LinearGradient(
                box.left, box.top, box.right * 0.7f, box.top + s * 0.4f,
                pedestalStyle == PEDESTAL_IVORY ? 0x66FFFFFF : 0x4AFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP
        ));
        canvas.drawPath(sheenPath, sheenPaint);

        // 5. 24K Metallic Beveled Rim
        rimPaint.setStrokeWidth(Math.max(dp(1.2f), s * 0.036f));
        rimPaint.setShader(new LinearGradient(
                box.left, box.top, box.right, box.bottom,
                new int[]{0xFFFBECCE, 0xFFC8A46B, 0xFF846232, 0xFFEAD09E},
                new float[]{0f, 0.42f, 0.76f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawRoundRect(box, radius, radius, rimPaint);

        // 6. Sculpted Vector Inner Glyph (mapped on 64x64 virtual coordinate space)
        LinearGradient fgGrad;
        if (pedestalStyle == PEDESTAL_OBSIDIAN) {
            fgGrad = emerald
                    ? new LinearGradient(0, pad, 0, h - pad, 0xFFF9F1DC, 0xFFD8BE8C, Shader.TileMode.CLAMP)
                    : new LinearGradient(0, pad, 0, h - pad, 0xFFFFF7E4, 0xFFD2AC70, Shader.TileMode.CLAMP);
        } else if (pedestalStyle == PEDESTAL_IVORY) {
            fgGrad = sapphire ? new LinearGradient(0, pad, 0, h - pad, 0xFF174777, 0xFF091D3B, Shader.TileMode.CLAMP) : emerald
                    ? new LinearGradient(0, pad, 0, h - pad, 0xFF0B4832, 0xFF06271B, Shader.TileMode.CLAMP)
                    : new LinearGradient(0, pad, 0, h - pad, 0xFF4A3B25, 0xFF1A1610, Shader.TileMode.CLAMP);
        } else {
            fgGrad = new LinearGradient(0, pad, 0, h - pad, 0xFF221F27, 0xFF0A0A0D, Shader.TileMode.CLAMP);
        }

        // 5b. Engraved inner hairline — the mark of a hand-finished medallion.
        rimPaint.setShader(null);
        rimPaint.setColor(pedestalStyle == PEDESTAL_IVORY ? 0x33FFFFFF : 0x26F6E7C8);
        rimPaint.setStrokeWidth(Math.max(dp(0.9f), s * 0.016f));
        int inset = (int) Math.max(dp(3f), s * 0.085f);
        canvas.drawRoundRect(new RectF(box.left + inset, box.top + inset,
                box.right - inset, box.bottom - inset), radius * 0.82f, radius * 0.82f, rimPaint);

        // 5c. Bottom reflected glint — sells the depth of the pedestal.
        sheenPaint.setShader(new LinearGradient(
                box.left, box.top + s * 0.55f, box.left, box.bottom,
                0x00FFFFFF, pedestalStyle == PEDESTAL_IVORY ? 0x3AFFFFFF : 0x22FBEFD6, Shader.TileMode.CLAMP));
        Path baseGlint = new Path();
        baseGlint.moveTo(box.left + radius * 0.4f, box.bottom - s * 0.005f);
        baseGlint.lineTo(box.right - radius * 0.4f, box.bottom - s * 0.005f);
        baseGlint.quadTo(box.right - s * 0.02f, box.bottom - s * 0.05f, box.left + s * 0.02f, box.bottom - s * 0.20f);
        baseGlint.close();
        canvas.drawPath(baseGlint, sheenPaint);

        glyphStroke.setShader(fgGrad);
        glyphFill.setShader(fgGrad);
        glyphStroke.setStrokeWidth(Math.max(dp(1.6f), s * 0.046f));

        drawVectorGlyph(canvas, w, h, s);

        // 6b. A cut-jewel facet on the lower edge — the small tell of a real polished stone.
        if (pedestalStyle == PEDESTAL_GOLD) {
            facetPaint.setShader(new LinearGradient(box.left, box.bottom - s * 0.34f,
                    box.right, box.bottom,
                    new int[]{0x00FFFFFF, 0x40FFF3D2, 0x00FFF6DF}, null, Shader.TileMode.CLAMP));
            Path facet = new Path();
            facet.moveTo(box.left + radius * 0.9f, box.bottom - s * 0.02f);
            facet.lineTo(box.right - radius * 0.35f, box.bottom - s * 0.02f);
            facet.lineTo(box.right - s * 0.06f, box.bottom - s * 0.17f);
            facet.close();
            canvas.drawPath(facet, facetPaint);
        }

        // 6c. A hairline keyline just outside the bezel keeps the mark crisp on porcelain.
        keylinePaint.setColor(pedestalStyle == PEDESTAL_IVORY ? 0x12000000 : 0x22000000);
        keylinePaint.setStrokeWidth(Math.max(dp(0.8f), s * 0.012f));
        canvas.drawRoundRect(new RectF(box.left - dp(0.6f), box.top - dp(0.6f),
                box.right + dp(0.6f), box.bottom + dp(0.6f)), radius + dp(0.6f), radius + dp(0.6f), keylinePaint);

        // 7. 24K travelling light sweep (opt-in, hero crests only)
        if (sweepEnabled) {
            float travel = -0.55f + sweepPhase * 2.1f;
            float cx = w * travel;
            float band = s * 0.30f;
            sweepPaint.setShader(new LinearGradient(
                    cx - band, box.top, cx + band, box.bottom,
                    new int[]{0x00FFF6DF, pedestalStyle == PEDESTAL_IVORY ? 0x66FFFFFF : 0x5CFFF3D2, 0x00FFF6DF},
                    null, Shader.TileMode.CLAMP));
            canvas.save();
            Path clip = new Path();
            clip.addRoundRect(box, radius, radius, Path.Direction.CW);
            canvas.clipPath(clip);
            canvas.drawRect(box, sweepPaint);
            canvas.restore();
        }
    }

    private float gx(float v, int w) { return (v / 64f) * w; }
    private float gy(float v, int h) { return (v / 64f) * h; }

    private void drawVectorGlyph(Canvas c, int w, int h, float s) {
        glyphPath.reset();
        switch (iconType) {
            case ICON_PHONE: {
                glyphPath.moveTo(gx(23.5f, w), gy(20.5f, h));
                glyphPath.lineTo(gx(27.8f, w), gy(20.5f, h));
                glyphPath.lineTo(gx(30.6f, w), gy(25.2f, h));
                glyphPath.lineTo(gx(28.2f, w), gy(28.4f, h));
                glyphPath.quadTo(gx(31.5f, w), gy(33.5f, h), gx(35.8f, w), gy(36.0f, h));
                glyphPath.lineTo(gx(39.0f, w), gy(33.6f, h));
                glyphPath.lineTo(gx(43.8f, w), gy(36.4f, h));
                glyphPath.lineTo(gx(43.8f, w), gy(40.6f, h));
                glyphPath.quadTo(gx(41.0f, w), gy(45.2f, h), gx(35.0f, w), gy(43.5f, h));
                glyphPath.quadTo(gx(24.0f, w), gy(39.2f, h), gx(20.4f, w), gy(28.5f, h));
                glyphPath.quadTo(gx(19.5f, w), gy(23.5f, h), gx(23.5f, w), gy(20.5f, h));
                glyphPath.close();
                c.drawPath(glyphPath, glyphStroke);
                break;
            }
            case ICON_SMS: {
                RectF bubble = new RectF(gx(17.5f, w), gy(20.5f, h), gx(46.5f, w), gy(40.5f, h));
                c.drawRoundRect(bubble, s * 0.08f, s * 0.08f, glyphStroke);
                glyphPath.moveTo(gx(24f, w), gy(40.5f, h));
                glyphPath.lineTo(gx(21f, w), gy(46f, h));
                glyphPath.lineTo(gx(30f, w), gy(40.5f, h));
                c.drawPath(glyphPath, glyphStroke);
                float r = s * 0.028f;
                c.drawCircle(gx(26.5f, w), gy(30.5f, h), r, glyphFill);
                c.drawCircle(gx(32.0f, w), gy(30.5f, h), r, glyphFill);
                c.drawCircle(gx(37.5f, w), gy(30.5f, h), r, glyphFill);
                break;
            }
            case ICON_WHATSAPP: {
                c.drawCircle(gx(32f, w), gy(31f, h), s * 0.215f, glyphStroke);
                glyphPath.moveTo(gx(22f, w), gy(41.5f, h));
                glyphPath.lineTo(gx(18.5f, w), gy(46.5f, h));
                glyphPath.lineTo(gx(26.5f, w), gy(43.8f, h));
                c.drawPath(glyphPath, glyphStroke);
                Path inner = new Path();
                inner.moveTo(gx(27.5f, w), gy(25.8f, h));
                inner.quadTo(gx(26.2f, w), gy(31.5f, h), gx(32.5f, w), gy(36.2f, h));
                inner.quadTo(gx(36.2f, w), gy(37.5f, h), gx(37.8f, w), gy(34.5f, h));
                c.drawPath(inner, glyphStroke);
                break;
            }
            case ICON_INSTAGRAM: {
                RectF cam = new RectF(gx(18.5f, w), gy(18.5f, h), gx(45.5f, w), gy(45.5f, h));
                c.drawRoundRect(cam, s * 0.12f, s * 0.12f, glyphStroke);
                c.drawCircle(gx(32f, w), gy(32f, h), s * 0.10f, glyphStroke);
                c.drawCircle(gx(40.2f, w), gy(23.8f, h), s * 0.03f, glyphFill);
                break;
            }
            case ICON_BAG: {
                glyphPath.moveTo(gx(21f, w), gy(26.5f, h));
                glyphPath.lineTo(gx(43f, w), gy(26.5f, h));
                glyphPath.lineTo(gx(41.2f, w), gy(45.5f, h));
                glyphPath.lineTo(gx(22.8f, w), gy(45.5f, h));
                glyphPath.close();
                c.drawPath(glyphPath, glyphStroke);
                RectF handle = new RectF(gx(26.5f, w), gy(18.5f, h), gx(37.5f, w), gy(30f, h));
                c.drawArc(handle, 180f, 180f, false, glyphStroke);
                break;
            }
            case ICON_SHIELD:
            case ICON_ADVISOR: {
                glyphPath.moveTo(gx(32f, w), gy(17f, h));
                glyphPath.lineTo(gx(44.5f, w), gy(22f, h));
                glyphPath.lineTo(gx(44.5f, w), gy(32f, h));
                glyphPath.quadTo(gx(44.5f, w), gy(42.5f, h), gx(32f, w), gy(47f, h));
                glyphPath.quadTo(gx(19.5f, w), gy(42.5f, h), gx(19.5f, w), gy(32f, h));
                glyphPath.lineTo(gx(19.5f, w), gy(22f, h));
                glyphPath.close();
                c.drawPath(glyphPath, glyphStroke);
                Path chk = new Path();
                chk.moveTo(gx(26.8f, w), gy(32.2f, h));
                chk.lineTo(gx(30.6f, w), gy(36.2f, h));
                chk.lineTo(gx(38.0f, w), gy(28.2f, h));
                c.drawPath(chk, glyphStroke);
                break;
            }
            case ICON_CROWN:
            case ICON_CONCIERGE: {
                glyphPath.moveTo(gx(18.5f, w), gy(41f, h));
                glyphPath.lineTo(gx(21.2f, w), gy(25f, h));
                glyphPath.lineTo(gx(28.2f, w), gy(31.5f, h));
                glyphPath.lineTo(gx(32f, w), gy(21f, h));
                glyphPath.lineTo(gx(35.8f, w), gy(31.5f, h));
                glyphPath.lineTo(gx(42.8f, w), gy(25f, h));
                glyphPath.lineTo(gx(45.5f, w), gy(41f, h));
                glyphPath.close();
                c.drawPath(glyphPath, glyphStroke);
                c.drawLine(gx(19.5f, w), gy(45f, h), gx(44.5f, w), gy(45f, h), glyphStroke);
                break;
            }
            case ICON_PLUS: {
                c.drawLine(gx(32f, w), gy(21f, h), gx(32f, w), gy(43f, h), glyphStroke);
                c.drawLine(gx(21f, w), gy(32f, h), gx(43f, w), gy(32f, h), glyphStroke);
                break;
            }
            case ICON_MINUS: {
                c.drawLine(gx(21f, w), gy(32f, h), gx(43f, w), gy(32f, h), glyphStroke);
                break;
            }
            case ICON_HEART: {
                glyphPath.moveTo(gx(32f, w), gy(44f, h));
                glyphPath.cubicTo(gx(18f, w), gy(35f, h), gx(18f, w), gy(21f, h), gx(26.5f, w), gy(21f, h));
                glyphPath.quadTo(gx(30.5f, w), gy(21f, h), gx(32f, w), gy(24.5f, h));
                glyphPath.quadTo(gx(33.5f, w), gy(21f, h), gx(37.5f, w), gy(21f, h));
                glyphPath.cubicTo(gx(46f, w), gy(21f, h), gx(46f, w), gy(35f, h), gx(32f, w), gy(44f, h));
                if (goldPedestal) c.drawPath(glyphPath, glyphFill);
                else c.drawPath(glyphPath, glyphStroke);
                break;
            }
            case ICON_KEY_ADMIN: {
                // Ring bow (left-top) + shaft + two teeth: unmistakably a key, never a magnifier
                c.drawCircle(gx(24f, w), gy(24f, h), s * 0.075f, glyphStroke);
                c.drawLine(gx(29.2f, w), gy(29.2f, h), gx(44f, w), gy(44f, h), glyphStroke);
                c.drawLine(gx(34.5f, w), gy(34.5f, h), gx(30.5f, w), gy(38.5f, h), glyphStroke);
                c.drawLine(gx(39.5f, w), gy(39.5f, h), gx(35.5f, w), gy(43.5f, h), glyphStroke);
                break;
            }
            case ICON_LOCK: {
                glyphStroke.setStrokeWidth(Math.max(dp(1.6f), s * 0.042f));
                Path shackle = new Path();
                shackle.moveTo(gx(24.5f, w), gy(29f, h));
                shackle.lineTo(gx(24.5f, w), gy(24.5f, h));
                shackle.cubicTo(gx(24.5f, w), gy(18.5f, h), gx(39.5f, w), gy(18.5f, h), gx(39.5f, w), gy(24.5f, h));
                shackle.lineTo(gx(39.5f, w), gy(29f, h));
                c.drawPath(shackle, glyphStroke);
                RectF lockBody = new RectF(gx(19.5f, w), gy(29f, h), gx(44.5f, w), gy(46f, h));
                c.drawRoundRect(lockBody, s * 0.075f, s * 0.075f, glyphStroke);
                c.drawCircle(gx(32f, w), gy(35.6f, h), s * 0.032f, glyphFill);
                c.drawLine(gx(32f, w), gy(37.2f, h), gx(32f, w), gy(41.2f, h), glyphStroke);
                break;
            }
            case ICON_CARD: {
                RectF card = new RectF(gx(17.5f, w), gy(22f, h), gx(46.5f, w), gy(43.5f, h));
                c.drawRoundRect(card, s * 0.055f, s * 0.055f, glyphStroke);
                c.drawLine(gx(17.5f, w), gy(29.5f, h), gx(46.5f, w), gy(29.5f, h), glyphStroke);
                c.drawLine(gx(23f, w), gy(37.2f, h), gx(32f, w), gy(37.2f, h), glyphStroke);
                c.drawCircle(gx(41f, w), gy(37.2f, h), s * 0.028f, glyphFill);
                break;
            }
            case ICON_WALLET: {
                glyphPath.moveTo(gx(18.5f, w), gy(25f, h));
                glyphPath.lineTo(gx(41f, w), gy(25f, h));
                glyphPath.quadTo(gx(45.5f, w), gy(25f, h), gx(45.5f, w), gy(30f, h));
                glyphPath.lineTo(gx(45.5f, w), gy(41f, h));
                glyphPath.quadTo(gx(45.5f, w), gy(45f, h), gx(41f, w), gy(45f, h));
                glyphPath.lineTo(gx(21.5f, w), gy(45f, h));
                glyphPath.quadTo(gx(18.5f, w), gy(45f, h), gx(18.5f, w), gy(41f, h));
                glyphPath.close();
                c.drawPath(glyphPath, glyphStroke);
                c.drawLine(gx(18.5f, w), gy(31f, h), gx(45.5f, w), gy(31f, h), glyphStroke);
                c.drawCircle(gx(39.5f, w), gy(38f, h), s * 0.03f, glyphFill);
                break;
            }
            case ICON_INSTALLMENT: {
                c.drawCircle(gx(32f, w), gy(32f, h), s * 0.155f, glyphStroke);
                c.drawLine(gx(32f, w), gy(23.5f, h), gx(32f, w), gy(32f, h), glyphStroke);
                c.drawLine(gx(32f, w), gy(32f, h), gx(38.5f, w), gy(36f, h), glyphStroke);
                c.drawCircle(gx(32f, w), gy(32f, h), s * 0.018f, glyphFill);
                break;
            }
            case ICON_AI: {
                glyphPath.moveTo(gx(32f, w), gy(16.5f, h));
                glyphPath.lineTo(gx(35.2f, w), gy(26.4f, h));
                glyphPath.lineTo(gx(45f, w), gy(29.6f, h));
                glyphPath.lineTo(gx(35.2f, w), gy(32.8f, h));
                glyphPath.lineTo(gx(32f, w), gy(42.7f, h));
                glyphPath.lineTo(gx(28.8f, w), gy(32.8f, h));
                glyphPath.lineTo(gx(19f, w), gy(29.6f, h));
                glyphPath.lineTo(gx(28.8f, w), gy(26.4f, h));
                glyphPath.close();
                c.drawPath(glyphPath, glyphFill);
                c.drawCircle(gx(42.5f, w), gy(19.5f, h), s * 0.028f, glyphFill);
                c.drawCircle(gx(22f, w), gy(42f, h), s * 0.022f, glyphFill);
                break;
            }
            case ICON_CAMERA: {
                RectF body = new RectF(gx(17.5f, w), gy(23.5f, h), gx(46.5f, w), gy(44f, h));
                c.drawRoundRect(body, s * 0.07f, s * 0.07f, glyphStroke);
                c.drawCircle(gx(32f, w), gy(33.6f, h), s * 0.098f, glyphStroke);
                c.drawCircle(gx(32f, w), gy(33.6f, h), s * 0.042f, glyphFill);
                c.drawLine(gx(25f, w), gy(23.5f, h), gx(28f, w), gy(19.5f, h), glyphStroke);
                c.drawLine(gx(28f, w), gy(19.5f, h), gx(36f, w), gy(19.5f, h), glyphStroke);
                c.drawLine(gx(36f, w), gy(19.5f, h), gx(39f, w), gy(23.5f, h), glyphStroke);
                c.drawCircle(gx(42f, w), gy(28.5f, h), s * 0.022f, glyphFill);
                break;
            }
            case ICON_SETTINGS: {
                c.drawCircle(gx(32f, w), gy(32f, h), s * 0.075f, glyphStroke);
                for (int i = 0; i < 8; i++) {
                    double ang = Math.PI / 4.0 * i;
                    float x1 = (float) (32 + Math.cos(ang) * 12.5);
                    float y1 = (float) (32 + Math.sin(ang) * 12.5);
                    float x2 = (float) (32 + Math.cos(ang) * 17.5);
                    float y2 = (float) (32 + Math.sin(ang) * 17.5);
                    c.drawLine(gx(x1, w), gy(y1, h), gx(x2, w), gy(y2, h), glyphStroke);
                }
                c.drawCircle(gx(32f, w), gy(32f, h), s * 0.155f, glyphStroke);
                break;
            }
            case ICON_CHECK: {
                glyphPath.moveTo(gx(21f, w), gy(32.5f, h));
                glyphPath.lineTo(gx(29f, w), gy(40.5f, h));
                glyphPath.lineTo(gx(44f, w), gy(23.5f, h));
                c.drawPath(glyphPath, glyphStroke);
                break;
            }
            case ICON_BANK: {
                glyphPath.moveTo(gx(18f, w), gy(27f, h));
                glyphPath.lineTo(gx(32f, w), gy(18.5f, h));
                glyphPath.lineTo(gx(46f, w), gy(27f, h));
                glyphPath.close();
                c.drawPath(glyphPath, glyphStroke);
                RectF base = new RectF(gx(18f, w), gy(28.5f, h), gx(46f, w), gy(45f, h));
                c.drawRoundRect(base, s * 0.02f, s * 0.02f, glyphStroke);
                c.drawLine(gx(25f, w), gy(33f, h), gx(25f, w), gy(41f, h), glyphStroke);
                c.drawLine(gx(32f, w), gy(33f, h), gx(32f, w), gy(41f, h), glyphStroke);
                c.drawLine(gx(39f, w), gy(33f, h), gx(39f, w), gy(41f, h), glyphStroke);
                break;
            }
            case ICON_SEARCH: {
                c.drawCircle(gx(29.5f, w), gy(29.5f, h), s * 0.145f, glyphStroke);
                c.drawLine(gx(36.5f, w), gy(36.5f, h), gx(45f, w), gy(45f, h), glyphStroke);
                break;
            }
            case ICON_EDIT: {
                glyphPath.moveTo(gx(21f, w), gy(43f, h));
                glyphPath.lineTo(gx(23f, w), gy(36f, h));
                glyphPath.lineTo(gx(38.5f, w), gy(20.5f, h));
                glyphPath.lineTo(gx(43.5f, w), gy(25.5f, h));
                glyphPath.lineTo(gx(28f, w), gy(41f, h));
                glyphPath.close();
                c.drawPath(glyphPath, glyphStroke);
                break;
            }
            case ICON_TRASH: {
                c.drawLine(gx(20f, w), gy(23.5f, h), gx(44f, w), gy(23.5f, h), glyphStroke);
                c.drawLine(gx(27.5f, w), gy(20f, h), gx(36.5f, w), gy(20f, h), glyphStroke);
                RectF bin = new RectF(gx(23.5f, w), gy(23.5f, h), gx(40.5f, w), gy(44.5f, h));
                c.drawRoundRect(bin, s * 0.04f, s * 0.04f, glyphStroke);
                c.drawLine(gx(29.5f, w), gy(29f, h), gx(29.5f, w), gy(39.5f, h), glyphStroke);
                c.drawLine(gx(34.5f, w), gy(29f, h), gx(34.5f, w), gy(39.5f, h), glyphStroke);
                break;
            }
            case ICON_GIFT: {
                RectF ribbonTop = new RectF(gx(19.5f, w), gy(25.5f, h), gx(44.5f, w), gy(31.5f, h));
                RectF giftBody = new RectF(gx(21.5f, w), gy(31.5f, h), gx(42.5f, w), gy(45f, h));
                c.drawRoundRect(ribbonTop, s * 0.03f, s * 0.03f, glyphStroke);
                c.drawRoundRect(giftBody, s * 0.03f, s * 0.03f, glyphStroke);
                c.drawLine(gx(32f, w), gy(25.5f, h), gx(32f, w), gy(45f, h), glyphStroke);
                c.drawCircle(gx(28.5f, w), gy(22.2f, h), s * 0.045f, glyphStroke);
                c.drawCircle(gx(35.5f, w), gy(22.2f, h), s * 0.045f, glyphStroke);
                break;
            }
            case ICON_SHARE: {
                c.drawCircle(gx(41f, w), gy(22f, h), s * 0.055f, glyphStroke);
                c.drawCircle(gx(22f, w), gy(32f, h), s * 0.055f, glyphStroke);
                c.drawCircle(gx(41f, w), gy(42f, h), s * 0.055f, glyphStroke);
                c.drawLine(gx(25.5f, w), gy(30f, h), gx(37.5f, w), gy(24f, h), glyphStroke);
                c.drawLine(gx(25.5f, w), gy(34f, h), gx(37.5f, w), gy(40f, h), glyphStroke);
                break;
            }
            case ICON_CHART: {
                c.drawLine(gx(20f, w), gy(44f, h), gx(45f, w), gy(44f, h), glyphStroke);
                c.drawLine(gx(24f, w), gy(44f, h), gx(24f, w), gy(35f, h), glyphStroke);
                c.drawLine(gx(32f, w), gy(44f, h), gx(32f, w), gy(28f, h), glyphStroke);
                c.drawLine(gx(40f, w), gy(44f, h), gx(40f, w), gy(21f, h), glyphStroke);
                break;
            }
            case ICON_STAR: {
                Path star = new Path();
                for (int i = 0; i < 10; i++) {
                    double ang = -Math.PI / 2.0 + i * Math.PI / 5.0;
                    float rr = (i % 2 == 0) ? 15.6f : 6.9f;
                    float x = (float) (32 + Math.cos(ang) * rr);
                    float y = (float) (32 + Math.sin(ang) * rr);
                    if (i == 0) star.moveTo(gx(x, w), gy(y, h));
                    else star.lineTo(gx(x, w), gy(y, h));
                }
                star.close();
                c.drawPath(star, goldPedestal ? glyphFill : glyphStroke);
                break;
            }
            case ICON_TRUCK: {
                RectF cargo = new RectF(gx(16.5f, w), gy(23.5f, h), gx(37f, w), gy(38.5f, h));
                c.drawRoundRect(cargo, s * 0.035f, s * 0.035f, glyphStroke);
                Path cab = new Path();
                cab.moveTo(gx(37f, w), gy(29f, h));
                cab.lineTo(gx(42.5f, w), gy(29f, h));
                cab.lineTo(gx(47.5f, w), gy(34f, h));
                cab.lineTo(gx(47.5f, w), gy(38.5f, h));
                cab.lineTo(gx(37f, w), gy(38.5f, h));
                c.drawPath(cab, glyphStroke);
                c.drawCircle(gx(24f, w), gy(42.8f, h), s * 0.052f, glyphStroke);
                c.drawCircle(gx(41.5f, w), gy(42.8f, h), s * 0.052f, glyphStroke);
                c.drawLine(gx(31f, w), gy(29f, h), gx(31f, w), gy(38.5f, h), glyphStroke);
                break;
            }
            case ICON_MEDAL: {
                c.drawCircle(gx(32f, w), gy(35.5f, h), s * 0.155f, glyphStroke);
                Path ribbon = new Path();
                ribbon.moveTo(gx(23f, w), gy(15.5f, h));
                ribbon.lineTo(gx(28.4f, w), gy(27.5f, h));
                ribbon.lineTo(gx(32f, w), gy(23.5f, h));
                ribbon.lineTo(gx(35.6f, w), gy(27.5f, h));
                ribbon.lineTo(gx(41f, w), gy(15.5f, h));
                c.drawPath(ribbon, glyphStroke);
                Path core = new Path();
                for (int i = 0; i < 10; i++) {
                    double ang = -Math.PI / 2.0 + i * Math.PI / 5.0;
                    float rr = (i % 2 == 0) ? 7.4f : 3.2f;
                    float x = (float) (32 + Math.cos(ang) * rr);
                    float y = (float) (35.5f + Math.sin(ang) * rr);
                    if (i == 0) core.moveTo(gx(x, w), gy(y, h));
                    else core.lineTo(gx(x, w), gy(y, h));
                }
                core.close();
                c.drawPath(core, glyphFill);
                break;
            }
            case ICON_PERCENT: {
                c.drawCircle(gx(24.5f, w), gy(24f, h), s * 0.055f, glyphStroke);
                c.drawCircle(gx(39.5f, w), gy(40f, h), s * 0.055f, glyphStroke);
                c.drawLine(gx(40.5f, w), gy(21.5f, h), gx(23.5f, w), gy(42.5f, h), glyphStroke);
                break;
            }
            case ICON_BOX: {
                Path lid = new Path();
                lid.moveTo(gx(32f, w), gy(17.5f, h));
                lid.lineTo(gx(46.5f, w), gy(24.5f, h));
                lid.lineTo(gx(32f, w), gy(31.5f, h));
                lid.lineTo(gx(17.5f, w), gy(24.5f, h));
                lid.close();
                c.drawPath(lid, glyphStroke);
                c.drawLine(gx(17.5f, w), gy(24.5f, h), gx(17.5f, w), gy(39f, h), glyphStroke);
                c.drawLine(gx(46.5f, w), gy(24.5f, h), gx(46.5f, w), gy(39f, h), glyphStroke);
                c.drawLine(gx(32f, w), gy(31.5f, h), gx(32f, w), gy(46.5f, h), glyphStroke);
                c.drawLine(gx(17.5f, w), gy(39f, h), gx(32f, w), gy(46.5f, h), glyphStroke);
                c.drawLine(gx(46.5f, w), gy(39f, h), gx(32f, w), gy(46.5f, h), glyphStroke);
                break;
            }
            case ICON_USERS: {
                c.drawCircle(gx(26f, w), gy(26f, h), s * 0.062f, glyphStroke);
                c.drawCircle(gx(40.5f, w), gy(29.5f, h), s * 0.048f, glyphStroke);
                Path shoulders = new Path();
                shoulders.moveTo(gx(16.5f, w), gy(44f, h));
                shoulders.quadTo(gx(16.5f, w), gy(33.5f, h), gx(26f, w), gy(33.5f, h));
                shoulders.quadTo(gx(35.5f, w), gy(33.5f, h), gx(35.5f, w), gy(44f, h));
                c.drawPath(shoulders, glyphStroke);
                Path shoulders2 = new Path();
                shoulders2.moveTo(gx(38f, w), gy(44f, h));
                shoulders2.quadTo(gx(38f, w), gy(37f, h), gx(45.5f, w), gy(37f, h));
                shoulders2.quadTo(gx(48f, w), gy(37f, h), gx(48f, w), gy(44f, h));
                c.drawPath(shoulders2, glyphStroke);
                break;
            }
            case ICON_LEAF: {
                glyphPath.moveTo(gx(43.5f, w), gy(19f, h));
                glyphPath.quadTo(gx(44.5f, w), gy(35f, h), gx(32f, w), gy(42.5f, h));
                glyphPath.quadTo(gx(19.5f, w), gy(35f, h), gx(20.5f, w), gy(19f, h));
                glyphPath.quadTo(gx(32f, w), gy(23f, h), gx(43.5f, w), gy(19f, h));
                glyphPath.close();
                c.drawPath(glyphPath, glyphStroke);
                glyphPath.reset();
                glyphPath.moveTo(gx(21.5f, w), gy(44.5f, h));
                glyphPath.quadTo(gx(32f, w), gy(34f, h), gx(40.5f, w), gy(24f, h));
                c.drawPath(glyphPath, glyphStroke);
                break;
            }
            case ICON_DIAMOND: {
                Path gem = new Path();
                gem.moveTo(gx(22.5f, w), gy(21.5f, h));
                gem.lineTo(gx(41.5f, w), gy(21.5f, h));
                gem.lineTo(gx(47f, w), gy(29f, h));
                gem.lineTo(gx(32f, w), gy(46f, h));
                gem.lineTo(gx(17f, w), gy(29f, h));
                gem.close();
                c.drawPath(gem, glyphStroke);
                c.drawLine(gx(17f, w), gy(29f, h), gx(47f, w), gy(29f, h), glyphStroke);
                c.drawLine(gx(22.5f, w), gy(21.5f, h), gx(26.5f, w), gy(29f, h), glyphStroke);
                c.drawLine(gx(41.5f, w), gy(21.5f, h), gx(37.5f, w), gy(29f, h), glyphStroke);
                c.drawLine(gx(26.5f, w), gy(29f, h), gx(32f, w), gy(46f, h), glyphStroke);
                c.drawLine(gx(37.5f, w), gy(29f, h), gx(32f, w), gy(46f, h), glyphStroke);
                break;
            }
            case ICON_TAG: {
                Path tag = new Path();
                tag.moveTo(gx(22.5f, w), gy(19.5f, h));
                tag.lineTo(gx(36f, w), gy(19.5f, h));
                tag.lineTo(gx(46.5f, w), gy(30f, h));
                tag.lineTo(gx(31.5f, w), gy(45.5f, h));
                tag.lineTo(gx(18f, w), gy(32f, h));
                tag.close();
                c.drawPath(tag, glyphStroke);
                c.drawCircle(gx(28.5f, w), gy(26.5f, h), s * 0.033f, glyphFill);
                break;
            }
            case ICON_BELL: {
                glyphPath.moveTo(gx(32f, w), gy(17.5f, h));
                glyphPath.quadTo(gx(42.5f, w), gy(19.5f, h), gx(42.5f, w), gy(31f, h));
                glyphPath.quadTo(gx(42.5f, w), gy(37.5f, h), gx(46f, w), gy(40.5f, h));
                glyphPath.lineTo(gx(18f, w), gy(40.5f, h));
                glyphPath.quadTo(gx(21.5f, w), gy(37.5f, h), gx(21.5f, w), gy(31f, h));
                glyphPath.quadTo(gx(21.5f, w), gy(19.5f, h), gx(32f, w), gy(17.5f, h));
                glyphPath.close();
                c.drawPath(glyphPath, glyphStroke);
                c.drawCircle(gx(32f, w), gy(45f, h), s * 0.032f, glyphFill);
                break;
            }
            case ICON_GLOBE: {
                c.drawCircle(gx(32f, w), gy(32f, h), s * 0.185f, glyphStroke);
                RectF tilt = new RectF(gx(21.5f, w), gy(20f, h), gx(42.5f, w), gy(44f, h));
                c.drawOval(tilt, glyphStroke);
                c.drawLine(gx(16.5f, w), gy(32f, h), gx(47.5f, w), gy(32f, h), glyphStroke);
                break;
            }
            case ICON_DOC: {
                Path page = new Path();
                page.moveTo(gx(20f, w), gy(16.5f, h));
                page.lineTo(gx(37.5f, w), gy(16.5f, h));
                page.lineTo(gx(45f, w), gy(24f, h));
                page.lineTo(gx(45f, w), gy(47.5f, h));
                page.lineTo(gx(20f, w), gy(47.5f, h));
                page.close();
                c.drawPath(page, glyphStroke);
                c.drawLine(gx(37f, w), gy(16.5f, h), gx(37f, w), gy(24f, h), glyphStroke);
                c.drawLine(gx(37f, w), gy(24f, h), gx(45f, w), gy(24f, h), glyphStroke);
                c.drawLine(gx(25.5f, w), gy(31.5f, h), gx(39.5f, w), gy(31.5f, h), glyphStroke);
                c.drawLine(gx(25.5f, w), gy(37.5f, h), gx(39.5f, w), gy(37.5f, h), glyphStroke);
                c.drawLine(gx(25.5f, w), gy(43f, h), gx(33.5f, w), gy(43f, h), glyphStroke);
                break;
            }
            case ICON_IMAGE: {
                RectF frame = new RectF(gx(17f, w), gy(20.5f, h), gx(47f, w), gy(44.5f, h));
                c.drawRoundRect(frame, s * 0.055f, s * 0.055f, glyphStroke);
                c.drawCircle(gx(25.5f, w), gy(28f, h), s * 0.038f, glyphFill);
                glyphPath.reset();
                glyphPath.moveTo(gx(19.5f, w), gy(42f, h));
                glyphPath.lineTo(gx(30f, w), gy(32.5f, h));
                glyphPath.lineTo(gx(37f, w), gy(38.5f, h));
                glyphPath.lineTo(gx(42f, w), gy(34f, h));
                glyphPath.lineTo(gx(45.5f, w), gy(42f, h));
                c.drawPath(glyphPath, glyphStroke);
                break;
            }
            case ICON_MOON: {
                Path crescent = new Path();
                crescent.addArc(new RectF(gx(18f, w), gy(18f, h), gx(46f, w), gy(46f, h)), 60f, 240f);
                Path bite = new Path();
                bite.addCircle(gx(38.5f, w), gy(30f, h), s * 0.145f, Path.Direction.CW);
                crescent.op(bite, Path.Op.DIFFERENCE);
                c.drawPath(crescent, glyphStroke);
                c.drawCircle(gx(22.5f, w), gy(24f, h), s * 0.022f, glyphFill);
                break;
            }
            case ICON_SUN: {
                c.drawCircle(gx(32f, w), gy(32f, h), s * 0.085f, glyphStroke);
                for (int i = 0; i < 8; i++) {
                    double ang = Math.PI / 4.0 * i;
                    float x1 = (float) (32 + Math.cos(ang) * 14.5);
                    float y1 = (float) (32 + Math.sin(ang) * 14.5);
                    float x2 = (float) (32 + Math.cos(ang) * 19.5);
                    float y2 = (float) (32 + Math.sin(ang) * 19.5);
                    c.drawLine(gx(x1, w), gy(y1, h), gx(x2, w), gy(y2, h), glyphStroke);
                }
                break;
            }
            case ICON_BOUTIQUE:
            default: {
                glyphPath.moveTo(gx(32f, w), gy(16.5f, h));
                glyphPath.lineTo(gx(35.8f, w), gy(28.2f, h));
                glyphPath.lineTo(gx(47.5f, w), gy(32f, h));
                glyphPath.lineTo(gx(35.8f, w), gy(35.8f, h));
                glyphPath.lineTo(gx(32f, w), gy(47.5f, h));
                glyphPath.lineTo(gx(28.2f, w), gy(35.8f, h));
                glyphPath.lineTo(gx(16.5f, w), gy(32f, h));
                glyphPath.lineTo(gx(28.2f, w), gy(28.2f, h));
                glyphPath.close();
                c.drawPath(glyphPath, glyphFill);
                break;
            }
        }
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }
}
