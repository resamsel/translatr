package utils;

import java.util.Arrays;
import java.util.stream.Collectors;
import models.AccessToken;
import models.Scope;
import models.User;
import repositories.impl.AccessTokenRepositoryImpl;

public class AccessTokenRepositoryMock {

  public static AccessToken createAccessToken(AccessToken accessToken, String name) {
    return createAccessToken(accessToken.id, accessToken.user, name);
  }

  public static AccessToken createAccessToken(Long id, User user, String name) {
    AccessToken t = new AccessToken();

    t.id = id;
    t.name = name;
    t.user = user;
    t.key = AccessTokenRepositoryImpl.generateKey(AccessToken.KEY_LENGTH);
    t.scope = Arrays.stream(Scope.values()).map(Scope::scope).collect(Collectors.joining(","));

    return t;
  }
}
