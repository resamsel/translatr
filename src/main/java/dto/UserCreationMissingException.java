package dto;

import org.pac4j.core.profile.UserProfile;

public class UserCreationMissingException extends RuntimeException {
  private static final String MESSAGE_FORMAT = "User %s authenticated, but not yet created";
  private final UserProfile profile;

  public UserCreationMissingException(UserProfile profile) {
    super(String.format(MESSAGE_FORMAT, profile.getUsername()));
    this.profile = profile;
  }

  public UserProfile getProfile() {
    return profile;
  }
}
