package exporters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import models.Locale;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class JsonExporter extends AbstractExporter implements Exporter {
  protected static final ObjectMapper SORTED_MAPPER = new ObjectMapper();

  static {
    SORTED_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    SORTED_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
  }

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

    try {
      return mapper.writeValueAsBytes(
          locale.messages.stream()
              .collect(Collectors.toMap(m -> m.key.name, m -> m.value)));
    } catch (JsonProcessingException e) {
      return new byte[]{};
    }
  }

  @Override
  public String getFilename(Locale locale) {
    return locale.name + ".json";
  }
}
