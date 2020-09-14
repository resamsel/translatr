package services;

import com.google.inject.ImplementedBy;
import models.User;
import org.pac4j.oidc.profile.OidcProfile;
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
   * @deprecated Use {@link AuthProvider#loggedInUser(Http.Request)} instead.
   */
  @Deprecated
  User loggedInUser();

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
   * @deprecated Use {@link AuthProvider#loggedInUserId(Http.Request)} instead.
   */
  @Deprecated
  UUID loggedInUserId();

  /**
   * Retrieves the logged-in user ID.
   *
   * @return the logged-in user ID, or null, if unauthenticated
   */
  UUID loggedInUserId(Http.Request request);

  /**
   * Retrieves the logged-in user from the request.
   * @return
   */
  Optional<OidcProfile> loggedInProfile(Http.Request request);

  /**
   * Whether or not the logged-in profile (auth) needs registration (no matching user, yet).
   */
  boolean needsRegistration(Http.Request request);
}
