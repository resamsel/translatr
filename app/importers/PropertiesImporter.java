package importers;

import models.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import services.KeyService;
import services.MessageService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 *
 * @author resamsel
 * @version 30 Aug 2016
 */
public abstract class PropertiesImporter extends AbstractImporter implements Importer {
  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesImporter.class);

  protected PropertiesImporter(KeyService keyService, MessageService messageService) {
    super(keyService, messageService);
  }

  /**
   * {@inheritDoc}
   *
   * @throws IOException
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  @Override
  Properties retrieveProperties(InputStream inputStream, Locale locale) throws IOException {
    Properties properties = new Properties();

    try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      properties.load(reader);
    }

    return properties;
  }

  @Override
  public void apply(File file, Locale locale, Http.Request request) throws Exception {
    LOGGER.debug("Importing from file {}", file.getName());

    Properties properties = retrieveProperties(new FileInputStream(file), null);

    load(locale, properties.stringPropertyNames(), request);

    saveKeys(locale, properties, request);
    saveMessages(locale, properties, request);

    LOGGER.debug("Imported from file {}", file.getName());
  }
}
