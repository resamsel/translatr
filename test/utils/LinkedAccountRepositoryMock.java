package utils;

import models.LinkedAccount;
import models.User;

public class LinkedAccountRepositoryMock {

  public static LinkedAccount createLinkedAccount(LinkedAccount t, String providerKey, String providerUserId) {
    return createLinkedAccount(t.id, t.user, providerKey, providerUserId);
  }

  public static LinkedAccount createLinkedAccount(Long id, User user, String providerKey,
      String providerUserId) {
    LinkedAccount t = new LinkedAccount();

    t.id = id;
    t.user = user;
    t.providerKey = providerKey;
    t.providerUserId = providerUserId;

    return t;
  }
}
