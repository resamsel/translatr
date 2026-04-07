package utils;

import com.translatr.util.UuidUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JsonUtilsTest {

    @Test
    void getUuid() {
        assertThat(UuidUtils.getUuid(null)).isNull();
        assertThat(UuidUtils.getUuid("")).isNull();
        assertThat(UuidUtils.getUuid("   ")).isNull();
        assertThat(UuidUtils.getUuid("123456789")).isNull();
        UUID random = UUID.randomUUID();
        assertThat(UuidUtils.getUuid(random.toString())).isEqualTo(random);
    }

    @Test
    void getUuidsWithNull() {
        assertThat(UuidUtils.getUuids(null)).isNull();
    }

    @Test
    void getUuidsWithEmptyString() {
        assertThat(UuidUtils.getUuids("")).isNull();
    }

    @Test
    void getUuidsWithBlankString() {
        assertThat(UuidUtils.getUuids("   ")).isNull();
    }

    @Test
    void getUuidsWithInvalidUuid() {
        List<UUID> actual = UuidUtils.getUuids("invalid");
        assertThat(actual).isEmpty();
    }

    @Test
    void getUuidsWithValidUuid() {
        UUID uuid = UUID.randomUUID();
        assertThat(UuidUtils.getUuids(uuid.toString()))
                .hasSize(1)
                .contains(uuid);
    }

    @Test
    void getUuidsWithValidAndInvalidUuids() {
        UUID uuid = UUID.randomUUID();
        assertThat(UuidUtils.getUuids(uuid + ",invalid"))
                .hasSize(1)
                .contains(uuid);
    }

    @Test
    void getUuidsWithMultipleValidUuids() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        assertThat(UuidUtils.getUuids(uuid1 + "," + uuid2))
                .hasSize(2)
                .contains(uuid1, uuid2);
    }
}
