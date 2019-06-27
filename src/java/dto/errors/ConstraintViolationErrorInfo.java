package dto.errors;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author resamsel
 * @version 18 Jan 2017
 */
public class ConstraintViolationErrorInfo extends GenericErrorInfo {
  private static final long serialVersionUID = -8421065080228995362L;

  public List<ConstraintViolation> violations;

  /**
   * @param violation
   */
  public ConstraintViolationErrorInfo(javax.validation.ConstraintViolation<?> violation) {
    this(Collections.singletonList(violation));
  }

  /**
   * @param violations
   */
  public ConstraintViolationErrorInfo(
      Collection<javax.validation.ConstraintViolation<?>> violations) {
    super("ConstraintViolationException", "Constraint violations detected");

    if (violations != null)
      this.violations =
          violations.stream().map(ConstraintViolation::new).collect(Collectors.toList());
    else
      this.violations = new ArrayList<>();
  }

  public ConstraintViolationErrorInfo(ValidationException e) {
    super("ConstraintViolationException", "Constraint violations detected");

    if (e != null)
      this.violations = Collections.singletonList(new ConstraintViolation(e));
  }
}
