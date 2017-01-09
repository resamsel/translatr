package utils;

import javax.validation.ValidationException;

import com.fasterxml.jackson.databind.JsonNode;

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
  public static JsonNode toJson(ValidationException e) {
    return Json.newObject().put("type", "validation").put("error", e.getMessage());
  }

  /**
   * @param e
   * @return
   */
  public static JsonNode toJson(Exception e) {
    return Json.newObject().put("type", "generic").put("error", e.getMessage());
  }
}
