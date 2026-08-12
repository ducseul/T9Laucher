package com.t9launcher.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public final class HomeAppBindingsTest {
    private static final String APP_A = "com.example.a/.MainActivity";
    private static final String APP_B = "com.example.b/.MainActivity";
    private static final String APP_C = "com.example.c/.MainActivity";

    @Test
    public void migratedBinding_resolvesSameAppAfterInstalledAppsReorder() {
        String[] bindings = HomeAppBindings.migrateLegacy(
                new int[]{1}, Arrays.asList(APP_A, APP_B, APP_C));

        assertEquals(APP_B, bindings[0]);
        assertEquals(0, HomeAppBindings.resolveIndex(
                bindings[0], Arrays.asList(APP_B, APP_C, APP_A)));
    }

    @Test
    public void binding_staysUnresolvedWhileAppIsUninstalled() {
        assertEquals(-1, HomeAppBindings.resolveIndex(
                APP_B, Arrays.asList(APP_A, APP_C)));
    }

    @Test
    public void binding_resolvesAgainAfterAppIsReinstalled() {
        assertEquals(2, HomeAppBindings.resolveIndex(
                APP_B, Arrays.asList(APP_C, APP_A, APP_B)));
    }

    @Test
    public void migration_marksMissingLegacyAppAsUnassigned() {
        String[] bindings = HomeAppBindings.migrateLegacy(
                new int[]{5}, Collections.singletonList(APP_A));

        assertEquals(HomeAppBindings.UNASSIGNED, bindings[0]);
    }
}
