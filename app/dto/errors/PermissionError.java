package dto.errors;

import dto.PermissionException;

/**
 * @author resamsel
 * @version 18 Jan 2017
 */
public class PermissionError extends Error {
  public PermissionErrorInfo error;

  /**
   * 
   */
  public PermissionError(PermissionException e) {
    this.error = new PermissionErrorInfo(e);
  }
}
