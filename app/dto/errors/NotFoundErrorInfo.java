package dto.errors;

import dto.NotFoundException;

public class NotFoundErrorInfo extends GenericErrorInfo {
  public String entity;
  public String id;

  /**
   * @param type
   * @param message
   */
  public NotFoundErrorInfo(NotFoundException e) {
    super("NotFoundError", e.getMessage());

    this.entity = e.type;
    this.id = e.id;
  }
}
