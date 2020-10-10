package models;

import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import play.data.validation.Constraints.MaxLength;
import utils.CacheUtils;
import validators.AccessTokenNameUniqueChecker;
import validators.NameUnique;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@NameUnique(checker = AccessTokenNameUniqueChecker.class, message = "error.accesstokennameunique")
public class AccessToken implements Model<AccessToken, Long> {

  public static final int NAME_LENGTH = 32;

  public static final int KEY_LENGTH = 64;

  private static final int SCOPE_LENGTH = 255;

  @Id
  @GeneratedValue
  public Long id;

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

  public AccessToken withName(String name) {
    this.name = name;
    return this;
  }

  public AccessToken withKey(String key) {
    this.key = key;
    return this;
  }

  public AccessToken withScope(Scope... scopes) {
    this.scope = Arrays.stream(scopes).map(Scope::scope).collect(Collectors.joining(","));
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
    user = user.updateFrom(in.user);
    name = ObjectUtils.firstNonNull(in.name, name);
    key = in.key;
    scope = in.scope;

    return this;
  }

  public static String getCacheKey(Long id, String... fetches) {
    return CacheUtils.getCacheKey("accessToken:id", id, fetches);
  }

  public static String getCacheKey(String key) {
    return CacheUtils.getCacheKey("accessToken:key", key);
  }
}
