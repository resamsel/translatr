package com.translatr.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Email address utilities.
 */
public final class EmailUtils {

    private EmailUtils() {}

    /**
     * Hashes an email address (lower-cased, trimmed) for use as a Gravatar identifier.
     * Returns null if the email is null.
     */
    public static String hashEmail(String email) {
        if (email == null) return null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // MD5 is a standard JDK algorithm; this can't happen.
            throw new IllegalStateException(e);
        }
    }

    /**
     * Masks an email address by replacing characters before the @ sign with '*'.
     * Keeps the first and last character if there are more than 3 characters before @.
     */
    public static String maskEmail(String email) {
        int atPos = email.indexOf('@');

        if (atPos > -1) {
            if (atPos > 3) {
                // More than 3 chars before @: show first and last, mask the rest
                return email.charAt(0) + "*".repeat(atPos - 2) + email.substring(atPos - 1);
            }
            // 3 or fewer chars before @: mask all of them
            return "*".repeat(atPos) + email.substring(atPos);
        }

        return email;
    }
}

