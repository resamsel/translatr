package utils;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

/**
 * @author resamsel
 * @version 27 May 2017
 */
public class JsonUtils {

  public static Long getId(JsonNode node) {
    return getId(node, "id");
  }

  private static Long getId(JsonNode node, String key) {
    if (!node.hasNonNull(key)) {
      return null;
    }

    return node.get(key).asLong();
  }

  public static UUID getUuid(String uuid) {
    if (uuid == null || uuid.trim().length() < 1) {
      return null;
    }

    try {
      return UUID.fromString(uuid);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * @param node
   * @param fieldName
   * @return
   */
  public static String getAsText(JsonNode node, String fieldName) {
    if (!node.hasNonNull(fieldName)) {
      return null;
    }

    return node.get(fieldName).asText();
  }
}
