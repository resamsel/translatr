package integration.services;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;

import models.Key;
import models.Project;
import models.User;
import services.KeyService;
import services.ProjectService;
import tests.AbstractTest;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class KeyServiceTest extends AbstractTest {
  @Inject
  KeyService keyService;
  @Inject
  ProjectService projectService;

  @Test
  public void create() {
    User user = createUser("user1", "user1@resamsel.com");
    Project project =
        projectService.create(new Project().withOwner(user).withName("blubbb").withPath("blubbb"));
    Key key = keyService.create(new Key(project, "key.one"));

    assertThat(key.name).isEqualTo("key.one");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void injectMembers() {
    keyService = app.injector().instanceOf(KeyService.class);
    projectService = app.injector().instanceOf(ProjectService.class);
  }
}
