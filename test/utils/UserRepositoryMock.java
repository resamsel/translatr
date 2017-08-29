package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
        new User()
            .withId(UUID.randomUUID())
            .withName("John Smith")
            .withEmail("johnsmith@google.com")
            .withUsername("johnsmith")
            .withWhenCreated(DateTime.now()));
    REPOSITORY.put(
        "janedoe",
        new User()
            .withId(UUID.randomUUID())
            .withName("Jane Doe")
            .withEmail("janedoe@google.com")
            .withUsername("janedoe")
            .withWhenCreated(DateTime.now()));
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
    m.linkedAccounts = new ArrayList<>();
    m.activities = new ArrayList<>();
    m.memberships = new ArrayList<>();
    m.projects = new ArrayList<>();

    return m;
  }
}
