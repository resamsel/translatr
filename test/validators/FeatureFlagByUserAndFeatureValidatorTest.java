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
    UserFeatureFlag featureFlag = new UserFeatureFlag();
    featureFlag.feature = Feature.ProjectCliCard;
    featureFlag.user = new User().withId(UUID.randomUUID());

    // when
    boolean actual = target.isValid(featureFlag);

    // then
    assertThat(actual).isTrue();
  }

  @Test
  public void isValidWithExistingFeatureFlag() {
    // given
    UserFeatureFlag featureFlag = new UserFeatureFlag();
    featureFlag.feature = Feature.ProjectCliCard;
    featureFlag.user = new User().withId(UUID.randomUUID());
    UserFeatureFlag existing = new UserFeatureFlag();
    existing.id = UUID.randomUUID();
    existing.feature = Feature.ProjectCliCard;
    existing.user = new User().withId(featureFlag.user.id);

    when(userFeatureFlagRepository.byUserIdAndFeature(any(), any())).thenReturn(existing);

    // when
    boolean actual = target.isValid(featureFlag);

    // then
    assertThat(actual).isFalse();
  }

  @Test
  public void isValidWithExistingFeatureFlagUpdate() {
    // given
    UserFeatureFlag featureFlag = new UserFeatureFlag();
    featureFlag.id = UUID.randomUUID();
    featureFlag.feature = Feature.ProjectCliCard;
    featureFlag.user = new User().withId(UUID.randomUUID());
    UserFeatureFlag existing = new UserFeatureFlag();
    existing.id = featureFlag.id;
    existing.feature = Feature.ProjectCliCard;
    existing.user = new User().withId(featureFlag.user.id);

    when(userFeatureFlagRepository.byUserIdAndFeature(any(), any())).thenReturn(existing);

    // when
    boolean actual = target.isValid(featureFlag);

    // then
    assertThat(actual).isTrue();
  }
}
