package com.t9launcher.search;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AppNameMatcherTest {
    @Test
    public void matches_ignoresVietnameseMarksInEitherDirection() {
        assertTrue(AppNameMatcher.matches("Điện Thoại", "dien thoai"));
        assertTrue(AppNameMatcher.matches("Dien Thoai", "điện thoại"));
    }

    @Test
    public void matches_acceptsSmallTyposAndAdjacentTranspositions() {
        assertTrue(AppNameMatcher.matches("Telegram", "telegarm"));
        assertTrue(AppNameMatcher.matches("Chrome", "chorme"));
        assertTrue(AppNameMatcher.matches("Messenger", "mesenger"));
        assertTrue(AppNameMatcher.matches("Cửa hàng Play", "cua hang ply"));
    }

    @Test
    public void matches_doesNotFuzzyMatchVeryShortQueries() {
        assertFalse(AppNameMatcher.matches("Zalo", "zz"));
        assertFalse(AppNameMatcher.matches("Chrome", "cr"));
    }

    @Test
    public void matches_rejectsUnrelatedNames() {
        assertFalse(AppNameMatcher.matches("YouTube", "telegram"));
        assertFalse(AppNameMatcher.matches("Sound Recorder", "chorme"));
    }

    @Test
    public void score_prioritizesExactThenPrefixThenSubstringThenFuzzy() {
        int exact = AppNameMatcher.score("Chrome", "chrome");
        int prefix = AppNameMatcher.score("Chrome Beta", "chrome");
        int substring = AppNameMatcher.score("Google Chrome", "chrome");
        int fuzzy = AppNameMatcher.score("Chrome", "chorme");

        assertTrue(exact > prefix);
        assertTrue(prefix > substring);
        assertTrue(substring > fuzzy);
        assertEquals(0, AppNameMatcher.score("Chrome", ""));
    }
}
