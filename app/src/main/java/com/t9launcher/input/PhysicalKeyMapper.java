package com.t9launcher.input;

import android.view.KeyEvent;

public final class PhysicalKeyMapper {
    private PhysicalKeyMapper() { }

    public static LauncherKey fromKeyCode(int code) {
        if (code >= KeyEvent.KEYCODE_0 && code <= KeyEvent.KEYCODE_9) {
            return LauncherKey.digit(code - KeyEvent.KEYCODE_0);
        }
        switch (code) {
            case KeyEvent.KEYCODE_STAR: return LauncherKey.STAR;
            case KeyEvent.KEYCODE_POUND: return LauncherKey.POUND;
            case KeyEvent.KEYCODE_DPAD_UP: return LauncherKey.UP;
            case KeyEvent.KEYCODE_DPAD_DOWN: return LauncherKey.DOWN;
            case KeyEvent.KEYCODE_DPAD_LEFT: return LauncherKey.LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT: return LauncherKey.RIGHT;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER: return LauncherKey.OK;
            case KeyEvent.KEYCODE_BACK: return LauncherKey.BACK;
            // Doov R17 Pro mtk-kpd.kl mappings.
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_SOFT_LEFT: return LauncherKey.CORNER_1;
            case KeyEvent.KEYCODE_SOFT_RIGHT: return LauncherKey.CORNER_2;
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_BUTTON_L1: return LauncherKey.CORNER_3;
            case KeyEvent.KEYCODE_ENDCALL:
            case KeyEvent.KEYCODE_BUTTON_R1: return LauncherKey.CORNER_4;
            default: return null;
        }
    }
}
