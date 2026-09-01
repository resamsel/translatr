package com.translatr.service;

import com.translatr.model.Feature;
import com.translatr.model.FeatureFlag;
import com.translatr.model.UserFeatureFlag;
import com.translatr.repository.FeatureFlagRepository;
import com.translatr.repository.UserFeatureFlagRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Single owner of feature-flag precedence: per-user override → global {@link FeatureFlag} row →
 * {@link Feature#defaultEnabled}. Feature strings stored in the DB that are not in the
 * {@link Feature} enum are ignored.
 */
@ApplicationScoped
public class FeatureResolver {

    private final UserFeatureFlagRepository userFlagRepo;
    private final FeatureFlagRepository     globalFlagRepo;

    @Inject
    public FeatureResolver(UserFeatureFlagRepository userFlagRepo, FeatureFlagRepository globalFlagRepo) {
        this.userFlagRepo   = userFlagRepo;
        this.globalFlagRepo = globalFlagRepo;
    }

    /** Effective value per feature for {@code userId}; one entry per {@link Feature}. */
    public Map<String, Boolean> resolveAll(UUID userId) {
        Map<String, Boolean> overrides = userFlagRepo.listByUser(userId).stream()
                .filter(f -> Feature.of(f.feature).isPresent())
                .collect(Collectors.toMap(f -> f.feature, f -> f.enabled, (a, b) -> b));
        Map<String, Boolean> globals = globalFlagRepo.listAll().stream()
                .filter(f -> Feature.of(f.feature).isPresent())
                .collect(Collectors.toMap(f -> f.feature, f -> f.enabled, (a, b) -> b));

        Map<String, Boolean> result = new LinkedHashMap<>();
        for (Feature f : Feature.values()) {
            Boolean effective = overrides.get(f.key);
            if (effective == null) effective = globals.get(f.key);
            if (effective == null) effective = f.defaultEnabled;
            result.put(f.key, effective);
        }
        return result;
    }

    // resolveDetail(...) is added in Task 3, once ResolvedFeatureDto exists.
}
