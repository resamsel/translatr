package dto.errors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import play.libs.Json;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstraintViolation implements Serializable {
  private static final long serialVersionUID = -5843326601541262360L;

  public String message;
  public String field;
  public Object invalidValue;

  public ConstraintViolation(javax.validation.ConstraintViolation<?> violation) {
    this.message = MessageFormat.format(violation.getMessage(),
        violation.getPropertyPath(), violation.getInvalidValue());
    this.field = violation.getPropertyPath().toString();

    Object iv = violation.getInvalidValue();
    if (iv != null)
      this.invalidValue = Json.parse(MessageFormat.format("'{'\"{0}\": {1}'}'",
          iv.getClass().getSimpleName().toLowerCase(), Json.toJson(iv)));
  }

  public ConstraintViolation(JsonMappingException e) {
    this.message = e.getMessage();
    if (e instanceof InvalidFormatException) {
      InvalidFormatException invalidFormat = (InvalidFormatException) e;
      this.invalidValue = invalidFormat.getValue();
      this.message = invalidFormat.getOriginalMessage();
      List<JsonMappingException.Reference> path = invalidFormat.getPath();
      if (!path.isEmpty()) {
        this.field = path.get(path.size() - 1).getFieldName();
      } else {
        this.field = invalidFormat.getPathReference();
      }
    }
  }

  public ConstraintViolation(Exception e) {
    this.message = e.getMessage();
  }
}
