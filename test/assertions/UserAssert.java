package assertions;

import models.User;

public class UserAssert extends AbstractGenericAssert<UserAssert, User> {

  private UserAssert(User actual) {
    super("user", actual, UserAssert.class);
  }

  public static UserAssert assertThat(User actual) {
    return new UserAssert(actual);
  }

  public UserAssert nameIsEqualTo(String expected) {
    return isEqualTo("name", expected, actual.name);
  }
}
