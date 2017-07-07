package validators;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * @author resamsel
 * @version 6 Oct 2016
 */
public class ProjectNameValidator implements ConstraintValidator<ProjectName, Object> {

  public static final String MESSAGE = "error.projectname.invalid";

  private List<String> excluded;

  @Override
  public void initialize(ProjectName constraintAnnotation) {
    excluded = Arrays.asList(constraintAnnotation.excluded().split(",")).stream()
        .map(StringUtils::strip).collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object object, ConstraintValidatorContext constraintContext) {
    if (object == null) {
      return false;
    }

    return !excluded.contains(object);
  }
}
