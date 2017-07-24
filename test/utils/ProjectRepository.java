package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import org.joda.time.DateTime;

/**
 * Created by resamsel on 24/07/2017.
 */
public class ProjectRepository {

  private static final Map<String, Project> REPOSITORY = new HashMap<>();

  static {
    REPOSITORY.put(
        "johnsmith/project1",
        new Project()
            .withId(UUID.randomUUID())
            .withName("project1")
            .withOwner(UserRepository.byUsername("johnsmith"))
            .withWhenCreated(DateTime.now())
            .withWhenUpdated(DateTime.now())
            .withMembers(new ProjectUser()
                .withUser(UserRepository.byUsername("johnsmith"))
                .withRole(ProjectRole.Owner)));
  }

  public static Project byOwnerAndName(String username, String projectName) {
    return REPOSITORY.get(username + "/" + projectName);
  }
}
