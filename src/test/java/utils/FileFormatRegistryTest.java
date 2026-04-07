package utils;

import com.translatr.exporter.ExporterFactory;
import com.translatr.importer.ImporterFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies that {@link ImporterFactory} and {@link ExporterFactory} return a registered
 * handler for every supported file-type string.
 * Replaces the Play-era FileFormatRegistryTest.
 */
@QuarkusTest
class FileFormatRegistryTest {

    @Inject ImporterFactory importerFactory;
    @Inject ExporterFactory exporterFactory;

    @ParameterizedTest
    @ValueSource(strings = {"java_properties", "play_messages", "json", "gettext"})
    void importerIsRegistered(String fileType) {
        assertThat(importerFactory.forFileType(fileType)).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"java_properties", "play_messages", "json", "gettext"})
    void exporterIsRegistered(String fileType) {
        assertThat(exporterFactory.forFileType(fileType)).isNotNull();
    }

    @Test
    void importerThrowsForUnknownFileType() {
        assertThatThrownBy(() -> importerFactory.forFileType("unknown_format"))
                .isInstanceOf(jakarta.ws.rs.BadRequestException.class);
    }

    @Test
    void exporterThrowsForUnknownFileType() {
        assertThatThrownBy(() -> exporterFactory.forFileType("unknown_format"))
                .isInstanceOf(jakarta.ws.rs.BadRequestException.class);
    }
}
