package com.translatr.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "feature"}))
public class UserFeatureFlag extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public Instant whenCreated;

    @UpdateTimestamp
    @Column(nullable = false)
    public Instant whenUpdated;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Column(nullable = false, length = 64, name = "feature")
    public String feature;

    public boolean enabled;

    public static UserFeatureFlag of(User user, String feature, boolean enabled) {
        return of(null, user, feature, enabled);
    }

    public static UserFeatureFlag of(UUID id, User user, String feature, boolean enabled) {
        UserFeatureFlag f = new UserFeatureFlag();
        f.id      = id;
        f.user    = user;
        f.feature = feature;
        f.enabled = enabled;
        return f;
    }
}
