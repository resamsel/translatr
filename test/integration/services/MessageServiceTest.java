package integration.services;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;

import models.Key;
import models.Locale;
import models.Message;
import models.Project;
import models.User;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.ProjectService;
import tests.AbstractTest;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class MessageServiceTest extends AbstractTest {
  @Inject
  MessageService messageService;
  @Inject
  ProjectService projectService;
  @Inject
  KeyService keyService;
  @Inject
  LocaleService localeService;

  @Test
  public void create() {
    User user = createUser("user1", "user1@resamsel.com");
    Project project =
        projectService.create(new Project().withOwner(user).withName("blubbb").withPath("blubbb"));
    Key key = keyService.create(new Key(project, "key.one"));
    Locale locale = localeService.create(new Locale(project, "de"));
    Message message = messageService.create(new Message(locale, key, "Message One"));

    assertThat(message.id).isNotNull();
    assertThat(message.value).isEqualTo("Message One");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void injectMembers() {
    messageService = app.injector().instanceOf(MessageService.class);
    localeService = app.injector().instanceOf(LocaleService.class);
    keyService = app.injector().instanceOf(KeyService.class);
    projectService = app.injector().instanceOf(ProjectService.class);
  }
}
