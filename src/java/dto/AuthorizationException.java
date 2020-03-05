package dto;

public class AuthorizationException extends RuntimeException {
  public AuthorizationException() {
    super("Authorization required");
  }
}
