package dto.errors;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
public class ConstraintViolationError extends Error {
  public ConstraintViolationErrorInfo error;

  /**
   * @param e
   */
  public ConstraintViolationError(ConstraintViolationException e) {
    this.error = new ConstraintViolationErrorInfo(e.getConstraintViolations());
  }

  /**
   * @param e
   */
  public ConstraintViolationError(ValidationException e) {
    this.error = new ConstraintViolationErrorInfo(e);
  }
}
