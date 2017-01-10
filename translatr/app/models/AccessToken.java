package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.joda.time.DateTime;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

import criterias.AccessTokenCriteria;
import play.data.validation.Constraints.MaxLength;

@Entity
public class AccessToken implements Model<AccessToken> {
  public static final int NAME_LENGTH = 32;

  public static final int KEY_LENGTH = 64;

  public static final int SCOPE_LENGTH = 255;

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

  public static final Find<Long, AccessToken> find = new Find<Long, AccessToken>() {};

  public AccessToken withUser(User user) {
    this.user = user;
    return this;
  }

  public List<Scope> getScopeList() {
    if (scope == null)
      return new ArrayList<>();

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

  /**
   * @param id
   * @return
   */
  public static AccessToken byId(Long id) {
    return find.setId(id).findUnique();
  }

  /**
   * @param user
   * @param key
   * @return
   */
  public static AccessToken byKeyUncached(String key) {
    return find.fetch("user").where().eq("key", key).findUnique();
  }

  /**
   * @param criteria
   * @return
   */
  public static List<AccessToken> findBy(AccessTokenCriteria criteria) {
    ExpressionList<AccessToken> query = find.where();

    if (criteria.getUserId() != null)
      query.eq("user.id", criteria.getUserId());

    if (criteria.getLimit() != null)
      query.setMaxRows(criteria.getLimit() + 1);

    if (criteria.getOffset() != null)
      query.setFirstRow(criteria.getOffset());

    if (criteria.getOrder() != null)
      query.order(criteria.getOrder());
    else
      query.order("whenCreated");

    return query.findList();
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
