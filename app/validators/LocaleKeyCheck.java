package validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import play.data.Form;

/**
 *
 * @author resamsel
 * @version 6 Oct 2016
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LocaleKeyValidator.class)
@Form.Display(name = "constraint.localekeyinvalid")
public @interface LocaleKeyCheck {
  String message() default LocaleKeyValidator.MESSAGE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
