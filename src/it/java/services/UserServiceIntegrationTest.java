package services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import criterias.GetCriteria;
import criterias.UserCriteria;
import java.util.UUID;
import models.User;
import org.junit.Test;
import play.mvc.Http;
import tests.AbstractDatabaseTest;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class UserServiceIntegrationTest extends AbstractDatabaseTest {

  @Test
  public void find() {
    assertThat(userService.findBy(new UserCriteria()).getList()).hasSize(0);

    createUser("user1", "user1@resamsel.com");
    createUser("user2", "user2@resamsel.com");
    createUser("user3", "user3@resamsel.com");

    assertThat(userService.findBy(new UserCriteria()).getList()).hasSize(3);
  }

  @Test
  public void findInactive() {
    Http.Request request = mock(Http.Request.class);

    assertThat(userService.findBy(new UserCriteria()).getList()).hasSize(0);

    userService.update(createUser("user1", "user1@resamsel.com").withActive(false), request);
    createUser("user2", "user2@resamsel.com");
    createUser("user3", "user3@resamsel.com");

    assertThat(userService.findBy(new UserCriteria()).getList()).hasSize(2);
  }

  @Test
  public void get() {
    Http.Request request = mock(Http.Request.class);
    User user = createUser("user1", "user1@resamsel.com");

    assertThat(userService.byId(user.id, request).id).isEqualTo(user.id);
  }

  @Test
  public void getUnknownId() {
    assertThat(userService.byId(GetCriteria.from(UUID.randomUUID(), mock(Http.Request.class)))).isNull();
  }

  @Test
  public void getInactive() {
    Http.Request request = mock(Http.Request.class);
    User user = createUser("user1", "user1@resamsel.com");

    userService.update(user.withActive(false), request);

    assertThat(userService.byId(user.id, request).id).isEqualTo(user.id);
  }

  @Test
  public void create() {
    User user = createUser("user1", "user1@resamsel.com");

    assertThat(user.name).isEqualTo("user1");
  }

  @Test
  public void update() {
    Http.Request request = mock(Http.Request.class);
    User user = createUser("user1", "user1@resamsel.com");

    assertThat(user.email).isEqualTo("user1@resamsel.com");

    userService.update(user.withEmail("a@b.c"), request);

    assertThat(userService.byId(user.id, request).email).isEqualTo("a@b.c");
  }

  @Test
  public void delete() {
    Http.Request request = mock(Http.Request.class);
    User user = createUser("user1", "user1@resamsel.com");

    assertThat(user).isNotNull();
    assertThat(user.id).isNotNull();

    userService.delete(user, request);

    assertThat(userService.byId(user.id, request)).isNull();
  }
}
