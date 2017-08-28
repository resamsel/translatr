package validators;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import play.inject.Injector;

/**
 * @author resamsel
 * @version 6 Oct 2016
 */
@Singleton
public class NameUniqueValidator implements ConstraintValidator<NameUnique, Object> {

  public static final String MESSAGE = "error.nameunique";

  private final Injector injector;

  private NameUniqueChecker checker;

  @Inject
  public NameUniqueValidator(Injector injector) {
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(NameUnique constraintAnnotation) {
    this.checker = injector.instanceOf(constraintAnnotation.checker());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
    return object != null && checker.isValid(object);
  }
}
