package integration.services;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import models.Locale;
import models.Project;
import models.User;
import org.junit.Test;
import services.LocaleService;
import services.ProjectService;
import tests.AbstractTest;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class LocaleServiceTest extends AbstractTest {
  @Inject
  LocaleService localeService;
  @Inject
  ProjectService projectService;

  @Test
  public void create() {
    User user = createUser("user1", "user1@resamsel.com");
    Project project = projectService.create(new Project().withOwner(user).withName("blubbb"));
    Locale locale = localeService.create(new Locale(project, "de"));

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
