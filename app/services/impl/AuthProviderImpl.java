package services.impl;

import com.typesafe.config.Config;
import models.AccessToken;
import models.User;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.PlaySessionStore;
import play.inject.Injector;
import play.mvc.Http;
import services.AuthProvider;
import services.ContextProvider;
import services.UserService;
import utils.ContextKey;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class AuthProviderImpl implements AuthProvider {
  private final Config configuration;
  private final ContextProvider contextProvider;
  private final Injector injector;
  private final PlaySessionStore sessionStore;

  @Inject
  public AuthProviderImpl(
          Config configuration, ContextProvider contextProvider,
          Injector injector,
          PlaySessionStore sessionStore) {
    this.configuration = configuration;
    this.contextProvider = contextProvider;
    this.injector = injector;
    this.sessionStore = sessionStore;
  }

  @Override
  public User loggedInUser() {
    Http.Context ctx = contextProvider.getOrNull();
    if (ctx == null) {
      return null;
    }

    // Logged-in via access_token?
    AccessToken accessToken = ContextKey.AccessToken.get(ctx);
    if (accessToken != null) {
      return accessToken.user;
    }

    // Logged-in via auth plugin?
    WebContext context = new PlayWebContext(ctx.request(), sessionStore);
    Optional<UserProfile> profile = new ProfileManager<UserProfile>(context).get(true);
    if (profile.isPresent()) {
      String username = profile.get().getUsername();
      User user = ContextKey.get(ctx, username);
      if (user != null) {
        return user;
      }

      // Needed to avoid circular dependency of AuthProvider -> UserService -> AuthProvider
      UserService userService = injector.instanceOf(UserService.class);

      return ContextKey.put(ctx, username, userService.byUsername(username));
    }

    return null;
  }

  @Override
  public UUID loggedInUserId() {
    User loggedInUser = loggedInUser();

    if (loggedInUser == null) {
      return null;
    }

    return loggedInUser.id;
  }
}
