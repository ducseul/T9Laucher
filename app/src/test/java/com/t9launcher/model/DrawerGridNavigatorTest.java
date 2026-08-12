package com.t9launcher.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class DrawerGridNavigatorTest {
    @Test
    public void horizontalMovement_wrapsAcrossWholeGrid() {
        assertEquals(0, DrawerGridNavigator.moveRight(20, 21));
        assertEquals(20, DrawerGridNavigator.moveLeft(0, 21));
    }

    @Test
    public void verticalMovement_wrapsAndHandlesIncompleteLastRow() {
        assertEquals(4, DrawerGridNavigator.moveUp(3, 5, 4));
        assertEquals(0, DrawerGridNavigator.moveDown(4, 5, 4));
        assertEquals(7, DrawerGridNavigator.moveDown(3, 8, 4));
    }

    @Test
    public void visibleOffset_scrollsByCompleteRows() {
        assertEquals(4, DrawerGridNavigator.visibleOffset(20, 30, 4, 5, 0));
        assertEquals(12, DrawerGridNavigator.visibleOffset(29, 30, 4, 5, 4));
        assertEquals(0, DrawerGridNavigator.visibleOffset(2, 30, 4, 5, 12));
    }
}
