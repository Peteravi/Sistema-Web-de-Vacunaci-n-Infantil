package com.vacukids.utils;

import java.util.Locale;

public final class TextUtils {

    private TextUtils() {
    }

    /**
     * Clave normalizada para comparar nombres de vacunas: - trim - colapsa
     * espacios múltiples en uno - toLowerCase (coherente con colación
     * case-insensitive) NO elimina diacríticos para NO confundir 'ñ' con 'n'.
     */
    public static String vacunaKey(String nombre) {
        if (nombre == null) {
            return null;
        }
        String t = nombre.trim().toLowerCase(Locale.ROOT);
        // colapsar espacios (tabs, múltiples espacios, etc.)
        t = t.replaceAll("\\s+", " ");
        return t;
    }
}
