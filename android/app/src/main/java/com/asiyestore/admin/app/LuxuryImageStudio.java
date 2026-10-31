package com.asiyestore.admin.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

/**
 * ASIYE STORE — Luxury Image Studio (v6.0, on-device).
 *
 * Turns the merchant's raw phone photo into a consistent "obsidian & champagne-gold"
 * studio asset: solid-background removal, marble/silk backdrop, gold aura, contact
 * shadow, floor reflection, beveled frame and brand signature.
 */
public final class LuxuryImageStudio {

    private LuxuryImageStudio() { }

    public static File saveToPrivateStorage(Context context, Bitmap bitmap) {
        try {
            File dir = new File(context.getFilesDir(), "product_studio");
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, "studio_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(out);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos);
            fos.flush();
            fos.close();
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap render(Bitmap source, String brandLine, String productLine) {
        int size = 1080;
        Bitmap out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);

        // ── 1. Obsidian silk backdrop ─────────────────────────────────────
        paint.setShader(new android.graphics.LinearGradient(
                0, 0, size, size,
                new int[]{0xFF17141C, 0xFF100E14, 0xFF07070A},
                new float[]{0f, 0.55f, 1f}, android.graphics.Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, size, size, paint);

        android.graphics.Paint silk = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        silk.setStyle(android.graphics.Paint.Style.STROKE);
        silk.setStrokeCap(android.graphics.Paint.Cap.ROUND);
        for (int i = 0; i < 7; i++) {
            silk.setStrokeWidth(46f + i * 18f);
            silk.setColor(i % 2 == 0 ? 0x0CFFFFFF : 0x08D9BE8E);
            android.graphics.Path p = new android.graphics.Path();
            p.moveTo(-120, 210 + i * 120);
            p.cubicTo(size * 0.3f, 60 + i * 150, size * 0.72f, 320 + i * 90, size + 140, 60 + i * 140);
            canvas.drawPath(p, silk);
        }

        // ── 2. Champagne-gold aura ────────────────────────────────────────
        android.graphics.Paint aura = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        aura.setShader(new android.graphics.RadialGradient(
                size * 0.32f, size * 0.26f, size * 0.62f,
                new int[]{0x4DD9BE8E, 0x14C8A46B, 0x00000000},
                new float[]{0f, 0.45f, 1f}, android.graphics.Shader.TileMode.CLAMP));
        canvas.drawCircle(size * 0.32f, size * 0.26f, size * 0.62f, aura);

        // ── 3. Product with background removal ────────────────────────────
        Bitmap cut = removeFlatBackground(source);
        float target = size * 0.70f;
        float scale = Math.min(target / cut.getWidth(), target / cut.getHeight());
        int pw = Math.max(1, Math.round(cut.getWidth() * scale));
        int ph = Math.max(1, Math.round(cut.getHeight() * scale));
        Bitmap scaled = Bitmap.createScaledBitmap(cut, pw, ph, true);

        float left = (size - pw) / 2f;
        float top = size * 0.50f - ph / 2f;
        if (top < size * 0.13f) top = size * 0.13f;

        // contact shadow
        android.graphics.Paint shadow = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        shadow.setShader(new android.graphics.RadialGradient(
                size / 2f, top + ph + 26, pw * 0.62f,
                new int[]{0x66000000, 0x22000000, 0x00000000},
                new float[]{0f, 0.5f, 1f}, android.graphics.Shader.TileMode.CLAMP));
        canvas.save();
        canvas.scale(1f, 0.30f, size / 2f, top + ph + 26);
        canvas.drawCircle(size / 2f, top + ph + 26, pw * 0.62f, shadow);
        canvas.restore();

        // floor reflection (flipped, faded)
        canvas.save();
        android.graphics.Matrix m = new android.graphics.Matrix();
        m.setScale(1f, -1f, 0, top + ph);
        Bitmap flipped = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), m, true);
        android.graphics.Paint refl = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        refl.setAlpha(56);
        canvas.drawBitmap(flipped, left, top + ph, refl);
        canvas.restore();

        canvas.drawBitmap(scaled, left, top, null);

        // ── 4. Beveled gold frame ─────────────────────────────────────────
        android.graphics.Paint frame = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        frame.setStyle(android.graphics.Paint.Style.STROKE);
        frame.setStrokeWidth(4f);
        frame.setShader(new android.graphics.LinearGradient(
                0, 0, size, size,
                new int[]{0xFFF7E6C4, 0xFFC8A46B, 0xFF7C5C2E, 0xFFE7CA96},
                new float[]{0f, 0.4f, 0.75f, 1f}, android.graphics.Shader.TileMode.CLAMP));
        android.graphics.RectF frameRect = new android.graphics.RectF(30, 30, size - 30, size - 30);
        canvas.drawRoundRect(frameRect, 46, 46, frame);

        // ── 5. Brand signature ────────────────────────────────────────────
        android.graphics.Paint brand = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        brand.setColor(0xFFEBD6B0);
        brand.setTextSize(46f);
        brand.setTextAlign(android.graphics.Paint.Align.CENTER);
        brand.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD));
        brand.setLetterSpacing(0.18f);
        String brandText = (brandLine == null || brandLine.trim().isEmpty()) ? "ASIYE STORE" : brandLine.trim().toUpperCase(Locale.US);
        canvas.drawText(brandText, size / 2f, size - 118f, brand);

        android.graphics.Paint line = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        line.setColor(0x66C8A46B);
        line.setStrokeWidth(2f);
        canvas.drawLine(size / 2f - 90, size - 96f, size / 2f + 90, size - 96f, line);

        android.graphics.Paint sub = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        sub.setColor(0xFF8F8779);
        sub.setTextSize(30f);
        sub.setTextAlign(android.graphics.Paint.Align.CENTER);
        sub.setLetterSpacing(0.10f);
        String subText = (productLine == null || productLine.trim().isEmpty())
                ? "SIGNATURE COLLECTION" : productLine.trim();
        if (subText.length() > 34) subText = subText.substring(0, 34) + "…";
        canvas.drawText(subText, size / 2f, size - 62f, sub);

        return out;
    }

    /**
     * Removes a flat studio/wall background. Safe: if the border is not uniform
     * (busy real-world photo), the original pixels are returned untouched.
     */
    private static Bitmap removeFlatBackground(Bitmap src) {
        int maxSide = 760;
        float ratio = Math.min(1f, maxSide / (float) Math.max(src.getWidth(), src.getHeight()));
        int w = Math.max(2, Math.round(src.getWidth() * ratio));
        int h = Math.max(2, Math.round(src.getHeight() * ratio));
        Bitmap base = Bitmap.createScaledBitmap(src, w, h, true);

        int[] px = new int[w * h];
        base.getPixels(px, 0, w, 0, 0, w, h);

        // sample border ring
        long r = 0, g = 0, b = 0;
        int count = 0;
        for (int x = 0; x < w; x += 3) {
            for (int y : new int[]{0, h - 1}) {
                int c = px[y * w + x];
                r += (c >> 16) & 0xFF; g += (c >> 8) & 0xFF; b += c & 0xFF; count++;
            }
        }
        for (int y = 0; y < h; y += 3) {
            for (int x : new int[]{0, w - 1}) {
                int c = px[y * w + x];
                r += (c >> 16) & 0xFF; g += (c >> 8) & 0xFF; b += c & 0xFF; count++;
            }
        }
        if (count == 0) return base;
        int br = (int) (r / count), bg = (int) (g / count), bb = (int) (b / count);

        // border variance → is this a flat backdrop?
        double variance = 0;
        int checked = 0;
        for (int x = 0; x < w; x += 7) {
            int c = px[x];
            int dr = ((c >> 16) & 0xFF) - br, dg = ((c >> 8) & 0xFF) - bg, db = (c & 0xFF) - bb;
            variance += dr * dr + dg * dg + db * db;
            checked++;
        }
        variance = checked == 0 ? 9999 : Math.sqrt(variance / checked);
        if (variance > 62) return base; // busy background → keep original

        int[] outPx = new int[px.length];
        for (int i = 0; i < px.length; i++) {
            int c = px[i];
            int dr = ((c >> 16) & 0xFF) - br;
            int dg = ((c >> 8) & 0xFF) - bg;
            int db = (c & 0xFF) - bb;
            double dist = Math.sqrt(dr * dr + dg * dg + db * db);
            int alpha;
            if (dist < 24) alpha = 0;
            else if (dist < 52) alpha = (int) Math.min(255, (dist - 24) / 28.0 * 255);
            else alpha = 255;
            outPx[i] = (alpha << 24) | (c & 0x00FFFFFF);
        }
        return Bitmap.createBitmap(outPx, w, h, Bitmap.Config.ARGB_8888);
    }
}
