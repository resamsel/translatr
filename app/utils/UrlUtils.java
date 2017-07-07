package utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author resamsel
 * @version 2 Jul 2017
 */
public class UrlUtils {

  /**
   * URL encode the given string as param.
   */
  public static String encode(String s) {
    try {
      return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      return s;
    }
  }
}
