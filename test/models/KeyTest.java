package models;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(Key.getCacheKey(keyId)).isEqualTo("key:id:" + keyId);
    assertThat(Key.getCacheKey(keyId, "project"))
        .isEqualTo("key:id:" + keyId + ":project");
    assertThat(Key.getCacheKey(keyId, "project", "messages"))
        .isEqualTo("key:id:" + keyId + ":project:messages");
  }
}
