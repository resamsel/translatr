package models;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.feth.play.module.pa.user.AuthUserIdentity;
import criterias.HasNextPagedList;
import criterias.LinkedAccountCriteria;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import org.joda.time.DateTime;
import play.mvc.Call;
import utils.QueryUtils;

@Entity
public class LinkedAccount implements Model<LinkedAccount, Long> {

  private static final String FETCH_USER = "user";

  private static final List<String> PROPERTIES_TO_FETCH = Arrays.asList(FETCH_USER);

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

  public String providerUserId;

  public String providerKey;

  public static final Find<Long, LinkedAccount> find = new Find<Long, LinkedAccount>() {
  };

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getId() {
    return id;
  }

  public LinkedAccount withUser(User user) {
    this.user = user;
    return this;
  }

  /**
   * @param user
   * @return
   */
  public LinkedAccount update(final AuthUserIdentity user) {
    this.providerKey = user.getProvider();
    this.providerUserId = user.getId();
    return this;
  }

  public Call removeRoute() {
    Objects.requireNonNull(user, "User is null");
    return controllers.routes.Users
        .linkedAccountRemove(Objects.requireNonNull(user.username, "User username is null"), id);
  }

  /**
   * @param id
   * @return
   */
  public static LinkedAccount byId(Long id) {
    return find.setId(id).findUnique();
  }

  public static PagedList<LinkedAccount> findBy(LinkedAccountCriteria criteria) {
    ExpressionList<LinkedAccount> query = QueryUtils.fetch(find.query(), PROPERTIES_TO_FETCH)
        .where();

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (criteria.getOrder() != null) {
      query.order(criteria.getOrder());
    } else {
      query.order("whenCreated");
    }

    criteria.paged(query);

    return HasNextPagedList.create(query);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LinkedAccount updateFrom(LinkedAccount in) {
    user = in.user;
    providerUserId = in.providerUserId;
    providerKey = in.providerKey;

    return this;
  }

  public static LinkedAccount createFrom(final AuthUserIdentity authUser) {
    return new LinkedAccount().update(authUser);
  }
}
