package dto.errors;

import com.fasterxml.jackson.annotation.JsonInclude;
import play.libs.Json;

import javax.validation.ValidationException;
import java.io.Serializable;
import java.text.MessageFormat;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstraintViolation implements Serializable {
  private static final long serialVersionUID = -5843326601541262360L;

  public String message;
  public String field;
  public Object invalidValue;

  /**
   * @param violation
   */
  public ConstraintViolation(javax.validation.ConstraintViolation<?> violation) {
    this.message = MessageFormat.format(violation.getMessage(),
        violation.getPropertyPath(), violation.getInvalidValue());
    this.field = violation.getPropertyPath().toString();

    Object iv = violation.getInvalidValue();
    if (iv != null)
      this.invalidValue = Json.parse(MessageFormat.format("'{'\"{0}\": {1}'}'",
          iv.getClass().getSimpleName().toLowerCase(), Json.toJson(iv)));
  }

  /**
   *
   */
  public ConstraintViolation(ValidationException e) {
    this.message = e.getMessage();
  }
}
