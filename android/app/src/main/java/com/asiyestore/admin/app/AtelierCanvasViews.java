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
 * Custom Hardware-Accelerated Haute Beauté Atelier Views:
 * 1. LipTryOnCanvasView — Sculpted Couture Velvet Lip Try-On with Skin Undertone & Satin Highlight
 * 2. SkinDiagnosticBarsView — Cellular Skin Health Gold Diagnostic Meters
 */
public final class AtelierCanvasViews {

    private AtelierCanvasViews() {}

    public static final class LipTryOnCanvasView extends View {

        private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint lipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint sheenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint captionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Path upperLip = new Path();
        private final Path lowerLip = new Path();
        private final Path partingLine = new Path();
        private final RectF sheenOval = new RectF();
        private final RectF outerRect = new RectF();

        private int skinColor = 0xFFF3DDD0;
        private int lipColor = 0xFFC68B73;
        private String shadeTitle = "Champagne Nude • NUDE-01";

        public LipTryOnCanvasView(Context context) {
            super(context);
            framePaint.setStyle(Paint.Style.STROKE);
            framePaint.setColor(0xB8C6A16C);
            framePaint.setStrokeWidth(dp(1.5f));

            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setColor(0x661A080C);
            linePaint.setStrokeWidth(dp(2f));

            captionPaint.setColor(0xFF242321);
            captionPaint.setTextSize(sp(11.5f));
            captionPaint.setFakeBoldText(true);
            captionPaint.setTextAlign(Paint.Align.CENTER);
        }

        public void updateLook(int newSkinColor, int newLipColor, String title) {
            this.skinColor = newSkinColor;
            this.lipColor = newLipColor;
            this.shadeTitle = title != null ? title : "";
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            bgPaint.setShader(new RadialGradient(
                    w * 0.5f, h * 0.48f, Math.max(w, h) * 0.65f,
                    skinColor, darkenColor(skinColor, 0.80f), Shader.TileMode.CLAMP
            ));
            outerRect.set(0, 0, w, h);
            canvas.drawRoundRect(outerRect, dp(14), dp(14), bgPaint);

            // Sculpted Upper Couture Lip (Cupid's Bow)
            upperLip.reset();
            upperLip.moveTo(w * 0.16f, h * 0.50f);
            upperLip.cubicTo(w * 0.28f, h * 0.30f, w * 0.42f, h * 0.28f, w * 0.50f, h * 0.37f);
            upperLip.cubicTo(w * 0.58f, h * 0.28f, w * 0.72f, h * 0.30f, w * 0.84f, h * 0.50f);
            upperLip.quadTo(w * 0.50f, h * 0.55f, w * 0.16f, h * 0.50f);
            upperLip.close();

            lipPaint.setShader(new LinearGradient(
                    0, h * 0.28f, 0, h * 0.55f,
                    darkenColor(lipColor, 0.92f), lipColor, Shader.TileMode.CLAMP
            ));
            canvas.drawPath(upperLip, lipPaint);

            // Plush Velvet Lower Lip
            lowerLip.reset();
            lowerLip.moveTo(w * 0.16f, h * 0.50f);
            lowerLip.quadTo(w * 0.50f, h * 0.55f, w * 0.84f, h * 0.50f);
            lowerLip.cubicTo(w * 0.75f, h * 0.78f, w * 0.25f, h * 0.78f, w * 0.16f, h * 0.50f);
            lowerLip.close();

            lipPaint.setShader(new LinearGradient(
                    0, h * 0.50f, 0, h * 0.78f,
                    lightenColor(lipColor, 1.08f), darkenColor(lipColor, 0.85f), Shader.TileMode.CLAMP
            ));
            canvas.drawPath(lowerLip, lipPaint);

            // Soft Satin Specular Sheen
            sheenPaint.setShader(new LinearGradient(
                    w * 0.34f, h * 0.56f, w * 0.66f, h * 0.66f,
                    new int[]{0x00FFFFFF, 0x4DFFF5EE, 0x00FFFFFF},
                    new float[]{0f, 0.5f, 1f},
                    Shader.TileMode.CLAMP
            ));
            sheenOval.set(w * 0.33f, h * 0.57f, w * 0.67f, h * 0.65f);
            canvas.drawOval(sheenOval, sheenPaint);

            // Lip Parting Contour
            partingLine.reset();
            partingLine.moveTo(w * 0.17f, h * 0.50f);
            partingLine.quadTo(w * 0.50f, h * 0.55f, w * 0.83f, h * 0.50f);
            canvas.drawPath(partingLine, linePaint);

            // Luxury Atelier Gold Inner Border & Shade Caption
            float inset = dp(8);
            outerRect.set(inset, inset, w - inset, h - inset);
            canvas.drawRoundRect(outerRect, dp(10), dp(10), framePaint);
            canvas.drawText(shadeTitle, w * 0.5f, h - dp(16), captionPaint);
        }

