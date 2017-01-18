package dto;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import com.fasterxml.jackson.annotation.JsonInclude;

import dto.Error.ErrorInfo;

/**
 * @author resamsel
 * @version 18 Jan 2017
 */
public class ConstraintViolationExceptionInfo extends ErrorInfo {
  public List<Violation> violations;

  /**
   * @param violation
   */
  public ConstraintViolationExceptionInfo(ConstraintViolation<?> violation) {
    this(Arrays.asList(violation));
  }

  /**
   * @param violations
   */
  public ConstraintViolationExceptionInfo(Collection<ConstraintViolation<?>> violations) {
    super("ConstraintViolationException", "Constraint violations detected");

    this.violations = violations.stream().map(v -> new Violation(v)).collect(Collectors.toList());
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  static class Violation {
    public String message;
    public String field;
    public String invalidValue;

    /**
     * @param violation
     */
    public Violation(ConstraintViolation<?> violation) {
      this.message = violation.getMessage();
      this.field = violation.getPropertyPath().toString();

      if (violation.getInvalidValue() != null)
        this.invalidValue = String.valueOf(violation.getInvalidValue());
    }
  }
}
