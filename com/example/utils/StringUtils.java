package com.example.utils;

/**
 * Utility class for common string operations.
 */
public class StringUtils {

    /**
     * Reverses a given string.
     * @param str The string to reverse.
     * @return The reversed string, or null if input is null.
     */
    public static String reverse(String str) {
        if (str == null) {
            return null;
        }
        return new StringBuilder(str).reverse().toString();
    }

    /**
     * Checks if a string is null or empty.
     * @param str The string to check.
     * @return True if null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Checks if a string is null, empty, or consists only of whitespace.
     * @param str The string to check.
     * @return True if null, empty, or whitespace, false otherwise.
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
