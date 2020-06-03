package mappers;

import com.google.common.collect.ImmutableMap;
import dto.User;
import org.junit.Test;

import static assertions.CustomAssertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class UserMapperTest {

  @Test
  public void toModelWithEmptySettings() {
    // given
    User dto = new User();
    models.User model = new models.User();

    // when
    models.User actual = UserMapper.toModel(dto, model);

    // then
    assertThat(actual).settingsIsEqualTo(null);
  }

  @Test
  public void toModelWithSingleSetting() {
    // given
    User dto = new User();
    dto.settings = ImmutableMap.of("a", "A");
    models.User model = new models.User();

    // when
    models.User actual = UserMapper.toModel(dto, model);

    // then
    assertThat(actual).settingsContains(entry("a", "A"));
  }

  @Test
  public void toModelWithMultipleSettings() {
    // given
    User dto = new User();
    dto.settings = ImmutableMap.of(
            "a", "A",
            "b", "B",
            "c", "C"
    );
    models.User model = new models.User();

    // when
    models.User actual = UserMapper.toModel(dto, model);

    // then
    assertThat(actual).settingsContains(entry("a", "A"), entry("b", "B"), entry("c", "C"));
  }

  @Test
  public void toDtoWithEmptySettings() {
    // given
    models.User in = new models.User();

    // when
    User actual = UserMapper.toDto(in);

    // then
    assertThat(actual).settingsIsEqualTo(null);
  }

  @Test
  public void toDtoWithSingleSetting() {
    // given
    models.User in = new models.User();
    in.settings = ImmutableMap.of("a", "A");

    // when
    User actual = UserMapper.toDto(in);

    // then
    assertThat(actual).settingsContains(entry("a", "A"));
  }

  @Test
  public void toDtoWithMultipleSettings() {
    // given
    models.User in = new models.User();
    in.settings = ImmutableMap.of(
            "a", "A",
            "b", "B",
            "c", "C"
    );

    // when
    User actual = UserMapper.toDto(in);

    // then
    assertThat(actual).settingsContains(entry("a", "A"), entry("b", "B"), entry("c", "C"));
  }
}
