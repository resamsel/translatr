package utils;

import org.apache.commons.lang3.StringUtils;

public class EmailUtils {
  /**
   * Masks the given email address by replacing characters before the @ sign with asterisk (*). Keeps first and last
   * character if more than 3 before @ sign.
   *
   * @param email the given email to be masked
   * @return the masked email address
   */
  public static String maskEmail(String email) {
    int atPos = email.indexOf("@");

    if (atPos > -1) {
      // @ exists in string

      if (atPos > 3) {
        // There are more than 3 characters before the @ sign, show first and last character
        return email.substring(0, 1) + StringUtils.repeat('*', atPos - 2) + email.substring(atPos - 1);
      }

      // Too few characters to mask, hide all characters before the @ sign
      return StringUtils.repeat('*', atPos) + email.substring(atPos);
    }

    // No @ sign, return input string
    return email;
  }

  /**
   * Hashes the given email address to be used with Gravatar.
   *
   * @param email the given email to be hashed
   * @return the hashed email address
   */
  public static String hashEmail(String email) {
    if (email == null) {
      return email;
    }

    return MD5Util.md5Hex(email.toLowerCase());
  }
}
