package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.feth.play.module.pa.user.AuthUserIdentity;
import org.joda.time.DateTime;
import play.mvc.Call;
import utils.CacheUtils;

import javax.persistence.*;
import java.util.Objects;

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

  public static String getCacheKey(Long id, String... fetches) {
    return CacheUtils.getCacheKey("linkedAccount:id", id, fetches);
  }
}
