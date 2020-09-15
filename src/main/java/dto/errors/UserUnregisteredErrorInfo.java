package dto.errors;

import dto.UserUnregisteredException;

public class UserUnregisteredErrorInfo extends GenericErrorInfo {
  public UserUnregisteredErrorInfo(UserUnregisteredException e) {
    super("UserUnregistered", e.getMessage());
  }
}
