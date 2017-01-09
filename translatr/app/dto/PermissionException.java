package dto;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public class PermissionException extends RuntimeException {
  private static final long serialVersionUID = -5814708556078110519L;

  /**
   * 
   */
  public PermissionException(String message) {
    super(message);
  }

  /**
   * @return
   */
  public JsonNode toJson() {
    return Json.newObject().put("type", "permission").put("error", getMessage());
  }
}
