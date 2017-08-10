package utils;

import static utils.UserRepositoryMock.byUsername;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import org.joda.time.DateTime;

/**
 * Created by resamsel on 24/07/2017.
 */
public class ProjectRepositoryMock {

  private static final Map<String, Project> REPOSITORY = new HashMap<>();

  static {
    User johnSmith = byUsername("johnsmith");
    User janeDoe = byUsername("janedoe");
    REPOSITORY.put(
        "johnsmith/project1",
        new Project()
            .withId(UUID.randomUUID())
            .withName("project1")
            .withOwner(johnSmith)
            .withWhenCreated(DateTime.now())
            .withWhenUpdated(DateTime.now())
            .withMembers(new ProjectUser()
                .withUser(johnSmith)
                .withRole(ProjectRole.Owner)));
    REPOSITORY.put(
        "janedoe/project1",
        new Project()
            .withId(UUID.randomUUID())
            .withName("project1")
            .withOwner(janeDoe)
            .withWhenCreated(DateTime.now())
            .withWhenUpdated(DateTime.now())
            .withMembers(new ProjectUser()
                .withUser(janeDoe)
                .withRole(ProjectRole.Owner)));
  }

  public static Project byOwnerAndName(String username, String projectName) {
    return REPOSITORY.get(username + "/" + projectName);
  }

  public static Project createProject(Project project, String name) {
    return createProject(project.id, name, project.owner.username);
  }

  public static Project createProject(UUID id, String name, String username) {
    Project project = new Project();

    project.id = id;
    project.name = name;
    project.owner = UserRepositoryMock.byUsername(username);

    return project;
  }
}
