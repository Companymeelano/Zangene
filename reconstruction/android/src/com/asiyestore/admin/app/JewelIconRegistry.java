package com.asiyestore.admin.app;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Asset names reconstructed from the demo APK. No UI, I/O or credentials. */
public final class JewelIconRegistry {
    public static final int HEART = 11;
    private static final Map<Integer, String> ICONS;
    static {
        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(1, "home"); m.put(2, "shield"); m.put(3, "bag"); m.put(4, "crown");
        m.put(5, "phone"); m.put(7, "whatsapp"); m.put(8, "instagram"); m.put(9, "shield");
        m.put(11, "heart"); m.put(13, "crown"); m.put(14, "search"); m.put(17, "gift");
        m.put(18, "share"); m.put(20, "chart"); m.put(21, "lock"); m.put(24, "time");
        m.put(25, "ai"); m.put(26, "camera"); m.put(27, "settings"); m.put(31, "truck");
        m.put(34, "box"); m.put(35, "users"); m.put(37, "diamond");
        m.put(39, "bell"); m.put(41, "doc");
        ICONS = Collections.unmodifiableMap(m);
    }
    private JewelIconRegistry() { }

    /** Unselected favorite uses the outline fallback to distinguish its state. */
    public static String assetName(int type, boolean selected) {
        if (type == HEART && !selected) return null;
        String name = ICONS.get(type);
        return name == null ? null : "jewel_" + name + ".png";
    }
    public static Map<Integer, String> mappings() { return ICONS; }
}
