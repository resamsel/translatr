package services;

import com.google.inject.ImplementedBy;
import models.User;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import play.mvc.Http;
import services.impl.AuthProviderImpl;

import java.util.Optional;
import java.util.UUID;

@ImplementedBy(AuthProviderImpl.class)
public interface AuthProvider {
  /**
   * Retrieves the logged-in user.
   *
   * @return the logged-in user, or null, if unauthenticated
   */
  User loggedInUser(Http.Request request);

  /**
   * Retrieves the logged-in user ID.
   *
   * @return the logged-in user ID, or null, if unauthenticated
   */
  UUID loggedInUserId(Http.Request request);

  /**
   * Retrieves the logged-in user from the request.
   *
   * @return
   */
  Optional<CommonProfile> loggedInProfile(Http.Request request);

  Optional<CommonProfile> loggedInProfile(WebContext context);

  void updateUser(CommonProfile profile, Http.Request request);
}
