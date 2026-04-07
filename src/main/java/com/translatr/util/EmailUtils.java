package com.translatr.util;

/**
 * Email address utilities.
 */
public final class EmailUtils {

    private EmailUtils() {}

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

