package validators;

import play.data.Form;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameValidator.class)
@Form.Display(name = UsernameValidator.MESSAGE, attributes = {"excluded"})
public @interface Username {

  String message() default UsernameValidator.MESSAGE;

  Class<?>[] groups() default {};

  String excluded() default "projects, users, profile, activity, login, logout, authenticate, command, api, javascriptRoutes, assets";

  Class<? extends Payload>[] payload() default {};
}
