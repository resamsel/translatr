package dto.errors;

import dto.PermissionException;

public class PermissionErrorInfo extends GenericErrorInfo {
  private static final long serialVersionUID = 2333183003034734287L;

  public final String[] scopes;

  public PermissionErrorInfo(PermissionException e) {
    super("PermissionError", e.getMessage());

    this.scopes = e.scopes;
  }
}
