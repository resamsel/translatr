package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;

/**
 * @author resamsel
 * @version 9 Jan 2017
 */
public class ErrorUtils {
  /**
   * @param e
   * @return
   */
  public static JsonNode toJson(Exception e) {
    ObjectNode error = Json.newObject();
    error.put("error", e.getMessage());
    return error;
  }
}
