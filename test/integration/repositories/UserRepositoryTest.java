package integration.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import models.User;
import org.junit.Test;
import repositories.UserRepository;
import tests.AbstractDatabaseTest;

public class UserRepositoryTest extends AbstractDatabaseTest {

  private UserRepository userRepository;

  @Test
  public void emailToUsername() {
    String username =
        userRepository.emailToUsername("this.is.a.really.long.email@long-domain-for-test.com");

    assertThat(username).isEqualTo("thisisareallylongemaillongdomain");

    User user1 = createUser("User with long email 1",
        "this.is.a.really.long.email@long-domain-for-test.com");

    assertThat(user1.username).isEqualTo("thisisareallylongemaillongdomain");

    username = userRepository
        .emailToUsername("this.is.a.really.long.email@long-domain-for-test.com");

    assertThat(username).startsWith("thisisareallylongemaillongdo");

    User user2 = createUser("User with long email 2",
        "this.is.a.really.long.email@long-domain-for-test.com");

    assertThat(user2.username).isNotEqualTo(user1.username);
  }

  @Override
  protected void injectMembers() {
    userRepository = app.injector().instanceOf(UserRepository.class);
  }
}
