package utils;

import java.util.concurrent.ThreadLocalRandom;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import org.joda.time.DateTime;

public class ProjectUserRepositoryMock {

  public static ProjectUser by(Project project, User user, ProjectRole role) {
    return new ProjectUser()
        .withId(ThreadLocalRandom.current().nextLong())
        .withProject(project)
        .withUser(user)
        .withRole(role)
        .withWhenCreated(DateTime.now())
        .withWhenUpdated(DateTime.now());
  }
}
