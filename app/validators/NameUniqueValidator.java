package validators;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import play.i18n.Lang;
import play.i18n.MessagesApi;
import play.inject.Injector;
import play.mvc.Http;

/**
 * @author resamsel
 * @version 6 Oct 2016
 */
public class NameUniqueValidator implements ConstraintValidator<NameUnique, Object> {

  public static final String MESSAGE = "error.nameunique";

  private final Injector injector;

  private NameUniqueChecker checker;
  private NameUnique constraintAnnotation;
  private MessagesApi messagesApi;
  private Lang lang;

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
    this.messagesApi = injector.instanceOf(MessagesApi.class);
    this.lang = Http.Context.current().lang();
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
        .buildConstraintViolationWithTemplate(messagesApi.get(lang, constraintAnnotation.message()))
        .addPropertyNode(constraintAnnotation.field())
        .addConstraintViolation();

    return false;
  }
}
