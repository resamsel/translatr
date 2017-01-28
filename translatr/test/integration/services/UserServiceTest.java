package integration.services;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;

import models.User;
import services.UserService;
import tests.AbstractTest;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class UserServiceTest extends AbstractTest {
  @Inject
  UserService userService;

  @Test
  public void create() {
    User user = userService.create(new User().withName("user1"));

    assertThat(user.name).isEqualTo("user1");
  }
}
