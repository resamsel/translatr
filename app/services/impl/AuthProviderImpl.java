package services.impl;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import models.AccessToken;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.inject.Injector;
import play.mvc.Http;
import services.AuthProvider;
import services.ContextProvider;
import services.UserService;
import utils.ConfigKey;
import utils.ContextKey;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Singleton
public class AuthProviderImpl implements AuthProvider {
  private final Configuration configuration;
  private final PlayAuthenticate auth;
  private final ContextProvider contextProvider;
  private final Injector injector;

  @Inject
  public AuthProviderImpl(
      Configuration configuration, PlayAuthenticate auth, ContextProvider contextProvider,
      Injector injector) {
    this.configuration = configuration;
    this.auth = auth;
    this.contextProvider = contextProvider;
    this.injector = injector;
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
    AuthUser authUser = loggedInAuthUser();
    if (authUser != null) {
      User user = ContextKey.get(ctx, authUser.toString());
      if (user != null) {
        return user;
      }

      // Needed to avoid circular dependency of AuthProvider -> UserService -> AuthProvider
      UserService userService = injector.instanceOf(UserService.class);

      return ContextKey.put(ctx, authUser.toString(), userService.getLocalUser(authUser));
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

  private AuthUser loggedInAuthUser() {
    Http.Session session = contextProvider.get().session();
    String provider = session.get("pa.p.id");
    List<String> authProviders = Arrays.asList(StringUtils.split(
        configuration.getString(ConfigKey.AuthProviders.key()), ","));

    if (provider != null && !authProviders.contains(provider)) {
      // Prevent NPE when using an unavailable auth provider
      session.clear();
    }

    return auth.getUser(session);
  }
}
