package com.translatr.exporter;

import com.translatr.model.Locale;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

public abstract class PropertiesExporter extends AbstractExporter {

    protected static final String DEFAULT = "default";

    @Override
    public byte[] apply(Locale locale) {
        if (locale == null || locale.messages == null) return new byte[0];

        var sb = new StringBuilder();
        locale.messages.stream()
                .filter(m -> m.key != null && m.value != null)
                .sorted(Comparator.comparing(m -> m.key.name))
                .forEach(m -> sb.append(m.key.name).append("=")
                                .append(escape(m.value)).append("\n"));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        return value.replace("\n", "\\n").replace("\r", "");
    }
}
