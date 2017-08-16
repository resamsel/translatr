package utils;

import com.fasterxml.jackson.databind.JsonNode;
import dto.NotFoundException;
import dto.PermissionException;
import dto.errors.ConstraintViolationError;
import dto.errors.NotFoundError;
import dto.errors.PermissionError;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;

/**
 * @author resamsel
 * @version 9 Jan 2017
 */
public class ErrorUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorUtils.class);

  public static JsonNode toJson(PermissionException e) {
    return Json.toJson(new PermissionError(e));
  }

  public static JsonNode toJson(NotFoundException e) {
    return Json.toJson(new NotFoundError(e));
  }

  public static JsonNode toJson(ConstraintViolationException e) {
    return Json.toJson(new ConstraintViolationError(e));
  }

  public static JsonNode toJson(ValidationException e) {
    return Json.toJson(new ConstraintViolationError(e));
  }

  public static JsonNode toJson(Throwable t) {
    LOGGER.debug("Throwing generic error", t);
    return Json.toJson(new dto.errors.GenericError(t.getMessage()));
  }
}
