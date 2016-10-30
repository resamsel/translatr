package dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    return new ObjectNode(new JsonNodeFactory(false)).put("error", getMessage());
  }
}
