package validators;

import play.inject.Injector;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author resamsel
 * @version 6 Oct 2016
 */
public class NameUniqueValidator implements ConstraintValidator<NameUnique, Object> {

  public static final String MESSAGE = "error.nameunique";

  private final Injector injector;

  private NameUniqueChecker checker;
  private NameUnique constraintAnnotation;

  @Inject
  public NameUniqueValidator(Injector injector) {
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(NameUnique constraintAnnotation) {
    this.constraintAnnotation = constraintAnnotation;
    this.checker = injector.instanceOf(constraintAnnotation.checker());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
    if (object != null && checker.isValid(object)) {
      return true;
    }

    constraintContext.disableDefaultConstraintViolation();
    constraintContext
        .buildConstraintViolationWithTemplate(constraintAnnotation.message())
        .addPropertyNode(constraintAnnotation.field())
        .addConstraintViolation();

    return false;
  }
}
