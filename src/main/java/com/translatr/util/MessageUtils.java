package com.translatr.util;

import com.translatr.model.Message;
import org.jsoup.Jsoup;

import java.util.StringTokenizer;

/**
 * Word-count utilities for message values.
 */
public final class MessageUtils {

    private MessageUtils() {}

    /** Returns the word count of a string, stripping HTML tags first. */
    public static int wordCount(String s) {
        if (s == null) {
            return 0;
        }
        return new StringTokenizer(Jsoup.parse(s).text(), " \t\n\r\f,.:;?![]\"—…").countTokens();
    }

    /** Returns the word count of a {@link Message}'s value. */
    public static int wordCount(Message m) {
        if (m == null) {
            return 0;
        }
        return wordCount(m.value);
    }
}

