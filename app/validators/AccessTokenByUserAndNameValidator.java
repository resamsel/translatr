package validators;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import models.User;
import play.data.validation.Constraints;
import play.libs.F.Tuple;
import repositories.AccessTokenRepository;

/**
 * @author resamsel
 * @version 6 Oct 2016
 */
public class AccessTokenByUserAndNameValidator extends Constraints.Validator<Object>
    implements ConstraintValidator<AccessTokenByUserAndName, Object> {

  public static final String MESSAGE = "error.accesstokenbyuserandname";

  private final AccessTokenRepository accessTokenRepository;

  @Inject
  public AccessTokenByUserAndNameValidator(AccessTokenRepository accessTokenRepository) {
    this.accessTokenRepository = accessTokenRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(AccessTokenByUserAndName constraintAnnotation) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object object) {
    return object != null && object instanceof String
        && accessTokenRepository.byUserAndName(User.loggedInUserId(), (String) object) == null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {
    return null;
  }
}
