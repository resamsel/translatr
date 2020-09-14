package dto.errors;

import dto.UserCreationMissingException;

public class UserMissingError extends Error {
  public final UserMissingErrorInfo error;

  public UserMissingError(UserCreationMissingException e) {
    this.error = new UserMissingErrorInfo(e);
  }
}
