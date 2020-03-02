package validators;

import play.data.Form;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FeatureFlagByUserAndFeatureValidator.class)
@Form.Display(name = "constraint.featureflagbyuserandfeature")
public @interface FeatureFlagByUserAndFeature {
  String message() default FeatureFlagByUserAndFeatureValidator.MESSAGE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
