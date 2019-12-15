package services;

import com.google.inject.ImplementedBy;
import models.User;
import services.impl.AuthProviderImpl;

import java.util.UUID;

@ImplementedBy(AuthProviderImpl.class)
public interface AuthProvider {
  /**
   * Retrieves the logged-in user.
   *
   * @return the logged-in user, or null, if unauthenticated
   */
  User loggedInUser();

  /**
   * Retrieves the logged-in user ID.
   *
   * @return the logged-in user ID, or null, if unauthenticated
   */
  UUID loggedInUserId();
}
