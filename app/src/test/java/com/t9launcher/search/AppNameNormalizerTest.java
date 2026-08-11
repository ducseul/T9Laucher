package com.t9launcher.search;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class AppNameNormalizerTest {
    @Test
    public void normalize_removesVietnameseMarksAndNormalizesCase() {
        assertEquals("dien thoai", AppNameNormalizer.normalize("Điện Thoại"));
    }

    @Test
    public void normalize_handlesNull() {
        assertEquals("", AppNameNormalizer.normalize(null));
    }
}
