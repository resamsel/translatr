package validators;

import play.data.Form;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author resamsel
 * @version 6 Oct 2016
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AccessTokenByUserAndNameValidator.class)
@Form.Display(name = "constraint.accesstokenbyuserandname")
public @interface AccessTokenByUserAndName
{
	String message() default AccessTokenByUserAndNameValidator.MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
