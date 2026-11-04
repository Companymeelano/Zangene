package com.asiyestore.admin.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import java.io.File;
import java.io.FileOutputStream;

/** Local compositing, not generative AI. The original photograph is never segmented or altered. */
public final class LuxuryImageStudio {
    private LuxuryImageStudio() { }
    public static File saveToPrivateStorage(Context context, Bitmap bitmap) {
        try {
            File dir = new File(context.getFilesDir(), "product_studio");
            if (!dir.exists() && !dir.mkdirs()) return null;
            File out = new File(dir, "studio_" + java.util.UUID.randomUUID().toString() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)) { out.delete(); return null; }
                fos.flush();
            }
            return out;
        } catch (Exception e) { return null; }
    }
    private static int alpha(int color, int a) { return (color & 0x00ffffff) | (a << 24); }
    private static int mix(int a, int b, float t) {
        return Color.rgb((int)(Color.red(a)*(1-t)+Color.red(b)*t), (int)(Color.green(a)*(1-t)+Color.green(b)*t), (int)(Color.blue(a)*(1-t)+Color.blue(b)*t));
    }
    /** Colors are captured from the active theme; variation changes layout as well as lighting. */
    public static Bitmap render(Bitmap source, int dark, int surface, int metal, int variant) {
        if (source == null || source.isRecycled() || source.getWidth() < 1 || source.getHeight() < 1) throw new IllegalArgumentException("photo");
        int mode = Math.floorMod(variant, 3), cycle = Math.floorDiv(variant, 3);
        final int w=1440, h=1080; Bitmap out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(out); Paint p=new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        int deep=mix(dark, Color.BLACK, .22f), light=mix(surface, Color.WHITE, .30f);
        p.setShader(new LinearGradient(0,0,w,h,new int[]{dark,mix(dark,surface,.22f),deep},null,Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,w,h,p); p.setShader(null);
        float glowX = (mode == 1 ? w*.70f : mode == 2 ? w*.50f : w*.30f) + (Math.floorMod(cycle,3)-1)*90f;
        p.setShader(new RadialGradient(glowX, h*.30f, w*.72f,new int[]{alpha(surface,100),alpha(metal,28),0x00000000},null,Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,w,h,p); p.setShader(null);
        // A sculptural architectural backdrop. Keep details away from the product.
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3f); p.setColor(alpha(metal,75));
        if (mode==0) {
            canvas.drawRoundRect(new RectF(300,100,1140,1030),420,420,p);
            p.setColor(alpha(metal,38)); canvas.drawRoundRect(new RectF(335,135,1105,995),385,385,p);
        } else if (mode==1) {
            p.setStrokeWidth(7f); canvas.drawCircle(720,500,405,p);
            p.setStrokeWidth(2f); p.setColor(alpha(metal,48)); canvas.drawCircle(720,500,440,p);
        } else {
            Path arch=new Path(); arch.moveTo(285,1000); arch.lineTo(285,420); arch.cubicTo(285,10,1155,10,1155,420); arch.lineTo(1155,1000); canvas.drawPath(arch,p);
        }
        p.setStyle(Paint.Style.FILL);
        // Raised stone podium with a soft contact shadow and gold rim.
        p.setColor(0x44000000); canvas.drawOval(new RectF(260,823,1180,1003),p);
        p.setShader(new LinearGradient(0,800,0,980,new int[]{mix(dark,surface,.46f),deep},null,Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(320,800,1120,956),60,60,p); p.setShader(null);
        p.setShader(new LinearGradient(320,800,1120,875,new int[]{light,mix(surface,metal,.27f),mix(dark,surface,.65f)},null,Shader.TileMode.CLAMP));
        canvas.drawOval(new RectF(320,766,1120,880),p); p.setShader(null);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3); p.setColor(alpha(metal,170)); canvas.drawOval(new RectF(320,766,1120,880),p); p.setStyle(Paint.Style.FILL);
        // Product photo remains intact, centred inside a tactile floating frame.
        float dx = (mode == 0 ? -24 : mode == 1 ? 24 : 0) + (Math.floorMod(cycle,3)-1)*20;
        RectF frame=new RectF(375+dx,188,1065+dx,805);
        p.setColor(0x50000000); canvas.drawRoundRect(new RectF(frame.left+18,frame.top+25,frame.right+18,frame.bottom+25),42,42,p);
        p.setShader(new LinearGradient(frame.left,frame.top,frame.right,frame.bottom,new int[]{light,mix(surface,metal,.24f),light},null,Shader.TileMode.CLAMP));
        canvas.drawRoundRect(frame,42,42,p); p.setShader(null);
        RectF inner=new RectF(frame.left+16,frame.top+16,frame.right-16,frame.bottom-16);
        p.setColor(light); canvas.drawRoundRect(inner,30,30,p);
        float availW=inner.width()-28, availH=inner.height()-28;
        float scale=Math.min(availW/source.getWidth(),availH/source.getHeight());
        float pw=source.getWidth()*scale, ph=source.getHeight()*scale;
        RectF dest=new RectF(inner.centerX()-pw/2,inner.centerY()-ph/2,inner.centerX()+pw/2,inner.centerY()+ph/2);
        canvas.save(); Path clip=new Path(); clip.addRoundRect(inner,28,28,Path.Direction.CW); canvas.clipPath(clip);
        p.setColor(Color.WHITE); canvas.drawBitmap(source,null,dest,p); canvas.restore();
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(alpha(metal,135)); canvas.drawRoundRect(frame,42,42,p); p.setStyle(Paint.Style.FILL);
        // A subtle alternate highlight on every pass, without superimposed copy.
        p.setColor(alpha(metal, 45 + Math.floorMod(cycle,3)*10));
        canvas.drawCircle(mode==1 ? 190 : 1250, 260 + Math.floorMod(cycle,3)*35, 5, p);
        return out;
    }
}
