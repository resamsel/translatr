package exceptions;

public class KeyNotFoundException extends NotFoundException {

  private final String username;
  private final String projectName;
  private final String keyName;

  public KeyNotFoundException(String message, String username, String projectName, String keyName) {
    super(message);

    this.username = username;
    this.projectName = projectName;
    this.keyName = keyName;
  }

  public String getUsername() {
    return username;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getKeyName() {
    return keyName;
  }
}
