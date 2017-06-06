package validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author resamsel
 * @version 6 Oct 2016
 */
public class NameUniqueValidator implements ConstraintValidator<NameUnique, Object> {
  private static final Logger LOGGER = LoggerFactory.getLogger(NameUniqueValidator.class);

  public static final String MESSAGE = "error.nameunique";

  private NameUniqueChecker checker;

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(NameUnique constraintAnnotation) {
    try {
      this.checker = constraintAnnotation.checker().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      LOGGER.error("Error while creating checker instance", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
    if (object == null)
      return false;

    return checker.isValid(object);
  }
}
