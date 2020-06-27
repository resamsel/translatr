package utils;

import com.google.common.collect.ImmutableMap;
import exporters.*;
import importers.*;
import models.FileType;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Registration of file formats take place in here.
 */
public class FileFormatRegistry {
  /**
   * Importers need to be registered here to be available throughout the application.
   */
  public static final Map<FileType, Class<? extends Importer>> IMPORTER_MAP = ImmutableMap.<FileType, Class<? extends Importer>>builder()
          .put(FileType.PlayMessages, PlayMessagesImporter.class)
          .put(FileType.JavaProperties, JavaPropertiesImporter.class)
          .put(FileType.Gettext, GettextImporter.class)
          .put(FileType.Json, JsonImporter.class)
          .build();

  /**
   * Exporters need to be registered here to be available throughout the application.
   */
  public static final Map<FileType, Supplier<Exporter>> EXPORTER_MAP = ImmutableMap.<FileType, Supplier<Exporter>>builder()
          .put(FileType.PlayMessages, PlayMessagesExporter::new)
          .put(FileType.JavaProperties, JavaPropertiesExporter::new)
          .put(FileType.Gettext, GettextExporter::new)
          .put(FileType.Json, JsonExporter::new)
          .build();
}
