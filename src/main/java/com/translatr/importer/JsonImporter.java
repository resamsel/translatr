package com.translatr.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.translatr.model.Locale;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.util.Properties;

@ApplicationScoped
public class JsonImporter extends AbstractImporter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected Properties parse(InputStream stream, Locale locale) throws Exception {
        JsonNode root = MAPPER.readTree(stream);
        Properties p  = new Properties();
        if (root.isObject()) {
            root.fields().forEachRemaining(e -> p.setProperty(e.getKey(), e.getValue().asText()));
        }
        return p;
    }
}
