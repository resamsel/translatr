package com.translatr.exporter;

import com.translatr.model.*;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class JsonExporterTest {

    @Inject JsonExporter exporter;

    @Test
    void testApply_emptyLocale() {
        Locale locale = new Locale();
        locale.name = "en";
        locale.messages = List.of();

        byte[] result = exporter.apply(locale);
        assertThat(new String(result)).isEqualTo("{ }");
    }

    @Test
    void testApply_withMessages() {
        Locale locale  = new Locale();
        locale.name    = "en";
        Key key        = new Key(); key.name = "greeting";
        Message msg    = new Message(); msg.key = key; msg.value = "Hello";
        locale.messages = List.of(msg);

        byte[] result = exporter.apply(locale);
        String json   = new String(result);
        assertThat(json).contains("\"greeting\"");
        assertThat(json).contains("\"Hello\"");
    }

    @Test
    void testGetFilename() {
        Locale locale = new Locale(); locale.name = "de";
        assertThat(exporter.getFilename(locale)).isEqualTo("de.json");
    }
}
