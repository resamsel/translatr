package utils;

/**
 * @author resamsel
 * @version 24 Jan 2017
 */
public class NumberUtils {
  public static Integer parseInt(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
