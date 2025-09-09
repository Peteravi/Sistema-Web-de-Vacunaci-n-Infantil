package com.vacukids.utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Arrays;

public final class PasswordUtils {

    private PasswordUtils() {}

    // ---- Política Argon2id (OWASP) ----
    private static final int ARGON2_ITERATIONS  = 3;       // 3-5
    private static final int ARGON2_MEMORY_KB   = 65536;   // 64 MB
    private static final int ARGON2_PARALLELISM = 1;       // 1-2

    // BCrypt legacy (si necesitas generarlo aún)
    private static final int BCRYPT_COST        = 12;

    private static final Argon2 ARGON2 =
            Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    /** Genera hash NUEVO (Argon2id, formato PHC). */
    public static String hash(String plain) {
        if (plain == null) throw new IllegalArgumentException("plain password is null");
        char[] pwd = plain.toCharArray();
        try {
            // Overload válido en argon2-jvm: NO acepta saltLen ni hashLen
            return ARGON2.hash(ARGON2_ITERATIONS, ARGON2_MEMORY_KB, ARGON2_PARALLELISM, pwd);
        } finally {
            Arrays.fill(pwd, '\0');
        }
    }

    /** Verifica contra Argon2id o BCrypt ($2a$, $2b$, $2y$). */
    public static boolean verify(String plain, String hash) {
        if (plain == null || hash == null) return false;
        if (isArgon2(hash))  return verifyArgon2(plain, hash);
        if (isBcrypt(hash))  return verifyBcrypt(plain, hash);
        return false;
    }

    /** Recomienda rehash si el hash es BCrypt o Argon2id con parámetros débiles. */
    public static boolean needsRehash(String hash) {
        if (hash == null) return true;
        if (isBcrypt(hash)) return true;          // migrar a Argon2id
        if (isArgon2(hash)) return argon2NeedsRehash(hash);
        return true;
    }

    // -------- Helpers --------
    private static boolean isArgon2(String h) {
        return h.startsWith("$argon2");
    }

    private static boolean isBcrypt(String h) {
        return h.startsWith("$2a$") || h.startsWith("$2b$") || h.startsWith("$2y$");
    }

    private static boolean verifyArgon2(String plain, String phc) {
        char[] pwd = plain.toCharArray();
        try {
            return ARGON2.verify(phc, pwd);
        } finally {
            Arrays.fill(pwd, '\0');
        }
    }

    private static boolean verifyBcrypt(String plain, String hash) {
        // jBCrypt clásico no entiende $2y/$2b → normalizamos a $2a
        String normalized = (hash.startsWith("$2y$") || hash.startsWith("$2b$"))
                ? "$2a$" + hash.substring(4)
                : hash;
        try {
            return BCrypt.checkpw(plain, normalized);
        } catch (Exception e) {
            return false;
        }
    }

    // ¿Parámetros del PHC de Argon2 están por debajo de la política?
    private static boolean argon2NeedsRehash(String phc) {
        // $argon2id$v=19$m=65536,t=3,p=1$<salt>$<hash>
        try {
            String[] parts = phc.split("\\$");
            if (parts.length < 6) return true;
            String params = parts[3]; // m=...,t=...,p=...
            int m = extractInt(params, "m=");
            int t = extractInt(params, "t=");
            int p = extractInt(params, "p=");
            return t < ARGON2_ITERATIONS || m < ARGON2_MEMORY_KB || p < ARGON2_PARALLELISM;
        } catch (Exception e) {
            return true;
        }
    }

    private static int extractInt(String s, String key) {
        int i = s.indexOf(key);
        if (i < 0) return -1;
        int j = s.indexOf(',', i);
        String num = (j < 0) ? s.substring(i + key.length()) : s.substring(i + key.length(), j);
        return Integer.parseInt(num.trim());
    }

    /** Sólo si aún necesitas generar BCrypt legacy. */
    public static String hashBcryptLegacy(String plain) {
        if (plain == null) throw new IllegalArgumentException("plain password is null");
        return BCrypt.hashpw(plain, BCrypt.gensalt(BCRYPT_COST));
    }
}
