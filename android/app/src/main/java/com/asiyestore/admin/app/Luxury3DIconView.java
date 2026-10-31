package com.asiyestore.admin.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.TypedValue;
import android.view.View;

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

    public Luxury3DIconView(Context context, int iconType, boolean goldPedestal) {
        super(context);
        this.iconType = iconType;
        this.goldPedestal = goldPedestal;

        rimPaint.setStyle(Paint.Style.STROKE);
        glyphStroke.setStyle(Paint.Style.STROKE);
        glyphStroke.setStrokeCap(Paint.Cap.ROUND);
        glyphStroke.setStrokeJoin(Paint.Join.ROUND);
        glyphFill.setStyle(Paint.Style.FILL);
    }

    public void setIconState(int iconType, boolean goldPedestal) {
        this.iconType = iconType;
        this.goldPedestal = goldPedestal;
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

        // 1. Soft 3D Ground Shadow
        shadowBox.set(pad + s * 0.02f, pad + s * 0.055f, w - pad - s * 0.02f, h - pad + s * 0.045f);
        shadowPaint.setColor(0x54050507);
        canvas.drawRoundRect(shadowBox, radius, radius, shadowPaint);

        // 2. 3D Obsidian-Crystal or 24K Gold Bullion Pedestal
        box.set(pad, pad, w - pad, h - pad);
        if (goldPedestal) {
            bodyPaint.setShader(new RadialGradient(
                    w * 0.32f, h * 0.25f, s * 0.78f,
                    new int[]{0xFFFFF5DC, 0xFFD4AF73, 0xFF8C6834},
                    new float[]{0f, 0.58f, 1f},
                    Shader.TileMode.CLAMP
            ));
        } else {
            bodyPaint.setShader(new RadialGradient(
                    w * 0.32f, h * 0.24f, s * 0.78f,
                    new int[]{0xFF34303E, 0xFF15141C, 0xFF08080B},
                    new float[]{0f, 0.55f, 1f},
                    Shader.TileMode.CLAMP
            ));
        }
        canvas.drawRoundRect(box, radius, radius, bodyPaint);

        // 3. Subtle Jewel Inner Aura for Contact & Social Icons
        if (!goldPedestal) {
            int auraCol = 0;
            if (iconType == ICON_WHATSAPP) auraCol = 0x5228946B;
            else if (iconType == ICON_INSTAGRAM) auraCol = 0x52C95F79;
            else if (iconType == ICON_PHONE) auraCol = 0x36C8A46B;
            else if (iconType == ICON_GIFT) auraCol = 0x40C8A46B;
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
                0x4AFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP
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
        LinearGradient fgGrad = goldPedestal
                ? new LinearGradient(0, pad, 0, h - pad, 0xFF1E1C22, 0xFF09090C, Shader.TileMode.CLAMP)
                : new LinearGradient(0, pad, 0, h - pad, 0xFFFFF5DC, 0xFFD0AA6E, Shader.TileMode.CLAMP);

        glyphStroke.setShader(fgGrad);
        glyphFill.setShader(fgGrad);
        glyphStroke.setStrokeWidth(Math.max(dp(1.6f), s * 0.046f));

        drawVectorGlyph(canvas, w, h, s);
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
                c.drawCircle(gx(26f, w), gy(28f, h), s * 0.095f, glyphStroke);
                c.drawLine(gx(31.5f, w), gy(33.5f, h), gx(44f, w), gy(44f, h), glyphStroke);
                c.drawLine(gx(39f, w), gy(39.8f, h), gx(42.5f, w), gy(36.5f, h), glyphStroke);
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
