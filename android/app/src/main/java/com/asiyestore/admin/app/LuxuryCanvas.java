package com.asiyestore.admin.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * v6.7 · the drawing layer of the maison.
 *
 * Four small, self-contained custom views that carry the obsidian + 24K identity into
 * places a library widget cannot reach:
 *   • GoldFoilDivider   — an engraved hairline with a diamond at its heart
 *   • ArabesqueBackdrop — a whisper-quiet islimi lattice, drawn once and cached
 *   • ShimmerSkeleton   — the golden "loading" glow behind product photography
 *   • WaxSealView       — the stamp that closes an order
 *
 * Everything is drawn in code: no extra bitmaps, no dependency growth.
 */
public final class LuxuryCanvas {

    private LuxuryCanvas() { }

    // 24K palette, restated locally so these views stay independent of MainActivity.
    private static final int GOLD        = 0xFFC8A46B;
    private static final int GOLD_LIGHT  = 0xFFEBD6B0;
    private static final int GOLD_DEEP   = 0xFF8E6C3A;
    private static final int OBSIDIAN    = 0xFF0B0B0E;

    private static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  1 · GOLD FOIL DIVIDER
    // ══════════════════════════════════════════════════════════════════════
    public static final class GoldFoilDivider extends View {

        private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint jewel = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path diamond = new Path();
        private boolean darkSurface = false;

