package models;

import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import org.joda.time.DateTime;
import validators.FeatureFlagByUserAndFeature;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "feature"})})
@FeatureFlagByUserAndFeature
public class UserFeatureFlag implements Model<UserFeatureFlag, UUID> {
  @Id
  @GeneratedValue
  public UUID id;

  @CreatedTimestamp
  public DateTime whenCreated;

  @UpdatedTimestamp
  public DateTime whenUpdated;

  @ManyToOne
  public User user;

  @ManyToOne
  public Feature feature;

  public boolean enabled;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public UserFeatureFlag updateFrom(UserFeatureFlag in) {
    user = user.updateFrom(in.user);
    enabled = in.enabled;

    return this;
  }

  public static String getCacheKey(UUID uuid, String... fetches) {
    return String.format("userFeatureFlag:%s:%s", uuid, fetches);
  }

  public static UserFeatureFlag of(User user, Feature feature, boolean enabled) {
    return of(null, user, feature, enabled);
  }

  public static UserFeatureFlag of(UUID id, User user, Feature feature, boolean enabled) {
    UserFeatureFlag out = new UserFeatureFlag();

    out.id = id;
    out.user = user;
    out.feature = feature;
    out.enabled = enabled;

    return out;
  }
}
