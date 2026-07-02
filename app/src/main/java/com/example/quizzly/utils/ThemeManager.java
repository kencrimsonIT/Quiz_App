package com.example.quizzly.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";
    public static final String KEY_THEME_CHANGED = "theme_changed";

    public static final int MODE_FOLLOW_SYSTEM = -1;
    public static final int MODE_LIGHT = 0;
    public static final int MODE_DARK = 1;

    /**
     * Apply the saved theme mode on app startup.
     */
    public static void applySavedTheme(Context context) {
        int mode = getSavedThemeMode(context);
        AppCompatDelegate.setDefaultNightMode(modeToAppCompatMode(mode));
    }

    /**
     * Toggle dark mode on/off. If dark is true, switch to dark mode;
     * if false, switch to light mode.
     */
    public static void setDarkMode(Context context, boolean isDarkMode) {
        saveThemeMode(context, isDarkMode ? MODE_DARK : MODE_LIGHT);
        AppCompatDelegate.setDefaultNightMode(isDarkMode
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    /**
     * Check if the current theme mode is dark.
     */
    public static boolean isDarkMode(Context context) {
        int mode = getSavedThemeMode(context);
        if (mode == MODE_FOLLOW_SYSTEM) {
            // Check system setting
            int nightMode = context.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            return nightMode == Configuration.UI_MODE_NIGHT_YES;
        }
        return mode == MODE_DARK;
    }

    /**
     * Check if the user has explicitly set a theme (not following system).
     */
    public static boolean hasExplicitTheme(Context context) {
        return getSavedThemeMode(context) != MODE_FOLLOW_SYSTEM;
    }

    private static int getSavedThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Default to follow system
        return prefs.getInt(KEY_DARK_MODE, MODE_FOLLOW_SYSTEM);
    }

    private static void saveThemeMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_DARK_MODE, mode).apply();
    }

    /**
     * Reset to follow system setting.
     */
    public static void resetToSystem(Context context) {
        saveThemeMode(context, MODE_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private static int modeToAppCompatMode(int mode) {
        switch (mode) {
            case MODE_LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case MODE_DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}
