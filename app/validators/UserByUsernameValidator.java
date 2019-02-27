package validators;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import play.data.validation.Constraints;
import play.libs.F.Tuple;
import services.UserService;

/**
 * @author resamsel
 * @version 6 Oct 2016
 */
public class UserByUsernameValidator extends Constraints.Validator<Object>
    implements ConstraintValidator<UserByUsername, Object> {

  public static final String MESSAGE = "error.userbyusername";

  private UserService userService;

  @Inject
  public UserByUsernameValidator(UserService userService) {
    this.userService = userService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(UserByUsername constraintAnnotation) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object object) {
    return object != null && object instanceof String
        && userService.byUsername((String) object) != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {
    return null;
  }
}
