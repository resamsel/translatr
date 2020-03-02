package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import org.joda.time.DateTime;
import validators.FeatureFlagByUserAndFeature;
import validators.NameUnique;
import validators.ProjectUserUniqueChecker;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "feature_flag"})})
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
  public FeatureFlag featureFlag;

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
}
