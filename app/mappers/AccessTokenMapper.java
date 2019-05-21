package mappers;

import dto.AccessToken;
import models.User;

public class AccessTokenMapper {
  public static models.AccessToken toModel(dto.AccessToken in) {
    if (in == null) {
      return null;
    }

    models.AccessToken out = new models.AccessToken();

    out.id = in.id;
    out.user = new User().withId(in.userId);
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.name = in.name;
    out.key = in.key;
    out.scope = in.scope;

    return out;
  }

  public static AccessToken toDto(models.AccessToken in) {
    AccessToken out = new AccessToken();

    out.id = in.id;
    out.userId = in.user.id;
    out.userName = in.user.name;
    out.name = in.name;
    out.key = in.key;
    out.scope = in.scope;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;

    return out;
  }
}
