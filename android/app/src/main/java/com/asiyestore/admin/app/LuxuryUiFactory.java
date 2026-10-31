package com.asiyestore.admin.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class LuxuryUiFactory {

    public static final int COLOR_GOLD = 0xFFC6A16C;
    public static final int COLOR_GOLD_DARK = 0xFF9E7B47;
    public static final int COLOR_SOFT_GOLD = 0xFFD8BB8B;
    public static final int COLOR_IVORY = 0xFFF8F6F1;
    public static final int COLOR_WHITE = 0xFFFFFFFF;
    public static final int COLOR_CHARCOAL = 0xFF242321;
    public static final int COLOR_BLACK = 0xFF171717;
    public static final int COLOR_ROSE_BEIGE = 0xFFE8D8CE;
    public static final int COLOR_PLATINUM = 0xFFB9B6B0;
    public static final int COLOR_MUTED_TEXT = 0xFF6B665F;

    private LuxuryUiFactory() {}

    public static int dp(Context ctx, float value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, ctx.getResources().getDisplayMetrics()));
    }

    public static GradientDrawable roundedRect(Context ctx, int fillColor, float radiusDp, int strokeColor, float strokeDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(fillColor);
        gd.setCornerRadius(dp(ctx, radiusDp));
        if (strokeDp > 0) {
            gd.setStroke(Math.max(1, dp(ctx, strokeDp)), strokeColor);
        }
        return gd;
    }

    public static LinearLayout cardContainer(Context ctx, float radiusDp, int padDp) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setBackground(roundedRect(ctx, COLOR_WHITE, radiusDp, 0x44C6A16C, 1f));
        card.setElevation(dp(ctx, 2));
        int p = dp(ctx, padDp);
        card.setPadding(p, p, p, p);
        return card;
    }

    public static Button goldButton(Context ctx, String text) {
        Button btn = new Button(ctx);
        btn.setAllCaps(false);
        btn.setText(text);
        btn.setTextColor(COLOR_BLACK);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable content = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{COLOR_GOLD, COLOR_SOFT_GOLD}
        );
        content.setCornerRadius(dp(ctx, 8));
        content.setStroke(dp(ctx, 1), COLOR_GOLD_DARK);
        RippleDrawable ripple = new RippleDrawable(ColorStateList.valueOf(0x33171717), content, null);
        btn.setBackground(ripple);
        btn.setPadding(dp(ctx, 14), dp(ctx, 10), dp(ctx, 14), dp(ctx, 10));
        return btn;
    }

    public static Button darkButton(Context ctx, String text) {
        Button btn = new Button(ctx);
        btn.setAllCaps(false);
        btn.setText(text);
        btn.setTextColor(COLOR_SOFT_GOLD);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable content = roundedRect(ctx, COLOR_BLACK, 8, COLOR_GOLD, 1f);
        RippleDrawable ripple = new RippleDrawable(ColorStateList.valueOf(0x33C6A16C), content, null);
        btn.setBackground(ripple);
        btn.setPadding(dp(ctx, 14), dp(ctx, 9), dp(ctx, 14), dp(ctx, 9));
        return btn;
    }

    public static Button outlineChip(Context ctx, String text, boolean active) {
        Button btn = new Button(ctx);
        btn.setAllCaps(false);
        btn.setText(text);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        btn.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        btn.setTextColor(active ? COLOR_SOFT_GOLD : COLOR_CHARCOAL);
        btn.setBackground(roundedRect(ctx, active ? COLOR_BLACK : COLOR_WHITE, 20, COLOR_GOLD, 1f));
        btn.setMinimumHeight(0);
        btn.setMinHeight(0);
        btn.setMinimumWidth(0);
        btn.setMinWidth(0);
        btn.setPadding(dp(ctx, 14), dp(ctx, 7), dp(ctx, 14), dp(ctx, 7));
        return btn;
    }

    public static TextView badge(Context ctx, String text, int bgColor, int textColor) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextColor(textColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setBackground(roundedRect(ctx, bgColor, 14, 0, 0));
        tv.setPadding(dp(ctx, 10), dp(ctx, 4), dp(ctx, 10), dp(ctx, 4));
        return tv;
    }

    public static EditText luxuryInput(Context ctx, String hint, boolean numeric) {
        EditText et = new EditText(ctx);
        et.setHint(hint);
        et.setHintTextColor(COLOR_PLATINUM);
        et.setTextColor(COLOR_CHARCOAL);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        et.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        et.setTextDirection(View.TEXT_DIRECTION_RTL);
        if (numeric) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        et.setBackground(roundedRect(ctx, COLOR_IVORY, 8, COLOR_PLATINUM, 1f));
        int p = dp(ctx, 11);
        et.setPadding(p, p, p, p);
        return et;
    }

    public static TextView sectionTitle(Context ctx, String title) {
        TextView tv = new TextView(ctx);
        tv.setText(title);
        tv.setTextColor(COLOR_BLACK);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(ctx, 8);
        lp.bottomMargin = dp(ctx, 8);
        tv.setLayoutParams(lp);
        return tv;
    }
}
