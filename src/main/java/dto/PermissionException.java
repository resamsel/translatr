package dto;

import java.util.Arrays;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public class PermissionException extends RuntimeException {
  private static final long serialVersionUID = -5814708556078110519L;

  public String[] scopes;

  /**
   *
   */
  public PermissionException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param scopes
   */
  public PermissionException(String message, String... scopes) {
    this(String.format("%s (scopes required: %s)", message, String.join(", ", scopes)));

    this.scopes = scopes;
  }

  /**
   * FindBugs EI: getScopes() may expose internal representation by returning
   * PermissionException.scopes.
   *
   * @return the scopes
   */
  public String[] getScopes() {
    return Arrays.copyOf(scopes, scopes.length);
  }

  /**
   * FindBugs EI2: setScopes(Scope[]) may expose internal representation by storing an externally
   * mutable object into PermissionException.scopes.
   *
   * @param scopes the scopes to set
   */
  public void setScopes(String[] scopes) {
    this.scopes = Arrays.copyOf(scopes, scopes.length);
  }
}
