package services.impl;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import models.AccessToken;
import models.User;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

@Singleton
public class AuthProviderImpl implements AuthProvider {
  private final ContextProvider contextProvider;
  private final Injector injector;
  private final PlaySessionStore sessionStore;

  private static final Map<String, BiFunction<UserService, CommonProfile, User>> USER_MAPPER = ImmutableMap
          .<String, BiFunction<UserService, CommonProfile, User>>builder()
          .put("parameter", (service, profile) -> service.byAccessToken(profile.getId()))
          .put("header", (service, profile) -> service.byAccessToken(profile.getId()))
          .build();

  @Inject
  public AuthProviderImpl(
          Config configuration, ContextProvider contextProvider,
          Injector injector,
          PlaySessionStore sessionStore) {
    this.contextProvider = contextProvider;
    this.injector = injector;
    this.sessionStore = sessionStore;
  }

  /**
   * @deprecated use {{@link AuthProviderImpl#loggedInUser(Http.Request)} instead.
   */
  @Override
  @Deprecated
  public User loggedInUser() {
    return loggedInUser(contextProvider.getOrNull().request());
  }

  @Override
  public User loggedInUser(Http.Request request) {
    // Logged-in via access_token?
    AccessToken accessToken = ContextKey.AccessToken.get(request);
    if (accessToken != null) {
      return accessToken.user;
    }

    // Logged-in via auth plugin?
    Optional<CommonProfile> profile = loggedInProfile(request);
    if (profile.isPresent()) {
      String clientName = profile.get().getClientName();
      if (clientName.equals("anonymous")) {
        return null;
      }

      String id = profile.get().getId();
      String contextKey = clientName + ":" + id;
      User user = ContextKey.get(request, contextKey);
      if (user != null) {
        return user;
      }

      // Needed to avoid circular dependency of AuthProvider -> UserService -> AuthProvider
      UserService userService = injector.instanceOf(UserService.class);

      user = USER_MAPPER.getOrDefault(clientName,
              (service, p) -> service.byLinkedAccount(p.getClientName(), p.getId()))
              .apply(userService, profile.get());

      return ContextKey.put(request, contextKey, user);
    }

    return null;
  }

  @Override
  public UUID loggedInUserId() {
    return loggedInUserId(contextProvider.getOrNull().request());
  }

  @Override
  public UUID loggedInUserId(Http.Request request) {
    User loggedInUser = loggedInUser();

    if (loggedInUser == null) {
      return null;
    }

    return loggedInUser.id;
  }

  @Override
  public Optional<CommonProfile> loggedInProfile(Http.Request request) {
    WebContext context = new PlayWebContext(request, sessionStore);
    return new ProfileManager<CommonProfile>(context).get(true);
  }

  @Override
  public boolean needsRegistration(Http.Request request) {
    Optional<CommonProfile> profile = loggedInProfile(request);
    if (profile.isPresent()) {
      String clientName = profile.get().getClientName();
      String userId = profile.get().getId();

      // Needed to avoid circular dependency of AuthProvider -> UserService -> AuthProvider
      UserService userService = injector.instanceOf(UserService.class);

      return userService.byLinkedAccount(clientName, userId) == null;
    }

    return false;
  }
}
