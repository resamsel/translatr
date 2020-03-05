package models;

import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
import play.mvc.Call;
import utils.CacheUtils;
import validators.NameUnique;
import validators.UserUsernameUniqueChecker;
import validators.Username;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static play.libs.Json.toJson;

/**
 * @author René Panzar
 * @version 2 Jun 2017
 */
@Entity
@Table(name = "user_")
@NameUnique(checker = UserUsernameUniqueChecker.class, field = "username", message = "error.usernameunique")
public class User implements Model<User, UUID>, Subject {

  public static final int USERNAME_LENGTH = 32;

  public static final int NAME_LENGTH = 32;

  public static final int EMAIL_LENGTH = 255;

  private static final int ROLE_LENGTH = 16;

  @Id
  @GeneratedValue
  public UUID id;

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

  @JsonIgnore
  @OneToMany
  public List<UserFeatureFlag> features;

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

  public User withRole(UserRole role) {
    this.role = role;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User updateFrom(User in) {
    active = in.active;
    username = ObjectUtils.firstNonNull(in.username, username);
    name = ObjectUtils.firstNonNull(in.name, name);
    email = ObjectUtils.firstNonNull(in.email, email);
    emailValidated = in.emailValidated;
    role = in.role;

    return this;
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

  public boolean isAdmin() {
    return role == UserRole.Admin;
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
    return Objects.equals(id, that.id);
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
