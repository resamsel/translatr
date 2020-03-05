package validators;

import models.UserFeatureFlag;
import play.data.validation.Constraints;
import play.libs.F.Tuple;
import repositories.UserFeatureFlagRepository;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import java.util.Objects;

public class FeatureFlagByUserAndFeatureValidator extends Constraints.Validator<Object>
    implements ConstraintValidator<FeatureFlagByUserAndFeature, Object> {

  public static final String MESSAGE = "error.featureflagbyuserandfeature";

  private final UserFeatureFlagRepository userFeatureFlagRepository;

  @Inject
  public FeatureFlagByUserAndFeatureValidator(UserFeatureFlagRepository userFeatureFlagRepository) {
    this.userFeatureFlagRepository = userFeatureFlagRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(FeatureFlagByUserAndFeature constraintAnnotation) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object object) {
    if (!(object instanceof UserFeatureFlag)) {
      return true;
    }

    UserFeatureFlag featureFlag = (UserFeatureFlag) object;

    UserFeatureFlag existing = userFeatureFlagRepository.byUserIdAndFeature(featureFlag.user.id, featureFlag.feature);

    if (featureFlag.id == null && existing == null) {
      // given feature flag doesn't have an ID, and combination user/feature doesn't exist: OK
      return true;
    }

    // given feature flag is OK if it is the same as the one in the database
    return existing != null && Objects.equals(existing.id, featureFlag.id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {
    return null;
  }
}
