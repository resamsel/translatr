package exceptions;

public class UserNotFoundException extends NotFoundException {

  private final String username;

  public UserNotFoundException(String message, String username) {
    super(message);

    this.username = username;
  }

  public String getUsername() {
    return username;
  }
}
