package utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author resamsel
 * @version 2 Jul 2017
 */
public class UrlUtils {
  /**
   * URL encode the given string as param.
   * 
   * @param s
   * @return
   */
  public static String encode(String s) {
    try {
      return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      return s;
    }
  }

  /**
   * URL decode the given string as param.
   * 
   * @param s
   * @return
   */
  public static String decode(String s) {
    try {
      return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      return s;
    }
  }
}
