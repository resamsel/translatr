package models;

import static utils.Stopwatch.log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;

import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;
import controllers.Application;
import criterias.HasNextPagedList;
import criterias.LogEntryCriteria;
import criterias.ProjectUserCriteria;
import criterias.UserCriteria;
import play.api.Play;
import play.libs.Json;
import play.mvc.Http.Context;
import play.mvc.Http.Session;
import services.UserService;
import utils.ConfigKey;

@Entity
@Table(name = "user_")
public class User implements Model<User, UUID>, Subject {
  private static final Logger LOGGER = LoggerFactory.getLogger(User.class);

  public static final int USERNAME_LENGTH = 32;

  public static final int NAME_LENGTH = 32;

  public static final int EMAIL_LENGTH = 255;

  @Id
  @GeneratedValue
  public UUID id;

  @Version
  public Long version;

  @CreatedTimestamp
  public DateTime whenCreated;

  @UpdatedTimestamp
  public DateTime whenUpdated;

  public boolean active;

  @Column(length = USERNAME_LENGTH, unique = true)
  public String username;

  @Column(nullable = false, length = NAME_LENGTH)
  public String name;

  @Column(length = EMAIL_LENGTH, unique = false)
  public String email;

  public boolean emailValidated;

  @Temporal(TemporalType.TIMESTAMP)
  public DateTime lastLogin;

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL)
  public List<LinkedAccount> linkedAccounts;

  @JsonIgnore
  @OneToMany
  public List<ProjectUser> projects;

  private static final Find<UUID, User> find = new Find<UUID, User>() {};

  /**
   * {@inheritDoc}
   */
  @Override
  public UUID getId() {
    return id;
  }

  public static boolean existsByAuthUserIdentity(final AuthUserIdentity identity) {
    final ExpressionList<User> exp = getAuthUserFind(identity);
    return exp.findCount() > 0;
  }

  private static ExpressionList<User> getAuthUserFind(final AuthUserIdentity identity) {
    return find.where().eq("active", true).eq("linkedAccounts.providerUserId", identity.getId())
        .eq("linkedAccounts.providerKey", identity.getProvider());
  }

  public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
    if (identity == null)
      return null;

    return log(() -> getAuthUserFind(identity).findUnique(), LOGGER, "findByAuthUserIdentity");
  }

  public Set<String> getProviders() {
    final Set<String> providerKeys = new HashSet<String>(linkedAccounts.size());
    for (final LinkedAccount acc : linkedAccounts)
      providerKeys.add(acc.providerKey);

    return providerKeys;
  }

  public static User findByEmail(final String email) {
    return getEmailUserFind(email).findUnique();
  }

  public static PagedList<User> pagedBy(UserCriteria criteria) {
    ExpressionList<User> query = find.where();

    query.eq("active", true);

    if (criteria.getSearch() != null)
      query.disjunction().ilike("name", "%" + criteria.getSearch() + "%")
          .ilike("username", "%" + criteria.getSearch() + "%").endJunction();

    criteria.paged(query);

    return log(() -> new HasNextPagedList<User>(query), LOGGER, "pagedBy");
  }

  public static List<User> findBy(UserCriteria criteria) {
    return pagedBy(criteria).getList();
  }

  private static ExpressionList<User> getEmailUserFind(final String email) {
    return find.where().eq("active", true).eq("email", email);
  }

  public LinkedAccount getAccountByProvider(final String providerKey) {
    return LinkedAccount.findByProviderKey(this, providerKey);
  }

  public User withId(UUID id) {
    this.id = id;
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
    lastLogin = in.lastLogin;

    return this;
  }

  public static User byId(UUID id) {
    return find.byId(id);
  }

  /**
   * @param username
   * @return
   */
  public static User byUsername(String username) {
    return Play.current().injector().instanceOf(UserService.class).getByUsername(username);
  }

  public static User byUsernameUncached(String username) {
    return find.where().eq("username", username).findUnique();
  }

  public static AuthUser loggedInAuthUser() {
    Session session = Context.current().session();
    String provider = session.get("pa.p.id");
    if (provider != null && !Play.current().injector().instanceOf(play.Application.class)
        .configuration().getStringList(ConfigKey.AuthProviders.key()).contains(provider))
      // Prevent NPE when using an unavailable auth provider
      session.clear();

    PlayAuthenticate auth = Play.current().injector().instanceOf(PlayAuthenticate.class);
    AuthUser authUser = auth.getUser(session);

    return authUser;
  }

  public static User loggedInUser() {
    Map<String, Object> args = Context.current().args;

    // Logged-in via access_token?
    if (args.containsKey("accessToken"))
      return ((AccessToken) args.get("accessToken")).user;

    // Logged-in via auth plugin?
    AuthUser authUser = loggedInAuthUser();
    if (authUser != null) {
      if (!args.containsKey(authUser.toString()))
        args.put(authUser.toString(),
            Play.current().injector().instanceOf(UserService.class).getLocalUser(authUser));

      return (User) args.get(authUser.toString());
    }

    // Not logged-in
    return null;
  }

  /**
   * @return
   */
  public static UUID loggedInUserId() {
    User loggedInUser = loggedInUser();

    if (loggedInUser == null)
      return null;

    return loggedInUser.id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<? extends Role> getRoles() {
    return Arrays.asList(UserRole.from(Application.USER_ROLE));
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

  /**
   * @return
   */
  public boolean isComplete() {
    return username != null && name != null && email != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return String.format("{\"name\": %s}", Json.toJson(name));
  }

  /**
   * @param userId
   * @return
   */
  public static UserStats userStatsUncached(UUID userId) {
    return UserStats.create(ProjectUser.countBy(new ProjectUserCriteria().withUserId(userId)),
        LogEntry.countBy(new LogEntryCriteria().withUserId(userId)));
  }

  /**
   * @param userId
   * @return
   */
  public static String getCacheKey(UUID userId) {
    return String.format("user:%s", userId.toString());
  }

  public static User from(JsonNode json) {
    return Json.fromJson(json, dto.User.class).toModel();
  }
}
