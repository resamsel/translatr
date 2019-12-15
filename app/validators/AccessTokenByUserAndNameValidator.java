package validators;

import play.data.validation.Constraints;
import play.libs.F.Tuple;
import repositories.AccessTokenRepository;
import services.AuthProvider;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;

/**
 * @author resamsel
 * @version 6 Oct 2016
 */
public class AccessTokenByUserAndNameValidator extends Constraints.Validator<Object>
    implements ConstraintValidator<AccessTokenByUserAndName, Object> {

  public static final String MESSAGE = "error.accesstokenbyuserandname";

  private final AccessTokenRepository accessTokenRepository;
  private final AuthProvider authProvider;

  @Inject
  public AccessTokenByUserAndNameValidator(AccessTokenRepository accessTokenRepository,
                                           AuthProvider authProvider) {
    this.accessTokenRepository = accessTokenRepository;
    this.authProvider = authProvider;
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
        && accessTokenRepository.byUserAndName(authProvider.loggedInUserId(), (String) object) == null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {
    return null;
  }
}
