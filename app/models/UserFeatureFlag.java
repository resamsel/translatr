package models;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "feature_flag"})})
public class UserFeatureFlag implements Model<UserFeatureFlag, UUID> {
  @Id
  @GeneratedValue
  public UUID id;

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
