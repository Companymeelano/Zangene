package com.asiyestore.admin.app;
import android.content.Context;
public final class AndroidProductImagesTest {
    private static void check(boolean condition) { if (!condition) throw new AssertionError(); }
    public static void main(String[] args) {
        Context c=new Context();
        check(AndroidProductImages.resolve(c, "reference_gatieau", 99)==123);
        check(AndroidProductImages.resolve(c, "reference_wheat", 99)==99); // resource missing
        check(AndroidProductImages.resolve(c, "unknown", 99)==99);
        check(AndroidProductImages.resolve(c, null, 99)==99);
        try { AndroidProductImages.resolve(c, null, 0); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { }
        try { AndroidProductImages.resolve(null, null, 99); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { }
        System.out.println("Android product image adapter verified with resource stubs, not on device.");
    }
}
