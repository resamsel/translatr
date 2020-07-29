package assertions;

import dto.User;

import java.util.Map;

public class UserDtoAssert extends AbstractGenericAssert<UserDtoAssert, User> {

  UserDtoAssert(User actual) {
    super("user", actual, UserDtoAssert.class);
  }

  public static UserDtoAssert assertThat(User actual) {
    return new UserDtoAssert(actual);
  }

  public UserDtoAssert nameIsEqualTo(String expected) {
    return isEqualTo("name", expected, actual.name);
  }

  public UserDtoAssert emailIsEqualTo(String expected) {
    return isEqualTo("email", expected, actual.email);
  }

  public UserDtoAssert settingsIsEqualTo(Map.Entry<String, String> expected) {
    return isEqualTo("settings", expected, actual.settings);
  }

  @SafeVarargs
  public final UserDtoAssert settingsContains(Map.Entry<String, String>... expected) {
    return contains("settings", actual.settings, expected);
  }

  public UserDtoAssert settingsContainsKey(String expected) {
    return containsKey("settings", expected, actual.settings);
  }

  public UserDtoAssert settingsHasSize(int expected) {
    return hasSize("settings", expected, actual.settings);
  }
}
