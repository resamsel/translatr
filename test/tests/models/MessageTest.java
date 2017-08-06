package tests.models;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.UUID;
import models.Message;
import org.junit.Test;

/**
 * @author resamsel
 * @version 18 May 2017
 */
public class MessageTest {

  @Test
  public void getCacheKey() {
    UUID messageId = UUID.randomUUID();

    assertThat(Message.getCacheKey(null)).isNull();
    assertThat(Message.getCacheKey(messageId)).isEqualTo("message:id:" + messageId);
    assertThat(Message.getCacheKey(messageId, "keys"))
        .isEqualTo("message:id:" + messageId + ":keys");
    assertThat(Message.getCacheKey(messageId, "keys", "locales"))
        .isEqualTo("message:id:" + messageId + ":keys:locales");
  }
}
