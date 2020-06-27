package integration.repositories;

import models.User;
import org.junit.Test;
import repositories.UserRepository;
import tests.AbstractDatabaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryTest extends AbstractDatabaseTest {

  private UserRepository userRepository;

  @Test
  public void emailToUsernameWithLongEmailAddress() {
    // given
    String email = "this.is.a.really.long.email@long-domain-for-test.com";

    // when
    String actual = userRepository.emailToUsername(email);

    // then
    assertThat(actual).isEqualTo("thisisareallylongemaillongdomain");
  }

  @Test
  public void emailToUsernameFromCreateUser() {
    // given
    String email = "this.is.a.really.long.email@long-domain-for-test.com";

    // when
    User user1 = createUser("User with long email 1", email);
    User user2 = createUser("User with long email 2", email);

    // then
    assertThat(user1.username).isEqualTo("thisisareallylongemaillongdomain");
    assertThat(user2.username).isNotEqualTo(user1.username);
  }

  @Override
  protected void injectMembers() {
    userRepository = app.injector().instanceOf(UserRepository.class);
  }
}
