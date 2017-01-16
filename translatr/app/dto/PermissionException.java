package dto;

import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Scope;
import play.libs.Json;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public class PermissionException extends RuntimeException {
  private static final long serialVersionUID = -5814708556078110519L;

  private Scope[] scopes;

  /**
   * 
   */
  public PermissionException(String message) {
    super(message);
  }

  /**
   * @param errorMessage
   * @param scopes
   */
  public PermissionException(String message, Scope... scopes) {
    this(String.format("%s (scopes required: %s)", message, Arrays.asList(scopes)));

    this.scopes = scopes;
  }

  /**
   * @return
   */
  public JsonNode toJson() {
    ObjectNode json = Json.newObject().put("type", "permission").put("error", getMessage());

    if (scopes != null && scopes.length > 0) {
      ArrayNode scopesJson = Json.newArray();
      for (Scope scope : scopes)
        scopesJson.add(Json.toJson(scope));
      json.set("scopes", scopesJson);
    }

    return json;
  }
}
