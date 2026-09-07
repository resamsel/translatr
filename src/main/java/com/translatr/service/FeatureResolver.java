package com.translatr.service;

import com.translatr.dto.ResolvedFeatureDto;
import com.translatr.model.Feature;
import com.translatr.model.FeatureFlag;
import com.translatr.model.UserFeatureFlag;
import com.translatr.repository.FeatureFlagRepository;
import com.translatr.repository.UserFeatureFlagRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

    /** Per-feature detail for the admin UI; one entry per {@link Feature}, in enum order. */
    public List<ResolvedFeatureDto> resolveDetail(UUID userId) {
        Map<String, UserFeatureFlag> overrides = userFlagRepo.listByUser(userId).stream()
                .filter(f -> Feature.of(f.feature).isPresent())
                .collect(Collectors.toMap(f -> f.feature, f -> f, (a, b) -> b));
        Map<String, FeatureFlag> globals = globalFlagRepo.listAll().stream()
                .filter(f -> Feature.of(f.feature).isPresent())
                .collect(Collectors.toMap(f -> f.feature, f -> f, (a, b) -> b));

        List<ResolvedFeatureDto> out = new ArrayList<>();
        for (Feature f : Feature.values()) {
            FeatureFlag g = globals.get(f.key);
            Boolean globalValue = g != null ? g.enabled : null;

            UserFeatureFlag o = overrides.get(f.key);
            Boolean userOverride   = o != null ? o.enabled : null;
            UUID    userOverrideId = o != null ? o.id : null;

            boolean effective = userOverride != null ? userOverride
                              : globalValue   != null ? globalValue
                              : f.defaultEnabled;

            out.add(new ResolvedFeatureDto()
                    .feature(f.key)
                    .defaultEnabled(f.defaultEnabled)
                    .global(globalValue)
                    .userOverride(userOverride)
                    .userOverrideId(userOverrideId)
                    .effective(effective));
        }
        return out;
    }
}
