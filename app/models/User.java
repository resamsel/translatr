package models;

import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Play;
import play.api.inject.Injector;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
import play.mvc.Call;
import play.mvc.Http.Context;
import play.mvc.Http.Session;
import services.UserService;
import utils.CacheUtils;
import utils.ConfigKey;
import utils.ContextKey;
import validators.NameUnique;
import validators.Username;
import validators.UserUsernameUniqueChecker;

import javax.persistence.*;
import java.util.*;

import static play.libs.Json.toJson;

/**
 * @author René Panzar
 * @version 2 Jun 2017
 */
@Entity
@Table(name = "user_")
@NameUnique(checker = UserUsernameUniqueChecker.class, field = "username", message = "error.usernameunique")
public class User implements Model<User, UUID>, Subject {

  private static final Logger LOGGER = LoggerFactory.getLogger(User.class);

  public static final int USERNAME_LENGTH = 32;

  public static final int NAME_LENGTH = 32;

  public static final int EMAIL_LENGTH = 255;

  public static final int ROLE_LENGTH = 16;

  @Id
  @GeneratedValue
  public UUID id;

  @Version
  public Long version;

  @CreatedTimestamp
  public DateTime whenCreated;

  @UpdatedTimestamp
  public DateTime whenUpdated;

  public boolean active = true;

  @Column(nullable = false, length = USERNAME_LENGTH, unique = true)
  @Required
  @Constraints.Pattern("[a-zA-Z0-9_\\.-]*")
  @Username
  public String username;

  @Required
  @Column(nullable = false, length = NAME_LENGTH)
  public String name;

  public String email;

  public boolean emailValidated;

  @Enumerated(EnumType.STRING)
  @Column(length = ROLE_LENGTH)
  public UserRole role = UserRole.User;

  @JsonIgnore
  @OneToMany
  @JoinColumn(name = "owner_id")
  public List<Project> projects;

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL)
  public List<LinkedAccount> linkedAccounts;

  @JsonIgnore
  @OneToMany
  public List<ProjectUser> memberships;

  @JsonIgnore
  @OneToMany
  public List<LogEntry> activities;

  /**
   * {@inheritDoc}
   */
  @Override
  public UUID getId() {
    return id;
  }

  public Set<String> getProviders() {
    final Set<String> providerKeys = new HashSet<>(linkedAccounts.size());
    for (final LinkedAccount acc : linkedAccounts) {
      providerKeys.add(acc.providerKey);
    }

    return providerKeys;
  }

  public User withId(UUID id) {
    this.id = id;
    return this;
  }

  public User withName(String name) {
    this.name = name;
    return this;
  }


  public User withUsername(String username) {
    this.username = username;
    return this;
  }

  public User withEmail(String email) {
    this.email = email;
    return this;
  }

  public User withActive(boolean active) {
    this.active = active;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User updateFrom(User in) {
    active = in.active;
    username = in.username;
    name = in.name;
    email = in.email;
    emailValidated = in.emailValidated;

    return this;
  }

  private static AuthUser loggedInAuthUser() {
    Injector injector = Play.current().injector();
    Session session = Context.current().session();
    String provider = session.get("pa.p.id");
    List<String> authProviders =
        Arrays.asList(StringUtils.split(injector.instanceOf(play.Application.class).configuration()
            .getString(ConfigKey.AuthProviders.key()), ","));

    LOGGER.debug("Auth providers: {}", authProviders);

    if (provider != null && !authProviders.contains(provider))
    // Prevent NPE when using an unavailable auth provider
    {
      session.clear();
    }

    PlayAuthenticate auth = injector.instanceOf(PlayAuthenticate.class);

    return auth.getUser(session);
  }

  public static User loggedInUser() {
    Context ctx = ContextKey.context();
    if (ctx == null) {
      LOGGER.debug("Context is null");
      return null;
    }

    // Logged-in via access_token?
    AccessToken accessToken = ContextKey.AccessToken.get();
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

      LOGGER.debug("Auth user not in context");
      return ContextKey.put(ctx, authUser.toString(),
          Play.current().injector().instanceOf(UserService.class).getLocalUser(authUser));
    }

    LOGGER.debug("Not logged-in");
    return null;
  }

  public static UUID loggedInUserId() {
    User loggedInUser = loggedInUser();

    if (loggedInUser == null) {
      return null;
    }

    return loggedInUser.id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<? extends Role> getRoles() {
    return Collections.singletonList(UserRole.User);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<? extends Permission> getPermissions() {
    return Collections.emptyList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getIdentifier() {
    return id != null ? id.toString() : null;
  }

  public boolean isComplete() {
    return username != null && name != null && email != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return String.format("{\"name\": %s, \"username\": %s}", toJson(name), toJson(username));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof User)) {
      return false;
    }
    User that = (User) obj;
    return this.id == that.id || id != null && id.equals(that.id);
  }

  /**
   * Return the route to the user.
   */
  public Call route() {
    return controllers.routes.Users.user(Objects.requireNonNull(username, "Username is null"));
  }

  public static String getCacheKey(UUID userId, String... fetches) {
    return CacheUtils.getCacheKey("user:id", userId, fetches);
  }

  public static String getCacheKey(String username, String... fetches) {
    return CacheUtils.getCacheKey("user:username", username, fetches);
  }

  public Call profileRoute() {
    return controllers.routes.Users.user(username);
  }

  public Call projectsRoute() {
    return controllers.routes.Users.projects(username);
  }

  public Call activityRoute() {
    return controllers.routes.Users.activity(username);
  }

  public Call accessTokensRoute() {
    return controllers.routes.Users.accessTokens(username);
  }

  public Call linkedAccountsRoute() {
    return controllers.routes.Users.linkedAccounts(username);
  }
}
