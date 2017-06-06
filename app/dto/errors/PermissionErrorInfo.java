package dto.errors;

import dto.PermissionException;
import models.Scope;

public class PermissionErrorInfo extends GenericErrorInfo {
  public Scope[] scopes;

  /**
   * @param type
   * @param message
   */
  public PermissionErrorInfo(PermissionException e) {
    super("PermissionError", e.getMessage());

    this.scopes = e.scopes;
  }
}
