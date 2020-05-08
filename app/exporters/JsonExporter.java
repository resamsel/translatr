package exporters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import models.Locale;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class JsonExporter extends AbstractExporter implements Exporter {
  protected static final ObjectMapper SORTED_MAPPER = new ObjectMapper()
          .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
          .configure(SerializationFeature.INDENT_OUTPUT, true);

  private final ObjectMapper mapper;

  public JsonExporter() {
    this(SORTED_MAPPER);
  }

  public JsonExporter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public byte[] apply(Locale locale) {
    if (locale == null || locale.messages == null) {
      return new byte[]{};
    }

    Map<String, String> messages = locale.messages
            .stream()
            .collect(toMap(m -> m.key.name, m -> m.value));

    try {
      return mapper.writeValueAsBytes(messages);
    } catch (JsonProcessingException e) {
      return new byte[]{};
    }
  }

  @Override
  public String getFilename(Locale locale) {
    return locale.name + ".json";
  }
}
