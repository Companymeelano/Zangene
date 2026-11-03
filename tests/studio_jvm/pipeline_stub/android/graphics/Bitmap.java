package android.graphics;
import java.io.OutputStream;
public class Bitmap {
    public enum CompressFormat { JPEG }
    public boolean compress(CompressFormat format, int quality, OutputStream out) {
        try { out.write(new byte[]{1,2,3}); return true; } catch (Exception e) { return false; }
    }
}
