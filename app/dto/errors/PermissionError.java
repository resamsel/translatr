package dto.errors;

import dto.PermissionException;

/**
 * @author resamsel
 * @version 18 Jan 2017
 */
public class PermissionError extends Error {
  private static final long serialVersionUID = -9177620649967386983L;
  public PermissionErrorInfo error;

  /**
   * 
   */
  public PermissionError(PermissionException e) {
    this.error = new PermissionErrorInfo(e);
  }
}