        private int darkenColor(int color, float factor) {
            int a = (color >> 24) & 0xFF;
            int r = Math.max(0, Math.min(255, Math.round(((color >> 16) & 0xFF) * factor)));
            int g = Math.max(0, Math.min(255, Math.round(((color >> 8) & 0xFF) * factor)));
            int b = Math.max(0, Math.min(255, Math.round((color & 0xFF) * factor)));
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        private int lightenColor(int color, float factor) {
            return darkenColor(color, factor);
        }

        private float dp(float v) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
        }

        private float sp(float v) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, getResources().getDisplayMetrics());
        }
    }

    public static final class SkinDiagnosticBarsView extends View {

        private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pctPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        private int[] scores = new int[]{64, 68, 88};
        private final String[] titles = new String[]{
                "تراز آبرسانی بین‌سلولی",
                "تراکم کلاژن و الاستیسیته",
                "پتانسیل درخشش طلای ۲۴ عیار"
        };

        public SkinDiagnosticBarsView(Context context) {
            super(context);
            trackPaint.setColor(0x28FFFFFF);
            labelPaint.setColor(0xFFF8F6F1);
            labelPaint.setTextSize(sp(12f));
            labelPaint.setTextAlign(Paint.Align.RIGHT);

            pctPaint.setColor(0xFFE2C28E);
            pctPaint.setTextSize(sp(12f));
            pctPaint.setFakeBoldText(true);
            pctPaint.setTextAlign(Paint.Align.LEFT);
        }

        public void setScores(int hydration, int firmness, int radiance) {
            this.scores = new int[]{hydration, firmness, radiance};
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            float pad = dp(10);
            float rowH = (h - pad * 2) / 3f;

            fillPaint.setShader(new LinearGradient(
                    0, 0, w, 0,
                    0xFF9B7844, 0xFFE2C28E, Shader.TileMode.CLAMP
            ));

            for (int i = 0; i < 3; i++) {
                float top = pad + i * rowH;
                canvas.drawText(titles[i], w - pad, top + dp(14), labelPaint);
                String pText = PersianFormatter.toPersianDigits(String.valueOf(scores[i])) + "٪";
                canvas.drawText(pText, pad, top + dp(14), pctPaint);

                float barTop = top + dp(21);
                float barH = dp(8);
                rect.set(pad, barTop, w - pad, barTop + barH);
                canvas.drawRoundRect(rect, dp(4), dp(4), trackPaint);

                float fullW = (w - 2 * pad) * (scores[i] / 100f);
                // RTL fill from right to left
                rect.set(w - pad - fullW, barTop, w - pad, barTop + barH);
                canvas.drawRoundRect(rect, dp(4), dp(4), fillPaint);
            }
        }

        private float dp(float v) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
        }

        private float sp(float v) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, getResources().getDisplayMetrics());
        }
    }
}
