package dto;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public class NotFoundException extends RuntimeException {
  private static final long serialVersionUID = 944214728665880687L;
  private static final String MESSAGE_FORMAT = "%s '%s' not found";
  public String type;
  public String id;

  public NotFoundException(String type, Object id) {
    super(String.format(MESSAGE_FORMAT, type, String.valueOf(id)));
    this.type = type;
    this.id = String.valueOf(id);
  }
}
