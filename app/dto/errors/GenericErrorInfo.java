package dto.errors;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericErrorInfo {
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
