package utils;

import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
  public static JsonNode toJson(ConstraintViolationException e) {
    ArrayNode violations = Json.newArray().addAll(
        e.getConstraintViolations().stream().map(cv -> toJson(cv)).collect(Collectors.toList()));
    return Json.newObject().put("type", "validation").put("error", e.getMessage()).set("violations",
        violations);
  }

  /**
   * @param e
   * @return
   */
  public static JsonNode toJson(ValidationException e) {
    return Json.newObject().put("type", "validation").put("error", e.getMessage());
  }

  /**
   * @param t
   * @return
   */
  public static JsonNode toJson(Throwable t) {
    return Json.newObject().put("type", "generic").put("error", t.getMessage());
  }

  /**
   * @param cv
   * @return
   */
  private static JsonNode toJson(ConstraintViolation<?> violation) {
    ObjectNode json = Json.newObject().put("message", violation.getMessage()).put("field",
        violation.getPropertyPath().toString());

    if (violation.getInvalidValue() != null)
      json.put("invalidValue", String.valueOf(violation.getInvalidValue()));

    return json;
  }
}
