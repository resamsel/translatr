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
 * <p>
 *
 * @author resamsel
 * @version 6 Oct 2016
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserByUsernameValidator.class)
@Form.Display(name = "constraint.userbyusername")
public @interface UserByUsername
{
	String message() default UserByUsernameValidator.MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
