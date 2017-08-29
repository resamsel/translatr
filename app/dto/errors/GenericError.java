package dto.errors;

/**
 * @author resamsel
 * @version 18 Jan 2017
 */
public class GenericError extends Error {

  private static final long serialVersionUID = -3306452859073159514L;
  public GenericErrorInfo error;

  public GenericError(String message) {
    this(new GenericErrorInfo("Generic", message));
  }

  public GenericError(GenericErrorInfo error) {
    this.error = error;
  }
}
