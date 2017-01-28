package integration.services;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;

import models.Locale;
import models.Project;
import models.User;
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
    User user = createUser("user1");
    Project project = projectService.create(new Project().withOwner(user).withName("blubbb"));
    Locale locale = localeService.create(new Locale(project, "de"));

    assertThat(locale.name).isEqualTo("de");
  }
}
