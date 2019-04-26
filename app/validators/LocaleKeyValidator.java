package validators;

import models.Message;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author resamsel
 * @version 6 Oct 2016
 */
public class LocaleKeyValidator implements ConstraintValidator<LocaleKeyCheck, Message> {

  private static final String LOCALE_NOT_FOUND_MESSAGE = "Locale not found";
  private static final String KEY_NOT_FOUND_MESSAGE = "Key not found";
  private static final String PROJECT_MISMATCH_MESSAGE = "Projects do not match (%s != %s)";

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(LocaleKeyCheck constraintAnnotation) {
  }

  @Override
  public boolean isValid(Message message, ConstraintValidatorContext context) {
    if (message == null) {
      context.buildConstraintViolationWithTemplate("Value required")
          .addConstraintViolation();
      return false;
    }

    if (message.locale == null) {
      context.buildConstraintViolationWithTemplate(LOCALE_NOT_FOUND_MESSAGE)
          .addPropertyNode("locale")
          .addConstraintViolation();
      return false;
    }

    if (message.key == null) {
      context.buildConstraintViolationWithTemplate(KEY_NOT_FOUND_MESSAGE)
          .addPropertyNode("locale")
          .addConstraintViolation();
      return false;
    }

    if (!message.locale.project.equals(message.key.project)) {
      context.buildConstraintViolationWithTemplate(
          String.format(PROJECT_MISMATCH_MESSAGE, message.locale.project.id, message.key.project.id))
          .addPropertyNode("locale")
          .addPropertyNode("project")
          .addConstraintViolation()
          .buildConstraintViolationWithTemplate(
              String.format(PROJECT_MISMATCH_MESSAGE, message.locale.project.id, message.key.project.id))
          .addPropertyNode("key")
          .addPropertyNode("project")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
