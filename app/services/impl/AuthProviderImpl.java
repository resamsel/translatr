package services.impl;

import auth.ClientName;
import com.google.common.collect.ImmutableMap;
import dto.UserUnregisteredException;
import models.User;
import models.UserRole;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.PlaySessionStore;
import play.inject.Injector;
import play.libs.typedmap.TypedKey;
import play.mvc.Http;
import services.AuthProvider;
import services.ContextProvider;
import services.UserService;

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
          .put(ClientName.PARAMETER, (service, profile) -> service.byAccessToken(profile.getId()))
          .put(ClientName.HEADER, (service, profile) -> service.byAccessToken(profile.getId()))
          .build();

  @Inject
  public AuthProviderImpl(
          ContextProvider contextProvider,
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
    // Logged-in via auth plugin?
    Optional<CommonProfile> profile = loggedInProfile(request);
    if (profile.isPresent()) {
      String clientName = profile.get().getClientName();
      if (clientName.equals(ClientName.ANONYMOUS)) {
        return null;
      }

      String id = profile.get().getId();
      TypedKey<User> attributeKey = TypedKey.create(clientName + ":" + id);
      Optional<User> optionalUser = request.attrs().getOptional(attributeKey);
      if (optionalUser.isPresent()) {
        return optionalUser.get();
      }

      // Needed to avoid circular dependency of AuthProvider -> UserService -> AuthProvider
      UserService userService = injector.instanceOf(UserService.class);

      User user = USER_MAPPER.getOrDefault(clientName,
              (service, p) -> service.byLinkedAccount(p.getClientName(), p.getId()))
              .apply(userService, profile.get());

      request.addAttr(attributeKey, user);

      return user;
    }

    return null;
  }

  @Deprecated
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
    return loggedInProfile(new PlayWebContext(request, sessionStore));
  }

  @Override
  public Optional<CommonProfile> loggedInProfile(WebContext context) {
    return new ProfileManager<CommonProfile>(context).get(true);
  }

  @Override
  public void updateUser(CommonProfile profile) {
    if (profile == null) {
      return;
    }

    UserService userService = getUserService();

    User user = userService.byLinkedAccount(profile.getClientName(), profile.getId());

    if (user == null) {
      throw new UserUnregisteredException(profile);
    }

    UserRole newRole = profile.getRoles().contains("admin") ? UserRole.Admin : UserRole.User;

    if (!newRole.equals(user.role)) {
      user.role = newRole;
      userService.update(user);
    }
  }

  private UserService getUserService() {
    // Needed to avoid circular dependency of AuthProvider -> UserService -> AuthProvider
    return injector.instanceOf(UserService.class);
  }
}
