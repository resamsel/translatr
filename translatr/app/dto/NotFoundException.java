package dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    return new ObjectNode(new JsonNodeFactory(false)).put("error", getMessage());
  }
}
