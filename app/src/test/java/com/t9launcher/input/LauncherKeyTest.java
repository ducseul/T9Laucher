package com.t9launcher.input;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class LauncherKeyTest {
    @Test
    public void digit_returnsTypedDigitKey() {
        LauncherKey key = LauncherKey.digit(7);

        assertTrue(key.isDigit());
        assertEquals(7, key.digit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void digit_rejectsOutOfRangeValue() {
        LauncherKey.digit(10);
    }

    @Test
    public void directionalAndLongPressCapabilitiesAreExplicit() {
        assertTrue(LauncherKey.LEFT.isDirectional());
        assertFalse(LauncherKey.OK.isDirectional());
        assertTrue(LauncherKey.POUND.supportsLongPress());
        assertFalse(LauncherKey.DIGIT_1.supportsLongPress());
    }
}