        public GoldFoilDivider(Context c) {
            super(c);
            line.setStyle(Paint.Style.STROKE);
            line.setStrokeWidth(Math.max(1f, dp(c, 0.9f)));
            line.setStrokeCap(Paint.Cap.ROUND);
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        /** Set true when the divider sits on the obsidian canvas instead of ivory. */
        public GoldFoilDivider onDark(boolean dark) {
            this.darkSurface = dark;
            invalidate();
            return this;
        }

        @Override
        protected void onMeasure(int widthSpec, int heightSpec) {
            int w = resolveSize(dp(getContext(), 240), widthSpec);
            int h = resolveSize(dp(getContext(), 22), heightSpec);
            setMeasuredDimension(w, h);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            float cy = h / 2f;
            float half = dp(getContext(), 7);
            float gap = dp(getContext(), 13);
            float alpha = darkSurface ? 0.95f : 0.75f;

            int edge = darkSurface ? 0x000B0B0E : 0x00FAF8F3;
            int mid = darkSurface ? 0x66EBD6B0 : 0x99C8A46B;

            line.setShader(new LinearGradient(0, cy, w, cy,
                    new int[]{ edge, mid, GOLD, mid, edge },
                    new float[]{ 0f, 0.28f, 0.5f, 0.72f, 1f }, Shader.TileMode.CLAMP));
            line.setAlpha((int) (255 * alpha));

            c.drawLine(dp(getContext(), 4), cy, w / 2f - gap, cy, line);
            c.drawLine(w / 2f + gap, cy, w - dp(getContext(), 4), cy, line);

            // the jewel at the centre — a diamond with a soft gold halo
            jewel.setStyle(Paint.Style.FILL);
            jewel.setShader(new RadialGradient(w / 2f, cy, half * 3.4f,
                    new int[]{ 0x66EBD6B0, 0x22C8A46B, 0x00C8A46B },
                    null, Shader.TileMode.CLAMP));
            c.drawCircle(w / 2f, cy, half * 3.4f, jewel);

            diamond.reset();
            diamond.moveTo(w / 2f, cy - half);
            diamond.lineTo(w / 2f + half * 0.72f, cy);
            diamond.lineTo(w / 2f, cy + half);
            diamond.lineTo(w / 2f - half * 0.72f, cy);
            diamond.close();

            jewel.setShader(new LinearGradient(w / 2f - half, cy - half, w / 2f + half, cy + half,
                    new int[]{ GOLD_LIGHT, GOLD, GOLD_DEEP }, null, Shader.TileMode.CLAMP));
            c.drawPath(diamond, jewel);

            jewel.setShader(null);
            jewel.setStyle(Paint.Style.STROKE);
            jewel.setStrokeWidth(Math.max(1f, dp(getContext(), 0.8f)));
            jewel.setColor(0x88FFF6E2);
            c.drawPath(diamond, jewel);

            // two seed pearls flanking the jewel
            jewel.setStyle(Paint.Style.FILL);
            jewel.setColor(0x99C8A46B);
            float pr = dp(getContext(), 1.5f);
            c.drawCircle(w / 2f - gap, cy, pr, jewel);
            c.drawCircle(w / 2f + gap, cy, pr, jewel);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  2 · ARABESQUE BACKDROP
    // ══════════════════════════════════════════════════════════════════════
    public static final class ArabesqueBackdrop extends View {

        private final Paint ink = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path lattice = new Path();
        private boolean built = false;
        private boolean goldInk = true;
        private int intensity = 26;

        public ArabesqueBackdrop(Context c) {
            super(c);
            ink.setStyle(Paint.Style.STROKE);
            ink.setStrokeWidth(Math.max(1f, dp(c, 0.7f)));
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        /** Gold ink on obsidian, or graphite ink on ivory. */
        public ArabesqueBackdrop ink(boolean gold) {
            this.goldInk = gold;
            this.built = false;
            invalidate();
            return this;
        }

        /** 0 = invisible, 40 = a clear pattern. Twelve-star films stay quiet. */
        public ArabesqueBackdrop strength(int alphaOutOf255) {
            this.intensity = Math.max(0, Math.min(60, alphaOutOf255));
            invalidate();
            return this;
        }

        private void buildLattice(int w, int h) {
            lattice.reset();
            if (w <= 0 || h <= 0) return;
            float cell = dp(getContext(), 46);
            if (cell < 18) cell = 18;

            for (float y = cell / 2f; y < h + cell; y += cell) {
                for (float x = cell / 2f; x < w + cell; x += cell) {
                    float r = cell * 0.34f;
                    // eight-pointed star: two overlapping squares, rotated
                    for (int i = 0; i < 2; i++) {
                        float a0 = (float) (Math.PI / 4 * i);
                        for (int k = 0; k < 4; k++) {
                            double a = a0 + k * Math.PI / 2;
                            float px = x + (float) Math.cos(a) * r;
                            float py = y + (float) Math.sin(a) * r;
                            if (k == 0) lattice.moveTo(px, py);
                            else lattice.lineTo(px, py);
                        }
                        lattice.close();
                    }
                    // a fine islimi vine between the stars
                    lattice.moveTo(x - cell / 2f, y);
                    lattice.quadTo(x, y - cell * 0.18f, x + cell / 2f, y);
                }
            }
            built = true;
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            if (!built) buildLattice(getWidth(), getHeight());
            ink.setColor(goldInk ? GOLD : 0xFF8A7F6D);
            ink.setAlpha(intensity);
            c.drawPath(lattice, ink);

            // a soft vignette keeps the centre readable
            Paint veil = new Paint(Paint.ANTI_ALIAS_FLAG);
            veil.setShader(new RadialGradient(getWidth() / 2f, getHeight() / 2f,
                    Math.max(getWidth(), getHeight()) * 0.62f,
                    new int[]{ 0x00FFFFFF, goldInk ? 0xB30B0B0E : 0x66FAF8F3 },
                    new float[]{ 0.45f, 1f }, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, getWidth(), getHeight(), veil);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  3 · SHIMMER SKELETON
    // ══════════════════════════════════════════════════════════════════════
    public static final class ShimmerSkeleton extends View {

        private final Paint base = new Paint();
        private final Paint sheen = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Matrix matrix = new Matrix();
        private ValueAnimator anim;
        private float phase = 0f;
        private boolean dark = false;

        public ShimmerSkeleton(Context c, boolean darkSurface) {
            super(c);
            this.dark = darkSurface;
            sheen.setShader(makeSheen());
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        private Shader makeSheen() {
            int glow = dark ? 0x44EBD6B0 : 0x66FFFFFF;
            int clear = 0x00000000;
            return new LinearGradient(0, 0, dp(getContext(), 150), dp(getContext(), 150),
                    new int[]{ clear, glow, clear },
                    new float[]{ 0f, 0.5f, 1f }, Shader.TileMode.CLAMP);
        }

        public void startShimmer() {
            if (anim != null && anim.isRunning()) return;
            anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setDuration(1450L);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.setInterpolator(new LinearInterpolator());
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator a) {
                    phase = (float) a.getAnimatedValue();
                    invalidate();
                }
            });
            anim.start();
        }

        /** Stops the animation and dissolves the skeleton so the photograph can land. */
        public void release() {
            if (anim != null) anim.cancel();
            animate().alpha(0f).setDuration(320L).withEndAction(new Runnable() {
                @Override public void run() { setVisibility(GONE); }
            }).start();
        }

        @Override
        protected void onDetachedFromWindow() {
            if (anim != null) anim.cancel();
            super.onDetachedFromWindow();
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            base.setColor(dark ? 0xFF16151C : 0xFFEFE9DE);
            c.drawRect(0, 0, w, h, base);

            // a faint centred crest so the empty state still feels like the maison
            Paint crest = new Paint(Paint.ANTI_ALIAS_FLAG);
            crest.setStyle(Paint.Style.STROKE);
            crest.setStrokeWidth(Math.max(1f, dp(getContext(), 0.8f)));
            crest.setColor(dark ? 0x22EBD6B0 : 0x228E6C3A);
            float r = Math.min(w, h) * 0.16f;
            c.drawCircle(w / 2f, h / 2f, r, crest);
            c.drawCircle(w / 2f, h / 2f, r * 1.35f, crest);

            matrix.reset();
            float travel = w + dp(getContext(), 300);
            matrix.setTranslate(-dp(getContext(), 300) + travel * phase, 0);
            sheen.getShader().setLocalMatrix(matrix);
            c.drawRect(0, 0, w, h, sheen);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  4 · WAX SEAL
    // ══════════════════════════════════════════════════════════════════════
    public static final class WaxSealView extends View {

        private final Paint disc = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glyph = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint halo = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF bounds = new RectF();

        private float stamp = 1f;
        private float glow = 0f;

        public WaxSealView(Context c) {
            super(c);
            ring.setStyle(Paint.Style.STROKE);
            glyph.setTextAlign(Paint.Align.CENTER);
            glyph.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            setAlpha(0f);
        }

        /** Runs the stamping motion: drop, compress, settle, halo. */
        public void stamp() {
            animate().alpha(1f).setDuration(180L).start();

            ValueAnimator drop = ValueAnimator.ofFloat(1.45f, 0.94f, 1.02f, 1f);
            drop.setDuration(620L);
            drop.setInterpolator(new OvershootInterpolator(1.2f));
            drop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator a) {
                    stamp = (float) a.getAnimatedValue();
                    invalidate();
                }
            });
            drop.start();

            ValueAnimator pulse = ValueAnimator.ofFloat(0f, 1f);
            pulse.setDuration(1500L);
            pulse.setStartDelay(220L);
            pulse.setInterpolator(new DecelerateInterpolator());
            pulse.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator a) {
                    glow = (float) a.getAnimatedValue();
                    invalidate();
                }
            });
            pulse.start();
        }

        @Override
        protected void onMeasure(int wSpec, int hSpec) {
            int size = resolveSize(dp(getContext(), 96), wSpec);
            setMeasuredDimension(size, size);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            float cx = w / 2f;
            float cy = h / 2f;
            float r = Math.min(w, h) * 0.38f * stamp;

            halo.setShader(new RadialGradient(cx, cy, r * (2.1f + glow * 0.9f),
                    new int[]{ 0x55EBD6B0, 0x22C8A46B, 0x00C8A46B }, null, Shader.TileMode.CLAMP));
            c.drawCircle(cx, cy, r * (2.1f + glow * 0.9f), halo);

            disc.setShader(new RadialGradient(cx - r * 0.3f, cy - r * 0.34f, r * 1.7f,
                    new int[]{ 0xFF2A2118, 0xFF14110C, OBSIDIAN }, null, Shader.TileMode.CLAMP));
            c.drawCircle(cx, cy, r, disc);

            ring.setShader(new LinearGradient(cx - r, cy - r, cx + r, cy + r,
                    new int[]{ GOLD_LIGHT, GOLD, GOLD_DEEP, GOLD }, null, Shader.TileMode.CLAMP));
            ring.setStrokeWidth(Math.max(1.5f, r * 0.075f));
            c.drawCircle(cx, cy, r * 0.88f, ring);

            ring.setStrokeWidth(Math.max(1f, r * 0.03f));
            c.drawCircle(cx, cy, r * 0.68f, ring);

            // eight short rays, like the classic Persian seal
            ring.setStrokeWidth(Math.max(1f, r * 0.035f));
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                float x1 = cx + (float) Math.cos(a) * r * 0.74f;
                float y1 = cy + (float) Math.sin(a) * r * 0.74f;
                float x2 = cx + (float) Math.cos(a) * r * 0.82f;
                float y2 = cy + (float) Math.sin(a) * r * 0.82f;
                c.drawLine(x1, y1, x2, y2, ring);
            }

            glyph.setColor(0xFFF3E3C4);
            glyph.setTextSize(r * 0.86f);
            float baseline = cy - (glyph.descent() + glyph.ascent()) / 2f;
            c.drawText("AS", cx, baseline, glyph);

            glyph.setColor(0xAAEBD6B0);
            glyph.setTextSize(r * 0.2f);
            glyph.setLetterSpacing(0.22f);
            c.drawText("ASIYE", cx, cy + r * 0.62f, glyph);
            glyph.setLetterSpacing(0f);

            bounds.set(cx - r, cy - r, cx + r, cy + r);
        }
    }
}
