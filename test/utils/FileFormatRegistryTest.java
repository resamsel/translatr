package utils;

import assertions.CustomAssertions;
import exporters.Exporter;
import importers.Importer;
import models.FileType;
import org.junit.Test;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class FileFormatRegistryTest {
  @Test
  public void testImporterRegistration() {
    Stream.of(FileType.values()).forEach(fileFormat -> {
      // when
      Class<? extends Importer> actual = FileFormatRegistry.IMPORTER_MAP.get(fileFormat);

      // then
      CustomAssertions.assertThat(actual)
              .as("Importer for file format " + fileFormat)
              .isNotNull();
    });
  }

  @Test
  public void testExporterRegistration() {
    Stream.of(FileType.values()).forEach(fileFormat -> {
      // when
      Supplier<Exporter> actual = FileFormatRegistry.EXPORTER_MAP.get(fileFormat);

      // then
      CustomAssertions.assertThat(actual)
              .as("Exporter for file format " + fileFormat)
              .isNotNull();
    });
  }
}
