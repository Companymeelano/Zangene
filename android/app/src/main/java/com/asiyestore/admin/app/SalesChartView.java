package com.asiyestore.admin.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.TypedValue;
import android.view.View;

public final class SalesChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();
    private final Path trendPath = new Path();

    private long[] values = new long[]{18_500_000L, 24_200_000L, 21_000_000L, 32_800_000L, 29_400_000L, 41_500_000L, 48_900_000L};
    private String[] labels = new String[]{"ش", "ی", "د", "س", "چ", "پ", "ج"};

    public SalesChartView(Context context) {
        super(context);
        gridPaint.setColor(0x26B9B6B0);
        gridPaint.setStrokeWidth(dp(1));

        labelPaint.setColor(0xFF6B665F);
        labelPaint.setTextSize(sp(11));
        labelPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint.setColor(0xFFA6824D);
        valuePaint.setTextSize(sp(9.5f));
        valuePaint.setFakeBoldText(true);
        valuePaint.setTextAlign(Paint.Align.CENTER);

        linePaint.setColor(0xFF171717);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2));

        dotPaint.setColor(0xFFC6A16C);
        dotPaint.setStyle(Paint.Style.FILL);
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

        float topPad = dp(24);
        float bottomPad = dp(28);
        float sidePad = dp(16);
        float chartHeight = h - topPad - bottomPad;

        // Horizontal subtle reference lines
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
        float barWidth = Math.min(dp(26), slotWidth * 0.52f);

        barPaint.setShader(new LinearGradient(
                0, topPad, 0, topPad + chartHeight,
                0xFFC6A16C, 0xFFE8D8CE, Shader.TileMode.CLAMP
        ));

        trendPath.reset();
        // Draw RTL right-to-left so day 0 is on the right and today is on the left
        for (int i = 0; i < count; i++) {
            int rtlIndex = count - 1 - i;
            float cx = sidePad + slotWidth * rtlIndex + slotWidth / 2f;
            float ratio = (float) values[i] / (float) maxVal;
            float barTop = topPad + chartHeight * (1f - Math.max(0.06f, ratio * 0.88f));
            float barBottom = topPad + chartHeight;

            barRect.set(cx - barWidth / 2f, barTop, cx + barWidth / 2f, barBottom);
            canvas.drawRoundRect(barRect, dp(6), dp(6), barPaint);

            String mil = PersianFormatter.toPersianDigits(String.valueOf(Math.round(values[i] / 1_000_000f))) + "M";
            canvas.drawText(mil, cx, barTop - dp(6), valuePaint);
            canvas.drawText(labels[i], cx, h - dp(8), labelPaint);

            float lineY = barTop + dp(4);
            if (i == 0) {
                trendPath.moveTo(cx, lineY);
            } else {
                trendPath.lineTo(cx, lineY);
            }
        }

        canvas.drawPath(trendPath, linePaint);
        for (int i = 0; i < count; i++) {
            int rtlIndex = count - 1 - i;
            float cx = sidePad + slotWidth * rtlIndex + slotWidth / 2f;
            float ratio = (float) values[i] / (float) maxVal;
            float barTop = topPad + chartHeight * (1f - Math.max(0.06f, ratio * 0.88f));
            canvas.drawCircle(cx, barTop + dp(4), dp(3.5f), dotPaint);
        }
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, getResources().getDisplayMetrics());
    }
}
