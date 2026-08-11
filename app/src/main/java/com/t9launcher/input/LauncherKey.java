package com.t9launcher.input;

public enum LauncherKey {
    DIGIT_0(0),
    DIGIT_1(1),
    DIGIT_2(2),
    DIGIT_3(3),
    DIGIT_4(4),
    DIGIT_5(5),
    DIGIT_6(6),
    DIGIT_7(7),
    DIGIT_8(8),
    DIGIT_9(9),
    STAR,
    POUND,
    UP,
    DOWN,
    LEFT,
    RIGHT,
    OK,
    BACK,
    CORNER_1,
    CORNER_2,
    CORNER_3,
    CORNER_4;

    private final int digit;

    LauncherKey() {
        this(-1);
    }

    LauncherKey(int digit) {
        this.digit = digit;
    }

    public boolean isDigit() {
        return digit >= 0;
    }

    public int digit() {
        if (!isDigit()) throw new IllegalStateException(name() + " is not a digit key");
        return digit;
    }

    public boolean isDirectional() {
        return this == UP || this == DOWN || this == LEFT || this == RIGHT;
    }

    public boolean supportsLongPress() {
        return this == POUND;
    }

    public long longPressDelayMs() {
        return this == POUND ? 5000L : 500L;
    }

    public static LauncherKey digit(int digit) {
        if (digit < 0 || digit > 9) throw new IllegalArgumentException("digit must be 0..9");
        return values()[digit];
    }
}
