package dto;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public class NotFoundException extends RuntimeException {
  private static final long serialVersionUID = 944214728665880687L;

  /**
   * 
   */
  public NotFoundException(String message) {
    super(message);
  }

  /**
   * @return
   */
  public JsonNode toJson() {
    return Json.newObject().put("type", "notFound").put("error", getMessage());
  }
}
