package validators;

import models.Model;
import play.mvc.Http;

public interface CustomValidator<T extends Model<T, ?>> {
  boolean isValid(T model, Http.Request request);
}
