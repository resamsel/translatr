package validators;

import javax.validation.ConstraintValidator;
import models.Message;
import play.data.validation.Constraints;
import play.libs.F.Tuple;

/**
 * @author resamsel
 * @version 6 Oct 2016
 */
public class LocaleKeyValidator extends Constraints.Validator<Message>
    implements ConstraintValidator<LocaleKeyCheck, Message> {

  public static final String MESSAGE = "error.localekeyinvalid";

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(LocaleKeyCheck constraintAnnotation) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Message message) {
    return message != null && message.locale != null && message.key != null
        && message.locale.project.equals(message.key.project);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {
    return null;
  }

}
