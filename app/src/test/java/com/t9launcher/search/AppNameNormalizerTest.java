package com.t9launcher.search;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class AppNameNormalizerTest {
    @Test
    public void normalize_removesVietnameseMarksAndNormalizesCase() {
        assertEquals("dien thoai", AppNameNormalizer.normalize("Điện Thoại"));
    }

    @Test
    public void normalize_removesAllVietnameseVowelMarks() {
        assertEquals(
                "aaaaa aaaaaa aaaaaa eeeee eeeeee iiiii ooooo oooooo oooooo "
                        + "uuuuu uuuuuu yyyyy",
                AppNameNormalizer.normalize(
                        "ÀÁẢÃẠ ĂẰẮẲẴẶ ÂẦẤẨẪẬ ÈÉẺẼẸ ÊỀẾỂỄỆ ÌÍỈĨỊ "
                                + "ÒÓỎÕỌ ÔỒỐỔỖỘ ƠỜỚỞỠỢ ÙÚỦŨỤ ƯỪỨỬỮỰ ỲÝỶỸỴ"));
    }

    @Test
    public void normalize_transliteratesLatinLettersThatDoNotDecompose() {
        assertEquals("lodz smorrebrod oeuvre strasse thorn",
                AppNameNormalizer.normalize("Łódź · smørrebrød · œuvre · Straße · Þorn"));
    }

    @Test
    public void normalize_collapsesPunctuationAndWhitespace() {
        assertEquals("coc coc browser", AppNameNormalizer.normalize("  Cốc-Cốc   Browser™ "));
    }

    @Test
    public void normalize_handlesNull() {
        assertEquals("", AppNameNormalizer.normalize(null));
    }
}
