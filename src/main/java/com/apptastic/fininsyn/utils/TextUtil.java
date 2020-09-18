package com.apptastic.fininsyn.utils;

import java.util.Arrays;

public class TextUtil {

    private TextUtil() {

    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.isBlank();
    }

    public static boolean containsAny(String text, String... words) {
        return Arrays.stream(words)
                     .parallel()
                     .anyMatch(text::contains);
    }

    public static boolean containsAll(String text, String... words) {
        return Arrays.stream(words)
                     .parallel()
                     .allMatch(text::contains);
    }

    public static boolean containsAtLeast(String text, int count, String... words) {
        return count <= words.length &&
                Arrays.stream(words)
                      .parallel()
                      .filter(text::contains)
                      .count() >= count;
    }

    public static boolean endsWith(String text, String... suffix) {
        return Arrays.stream(suffix)
                     .parallel()
                     .anyMatch(text::endsWith);
    }
}
