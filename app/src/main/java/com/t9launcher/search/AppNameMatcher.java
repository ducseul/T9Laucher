package com.t9launcher.search;

public final class AppNameMatcher {
    public static final int NO_MATCH = -1;

    private AppNameMatcher() { }

    public static int score(CharSequence appName, CharSequence query) {
        String normalizedName = AppNameNormalizer.normalize(appName);
        String normalizedQuery = AppNameNormalizer.normalize(query);
        if (normalizedQuery.isEmpty()) return 0;
        if (normalizedName.equals(normalizedQuery)) return 1000;
        if (normalizedName.startsWith(normalizedQuery)) {
            return 900 - Math.min(99, normalizedName.length() - normalizedQuery.length());
        }

        int substringIndex = normalizedName.indexOf(normalizedQuery);
        if (substringIndex >= 0) return 800 - Math.min(99, substringIndex);

        int queryLength = letterOrDigitCount(normalizedQuery);
        int maxDistance = allowedDistance(queryLength);
        if (maxDistance == 0) return NO_MATCH;

        int distance = fuzzyTokenDistance(normalizedQuery, normalizedName);
        return distance <= maxDistance ? 600 - distance * 100 : NO_MATCH;
    }

    public static boolean matches(CharSequence appName, CharSequence query) {
        return score(appName, query) != NO_MATCH;
    }

    private static int allowedDistance(int queryLength) {
        if (queryLength < 3) return 0;
        if (queryLength <= 4) return 1;
        if (queryLength <= 8) return 2;
        return Math.min(3, queryLength / 4);
    }

    private static int letterOrDigitCount(String text) {
        int count = 0;
        for (int offset = 0; offset < text.length();) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (Character.isLetterOrDigit(codePoint)) count++;
        }
        return count;
    }

    private static int fuzzyTokenDistance(String query, String appName) {
        String[] queryTokens = query.split(" ");
        String[] nameTokens = appName.split(" ");
        if (queryTokens.length > nameTokens.length) return Integer.MAX_VALUE;

        int best = Integer.MAX_VALUE;
        for (int start = 0; start <= nameTokens.length - queryTokens.length; start++) {
            StringBuilder candidate = new StringBuilder();
            for (int offset = 0; offset < queryTokens.length; offset++) {
                if (offset > 0) candidate.append(' ');
                candidate.append(nameTokens[start + offset]);
            }
            best = Math.min(best, damerauLevenshtein(query, candidate.toString()));
        }
        return best;
    }

    private static int damerauLevenshtein(String left, String right) {
        int[][] distance = new int[left.length() + 1][right.length() + 1];
        for (int row = 0; row <= left.length(); row++) distance[row][0] = row;
        for (int column = 0; column <= right.length(); column++) distance[0][column] = column;

        for (int row = 1; row <= left.length(); row++) {
            for (int column = 1; column <= right.length(); column++) {
                int substitutionCost = left.charAt(row - 1) == right.charAt(column - 1) ? 0 : 1;
                int value = Math.min(
                        Math.min(distance[row - 1][column] + 1,
                                distance[row][column - 1] + 1),
                        distance[row - 1][column - 1] + substitutionCost);
                if (row > 1 && column > 1
                        && left.charAt(row - 1) == right.charAt(column - 2)
                        && left.charAt(row - 2) == right.charAt(column - 1)) {
                    value = Math.min(value, distance[row - 2][column - 2] + 1);
                }
                distance[row][column] = value;
            }
        }
        return distance[left.length()][right.length()];
    }
}
