package models;

import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import org.joda.time.DateTime;
import utils.CacheUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class LinkedAccount implements Model<LinkedAccount, Long> {

  @Id
  @GeneratedValue
  public Long id;

  @CreatedTimestamp
  public DateTime whenCreated;

  @UpdatedTimestamp
  public DateTime whenUpdated;

  @ManyToOne
  public User user;

  public String providerUserId;

  public String providerKey;

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
   * {@inheritDoc}
   */
  @Override
  public LinkedAccount updateFrom(LinkedAccount in) {
    user = in.user;
    providerUserId = in.providerUserId;
    providerKey = in.providerKey;

    return this;
  }

  public static String getCacheKey(Long id, String... fetches) {
    return CacheUtils.getCacheKey("linkedAccount:id", id, fetches);
  }
}
