package dto.errors;

import dto.PermissionException;
import models.Scope;

public class PermissionErrorInfo extends GenericErrorInfo {
  private static final long serialVersionUID = 2333183003034734287L;

  public final Scope[] scopes;

  /**
   * @param type
   * @param message
   */
  public PermissionErrorInfo(PermissionException e) {
    super("PermissionError", e.getMessage());

    this.scopes = e.scopes;
  }
}
