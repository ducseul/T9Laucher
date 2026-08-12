package com.t9launcher.model;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertEquals(LauncherConfiguration.DEFAULT_CLOCK_FONT_SIZE_SP,
                configuration.clockFontSizeSp);
        assertEquals(LauncherConfiguration.CLOCK_STYLE_CLASSIC,
                configuration.clockStyle);
        assertEquals(LauncherConfiguration.CLOCK_ALIGNMENT_CENTER,
                configuration.clockAlignment);
        assertEquals(16, configuration.dateFontSizeSp());
        assertEquals(LauncherConfiguration.HOME_KEYS_QUICK_ACTION,
                configuration.homeKeyBehavior);
        assertEquals(LauncherConfiguration.DRAWER_LAYOUT_LIST, configuration.drawerLayout);
        assertEquals(4, configuration.drawerGridColumns);
        assertEquals(5, configuration.drawerGridRows);
        assertEquals(40, configuration.drawerGridIconSizeDp);
        assertEquals(8, configuration.drawerGridIconCornerRadiusDp);
        assertTrue(configuration.animationsEnabled);
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

    @Test
    public void constructor_keepsExplicitAnimationSetting() {
        LauncherConfiguration configuration = new LauncherConfiguration(
                4, 0, 14, true, false,
                LauncherConfiguration.DRAWER_LAYOUT_LIST, 4, 5, 40, 8,
                LauncherConfiguration.HOME_KEYS_QUICK_ACTION,
                LauncherConfiguration.ACTION_CONTACTS,
                LauncherConfiguration.ACTION_MESSAGING,
                null);

        assertFalse(configuration.animationsEnabled);
    }

    @Test
    public void constructor_clampsClockFontAndCalculatesDateFont() {
        LauncherConfiguration minimum = new LauncherConfiguration(
                4, 0, 14, -1, true, true,
                LauncherConfiguration.DRAWER_LAYOUT_LIST, 4, 5, 40, 8,
                LauncherConfiguration.HOME_KEYS_QUICK_ACTION,
                LauncherConfiguration.ACTION_CONTACTS,
                LauncherConfiguration.ACTION_MESSAGING,
                null);
        LauncherConfiguration maximum = new LauncherConfiguration(
                4, 0, 14, 999, true, true,
                LauncherConfiguration.DRAWER_LAYOUT_LIST, 4, 5, 40, 8,
                LauncherConfiguration.HOME_KEYS_QUICK_ACTION,
                LauncherConfiguration.ACTION_CONTACTS,
                LauncherConfiguration.ACTION_MESSAGING,
                null);

        assertEquals(LauncherConfiguration.MIN_CLOCK_FONT_SIZE_SP,
                minimum.clockFontSizeSp);
        assertEquals(LauncherConfiguration.MIN_DATE_FONT_SIZE_SP,
                minimum.dateFontSizeSp());
        assertEquals(LauncherConfiguration.MAX_CLOCK_FONT_SIZE_SP,
                maximum.clockFontSizeSp);
        assertEquals(24, maximum.dateFontSizeSp());
        assertEquals(LauncherConfiguration.MIN_DATE_FONT_SIZE_SP,
                LauncherConfiguration.dateFontSizeSp(10));
    }

    @Test
    public void constructor_normalizesClockStyle() {
        LauncherConfiguration vertical = configurationWithClockStyle(
                LauncherConfiguration.CLOCK_STYLE_VERTICAL);
        LauncherConfiguration eightSegment = configurationWithClockStyle(
                LauncherConfiguration.CLOCK_STYLE_EIGHT_SEGMENT);
        LauncherConfiguration invalid = configurationWithClockStyle(99);

        assertEquals(LauncherConfiguration.CLOCK_STYLE_VERTICAL, vertical.clockStyle);
        assertEquals(LauncherConfiguration.CLOCK_STYLE_EIGHT_SEGMENT,
                eightSegment.clockStyle);
        assertEquals(LauncherConfiguration.CLOCK_STYLE_CLASSIC, invalid.clockStyle);
    }

    @Test
    public void constructor_defaultsAndNormalizesClockAlignmentToCenter() {
        LauncherConfiguration legacy = configurationWithClockStyle(
                LauncherConfiguration.CLOCK_STYLE_VERTICAL);
        LauncherConfiguration left = configurationWithClockAlignment(
                LauncherConfiguration.CLOCK_ALIGNMENT_LEFT);
        LauncherConfiguration right = configurationWithClockAlignment(
                LauncherConfiguration.CLOCK_ALIGNMENT_RIGHT);
        LauncherConfiguration invalid = configurationWithClockAlignment(99);

        assertEquals(LauncherConfiguration.CLOCK_ALIGNMENT_CENTER,
                legacy.clockAlignment);
        assertEquals(LauncherConfiguration.CLOCK_ALIGNMENT_LEFT, left.clockAlignment);
        assertEquals(LauncherConfiguration.CLOCK_ALIGNMENT_RIGHT, right.clockAlignment);
        assertEquals(LauncherConfiguration.CLOCK_ALIGNMENT_CENTER,
                invalid.clockAlignment);
    }

    private static LauncherConfiguration configurationWithClockStyle(int clockStyle) {
        return new LauncherConfiguration(
                4, 0, 14, LauncherConfiguration.DEFAULT_CLOCK_FONT_SIZE_SP,
                clockStyle, true, true,
                LauncherConfiguration.DRAWER_LAYOUT_LIST, 4, 5, 40, 8,
                LauncherConfiguration.HOME_KEYS_QUICK_ACTION,
                LauncherConfiguration.ACTION_CONTACTS,
                LauncherConfiguration.ACTION_MESSAGING,
                null);
    }

    private static LauncherConfiguration configurationWithClockAlignment(int clockAlignment) {
        return new LauncherConfiguration(
                4, 0, 14, LauncherConfiguration.DEFAULT_CLOCK_FONT_SIZE_SP,
                LauncherConfiguration.CLOCK_STYLE_CLASSIC, clockAlignment,
                true, true, LauncherConfiguration.DRAWER_LAYOUT_LIST,
                4, 5, 40, 8,
                LauncherConfiguration.HOME_KEYS_QUICK_ACTION,
                LauncherConfiguration.ACTION_CONTACTS,
                LauncherConfiguration.ACTION_MESSAGING,
                null);
    }
}
