package com.t9launcher.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.t9launcher.model.LauncherConfiguration;

import static com.t9launcher.model.LauncherConfiguration.ACTION_CONTACTS;
import static com.t9launcher.model.LauncherConfiguration.ACTION_MESSAGING;
import static com.t9launcher.model.LauncherConfiguration.BINDING_COUNT;
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
                preferences.getBoolean("showStatusBar", true),
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
                .putBoolean("showStatusBar", configuration.showStatusBar)
                .putInt("homeKeyBehavior", configuration.homeKeyBehavior)
                .putInt("swipeLeftToRightAction", configuration.swipeLeftToRightAction)
                .putInt("swipeRightToLeftAction", configuration.swipeRightToLeftAction);
        for (int index = 0; index < configuration.bindings.length; index++) {
            editor.putInt("binding" + index, configuration.bindings[index]);
        }
        editor.apply();
    }
}
