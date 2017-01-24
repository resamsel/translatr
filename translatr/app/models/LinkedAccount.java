package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.joda.time.DateTime;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.user.AuthUserIdentity;

import criterias.HasNextPagedList;
import criterias.LinkedAccountCriteria;
import play.libs.Json;

@Entity
public class LinkedAccount implements Model<LinkedAccount, Long> {
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

  public static final Find<Long, LinkedAccount> find = new Find<Long, LinkedAccount>() {};

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

  /**
   * @param id
   * @return
   */
  public static LinkedAccount byId(Long id) {
    return find.setId(id).findUnique();
  }

  /**
   * @param user
   * @param key
   * @return
   */
  public static LinkedAccount findByProviderKey(final User user, String key) {
    return find.where().eq("user", user).eq("providerKey", key).findUnique();
  }

  /**
   * @param criteria
   * @return
   */
  public static List<LinkedAccount> findBy(LinkedAccountCriteria criteria) {
    return pagedBy(criteria).getList();
  }

  public static PagedList<LinkedAccount> pagedBy(LinkedAccountCriteria criteria) {
    ExpressionList<LinkedAccount> query = find.where();

    if (criteria.getUserId() != null)
      query.eq("user.id", criteria.getUserId());

    if (criteria.getOrder() != null)
      query.order(criteria.getOrder());
    else
      query.order("whenCreated");

    criteria.paged(query);

    return new HasNextPagedList<>(query);
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

  public static LinkedAccount from(JsonNode json) {
    return Json.fromJson(json, LinkedAccount.class);
  }
}
