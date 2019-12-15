package assertions;

import models.ProjectRole;
import models.ProjectUser;

import java.util.UUID;

public class ProjectUserAssert extends AbstractGenericAssert<ProjectUserAssert, ProjectUser> {
  protected ProjectUserAssert(ProjectUser actual) {
    super("projectUser", actual, ProjectUserAssert.class);
  }

  public ProjectUserAssert userIdIsEqualTo(UUID expected) {
    return isEqualTo("user.id", expected, actual.user.id);
  }

  public ProjectUserAssert projectIdIsEqualTo(UUID expected) {
    return isEqualTo("project.id", expected, actual.project.id);
  }

  public ProjectUserAssert roleIsEqualTo(ProjectRole expected) {
    return isEqualTo("role", expected, actual.role);
  }
}
