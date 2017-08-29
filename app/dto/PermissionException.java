package dto;

import java.util.Arrays;
import java.util.stream.Collectors;
import models.Scope;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public class PermissionException extends RuntimeException {
  private static final long serialVersionUID = -5814708556078110519L;

  public Scope[] scopes;

  /**
   * 
   */
  public PermissionException(String message) {
    super(message);
  }

  /**
   * @param errorMessage
   * @param scopes
   */
  public PermissionException(String message, Scope... scopes) {
    this(String.format("%s (scopes required: %s)", message,
        Arrays.stream(scopes).map(Scope::toString).collect(Collectors.joining(", "))));

    this.scopes = scopes;
  }

  /**
   * FindBugs EI: getScopes() may expose internal representation by returning
   * PermissionException.scopes.
   * 
   * @return the scopes
   */
  public Scope[] getScopes() {
    return Arrays.copyOf(scopes, scopes.length);
  }

  /**
   * FindBugs EI2: setScopes(Scope[]) may expose internal representation by storing an externally
   * mutable object into PermissionException.scopes.
   * 
   * @param scopes the scopes to set
   */
  public void setScopes(Scope[] scopes) {
    this.scopes = Arrays.copyOf(scopes, scopes.length);
  }
}
