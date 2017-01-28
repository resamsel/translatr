package integration.services;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;

import models.Project;
import models.User;
import services.ProjectService;
import tests.AbstractTest;

/**
 * @author resamsel
 * @version 11 Jan 2017
 */
public class ProjectServiceTest extends AbstractTest {
  @Inject
  ProjectService projectService;

  @Test
  public void create() {
    User user = createUser("user1");
    Project project = projectService.create(new Project().withOwner(user).withName("blubbb"));

    assertThat(project.owner.name).isEqualTo("user1");
    assertThat(project.name).isEqualTo("blubbb");
  }
}
