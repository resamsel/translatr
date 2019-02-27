package exporters;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import models.Locale;
import models.Message;

/**
 *
 * @author resamsel
 * @version 30 Aug 2016
 */
public abstract class PropertiesExporter extends AbstractExporter implements Exporter {
  protected static final String DEFAULT = "default";

  @Override
  public byte[] apply(Locale locale) {
    Collections.sort(locale.messages, (a, b) -> a.key.name.compareTo(b.key.name));

    StringBuilder sb = new StringBuilder();
    for (Message message : locale.messages)
      sb.append(message.key.name).append(" = ").append(escapeValue(message.value)).append("\n");

    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  /**
   * @param value
   * @return
   */
  private String escapeValue(String value) {
    return value.replace("\n", "\\n").replace("\r", "");
  }
}
