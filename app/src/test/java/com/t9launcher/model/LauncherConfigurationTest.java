package com.t9launcher.model;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public final class LauncherConfigurationTest {
    @Test
    public void constructor_clampsInvalidDisplaySettings() {
        LauncherConfiguration configuration = new LauncherConfiguration(
                99, -3, 100, true, 99,
                LauncherConfiguration.ACTION_CONTACTS,
                LauncherConfiguration.ACTION_MESSAGING,
                null);

        assertEquals(9, configuration.homeCount);
        assertEquals(0, configuration.wallpaperIndex);
        assertEquals(36, configuration.fontSizeSp);
        assertEquals(LauncherConfiguration.HOME_KEYS_QUICK_ACTION,
                configuration.homeKeyBehavior);
    }

    @Test
    public void constructor_copyAndPadBindings() {
        LauncherConfiguration configuration = new LauncherConfiguration(
                4, 0, 14, true, LauncherConfiguration.HOME_KEYS_DIALER,
                LauncherConfiguration.ACTION_CONTACTS,
                LauncherConfiguration.ACTION_MESSAGING,
                new int[]{3, 4});

        assertArrayEquals(new int[]{3, 4, -1, -1, -1, -1, -1, -1, -1},
                configuration.bindings);
    }
}
