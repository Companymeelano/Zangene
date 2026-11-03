package com.asiyestore.admin.app;
import android.content.Context;
import java.util.HashMap;
public final class StudioCredentials {
    static final HashMap<String,String> VALUES = new HashMap<>();
    public static String get(Context c, String id) throws Exception {
        if ("bad-storage".equals(VALUES.get(id))) throw new IllegalStateException();
        return VALUES.containsKey(id) ? VALUES.get(id) : "test-key";
    }
}
