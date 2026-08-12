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
        assertEquals(LauncherConfiguration.DRAWER_LAYOUT_LIST, configuration.drawerLayout);
        assertEquals(4, configuration.drawerGridColumns);
        assertEquals(5, configuration.drawerGridRows);
        assertEquals(40, configuration.drawerGridIconSizeDp);
        assertEquals(8, configuration.drawerGridIconCornerRadiusDp);
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

    @Test
    public void constructor_normalizesDrawerGridSettings() {
        LauncherConfiguration configuration = new LauncherConfiguration(
                4, 0, 14, true, 99, 100, -4,
                LauncherConfiguration.HOME_KEYS_QUICK_ACTION,
                LauncherConfiguration.ACTION_CONTACTS,
                LauncherConfiguration.ACTION_MESSAGING,
                null);

        assertEquals(LauncherConfiguration.DRAWER_LAYOUT_LIST, configuration.drawerLayout);
        assertEquals(LauncherConfiguration.MAX_DRAWER_GRID_COLUMNS,
                configuration.drawerGridColumns);
        assertEquals(LauncherConfiguration.MIN_DRAWER_GRID_ROWS,
                configuration.drawerGridRows);
    }

    @Test
    public void constructor_keepsValidDrawerGridSettings() {
        LauncherConfiguration configuration = new LauncherConfiguration(
                4, 0, 14, true, LauncherConfiguration.DRAWER_LAYOUT_GRID, 3, 6,
                48, 12,
                LauncherConfiguration.HOME_KEYS_QUICK_ACTION,
                LauncherConfiguration.ACTION_CONTACTS,
                LauncherConfiguration.ACTION_MESSAGING,
                null);

        assertEquals(LauncherConfiguration.DRAWER_LAYOUT_GRID, configuration.drawerLayout);
        assertEquals(3, configuration.drawerGridColumns);
        assertEquals(6, configuration.drawerGridRows);
        assertEquals(48, configuration.drawerGridIconSizeDp);
        assertEquals(12, configuration.drawerGridIconCornerRadiusDp);
    }

    @Test
    public void constructor_clampsDrawerGridIconSettings() {
        LauncherConfiguration configuration = new LauncherConfiguration(
                4, 0, 14, true, LauncherConfiguration.DRAWER_LAYOUT_GRID, 4, 5,
                999, -10,
                LauncherConfiguration.HOME_KEYS_QUICK_ACTION,
                LauncherConfiguration.ACTION_CONTACTS,
                LauncherConfiguration.ACTION_MESSAGING,
                null);

        assertEquals(LauncherConfiguration.MAX_DRAWER_GRID_ICON_SIZE_DP,
                configuration.drawerGridIconSizeDp);
        assertEquals(LauncherConfiguration.MIN_DRAWER_GRID_ICON_CORNER_RADIUS_DP,
                configuration.drawerGridIconCornerRadiusDp);
    }
}
