package utils;

import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author resamsel
 * @version 27 May 2017
 */
public class JsonUtilsTest {
  @Test
  public void getUuid() {
    assertThat(JsonUtils.getUuid(null)).isNull();
    assertThat(JsonUtils.getUuid("")).isNull();
    assertThat(JsonUtils.getUuid("   ")).isNull();
    assertThat(JsonUtils.getUuid("123456789")).isNull();
    UUID random = UUID.randomUUID();
    assertThat(JsonUtils.getUuid(random.toString())).isEqualTo(random);
  }

  @Test
  public void getUuidsWithNull() {
    // given
    String uuids = null;

    // when
    //noinspection ConstantConditions
    List<UUID> actual = JsonUtils.getUuids(uuids);

    // then
    assertThat(actual).isNull();
  }

  @Test
  public void getUuidsWithEmptyString() {
    // given
    String uuids = "";

    // when
    List<UUID> actual = JsonUtils.getUuids(uuids);

    // then
    assertThat(actual).isNull();
  }

  @Test
  public void getUuidsWithBlankString() {
    // given
    String uuids = "   ";

    // when
    List<UUID> actual = JsonUtils.getUuids(uuids);

    // then
    assertThat(actual).isNull();
  }

  @Test
  public void getUuidsWithInvalidUuid() {
    // given
    String uuids = "invalid";

    // when
    List<UUID> actual = JsonUtils.getUuids(uuids);

    // then
    assertThat(actual).isEmpty();
  }

  @Test
  public void getUuidsWithValidUuid() {
    // given
    UUID uuid = UUID.randomUUID();
    String uuids = uuid.toString();

    // when
    List<UUID> actual = JsonUtils.getUuids(uuids);

    // then
    assertThat(actual)
        .hasSize(1)
        .contains(uuid);
  }

  @Test
  public void getUuidsWithValidAndInvalidUuids() {
    // given
    UUID uuid = UUID.randomUUID();
    String uuids = uuid.toString() + ",invalid";

    // when
    List<UUID> actual = JsonUtils.getUuids(uuids);

    // then
    assertThat(actual)
        .hasSize(1)
        .contains(uuid);
  }

  @Test
  public void getUuidsWithMultipleValidUuids() {
    // given
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    String uuids = uuid1.toString() + "," + uuid2.toString();

    // when
    List<UUID> actual = JsonUtils.getUuids(uuids);

    // then
    assertThat(actual)
        .hasSize(2)
        .contains(uuid1)
        .contains(uuid2);
  }
}
