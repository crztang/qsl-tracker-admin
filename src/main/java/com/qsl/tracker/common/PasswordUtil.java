package com.qsl.tracker.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return encodedPassword != null
                && encodedPassword.startsWith("$2")
                && ENCODER.matches(rawPassword, encodedPassword);
    }

    public static boolean isLegacyHash(String encodedPassword) {
        return encodedPassword != null && encodedPassword.matches("(?i)^[0-9a-f]{32}$");
    }
}
