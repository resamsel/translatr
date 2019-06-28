package validators;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author resamsel
 * @version 6 Oct 2016
 */
public class UsernameValidator implements ConstraintValidator<Username, Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UsernameValidator.class);

  public static final String MESSAGE = "error.username.invalid";
  private List<String> excluded;

  @Override
  public void initialize(Username constraintAnnotation) {
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
