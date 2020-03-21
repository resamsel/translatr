package importers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Locale;
import play.libs.Json;
import services.KeyService;
import services.MessageService;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Properties;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

public class JsonImporter extends AbstractImporter implements Importer {

  @Inject
  public JsonImporter(KeyService keyService, MessageService messageService) {
    super(keyService, messageService);
  }

  @Override
  Properties retrieveProperties(InputStream inputStream, Locale locale) throws Exception {
    JsonNode json = Json.mapper().readTree(inputStream);
    Properties properties = new Properties();

    if (json.isObject()) {
      ObjectNode jsonObject = (ObjectNode) json;

      stream(spliteratorUnknownSize(jsonObject.fields(), 0), false)
          .forEach(entry -> properties.put(entry.getKey(), entry.getValue().asText()));
    }

    return properties;
  }
}
