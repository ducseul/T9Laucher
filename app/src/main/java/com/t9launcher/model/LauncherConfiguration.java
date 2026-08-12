package com.t9launcher.model;

import java.util.Arrays;

public final class LauncherConfiguration {
    public static final int BINDING_COUNT = 9;
    public static final int ACTION_NONE = -1;
    public static final int ACTION_CONTACTS = -2;
    public static final int ACTION_MESSAGING = -3;
    public static final int HOME_KEYS_QUICK_ACTION = 0;
    public static final int HOME_KEYS_DIALER = 1;
    public static final int DRAWER_LAYOUT_LIST = 0;
    public static final int DRAWER_LAYOUT_GRID = 1;
    public static final int DEFAULT_DRAWER_GRID_COLUMNS = 4;
    public static final int DEFAULT_DRAWER_GRID_ROWS = 5;
    public static final int DEFAULT_DRAWER_GRID_ICON_SIZE_DP = 40;
    public static final int DEFAULT_DRAWER_GRID_ICON_CORNER_RADIUS_DP = 8;
    public static final int MIN_DRAWER_GRID_COLUMNS = 2;
    public static final int MAX_DRAWER_GRID_COLUMNS = 6;
    public static final int MIN_DRAWER_GRID_ROWS = 2;
    public static final int MAX_DRAWER_GRID_ROWS = 6;
    public static final int MIN_DRAWER_GRID_ICON_SIZE_DP = 20;
    public static final int MAX_DRAWER_GRID_ICON_SIZE_DP = 64;
    public static final int MIN_DRAWER_GRID_ICON_CORNER_RADIUS_DP = 0;
    public static final int MAX_DRAWER_GRID_ICON_CORNER_RADIUS_DP = 24;
    public static final int DEFAULT_CLOCK_FONT_SIZE_SP = 32;
    public static final int MIN_CLOCK_FONT_SIZE_SP = 24;
    public static final int MAX_CLOCK_FONT_SIZE_SP = 48;
    public static final int MIN_DATE_FONT_SIZE_SP = 14;

    public final int homeCount;
    public final int wallpaperIndex;
    public final int fontSizeSp;
    public final int clockFontSizeSp;
    public final boolean showStatusBar;
    public final boolean animationsEnabled;
    public final int drawerLayout;
    public final int drawerGridColumns;
    public final int drawerGridRows;
    public final int drawerGridIconSizeDp;
    public final int drawerGridIconCornerRadiusDp;
    public final int homeKeyBehavior;
    public final int swipeLeftToRightAction;
    public final int swipeRightToLeftAction;
    public final int[] bindings;

    public LauncherConfiguration(int homeCount, int wallpaperIndex, int fontSizeSp,
                                 boolean showStatusBar, int homeKeyBehavior,
                                 int swipeLeftToRightAction, int swipeRightToLeftAction,
                                 int[] bindings) {
        this(homeCount, wallpaperIndex, fontSizeSp, showStatusBar,
                DRAWER_LAYOUT_LIST, DEFAULT_DRAWER_GRID_COLUMNS, DEFAULT_DRAWER_GRID_ROWS,
                homeKeyBehavior, swipeLeftToRightAction, swipeRightToLeftAction, bindings);
    }

    public LauncherConfiguration(int homeCount, int wallpaperIndex, int fontSizeSp,
                                 boolean showStatusBar, int drawerLayout,
                                 int drawerGridColumns, int drawerGridRows,
                                 int homeKeyBehavior, int swipeLeftToRightAction,
                                 int swipeRightToLeftAction, int[] bindings) {
        this(homeCount, wallpaperIndex, fontSizeSp, showStatusBar, drawerLayout,
                drawerGridColumns, drawerGridRows, DEFAULT_DRAWER_GRID_ICON_SIZE_DP,
                DEFAULT_DRAWER_GRID_ICON_CORNER_RADIUS_DP, homeKeyBehavior,
                swipeLeftToRightAction, swipeRightToLeftAction, bindings);
    }

