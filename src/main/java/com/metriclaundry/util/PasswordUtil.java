package com.metriclaundry.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing with PBKDF2-HMAC-SHA256 (built into the JDK, no extra library).
 * Each user gets a random 16-byte salt. Comparison is constant-time.
 */
public final class PasswordUtil {
    private static final int ITERATIONS = 65_536;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { }

    public static String newSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String saltB64) {
        return hash(password, saltB64, ITERATIONS);
    }

    private static String hash(String password, String saltB64, int iterations) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltB64);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    public static boolean verify(String password, String saltB64, String expectedHashB64) {
        if (password == null || expectedHashB64 == null) return false;
        try {
            int iterations = ITERATIONS;
            // Preserve passwords imported from the older email-login database.
            if (expectedHashB64.startsWith("pbkdf2_sha256$")) {
                String[] parts = expectedHashB64.split("\\$", -1);
                if (parts.length != 4) return false;
                iterations = Integer.parseInt(parts[1]);
                if (iterations < 1 || iterations > 1_000_000) return false;
                saltB64 = parts[2];
                expectedHashB64 = parts[3];
            }
            if (saltB64 == null || saltB64.isEmpty()) return false;
            byte[] actual = Base64.getDecoder().decode(hash(password, saltB64, iterations));
            byte[] expected = Base64.getDecoder().decode(expectedHashB64);
            return MessageDigest.isEqual(actual, expected);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return false;
        }
    }
}
