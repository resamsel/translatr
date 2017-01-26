package utils;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import com.fasterxml.jackson.databind.JsonNode;

import dto.NotFoundException;
import dto.PermissionException;
import dto.errors.ConstraintViolationError;
import dto.errors.NotFoundError;
import dto.errors.PermissionError;
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
  public static JsonNode toJson(PermissionException e) {
    return Json.toJson(new PermissionError(e));
  }

  /**
   * @param e
   * @return
   */
  public static JsonNode toJson(NotFoundException e) {
    return Json.toJson(new NotFoundError(e));
  }

  /**
   * @param e
   * @return
   */
  public static JsonNode toJson(ConstraintViolationException e) {
    return Json.toJson(new ConstraintViolationError(e));
  }

  /**
   * @param e
   * @return
   */
  public static JsonNode toJson(ValidationException e) {
    return Json.toJson(new ConstraintViolationError(e));
  }

  /**
   * @param t
   * @return
   */
  public static JsonNode toJson(Throwable t) {
    return Json.toJson(new dto.errors.GenericError(t.getMessage()));
  }
}
