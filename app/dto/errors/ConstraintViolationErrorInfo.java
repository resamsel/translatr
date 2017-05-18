package dto.errors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

/**
 * @author resamsel
 * @version 18 Jan 2017
 */
public class ConstraintViolationErrorInfo extends GenericErrorInfo {
  public List<ConstraintViolation> violations;

  /**
   * @param violation
   */
  public ConstraintViolationErrorInfo(javax.validation.ConstraintViolation<?> violation) {
    this(Arrays.asList(violation));
  }

  /**
   * @param violations
   */
  public ConstraintViolationErrorInfo(
      Collection<javax.validation.ConstraintViolation<?>> violations) {
    super("ConstraintViolationException", "Constraint violations detected");

    if (violations != null)
      this.violations =
          violations.stream().map(v -> new ConstraintViolation(v)).collect(Collectors.toList());
    else
      this.violations = new ArrayList<>();
  }

  public ConstraintViolationErrorInfo(ValidationException e) {
    super("ConstraintViolationException", "Constraint violations detected");

    if (e != null)
      this.violations = Arrays.asList(new ConstraintViolation(e));
  }
}
