package utils;

import models.AccessToken;
import models.Scope;
import models.User;
import services.impl.AccessTokenServiceImpl;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AccessTokenRepositoryMock {

  public static AccessToken createAccessToken(AccessToken accessToken, String name) {
    return createAccessToken(accessToken.id, accessToken.user, name);
  }

  public static AccessToken createAccessToken(Long id, User user, String name) {
    AccessToken t = new AccessToken();

    t.id = id;
    t.name = name;
    t.user = user;
    t.key = AccessTokenServiceImpl.generateKey(AccessToken.KEY_LENGTH);
    t.scope = Arrays.stream(Scope.values()).map(Scope::scope).collect(Collectors.joining(","));

    return t;
  }
}
