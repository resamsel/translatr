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
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProjectNameValidator.class)
@Form.Display(name = ProjectNameValidator.MESSAGE, attributes = {"excluded"})
public @interface ProjectName {

  String message() default ProjectNameValidator.MESSAGE;

  Class<?>[] groups() default {};

  String excluded() default "projects, activity, linkedAccounts, accessTokens, create, edit, settings";

  Class<? extends Payload>[] payload() default {};
}
