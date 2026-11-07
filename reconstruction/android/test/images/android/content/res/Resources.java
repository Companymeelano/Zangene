package android.content.res;
public class Resources {
    public int getIdentifier(String name, String type, String pkg) {
        if (!"drawable".equals(type) || !"com.test".equals(pkg)) throw new AssertionError("wrong resource lookup");
        return "img_reference_gatieau".equals(name) ? 123 : 0;
    }
}