    public LauncherConfiguration(int homeCount, int wallpaperIndex, int fontSizeSp,
                                 boolean showStatusBar, int drawerLayout,
                                 int drawerGridColumns, int drawerGridRows,
                                 int drawerGridIconSizeDp, int drawerGridIconCornerRadiusDp,
                                 int homeKeyBehavior, int swipeLeftToRightAction,
                                 int swipeRightToLeftAction, int[] bindings) {
        this(homeCount, wallpaperIndex, fontSizeSp, showStatusBar, true,
                drawerLayout, drawerGridColumns, drawerGridRows,
                drawerGridIconSizeDp, drawerGridIconCornerRadiusDp,
                homeKeyBehavior, swipeLeftToRightAction, swipeRightToLeftAction,
                bindings);
    }

    public LauncherConfiguration(int homeCount, int wallpaperIndex, int fontSizeSp,
                                 boolean showStatusBar, boolean animationsEnabled,
                                 int drawerLayout, int drawerGridColumns, int drawerGridRows,
                                 int drawerGridIconSizeDp, int drawerGridIconCornerRadiusDp,
                                 int homeKeyBehavior, int swipeLeftToRightAction,
                                 int swipeRightToLeftAction, int[] bindings) {
        this(homeCount, wallpaperIndex, fontSizeSp, DEFAULT_CLOCK_FONT_SIZE_SP,
                showStatusBar, animationsEnabled, drawerLayout, drawerGridColumns,
                drawerGridRows, drawerGridIconSizeDp, drawerGridIconCornerRadiusDp,
                homeKeyBehavior, swipeLeftToRightAction, swipeRightToLeftAction,
                bindings);
    }

    public LauncherConfiguration(int homeCount, int wallpaperIndex, int fontSizeSp,
                                 int clockFontSizeSp, boolean showStatusBar,
                                 boolean animationsEnabled, int drawerLayout,
                                 int drawerGridColumns, int drawerGridRows,
                                 int drawerGridIconSizeDp, int drawerGridIconCornerRadiusDp,
                                 int homeKeyBehavior, int swipeLeftToRightAction,
                                 int swipeRightToLeftAction, int[] bindings) {
        this.homeCount = clamp(homeCount, 1, BINDING_COUNT);
        this.wallpaperIndex = clamp(wallpaperIndex, 0, 3);
        this.fontSizeSp = clamp(fontSizeSp, 12, 36);
        this.clockFontSizeSp = clamp(clockFontSizeSp,
                MIN_CLOCK_FONT_SIZE_SP, MAX_CLOCK_FONT_SIZE_SP);
        this.showStatusBar = showStatusBar;
        this.animationsEnabled = animationsEnabled;
        this.drawerLayout = drawerLayout == DRAWER_LAYOUT_GRID
                ? DRAWER_LAYOUT_GRID : DRAWER_LAYOUT_LIST;
        this.drawerGridColumns = clamp(drawerGridColumns,
                MIN_DRAWER_GRID_COLUMNS, MAX_DRAWER_GRID_COLUMNS);
        this.drawerGridRows = clamp(drawerGridRows,
                MIN_DRAWER_GRID_ROWS, MAX_DRAWER_GRID_ROWS);
        this.drawerGridIconSizeDp = clamp(drawerGridIconSizeDp,
                MIN_DRAWER_GRID_ICON_SIZE_DP, MAX_DRAWER_GRID_ICON_SIZE_DP);
        this.drawerGridIconCornerRadiusDp = clamp(drawerGridIconCornerRadiusDp,
                MIN_DRAWER_GRID_ICON_CORNER_RADIUS_DP,
                MAX_DRAWER_GRID_ICON_CORNER_RADIUS_DP);
        this.homeKeyBehavior = homeKeyBehavior == HOME_KEYS_DIALER
                ? HOME_KEYS_DIALER : HOME_KEYS_QUICK_ACTION;
        this.swipeLeftToRightAction = swipeLeftToRightAction;
        this.swipeRightToLeftAction = swipeRightToLeftAction;
        this.bindings = normalizedBindings(bindings);
    }

    public int dateFontSizeSp() {
        return dateFontSizeSp(clockFontSizeSp);
    }

    public static int dateFontSizeSp(int clockFontSizeSp) {
        return Math.max(MIN_DATE_FONT_SIZE_SP, Math.round(clockFontSizeSp * 0.5f));
    }

    private static int[] normalizedBindings(int[] source) {
        int[] result = new int[BINDING_COUNT];
        Arrays.fill(result, ACTION_NONE);
        if (source != null) {
            System.arraycopy(source, 0, result, 0, Math.min(source.length, result.length));
        }
        return result;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
