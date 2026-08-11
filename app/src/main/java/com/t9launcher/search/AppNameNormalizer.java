package com.t9launcher.search;

import java.text.Normalizer;
import java.util.Locale;

public final class AppNameNormalizer {
    private AppNameNormalizer() { }

    public static String normalize(CharSequence text) {
        if (text == null) return "";
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        StringBuilder result = new StringBuilder(decomposed.length());
        boolean pendingSpace = false;
        for (int offset = 0; offset < decomposed.length();) {
            int codePoint = decomposed.codePointAt(offset);
            offset += Character.charCount(codePoint);

            int type = Character.getType(codePoint);
            if (type == Character.NON_SPACING_MARK
                    || type == Character.COMBINING_SPACING_MARK
                    || type == Character.ENCLOSING_MARK) {
                continue;
            }

            String replacement = latinReplacement(codePoint);
            if (replacement != null) {
                if (pendingSpace && result.length() > 0) result.append(' ');
                result.append(replacement);
                pendingSpace = false;
            } else if (Character.isLetterOrDigit(codePoint)) {
                if (pendingSpace && result.length() > 0) result.append(' ');
                result.appendCodePoint(Character.toLowerCase(codePoint));
                pendingSpace = false;
            } else if (result.length() > 0) {
                pendingSpace = true;
            }
        }
        return result.toString().toLowerCase(Locale.ROOT);
    }

    private static String latinReplacement(int codePoint) {
        switch (codePoint) {
            case 'Đ': case 'đ': case 'Ð': case 'ð': case 'Ɗ': case 'ɗ':
                return "d";
            case 'Ł': case 'ł':
                return "l";
            case 'Ø': case 'ø': case 'Ɵ': case 'ɵ':
                return "o";
            case 'Æ': case 'æ':
                return "ae";
            case 'Œ': case 'œ':
                return "oe";
            case 'ẞ': case 'ß':
                return "ss";
            case 'Þ': case 'þ':
                return "th";
            case 'Ħ': case 'ħ':
                return "h";
            case 'ı': case 'İ':
                return "i";
            case 'Ŋ': case 'ŋ':
                return "n";
            case 'Ŧ': case 'ŧ':
                return "t";
            case 'Ƒ': case 'ƒ':
                return "f";
            case 'ĸ':
                return "k";
            case 'ſ':
                return "s";
            default:
                return null;
        }
    }
}
