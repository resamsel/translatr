package assertions;

import models.Project;
import models.User;

public class ProjectAssert extends AbstractGenericAssert<ProjectAssert, Project> {

  private ProjectAssert(Project actual) {
    super("project", actual, ProjectAssert.class);
  }

  public static ProjectAssert assertThat(Project actual) {
    return new ProjectAssert(actual);
  }

  public ProjectAssert ownerIsEqualTo(User expected) {
    return isEqualTo("owner", expected, actual.owner);
  }
}
