package dto.errors;

import dto.NotFoundException;

/**
 * @author resamsel
 * @version 18 Jan 2017
 */
public class NotFoundError extends Error {
  public NotFoundErrorInfo error;

  /**
   * 
   */
  public NotFoundError(NotFoundException e) {
    this.error = new NotFoundErrorInfo(e);
  }
}
