package com.t9launcher.model;

public final class DrawerGridNavigator {
    private DrawerGridNavigator() {
    }

    public static int moveLeft(int selection, int itemCount) {
        if (itemCount <= 0) return 0;
        int current = clampSelection(selection, itemCount);
        return (current - 1 + itemCount) % itemCount;
    }

    public static int moveRight(int selection, int itemCount) {
        if (itemCount <= 0) return 0;
        int current = clampSelection(selection, itemCount);
        return (current + 1) % itemCount;
    }

    public static int moveUp(int selection, int itemCount, int columns) {
        if (itemCount <= 0) return 0;
        int current = clampSelection(selection, itemCount);
        if (current >= columns) return current - columns;
        int lastRowStart = (itemCount - 1) / columns * columns;
        return Math.min(lastRowStart + current, itemCount - 1);
    }

    public static int moveDown(int selection, int itemCount, int columns) {
        if (itemCount <= 0) return 0;
        int current = clampSelection(selection, itemCount);
        return current + columns < itemCount ? current + columns : current % columns;
    }

    public static int visibleOffset(int selection, int itemCount, int columns,
                                    int rows, int currentOffset) {
        int totalRows = Math.max(1, (itemCount + columns - 1) / columns);
        int maximumFirstRow = Math.max(0, totalRows - rows);
        int firstRow = Math.max(0, Math.min(maximumFirstRow, currentOffset / columns));
        int selectedRow = Math.max(0, selection) / columns;
        if (selectedRow < firstRow) firstRow = selectedRow;
        else if (selectedRow >= firstRow + rows) firstRow = selectedRow - rows + 1;
        return Math.max(0, Math.min(maximumFirstRow, firstRow)) * columns;
    }

    private static int clampSelection(int selection, int itemCount) {
        return Math.max(0, Math.min(itemCount - 1, selection));
    }
}
