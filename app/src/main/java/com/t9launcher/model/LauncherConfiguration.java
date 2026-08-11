package com.t9launcher.model;

import java.util.Arrays;

public final class LauncherConfiguration {
    public static final int BINDING_COUNT = 9;
    public static final int ACTION_NONE = -1;
    public static final int ACTION_CONTACTS = -2;
    public static final int ACTION_MESSAGING = -3;
    public static final int HOME_KEYS_QUICK_ACTION = 0;
    public static final int HOME_KEYS_DIALER = 1;

    public final int homeCount;
    public final int wallpaperIndex;
    public final int fontSizeSp;
    public final boolean showStatusBar;
    public final int homeKeyBehavior;
    public final int swipeLeftToRightAction;
    public final int swipeRightToLeftAction;
    public final int[] bindings;

    public LauncherConfiguration(int homeCount, int wallpaperIndex, int fontSizeSp,
                                 boolean showStatusBar, int homeKeyBehavior,
                                 int swipeLeftToRightAction, int swipeRightToLeftAction,
                                 int[] bindings) {
        this.homeCount = clamp(homeCount, 1, BINDING_COUNT);
        this.wallpaperIndex = clamp(wallpaperIndex, 0, 3);
        this.fontSizeSp = clamp(fontSizeSp, 12, 36);
        this.showStatusBar = showStatusBar;
        this.homeKeyBehavior = homeKeyBehavior == HOME_KEYS_DIALER
                ? HOME_KEYS_DIALER : HOME_KEYS_QUICK_ACTION;
        this.swipeLeftToRightAction = swipeLeftToRightAction;
        this.swipeRightToLeftAction = swipeRightToLeftAction;
        this.bindings = normalizedBindings(bindings);
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
