package utils;

import java.util.StringTokenizer;

import org.jsoup.Jsoup;

import models.Message;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
public class MessageUtils {
  public static int wordCount(String s) {
    if (s == null)
      return 0;

    return new StringTokenizer(Jsoup.parse(s).text(), " \t\n\r\f,.:;?![]\"—…").countTokens();
  }

  public static int wordCount(Message m) {
    if (m == null)
      return 0;

    return wordCount(m.value);
  }
}
