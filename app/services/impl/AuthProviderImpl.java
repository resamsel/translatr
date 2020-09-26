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
import services.UserService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

@Singleton
public class AuthProviderImpl implements AuthProvider {
  private static final TypedKey<UUID> ATTRIBUTE_USER_ID = TypedKey.create("translatr:userId");
  private static final TypedKey<User> ATTRIBUTE_USER = TypedKey.create("translatr:user");
  private static final TypedKey<CommonProfile> ATTRIBUTE_PROFILE = TypedKey.create("translatr:profile");

  private final Injector injector;
  private final PlaySessionStore sessionStore;

  private static final Map<String, BiFunction<UserService, CommonProfile, User>> USER_MAPPER = ImmutableMap
          .<String, BiFunction<UserService, CommonProfile, User>>builder()
          .put(ClientName.PARAMETER, (service, profile) -> service.byAccessToken(profile.getId()))
          .put(ClientName.HEADER, (service, profile) -> service.byAccessToken(profile.getId()))
          .build();

  @Inject
  public AuthProviderImpl(
          Injector injector,
          PlaySessionStore sessionStore) {
    this.injector = injector;
    this.sessionStore = sessionStore;
  }

  @Override
  public User loggedInUser(Http.Request request) {
    Optional<User> loggedInUserOptional = request.attrs().getOptional(ATTRIBUTE_USER);
    if (loggedInUserOptional.isPresent()) {
      return loggedInUserOptional.get();
    }

    // Logged-in via auth plugin?
    Optional<CommonProfile> profile = loggedInProfile(request);
    if (profile.isPresent()) {
      String clientName = profile.get().getClientName();
      if (clientName.equals(ClientName.ANONYMOUS)) {
        return null;
      }

      // Needed to avoid circular dependency of AuthProvider -> UserService -> AuthProvider
      UserService userService = getUserService();

      User user = USER_MAPPER.getOrDefault(clientName,
              (service, p) -> service.byLinkedAccount(p.getClientName(), p.getId()))
              .apply(userService, profile.get());

      request.addAttr(ATTRIBUTE_USER_ID, user.id);
      request.addAttr(ATTRIBUTE_USER, user);

      return user;
    }

    return null;
  }

  @Override
  public UUID loggedInUserId(Http.Request request) {
    if (request == null) {
      return null;
    }

    Optional<UUID> loggedInUserIdOptional = request.attrs().getOptional(ATTRIBUTE_USER_ID);
    if (loggedInUserIdOptional.isPresent()) {
      return loggedInUserIdOptional.get();
    }

    User loggedInUser = loggedInUser(request);

    if (loggedInUser == null) {
      return null;
    }

    return loggedInUser.id;
  }

  @Override
  public Optional<CommonProfile> loggedInProfile(Http.Request request) {
    Optional<CommonProfile> cacheProfile = request.attrs().getOptional(ATTRIBUTE_PROFILE);
    if (cacheProfile.isPresent()) {
      return cacheProfile;
    }

    Optional<CommonProfile> profileOptional = loggedInProfile(new PlayWebContext(request, sessionStore));

    profileOptional.ifPresent(commonProfile -> request.addAttr(ATTRIBUTE_PROFILE, commonProfile));

    return profileOptional;
  }

  @Override
  public Optional<CommonProfile> loggedInProfile(WebContext context) {
    return new ProfileManager<CommonProfile>(context).get(true);
  }

  @Override
  public void updateUser(CommonProfile profile, Http.Request request) {
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
      userService.update(user, request);
    }
  }

  private UserService getUserService() {
    // Needed to avoid circular dependency of AuthProvider -> UserService -> AuthProvider
    return injector.instanceOf(UserService.class);
  }
}
