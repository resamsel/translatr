package dto.errors;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericErrorInfo implements Serializable {
  private static final long serialVersionUID = 4947481159672010360L;

  public String type;
  public String message;

  /**
   * @param type
   * @param message
   */
  public GenericErrorInfo(String type, String message) {
    this.type = type;
    this.message = message;
  }
}
