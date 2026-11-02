package android.graphics;
/** Minimal JVM test double; production uses Android's Bitmap. */
public class Bitmap {
    public int getWidth() { return 1; }
    public int getHeight() { return 1; }
    public int getPixel(int x, int y) { return 0; }
    public static Bitmap createScaledBitmap(Bitmap b, int w, int h, boolean filter) { return b; }
}
