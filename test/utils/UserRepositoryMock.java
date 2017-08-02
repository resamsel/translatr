package utils;

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
}
