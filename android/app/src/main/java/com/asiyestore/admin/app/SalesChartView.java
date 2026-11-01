package com.asiyestore.admin.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.TypedValue;
import android.view.View;

/**
 * Executive 24K Gold & Obsidian Chronological Revenue Chart (v5.0 Royal).
 * Progresses chronologically left-to-right (ش -> ی -> د -> س -> چ -> پ -> امروز)
 * so upward revenue momentum culminates clearly at today's peak on the right.
 */
public final class SalesChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint peakBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint peakLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pillTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF barRect = new RectF();
    private final RectF pillRect = new RectF();
    private final Path trendPath = new Path();
    private final Path areaPath = new Path();

    private long[] values = new long[]{15_200_000L, 19_400_000L, 18_100_000L, 24_600_000L, 23_200_000L, 29_500_000L, 35_400_000L};
    private String[] labels = new String[]{"ش", "ی", "د", "س", "چ", "پ", "امروز"};

    /** v6.9.2 · champagne chart in the warm moods, emerald and gold in the daylight one. */
    public static final int METAL_GOLD    = 0;
    public static final int METAL_EMERALD = 1;
    public static final int METAL_SAPPHIRE = 2;
    public static int metal = METAL_GOLD;

    private int m(int gold, int emerald) {
        if (metal == METAL_SAPPHIRE) {
            switch (emerald) {
                case 0xFF1E7A57: return 0xFF19528E;
                case 0xFF0B4832: return 0xFF123B69;
                case 0xFF08251A: return 0xFF091D3B;
                case 0xFF0F3A29: return 0xFF14345F;
                case 0xFF0F2A20: return 0xFF102A4C;
                case 0xFF35886A: return 0xFF477EB4;
                case 0xFFEAF4EE: return 0xFFEAF2FC;
                case 0xFFE6F0E9: return 0xFFE3EDF9;
                case 0xFF7B8F82: return 0xFF586F86;
                case 0xFF435C4D: return 0xFF425971;
                case 0xFF10241B: return 0xFF12253F;
                case 0x22EAF4EE: return 0x22EAF2FC;
                case 0x220B4832: return 0x22123B69;
                case 0x221E7A57: return 0x2219528E;
                default: return emerald;
            }
        }
        return metal == METAL_EMERALD ? emerald : gold;
    }

    public SalesChartView(Context context) {
        super(context);
        gridPaint.setColor(0x28B8A58B);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(1));
        gridPaint.setPathEffect(new DashPathEffect(new float[]{dp(4), dp(4)}, 0));

        labelPaint.setColor(m(0xFF70685E, 0xFF435C4D));
        labelPaint.setTextSize(sp(11f));
        labelPaint.setTextAlign(Paint.Align.CENTER);

        peakLabelPaint.setColor(m(0xFF141418, 0xFF10241B));
        peakLabelPaint.setTextSize(sp(11f));
        peakLabelPaint.setFakeBoldText(true);
        peakLabelPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint.setColor(m(0xFF9A7742, 0xFF0B4832));
        valuePaint.setTextSize(sp(9.5f));
        valuePaint.setFakeBoldText(true);
        valuePaint.setTextAlign(Paint.Align.CENTER);

        pillPaint.setColor(m(0xFF111116, 0xFF08251A));
        pillPaint.setStyle(Paint.Style.FILL);

        pillTextPaint.setColor(m(0xFFF3DEC0, 0xFFEAF4EE));
        pillTextPaint.setTextSize(sp(9.5f));
        pillTextPaint.setFakeBoldText(true);
        pillTextPaint.setTextAlign(Paint.Align.CENTER);

        linePaint.setColor(m(0xFFB88E52, 0xFF1E7A57));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2.4f));
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        dotOuterPaint.setColor(m(0xFF111116, 0xFF08251A));
        dotOuterPaint.setStyle(Paint.Style.FILL);

        dotInnerPaint.setColor(m(0xFFE7CA96, 0xFFE7CA96));
        dotInnerPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(long[] newValues, String[] newLabels) {
        if (newValues != null && newValues.length > 0) {
            this.values = newValues;
        }
        if (newLabels != null && newLabels.length == this.values.length) {
            this.labels = newLabels;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || values.length == 0) return;

        float topPad = dp(32);
        float bottomPad = dp(30);
        float sidePad = dp(16);
        float chartHeight = h - topPad - bottomPad;
        float barBottom = topPad + chartHeight;

        for (int i = 0; i <= 3; i++) {
            float y = topPad + (chartHeight * i / 3f);
            canvas.drawLine(sidePad, y, w - sidePad, y, gridPaint);
        }

        long maxVal = 1L;
        for (long v : values) {
            if (v > maxVal) maxVal = v;
        }

        int count = values.length;
        float slotWidth = (w - 2 * sidePad) / count;
        float barWidth = Math.min(dp(24), slotWidth * 0.48f);

        barPaint.setShader(new LinearGradient(
                0, topPad, 0, barBottom,
                0x66D6B783, 0x22EFE4D3, Shader.TileMode.CLAMP
        ));
        peakBarPaint.setShader(new LinearGradient(
                0, topPad, 0, barBottom,
                m(0xFFD4AF73, 0xFF35886A), m(0xFF9A733B, 0xFF0B4832), Shader.TileMode.CLAMP
        ));
        areaPaint.setShader(new LinearGradient(
                0, topPad, 0, barBottom,
                0x36C8A46B, 0x00C8A46B, Shader.TileMode.CLAMP
        ));

        float[] xs = new float[count];
        float[] ys = new float[count];

        // Chronological left-to-right progression culminating at index (count - 1) = امروز
        for (int i = 0; i < count; i++) {
            float cx = sidePad + slotWidth * i + slotWidth / 2f;
            float ratio = (float) values[i] / (float) maxVal;
            float barTop = topPad + chartHeight * (1f - Math.max(0.10f, ratio * 0.86f));
            xs[i] = cx;
            ys[i] = barTop;

            barRect.set(cx - barWidth / 2f, barTop, cx + barWidth / 2f, barBottom);
            boolean isPeak = (i == count - 1);
            canvas.drawRoundRect(barRect, dp(7), dp(7), isPeak ? peakBarPaint : barPaint);

            canvas.drawText(labels[i], cx, h - dp(8), isPeak ? peakLabelPaint : labelPaint);
        }

        // Smooth cubic Bezier curve connecting points
        trendPath.reset();
        areaPath.reset();
        trendPath.moveTo(xs[0], ys[0]);
        areaPath.moveTo(xs[0], barBottom);
        areaPath.lineTo(xs[0], ys[0]);

        for (int i = 1; i < count; i++) {
            float midX = (xs[i - 1] + xs[i]) / 2f;
            trendPath.cubicTo(midX, ys[i - 1], midX, ys[i], xs[i], ys[i]);
            areaPath.cubicTo(midX, ys[i - 1], midX, ys[i], xs[i], ys[i]);
        }
        areaPath.lineTo(xs[count - 1], barBottom);
        areaPath.close();

        canvas.drawPath(areaPath, areaPaint);
        canvas.drawPath(trendPath, linePaint);

        // Draw nodes & value callouts
        for (int i = 0; i < count; i++) {
            boolean isPeak = (i == count - 1);
            canvas.drawCircle(xs[i], ys[i], dp(isPeak ? 5f : 3.8f), dotOuterPaint);
            canvas.drawCircle(xs[i], ys[i], dp(isPeak ? 2.6f : 1.8f), dotInnerPaint);

            String mil = PersianFormatter.toPersianDigits(String.valueOf(Math.round(values[i] / 1_000_000f))) + "M";
            if (isPeak) {
                float pw = dp(38);
                float ph = dp(18);
                pillRect.set(xs[i] - pw / 2f, ys[i] - dp(25), xs[i] + pw / 2f, ys[i] - dp(25) + ph);
                canvas.drawRoundRect(pillRect, dp(6), dp(6), pillPaint);
                canvas.drawText(mil, xs[i], pillRect.centerY() + dp(3.5f), pillTextPaint);
            } else {
                canvas.drawText(mil, xs[i], ys[i] - dp(8), valuePaint);
            }
        }
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, getResources().getDisplayMetrics());
    }
}
