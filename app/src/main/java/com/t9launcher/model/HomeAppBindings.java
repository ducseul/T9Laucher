package com.t9launcher.model;

import java.util.Arrays;
import java.util.List;

public final class HomeAppBindings {
    public static final String UNASSIGNED = "";

    private HomeAppBindings() {
    }

    public static String componentId(String packageName, String activityName) {
        if (packageName == null || activityName == null) return UNASSIGNED;
        return packageName + "/" + activityName;
    }

    public static String[] normalize(String[] source) {
        String[] result = new String[LauncherConfiguration.BINDING_COUNT];
        Arrays.fill(result, UNASSIGNED);
        if (source == null) return result;
        for (int index = 0; index < Math.min(source.length, result.length); index++) {
            result[index] = source[index] == null ? UNASSIGNED : source[index];
        }
        return result;
    }

    public static String[] migrateLegacy(int[] legacyIndexes,
                                         List<String> installedComponents) {
        String[] result = normalize(null);
        if (legacyIndexes == null || installedComponents == null) return result;
        for (int slot = 0; slot < Math.min(legacyIndexes.length, result.length); slot++) {
            int appIndex = legacyIndexes[slot];
            if (appIndex >= 0 && appIndex < installedComponents.size()) {
                String component = installedComponents.get(appIndex);
                result[slot] = component == null ? UNASSIGNED : component;
            }
        }
        return result;
    }

    public static int resolveIndex(String component, List<String> installedComponents) {
        if (component == null || component.isEmpty() || installedComponents == null) return -1;
        for (int index = 0; index < installedComponents.size(); index++) {
            if (component.equals(installedComponents.get(index))) return index;
        }
        return -1;
    }
}
