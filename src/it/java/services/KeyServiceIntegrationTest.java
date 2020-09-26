package services;

import models.Key;
import models.Project;
import models.User;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import tests.AbstractTest;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class KeyServiceIntegrationTest extends AbstractTest {
  @Inject
  KeyService keyService;
  @Inject
  ProjectService projectService;

  @Test
  @Ignore("FIXME: fails with strange exception")
  public void create() {
    Http.Request request = mock(Http.Request.class);
    User user = createUser("user1", "user1@resamsel.com");
    Project project = projectService.create(new Project().withOwner(user).withName("blubbb"), request);
    Key key = keyService.create(new Key(project, "key.one"), request);

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
