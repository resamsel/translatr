package validators;

import models.Feature;
import models.User;
import models.UserFeatureFlag;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import repositories.UserFeatureFlagRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class FeatureFlagByUserAndFeatureValidatorTest {

  @Mock
  private UserFeatureFlagRepository userFeatureFlagRepository;

  private FeatureFlagByUserAndFeatureValidator target;

  @Before
  public void setUp() {
    target = new FeatureFlagByUserAndFeatureValidator(userFeatureFlagRepository);
  }

  @Test
  public void isValidWithNewFeatureFlag() {
    // given
    UserFeatureFlag featureFlag = UserFeatureFlag.of(new User().withId(UUID.randomUUID()), Feature.ProjectCliCard, false);

    // when
    boolean actual = target.isValid(featureFlag);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  public void isValidWithExistingFeatureFlag() {
    // given
    UserFeatureFlag featureFlag = UserFeatureFlag.of(new User().withId(UUID.randomUUID()), Feature.ProjectCliCard, false);
    UserFeatureFlag existing = UserFeatureFlag.of(new User().withId(featureFlag.user.id), Feature.ProjectCliCard, false);
    existing.id = UUID.randomUUID();

    when(userFeatureFlagRepository.byUserIdAndFeature(any(), any())).thenReturn(existing);

    // when
    boolean actual = target.isValid(featureFlag);

    // then
    assertThat(actual).isFalse();
  }

  @Test
  public void isValidWithExistingFeatureFlagUpdate() {
    // given
    UserFeatureFlag featureFlag = UserFeatureFlag.of(UUID.randomUUID(), new User().withId(UUID.randomUUID()), Feature.ProjectCliCard, false);
    UserFeatureFlag existing = UserFeatureFlag.of(featureFlag.id, new User().withId(featureFlag.user.id), Feature.ProjectCliCard, false);

    when(userFeatureFlagRepository.byUserIdAndFeature(any(), any())).thenReturn(existing);

    // when
    boolean actual = target.isValid(featureFlag);

    // then
    assertThat(actual).isTrue();
  }
}
