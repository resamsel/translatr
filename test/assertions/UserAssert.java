package assertions;

import models.User;

import java.util.Map;

public class UserAssert extends AbstractGenericAssert<UserAssert, User> {

  UserAssert(User actual) {
    super("user", actual, UserAssert.class);
  }

  public static UserAssert assertThat(User actual) {
    return new UserAssert(actual);
  }

  public UserAssert nameIsEqualTo(String expected) {
    return isEqualTo("name", expected, actual.name);
  }

  public UserAssert emailIsEqualTo(String expected) {
    return isEqualTo("email", expected, actual.email);
  }

  public UserAssert activeIsTrue() {
    return isTrue("active", actual.active);
  }

  public UserAssert activeIsFalse() {
    return isFalse("active", actual.active);
  }

  public UserAssert linkedAccountsHasSize(int expected) {
    return hasSize("linkedAccounts", expected, actual.linkedAccounts);
  }

  public UserAssert settingsIsEqualTo(Map.Entry<String, String> expected) {
    return isEqualTo("settings", expected, actual.settings);
  }

  @SafeVarargs
  public final UserAssert settingsContains(Map.Entry<String, String>... expected) {
    return contains("settings", actual.settings, expected);
  }

  public UserAssert settingsContainsKey(String expected) {
    return containsKey("settings", expected, actual.settings);
  }

  public UserAssert settingsHasSize(int expected) {
    return hasSize("settings", expected, actual.settings);
  }
}
