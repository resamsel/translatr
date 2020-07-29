package assertions;

import models.AccessToken;

public class AccessTokenAssert extends AbstractGenericAssert<AccessTokenAssert, AccessToken> {

  private AccessTokenAssert(AccessToken actual) {
    super("accessToken", actual, AccessTokenAssert.class);
  }

  public static AccessTokenAssert assertThat(AccessToken actual) {
    return new AccessTokenAssert(actual);
  }

  public AccessTokenAssert nameIsEqualTo(String expected) {
    return isEqualTo("name", expected, actual.name);
  }
}
