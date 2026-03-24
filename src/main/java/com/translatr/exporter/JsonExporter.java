package com.translatr.exporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.translatr.model.Locale;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class JsonExporter extends AbstractExporter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    @Override
    public byte[] apply(Locale locale) {
        if (locale == null || locale.messages == null) return new byte[0];

        Map<String, String> messages = locale.messages.stream()
                .filter(m -> m.key != null && m.value != null)
                .sorted(Comparator.comparing(m -> m.key.name))
                .collect(Collectors.toMap(m -> m.key.name, m -> m.value,
                        (a, b) -> a, LinkedHashMap::new));
        try {
            return MAPPER.writeValueAsBytes(messages);
        } catch (JsonProcessingException e) {
            return new byte[0];
        }
    }

    @Override
    public String getFilename(Locale locale) { return locale.name + ".json"; }

    @Override
    public String getContentType() { return "application/json"; }
}
