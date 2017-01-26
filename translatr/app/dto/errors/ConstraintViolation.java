package dto.errors;

import java.text.MessageFormat;

import javax.validation.ValidationException;

import com.fasterxml.jackson.annotation.JsonInclude;

import play.api.Play;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.mvc.Http.Context;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstraintViolation {
  public String message;
  public String field;
  public Object invalidValue;

  /**
   * @param violation
   */
  public ConstraintViolation(javax.validation.ConstraintViolation<?> violation) {
    this.message = Play.current().injector().instanceOf(MessagesApi.class)
        .get(Context.current().lang(), violation.getMessage());
    this.field = violation.getPropertyPath().toString();

    Object iv = violation.getInvalidValue();
    if (iv != null)
      this.invalidValue = Json.parse(MessageFormat.format("'{'\"{0}\": {1}'}'",
          iv.getClass().getSimpleName().toLowerCase(), iv));
  }

  /**
   * 
   */
  public ConstraintViolation(ValidationException e) {
    this.message = e.getMessage();
  }
}
