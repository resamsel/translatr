package com.translatr.repository;

import com.translatr.model.FeatureFlag;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FeatureFlagRepository implements PanacheRepositoryBase<FeatureFlag, UUID> {

    public Optional<FeatureFlag> findByFeature(String feature) {
        return find("feature", feature).firstResultOptional();
    }
}
