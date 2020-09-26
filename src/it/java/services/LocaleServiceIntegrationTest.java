package services;

import models.Locale;
import models.Project;
import models.User;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import tests.AbstractTest;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class LocaleServiceIntegrationTest extends AbstractTest {
  @Inject
  LocaleService localeService;
  @Inject
  ProjectService projectService;

  @Test
  @Ignore("FIXME: fails with strange exception")
  public void create() {
    User user = createUser("user1", "user1@resamsel.com");
    Http.Request request = mock(Http.Request.class);

    when(authProvider.loggedInUser(request)).thenReturn(user);
    when(authProvider.loggedInUserId(request)).thenReturn(user.id);

    Project project = projectService.create(new Project().withOwner(user).withName("blubbb"), request);
    Locale locale = localeService.create(new Locale(project, "de"), request);

    assertThat(locale.name).isEqualTo("de");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void injectMembers() {
    localeService = app.injector().instanceOf(LocaleService.class);
    projectService = app.injector().instanceOf(ProjectService.class);
  }
}
