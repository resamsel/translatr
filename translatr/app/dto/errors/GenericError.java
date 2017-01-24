package dto.errors;

/**
 * @author resamsel
 * @version 18 Jan 2017
 */
public class GenericError extends Error {
  public GenericErrorInfo error;

  /**
   * 
   */
  public GenericError(String message) {
    this(new GenericErrorInfo("Generic", message));
  }

  /**
   * @param error
   */
  public GenericError(GenericErrorInfo error) {
    this.error = error;
  }
}
