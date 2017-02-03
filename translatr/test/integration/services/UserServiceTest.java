package integration.services;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;

import criterias.UserCriteria;
import models.User;
import tests.AbstractTest;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class UserServiceTest extends AbstractTest {
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
    assertThat(userService.findBy(new UserCriteria()).getList()).hasSize(0);

    userService.update(createUser("user1", "user1@resamsel.com").withActive(false));
    createUser("user2", "user2@resamsel.com");
    createUser("user3", "user3@resamsel.com");

    assertThat(userService.findBy(new UserCriteria()).getList()).hasSize(2);
  }

  @Test
  public void get() {
    User user = createUser("user1", "user1@resamsel.com");

    assertThat(userService.byId(user.id).id).isEqualTo(user.id);
  }

  @Test
  public void getUnknownId() {
    assertThat(userService.byId(UUID.randomUUID())).isNull();
  }

  @Test
  public void getInactive() {
    User user = createUser("user1", "user1@resamsel.com");

    userService.update(user.withActive(false));

    assertThat(userService.byId(user.id).id).isEqualTo(user.id);
  }

  @Test
  public void create() {
    User user = createUser("user1", "user1@resamsel.com");

    assertThat(user.name).isEqualTo("user1");
  }

  @Test
  public void update() {
    User user = createUser("user1", "user1@resamsel.com");

    assertThat(user.email).isEqualTo("user1@resamsel.com");

    userService.update(user.withEmail("a@b.c"));

    assertThat(userService.byId(user.id).email).isEqualTo("a@b.c");
  }

  @Test
  public void delete() {
    User user = createUser("user1", "user1@resamsel.com");

    assertThat(user).isNotNull();
    assertThat(user.id).isNotNull();

    userService.delete(user);

    assertThat(userService.byId(user.id)).isNull();
  }
}
