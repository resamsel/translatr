package tests.models;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.UUID;
import models.Key;
import org.junit.Test;

/**
 * @author resamsel
 * @version 18 May 2017
 */
public class KeyTest {
  @Test
  public void getCacheKey() {
    UUID keyId = UUID.randomUUID();

    assertThat(Key.getCacheKey(null)).isNull();
    assertThat(Key.getCacheKey(keyId)).isEqualTo("key:" + keyId);
    assertThat(Key.getCacheKey(keyId, "project")).isEqualTo("key:" + keyId + ":project");
    assertThat(Key.getCacheKey(keyId, "project", "messages"))
        .isEqualTo("key:" + keyId + ":project:messages");
  }
}
