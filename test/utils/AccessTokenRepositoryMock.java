package utils;

import models.AccessToken;
import models.User;

public class AccessTokenRepositoryMock {

  public static AccessToken createAccessToken(AccessToken accessToken, String name) {
    return createAccessToken(accessToken.id, accessToken.user, name);
  }

  public static AccessToken createAccessToken(Long id, User user, String name) {
    AccessToken t = new AccessToken();

    t.id = id;
    t.name = name;
    t.user = user;

    return t;
  }
}
