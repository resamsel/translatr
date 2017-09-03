package exceptions;

public class ProjectNotFoundException extends NotFoundException {

  private final String username;
  private final String projectName;

  public ProjectNotFoundException(String message, String username, String projectName) {
    super(message);

    this.username = username;
    this.projectName = projectName;
  }

  public String getUsername() {
    return username;
  }

  public String getProjectName() {
    return projectName;
  }
}
