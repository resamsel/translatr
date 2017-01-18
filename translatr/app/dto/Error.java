package dto;

import javax.validation.ConstraintViolationException;

/**
 * @author resamsel
 * @version 18 Jan 2017
 */
public class Error extends Dto {
  public ErrorInfo error;

  /**
   * 
   */
  public Error(String message) {
    this("generic", message);
  }

  /**
   * @param type
   * @param message
   */
  public Error(String type, String message) {
    this.error = new ErrorInfo(type, message);
  }

  /**
   * @param e
   */
  public Error(ConstraintViolationException e) {
    this("ValidationException", "Constraint violations detected");

    this.error = new ConstraintViolationExceptionInfo(e.getConstraintViolations());
  }

  /**
   * @param error
   */
  public Error(ErrorInfo error) {
    this.error = error;
  }

  static class ErrorInfo {
    public String type;
    public String message;

    /**
     * @param type
     * @param message
     */
    public ErrorInfo(String type, String message) {
      this.type = type;
      this.message = message;
    }
  }
}
