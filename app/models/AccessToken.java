package models;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import controllers.routes;
import criterias.AccessTokenCriteria;
import criterias.HasNextPagedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Constraints.MaxLength;
import play.mvc.Call;
import utils.QueryUtils;
import validators.AccessTokenNameUniqueChecker;
import validators.NameUnique;

@Entity
@NameUnique(checker = AccessTokenNameUniqueChecker.class)
public class AccessToken implements Model<AccessToken, Long> {

  public static final int NAME_LENGTH = 32;

  public static final int KEY_LENGTH = 64;

  public static final int SCOPE_LENGTH = 255;

  private static final String FETCH_USER = "user";

  private static final String[] PROPERTIES_TO_FETCH = new String[]{FETCH_USER};

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

  public static final Find<Long, AccessToken> find = new Find<Long, AccessToken>() {
  };

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
        .map(scope -> Scope.fromString(scope)).collect(Collectors.toList());
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

  /**
   * @param id
   * @return
   */
  public static AccessToken byId(Long id) {
    return fetch().setId(id).findUnique();
  }

  /**
   * @param key
   * @return
   */
  public static AccessToken byKey(String key) {
    return fetch().where().eq("key", key).findUnique();
  }

  private static Query<AccessToken> fetch() {
    return QueryUtils.fetch(find.query().setDisableLazyLoading(true), PROPERTIES_TO_FETCH);
  }

  public static PagedList<AccessToken> findBy(AccessTokenCriteria criteria) {
    ExpressionList<AccessToken> query = fetch().where();

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (StringUtils.isNoneEmpty(criteria.getSearch())) {
      query.ilike("name", "%" + criteria.getSearch() + "%");
    }

    criteria.paged(query);

    return HasNextPagedList.create(query);
  }

  /**
   * @param userId
   * @param name
   * @return
   */
  public static AccessToken byUserAndName(UUID userId, String name) {
    return find.where().eq("user.id", userId).eq("name", name).findUnique();
  }
}
