package integration.services;

import static org.fest.assertions.api.Assertions.assertThat;

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
  public void create() {
    User user = createUser("user1", "user1@resamsel.com");

    assertThat(user.name).isEqualTo("user1");
  }

  @Test
  public void update() {
    User user = createUser("user1", "user1@resamsel.com");

    assertThat(user.email).isEqualTo("user1@resamsel.com");

    user.email = "a@b.c";

    userService.update(user);

    user = userService.byId(user.id);

    assertThat(user.email).isEqualTo("a@b.c");
  }
}
