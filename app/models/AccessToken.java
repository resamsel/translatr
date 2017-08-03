package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import controllers.routes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import org.joda.time.DateTime;
import play.data.validation.Constraints.MaxLength;
import play.mvc.Call;
import validators.AccessTokenNameUniqueChecker;
import validators.NameUnique;

@Entity
@NameUnique(checker = AccessTokenNameUniqueChecker.class)
public class AccessToken implements Model<AccessToken, Long> {

  public static final int NAME_LENGTH = 32;

  public static final int KEY_LENGTH = 64;

  public static final int SCOPE_LENGTH = 255;

  public static final String FETCH_USER = "user";

  @Id
  @GeneratedValue
  public Long id;

  @Version
  public Long version;

  @CreatedTimestamp
  public DateTime whenCreated;

  @UpdatedTimestamp
  public DateTime whenUpdated;

  @ManyToOne
  public User user;

  @MaxLength(NAME_LENGTH)
  public String name;

  @MaxLength(KEY_LENGTH)
  public String key;

  @MaxLength(SCOPE_LENGTH)
  public String scope;

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getId() {
    return id;
  }

  public AccessToken withUser(User user) {
    this.user = user;
    return this;
  }

  public List<Scope> getScopeList() {
    if (scope == null) {
      return new ArrayList<>();
    }

    return new ArrayList<>(Arrays.asList(scope.split(","))).stream()
        .map(Scope::fromString).collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AccessToken updateFrom(AccessToken in) {
    user = in.user;
    name = in.name;
    key = in.key;
    scope = in.scope;

    return this;
  }

  public Call editRoute() {
    Objects.requireNonNull(user, "User is null");
    return routes.Users
        .accessTokenEdit(Objects.requireNonNull(user.username, "User username is null"), id);
  }

  public Call doEditRoute() {
    Objects.requireNonNull(user, "User is null");
    return routes.Users
        .doAccessTokenEdit(Objects.requireNonNull(user.username, "User username is null"), id);
  }

  public Call removeRoute() {
    Objects.requireNonNull(user, "User is null");
    return routes.Users
        .accessTokenRemove(Objects.requireNonNull(user.username, "User username is null"), id);
  }

  public static String getCacheKey(Long id, String... fetches) {
    return String.format("accessToken:id:%d", id);
  }

  public static String getCacheKey(String key) {
    return String.format("accessToken:key:%s", key);
  }
}
