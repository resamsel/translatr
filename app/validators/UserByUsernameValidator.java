package validators;

import javax.validation.ConstraintValidator;
import play.api.Play;
import play.data.validation.Constraints;
import play.libs.F.Tuple;
import services.UserService;

/**
 *
 * @author resamsel
 * @version 6 Oct 2016
 */
public class UserByUsernameValidator extends Constraints.Validator<Object>
    implements ConstraintValidator<UserByUsername, Object> {
  public static final String MESSAGE = "error.userbyusername";

  private UserService userService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(UserByUsername constraintAnnotation) {
    this.userService = Play.current().injector().instanceOf(UserService.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object object) {
    if (object == null)
      return false;

    if (!(object instanceof String))
      return false;

    return userService.byUsername((String) object) != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {
    return null;
  }
}
