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
}
