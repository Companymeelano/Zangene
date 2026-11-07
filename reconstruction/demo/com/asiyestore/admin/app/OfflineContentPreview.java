package com.asiyestore.admin.app;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Read-only editorial material in the isolated offline demo, never an admin feature. */
public final class OfflineContentPreview {
    private OfflineContentPreview() { }
    public static void addContentButton(final Context context, LinearLayout container, String sku) {
        LuxuryCoinDraftCatalog.Draft found = null;
        for (LuxuryCoinDraftCatalog.Draft d : LuxuryCoinDraftCatalog.drafts())
            if (d.sku.equals(sku)) { found = d; break; }
        if (found == null) return;
        final LuxuryCoinDraftCatalog.Draft d = found;
        Button button = new Button(context);
        button.setText("مشاهده محتوای کامل و کپشن‌ها");
        button.setTextColor(Color.rgb(40, 33, 38));
        container.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) { show(context, d); }
        });
    }
    private static void show(Context context, LuxuryCoinDraftCatalog.Draft d) {
        TextView text = new TextView(context);
        text.setTextColor(Color.rgb(40, 33, 38));
        text.setTextSize(16);
        text.setGravity(Gravity.RIGHT);
        text.setTextIsSelectable(true);
        int pad = (int)(20 * context.getResources().getDisplayMetrics().density);
        text.setPadding(pad, pad, pad, pad);
        text.setText("پیش‌نمایش آفلاین؛ قیمت و موجودی نمونه هستند. پیش از انتشار مشخصات، قیمت، موجودی و مجوز تصویر را تأیید کنید.\n\n"
            + "معرفی کوتاه\n" + d.shortDescription + "\n\nتوضیحات\n" + d.description
            + "\n\nروش مصرف\n" + d.usage + "\n\nحجم یا وزن\n" + d.size
            + "\n\nعنوان سئو\n" + d.seoTitle + "\n\nتوضیح سئو\n" + d.seoDescription
            + "\n\nکلمات کلیدی\n" + d.keywords + "\n\nشروع پست\n" + d.postHook
            + "\n\nمتن جایگزین تصویر\n" + d.imageAltText + "\n\nکپشن اینستاگرام\n" + d.instagram
            + "\n\nکپشن تلگرام\n" + d.telegram + "\n\nپیام واتساپ\n" + d.whatsapp
            + "\n\nمتن یوتیوب\n" + d.youtube + "\n\nهشتگ و کپشن ورود به اکسپلور را تضمین نمی‌کنند.");
        ScrollView scroll = new ScrollView(context);
        scroll.addView(text);
        new AlertDialog.Builder(context).setTitle(d.nameFa).setView(scroll).setPositiveButton("بستن", null).show();
    }
}
