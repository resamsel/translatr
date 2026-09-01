package com.translatr.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * The global (application-wide) setting for one feature. At most one row per {@code feature};
 * absent means "fall through to {@link Feature#defaultEnabled}". Per-user overrides live in
 * {@link UserFeatureFlag}.
 */
@Entity
@Table(name = "feature_flag",
       uniqueConstraints = @UniqueConstraint(columnNames = "feature"))
public class FeatureFlag extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public Instant whenCreated;

    @UpdateTimestamp
    @Column(nullable = false)
    public Instant whenUpdated;

    @Column(nullable = false, length = 64, unique = true)
    public String feature;

    @Column(nullable = false)
    public boolean enabled;

    public static FeatureFlag of(String feature, boolean enabled) {
        FeatureFlag f = new FeatureFlag();
        f.feature = feature;
        f.enabled = enabled;
        return f;
    }
}
