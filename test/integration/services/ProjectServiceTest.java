package integration.services;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.stream.Collectors;
import javax.inject.Inject;
import models.Project;
import models.User;
import org.junit.Test;
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
    User user = createUser("user1", "user1@resamsel.com");
    Project project = projectService.create(new Project().withOwner(user).withName("blubbb"));

    assertThat(project.id).isNotNull();

    project = projectService.byId(project.id);

    assertThat(project.owner.name).isEqualTo("user1");
    assertThat(project.name).isEqualTo("blubbb");
    assertThat(project.members.stream().map(m -> m.user).collect(Collectors.toList()))
        .contains(user);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void injectMembers() {
    projectService = app.injector().instanceOf(ProjectService.class);
  }
}
