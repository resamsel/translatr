package models;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.UUID;
import models.User;
import org.junit.Test;

/**
 * @author resamsel
 * @version 18 May 2017
 */
public class UserTest {
  @Test
  public void getCacheKey() {
    UUID userId = UUID.randomUUID();

    assertThat(User.getCacheKey((UUID) null)).isNull();
    assertThat(User.getCacheKey((String) null)).isNull();
    assertThat(User.getCacheKey(userId)).isEqualTo("user:id:" + userId);
    assertThat(User.getCacheKey(userId, "linkedAccounts"))
        .isEqualTo("user:id:" + userId + ":linkedAccounts");
    assertThat(User.getCacheKey(userId, "linkedAccounts", "projects"))
        .isEqualTo("user:id:" + userId + ":linkedAccounts:projects");
  }
}
