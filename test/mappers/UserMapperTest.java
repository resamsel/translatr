package mappers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dto.User;
import models.Feature;
import models.UserFeatureFlag;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

  @Test
  public void mapFeaturesToDtoWithNull() {
    // given
    List<UserFeatureFlag> features = null;

    // when
    Map<String, Boolean> actual = UserMapper.mapFeaturesToDto(features);

    // then
    assertThat(actual).containsOnly(
            entry(Feature.ProjectCliCard.getName(), true),
            entry(Feature.HeaderGraphic.getName(), true),
            entry(Feature.LanguageSwitcher.getName(), true)
    );
  }

  @Test
  public void mapFeaturesToDtoWithEmptyList() {
    // given
    List<UserFeatureFlag> features = Collections.emptyList();

    // when
    Map<String, Boolean> actual = UserMapper.mapFeaturesToDto(features);

    // then
    assertThat(actual).containsOnly(
            entry(Feature.ProjectCliCard.getName(), true),
            entry(Feature.HeaderGraphic.getName(), true),
            entry(Feature.LanguageSwitcher.getName(), true)
    );
  }

  @Test
  public void mapFeaturesToDtoWithSingleFeature() {
    // given
    List<UserFeatureFlag> features = ImmutableList.of(UserFeatureFlag.of(null, Feature.ProjectCliCard, false));

    // when
    Map<String, Boolean> actual = UserMapper.mapFeaturesToDto(features);

    // then
    assertThat(actual).containsOnly(
            entry(Feature.ProjectCliCard.getName(), false),
            entry(Feature.HeaderGraphic.getName(), true),
            entry(Feature.LanguageSwitcher.getName(), true)
    );
  }

  @Test
  public void mapFeaturesToDtoWithAllFeatures() {
    // given
    List<UserFeatureFlag> features = ImmutableList.of(
            UserFeatureFlag.of(null, Feature.ProjectCliCard, false),
            UserFeatureFlag.of(null, Feature.HeaderGraphic, false),
            UserFeatureFlag.of(null, Feature.LanguageSwitcher, false)
    );

    // when
    Map<String, Boolean> actual = UserMapper.mapFeaturesToDto(features);

    // then
    assertThat(actual).containsOnly(
            entry(Feature.ProjectCliCard.getName(), false),
            entry(Feature.HeaderGraphic.getName(), false),
            entry(Feature.LanguageSwitcher.getName(), false)
    );
  }
}
