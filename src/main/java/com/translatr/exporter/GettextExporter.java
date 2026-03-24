package com.translatr.exporter;

import com.translatr.model.Locale;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;

@ApplicationScoped
public class GettextExporter extends AbstractExporter {

    @Override
    public byte[] apply(Locale locale) {
        if (locale == null || locale.messages == null) return new byte[0];

        var sb = new StringBuilder();
        locale.messages.stream()
                .filter(m -> m.key != null && m.value != null)
                .sorted(Comparator.comparing(m -> m.key.name))
                .forEach(m -> sb
                        .append("msgid \"").append(escape(m.key.name)).append("\"\n")
                        .append("msgstr \"").append(escape(m.value)).append("\"\n\n"));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String s) {
        return s.replace("\n", "\\n\"\n\"");
    }

    @Override
    public String getFilename(Locale locale) { return "messages.po"; }

    @Override
    public String getContentType() { return "text/plain"; }
}
