package utils;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author resamsel
 * @version 27 May 2017
 */
public class JsonUtils {
  public static Long getId(JsonNode node) {
    return getId(node, "id");
  }

  private static Long getId(JsonNode node, String key) {
    if (!node.hasNonNull(key))
      return null;

    return node.get(key).asLong();
  }

  public static UUID getUuid(JsonNode node) {
    return getUuid(node, "id");
  }

  public static UUID getUuid(JsonNode node, String key) {
    if (!node.hasNonNull(key))
      return null;

    return getUuid(node.get(key).asText());
  }

  public static UUID getUuid(String uuid) {
    if (uuid == null || uuid.trim().length() < 1)
      return null;

    try {
      return UUID.fromString(uuid);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * @param node
   * @param string
   * @return
   */
  public static String getAsText(JsonNode node, String fieldName) {
    if (!node.hasNonNull(fieldName))
      return null;

    return node.get(fieldName).asText();
  }
}
