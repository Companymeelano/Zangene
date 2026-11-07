package android.content;
import android.content.res.Resources;
public class Context {
    public Resources getResources() { return new Resources(); }
    public String getPackageName() { return "com.test"; }
}
