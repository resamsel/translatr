package dto.errors;

import dto.UserUnregisteredException;

public class UserUnregisteredError extends Error {
  public final UserUnregisteredErrorInfo error;

  public UserUnregisteredError(UserUnregisteredException e) {
    this.error = new UserUnregisteredErrorInfo(e);
  }
}
