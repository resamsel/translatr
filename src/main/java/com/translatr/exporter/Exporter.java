package com.translatr.exporter;

import com.translatr.model.Locale;

/**
 * Converts a Locale's messages to a byte array.
 */
public interface Exporter {
    byte[] apply(Locale locale);
    String getFilename(Locale locale);
    String getContentType();
}
