package com.asiyestore.admin.app;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

public final class RecoveryTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        for (int theme = 0; theme <= 6; theme++) {
            check(ThemePreference.resolve(true, theme, true) == theme, "saved theme");
        }
        check(ThemePreference.resolve(false, 0, false) == 6, "default");
        check(ThemePreference.resolve(false, 0, true) == 2, "legacy night");
        check(ThemePreference.resolve(true, -1, false) == 6, "invalid low");
        check(ThemePreference.resolve(true, 7, true) == 2, "invalid high");
        for (int page = 0; page < 6; page++) {
            check(SimpleNavigation.customerMoreSelected(page) == (page == 1 || page == 3 || page == 5), "customer grouping");
            check(SimpleNavigation.adminMoreSelected(page) == (page == 3 || page == 4 || page == 5), "admin grouping");
        }
        check(JewelIconRegistry.assetName(11, false) == null, "unselected heart fallback");
        check("jewel_heart.png".equals(JewelIconRegistry.assetName(11, true)), "selected heart");
        check(JewelIconRegistry.assetName(999, true) == null, "unknown icon fallback");
        check(JewelIconRegistry.mappings().size() == 25, "mapping count");
        HashSet<String> files = new HashSet<>();
        for (int icon : JewelIconRegistry.mappings().keySet()) {
            String name = JewelIconRegistry.assetName(icon, true);
            check(Files.isRegularFile(Paths.get(args[0], name)), "missing asset " + name);
            files.add(name);
        }
        check(files.size() == 23, "distinct assets");
        try { JewelIconRegistry.mappings().clear(); throw new AssertionError("mutable mappings"); }
        catch (UnsupportedOperationException expected) { }
        System.out.println("Recovered preferences, navigation and 23 icon assets verified.");
    }
}
