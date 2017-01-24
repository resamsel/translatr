package validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import play.data.Form;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NameUniqueValidator.class)
@Form.Display(name = "constraint.nameunique")
public @interface NameUnique {
  String message() default NameUniqueValidator.MESSAGE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  Class<? extends NameUniqueChecker> checker();
}
