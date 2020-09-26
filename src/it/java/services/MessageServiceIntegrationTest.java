package services;

import models.Key;
import models.Locale;
import models.Message;
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
public class MessageServiceIntegrationTest extends AbstractTest {

  @Inject
  MessageService messageService;
  @Inject
  ProjectService projectService;
  @Inject
  KeyService keyService;
  @Inject
  LocaleService localeService;

  @Test
  @Ignore("FIXME: fails with strange exception")
  public void create() {
    Http.Request request = mock(Http.Request.class);
    User user = createUser("user1", "user1@resamsel.com");
    Project project = projectService.create(new Project().withOwner(user).withName("blubbb"), request);
    Key key = keyService.create(new Key(project, "key.one"), request);
    Locale locale = localeService.create(new Locale(project, "de"), request);
    Message message = messageService.create(new Message(locale, key, "Message One"), request);

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
