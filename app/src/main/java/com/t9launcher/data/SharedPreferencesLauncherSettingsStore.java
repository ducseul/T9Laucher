package com.t9launcher.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.t9launcher.model.LauncherConfiguration;

import static com.t9launcher.model.LauncherConfiguration.ACTION_CONTACTS;
import static com.t9launcher.model.LauncherConfiguration.ACTION_MESSAGING;
import static com.t9launcher.model.LauncherConfiguration.BINDING_COUNT;
import static com.t9launcher.model.LauncherConfiguration.DEFAULT_CLOCK_FONT_SIZE_SP;
import static com.t9launcher.model.LauncherConfiguration.DEFAULT_CLOCK_ALIGNMENT;
import static com.t9launcher.model.LauncherConfiguration.DEFAULT_CLOCK_STYLE;
import static com.t9launcher.model.LauncherConfiguration.DEFAULT_DRAWER_GRID_COLUMNS;
import static com.t9launcher.model.LauncherConfiguration.DEFAULT_DRAWER_GRID_ICON_CORNER_RADIUS_DP;
import static com.t9launcher.model.LauncherConfiguration.DEFAULT_DRAWER_GRID_ICON_SIZE_DP;
import static com.t9launcher.model.LauncherConfiguration.DEFAULT_DRAWER_GRID_ROWS;
import static com.t9launcher.model.LauncherConfiguration.DRAWER_LAYOUT_LIST;
import static com.t9launcher.model.LauncherConfiguration.HOME_KEYS_QUICK_ACTION;

public final class SharedPreferencesLauncherSettingsStore implements LauncherSettingsStore {
    private final SharedPreferences preferences;

    public SharedPreferencesLauncherSettingsStore(Context context) {
        preferences = context.getSharedPreferences("launcher", Context.MODE_PRIVATE);
    }

    @Override
    public LauncherConfiguration load() {
        int[] bindings = new int[BINDING_COUNT];
        for (int index = 0; index < bindings.length; index++) {
            bindings[index] = preferences.getInt("binding" + index, index);
        }
        return new LauncherConfiguration(
                preferences.getInt("homeCount", 4),
                preferences.getInt("wallpaper", 0),
                preferences.getInt("fontSizeSp", 14),
                preferences.getInt("clockFontSizeSp", DEFAULT_CLOCK_FONT_SIZE_SP),
                preferences.getInt("clockStyle", DEFAULT_CLOCK_STYLE),
                preferences.getInt("clockAlignment", DEFAULT_CLOCK_ALIGNMENT),
                preferences.getBoolean("showStatusBar", true),
                preferences.getBoolean("animationsEnabled", true),
                preferences.getInt("drawerLayout", DRAWER_LAYOUT_LIST),
                preferences.getInt("drawerGridColumns", DEFAULT_DRAWER_GRID_COLUMNS),
                preferences.getInt("drawerGridRows", DEFAULT_DRAWER_GRID_ROWS),
                preferences.getInt("drawerGridIconSizeDp", DEFAULT_DRAWER_GRID_ICON_SIZE_DP),
                preferences.getInt("drawerGridIconCornerRadiusDp",
                        DEFAULT_DRAWER_GRID_ICON_CORNER_RADIUS_DP),
                preferences.getInt("homeKeyBehavior", HOME_KEYS_QUICK_ACTION),
                preferences.getInt("swipeLeftToRightAction", ACTION_CONTACTS),
                preferences.getInt("swipeRightToLeftAction", ACTION_MESSAGING),
                bindings);
    }

    @Override
    public void save(LauncherConfiguration configuration) {
        SharedPreferences.Editor editor = preferences.edit()
                .putInt("homeCount", configuration.homeCount)
                .putInt("wallpaper", configuration.wallpaperIndex)
                .putInt("fontSizeSp", configuration.fontSizeSp)
                .putInt("clockFontSizeSp", configuration.clockFontSizeSp)
                .putInt("clockStyle", configuration.clockStyle)
                .putInt("clockAlignment", configuration.clockAlignment)
                .putBoolean("showStatusBar", configuration.showStatusBar)
                .putBoolean("animationsEnabled", configuration.animationsEnabled)
                .putInt("drawerLayout", configuration.drawerLayout)
                .putInt("drawerGridColumns", configuration.drawerGridColumns)
                .putInt("drawerGridRows", configuration.drawerGridRows)
                .putInt("drawerGridIconSizeDp", configuration.drawerGridIconSizeDp)
                .putInt("drawerGridIconCornerRadiusDp",
                        configuration.drawerGridIconCornerRadiusDp)
                .putInt("homeKeyBehavior", configuration.homeKeyBehavior)
                .putInt("swipeLeftToRightAction", configuration.swipeLeftToRightAction)
                .putInt("swipeRightToLeftAction", configuration.swipeRightToLeftAction);
        for (int index = 0; index < configuration.bindings.length; index++) {
            editor.putInt("binding" + index, configuration.bindings[index]);
        }
        editor.apply();
    }
}
