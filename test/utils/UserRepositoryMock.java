package utils;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static utils.LinkedAccountRepositoryMock.createLinkedAccount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import models.User;
import org.joda.time.DateTime;

/**
 * Created by resamsel on 24/07/2017.
 */
public class UserRepositoryMock {

  private static final Map<String, User> REPOSITORY = new HashMap<>();

  static {
    REPOSITORY.put(
        "johnsmith",
        createUser(randomUUID(), "John Smith", "johnsmith", "johnsmith@google.com")
    );
    REPOSITORY.put(
        "janedoe",
        createUser(randomUUID(), "Jane Doe", "janedoe", "janedoe@google.com")
    );
  }

  public static User byUsername(String username) {
    return REPOSITORY.get(username);
  }

  public static User createUser(User user, String name, String username, String email) {
    return createUser(user.id, name, username, email);
  }

  public static User createUser(UUID id, String name, String username, String email) {
    User m = new User();

    m.id = id;
    m.name = name;
    m.username = username;
    m.email = email;
    m.active = true;
    m.linkedAccounts = new ArrayList<>(singletonList(createLinkedAccount(
        ThreadLocalRandom.current().nextLong(),
        m,
        "google",
        randomUUID().toString()
    )));
    m.activities = new ArrayList<>();
    m.memberships = new ArrayList<>();
    m.projects = new ArrayList<>();
    m.whenCreated = DateTime.now();
    m.whenUpdated = DateTime.now();

    return m;
  }
}
