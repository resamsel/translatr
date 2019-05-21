package mappers;

import dto.LinkedAccount;
import models.User;

public class LinkedAccountMapper {
  public static models.LinkedAccount toModel(LinkedAccount in)
  {
    models.LinkedAccount out = new models.LinkedAccount();

    out.user = new User().withId(in.userId);
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.providerKey = in.providerKey;
    out.providerUserId = in.providerUserId;

    return out;
  }

  public static LinkedAccount toDto(models.LinkedAccount in)
  {
    LinkedAccount out = new LinkedAccount();

    out.userId = in.user.id;
    out.providerKey = in.providerKey;
    out.providerUserId = in.providerUserId;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;

    return out;
  }
}
