package com.qsl.tracker.common;

import java.security.SecureRandom;
import java.time.LocalDate;

public final class TrackingNoUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private TrackingNoUtil() {
    }

    public static String newTrackingNo() {
        StringBuilder builder = new StringBuilder("QSL");
        builder.append(LocalDate.now().toString().replace("-", ""));
        for (int i = 0; i < 8; i++) {
            builder.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        }
        return builder.toString();
    }

    public static String newToken() {
        StringBuilder builder = new StringBuilder(64);
        for (int i = 0; i < 64; i++) {
            builder.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        }
        return builder.toString();
    }
}
