package com.translatr.repository;

import com.translatr.model.UserFeatureFlag;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserFeatureFlagRepository implements PanacheRepositoryBase<UserFeatureFlag, UUID> {

    public Optional<UserFeatureFlag> findByUserAndFeature(UUID userId, String feature) {
        return find("user.id = ?1 AND feature = ?2", userId, feature).firstResultOptional();
    }
}
