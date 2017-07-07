package tests.utils;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;
import utils.JsonUtils;

/**
 * @author resamsel
 * @version 27 May 2017
 */
public class JsonUtilsTest {
  @Test
  public void getUuid() {
    assertThat(JsonUtils.getUuid((String) null)).isNull();
    assertThat(JsonUtils.getUuid("")).isNull();
    assertThat(JsonUtils.getUuid("   ")).isNull();
    assertThat(JsonUtils.getUuid("123456789")).isNull();
    UUID random = UUID.randomUUID();
    assertThat(JsonUtils.getUuid(random.toString())).isEqualTo(random);
  }
}
