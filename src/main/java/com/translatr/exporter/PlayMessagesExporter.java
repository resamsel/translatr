package com.translatr.exporter;

import com.translatr.model.Locale;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlayMessagesExporter extends PropertiesExporter {

    private static final String DEFAULT_NAME = "messages";
    private static final String FORMAT       = "messages.%s";

    @Override
    public String getFilename(Locale locale) {
        return DEFAULT.equals(locale.name) ? DEFAULT_NAME
                : String.format(FORMAT, locale.name);
    }
}
