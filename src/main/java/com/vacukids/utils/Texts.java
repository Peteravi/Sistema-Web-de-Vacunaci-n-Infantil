package com.vacukids.utils;

public final class Texts {
    private Texts() {}

    /** Equivalente a String.isBlank() (Java 11) pero compatible con Java 8 */
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    /** Devuelve null si es blank; útil para construir filtros opcionales */
    public static String nullIfBlank(String s) {
        return isBlank(s) ? null : s;
    }

    /** Devuelve "" si es null */
    public static String safe(String s) {
        return s == null ? "" : s;
    }
}
