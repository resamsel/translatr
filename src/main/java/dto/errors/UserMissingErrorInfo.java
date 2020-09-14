package dto.errors;

import dto.UserCreationMissingException;

public class UserMissingErrorInfo extends GenericErrorInfo {
  public UserMissingErrorInfo(UserCreationMissingException e) {
    super("UserMissing", e.getMessage());
  }
}
