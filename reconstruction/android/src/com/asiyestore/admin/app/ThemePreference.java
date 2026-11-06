package com.asiyestore.admin.app;

/** Reconstructed from the isolated demo; does not access or mutate user preferences. */
public final class ThemePreference {
    public static final int PASTEL_ROSE = 6;
    private static final int LEGACY_NIGHT = 2;
    private ThemePreference() { }

    /** Preserve valid saved themes; migrate legacy dark mode only when no valid value exists. */
    public static int resolve(boolean hasSavedTheme, int savedTheme, boolean legacyDarkMode) {
        if (hasSavedTheme && savedTheme >= 0 && savedTheme <= PASTEL_ROSE) return savedTheme;
        return legacyDarkMode ? LEGACY_NIGHT : PASTEL_ROSE;
    }
}
