package assertions;

import models.Project;
import models.ProjectUser;
import models.User;

import java.util.UUID;
import java.util.stream.Collectors;

public class ProjectAssert extends AbstractGenericAssert<ProjectAssert, Project> {

  ProjectAssert(Project actual) {
    super("project", actual, ProjectAssert.class);
  }

  public ProjectAssert nameIsEqualTo(String expected) {
    return isEqualTo("name", expected, actual.name);
  }

  public ProjectAssert ownerIsEqualTo(User expected) {
    return isEqualTo("owner", expected, actual.owner);
  }

  public ProjectAssert ownerNameIsEqualTo(String expected) {
    return isEqualTo("owner.name", expected, actual.owner.name);
  }

  public ProjectAssert wordCountIsNull() {
    return isNull("wordCount", actual.wordCount);
  }

  public ProjectAssert wordCountIsEqualTo(Integer expected) {
    return isEqualTo("wordCount", expected, actual.wordCount);
  }

  public ProjectAssert containsMemberWithId(UUID expected) {
    return contains("members", expected, actual.members.stream().map(ProjectUser::getId).collect(Collectors.toList()));
  }
}
