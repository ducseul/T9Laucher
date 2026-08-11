package com.t9launcher.search;

import java.text.Normalizer;
import java.util.Locale;

public final class AppNameNormalizer {
    private AppNameNormalizer() { }

    public static String normalize(CharSequence text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.US);
    }
}
