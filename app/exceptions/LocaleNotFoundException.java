package exceptions;

public class LocaleNotFoundException extends NotFoundException {

  private final String username;
  private final String projectName;
  private final String localeName;

  public LocaleNotFoundException(String message, String username, String projectName,
      String localeName) {
    super(message);

    this.username = username;
    this.projectName = projectName;
    this.localeName = localeName;
  }

  public String getUsername() {
    return username;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getLocaleName() {
    return localeName;
  }
}
