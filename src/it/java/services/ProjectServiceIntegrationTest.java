package services;

import models.Project;
import models.User;
import org.junit.Ignore;
import org.junit.Test;
import tests.AbstractTest;

import javax.inject.Inject;

import static assertions.CustomAssertions.assertThat;
import static repositories.ProjectRepository.FETCH_MEMBERS;

/**
 * @author resamsel
 * @version 11 Jan 2017
 */
public class ProjectServiceIntegrationTest extends AbstractTest {
  @Inject
  ProjectService projectService;

  @Test
  @Ignore("FIXME: fails with strange exception")
  public void create() {
    User user = createUser("user1", "user1@resamsel.com");
    Project project = projectService.create(new Project().withOwner(user).withName("blubbb"));

    assertThat(project.id).isNotNull();

    project = projectService.byId(project.id, FETCH_MEMBERS);

    assertThat(project).ownerNameIsEqualTo("user1");
    assertThat(project).nameIsEqualTo("blubbb");
    assertThat(project).containsMemberWithId(user.id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void injectMembers() {
    projectService = app.injector().instanceOf(ProjectService.class);
  }
}
