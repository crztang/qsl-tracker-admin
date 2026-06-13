package com.qsl.tracker.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class ShareTokenUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private ShareTokenUtil() {
    }

    public static String newToken() {
        return newToken(64);
    }

    public static String newToken(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        }
        return builder.toString();
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(Character.forDigit((b >> 4) & 0xF, 16));
                builder.append(Character.forDigit(b & 0xF, 16));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
