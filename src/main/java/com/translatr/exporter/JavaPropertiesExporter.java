package com.translatr.exporter;

import com.translatr.model.Locale;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JavaPropertiesExporter extends PropertiesExporter {

    private static final String DEFAULT_NAME = "messages.properties";
    private static final String FORMAT       = "messages_%s.properties";

    @Override
    public String getFilename(Locale locale) {
        return DEFAULT.equals(locale.name) ? DEFAULT_NAME
                : String.format(FORMAT, locale.name);
    }
}
