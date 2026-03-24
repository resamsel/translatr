package com.translatr.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.translatr.model.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for JsonImporter's parse logic (without CDI / DB).
 */
class JsonImporterTest {

    @Test
    void testParse_producesExpectedProperties() throws Exception {
        JsonImporter importer = new JsonImporter();

        String json = "{\"greeting\": \"Hello\", \"farewell\": \"Goodbye\"}";
        Method parse = AbstractImporter.class.getDeclaredMethod("parse",
                java.io.InputStream.class, Locale.class);
        parse.setAccessible(true);

        Properties props = (Properties) parse.invoke(importer,
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), null);

        assertThat(props).containsEntry("greeting", "Hello");
        assertThat(props).containsEntry("farewell", "Goodbye");
    }
}
