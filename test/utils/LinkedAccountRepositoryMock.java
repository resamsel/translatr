package utils;

import models.LinkedAccount;
import models.User;

public class LinkedAccountRepositoryMock {

  public static LinkedAccount createLinkedAccount(LinkedAccount t, String providerKey) {
    return createLinkedAccount(t.id, t.user, providerKey);
  }

  public static LinkedAccount createLinkedAccount(Long id, User user, String providerKey) {
    LinkedAccount t = new LinkedAccount();

    t.id = id;
    t.user = user;
    t.providerKey = providerKey;

    return t;
  }
}
