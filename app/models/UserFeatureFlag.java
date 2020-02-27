package models;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "feature_flag"})})
public class UserFeatureFlag {
    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne
    public User user;

    @ManyToOne
    public FeatureFlag featureFlag;

    public boolean enabled;
}
