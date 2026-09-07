package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.dto.GlobalFeatureFlagDto;
import com.translatr.dto.ResolvedFeatureDto;
import com.translatr.generated.api.FeatureflagsApi;
import com.translatr.service.FeatureResolver;
import com.translatr.service.GlobalFeatureFlagService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import java.util.List;
import java.util.UUID;

public class GlobalFeatureFlagResource implements FeatureflagsApi {

    private final GlobalFeatureFlagService globalService;
    private final FeatureResolver          featureResolver;
    private final CurrentUserResolver      currentUserResolver;

    @Inject
    public GlobalFeatureFlagResource(GlobalFeatureFlagService globalService,
                                     FeatureResolver featureResolver,
                                     CurrentUserResolver currentUserResolver) {
        this.globalService       = globalService;
        this.featureResolver     = featureResolver;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    @Authenticated
    public List<ResolvedFeatureDto> listResolvedFeatures() {
        return featureResolver.resolveDetail(currentUserResolver.resolve().id);
    }

    @Override
    @Authenticated
    public List<GlobalFeatureFlagDto> listGlobalFeatureFlags() {
        return globalService.list();
    }

    @Override
    @Authenticated
    public GlobalFeatureFlagDto setGlobalFeatureFlag(GlobalFeatureFlagDto globalFeatureFlagDto) {
        requireAdmin();
        return globalService.set(globalFeatureFlagDto.getFeature(), Boolean.TRUE.equals(globalFeatureFlagDto.getEnabled()));
    }

    @Override
    @Authenticated
    public void deleteGlobalFeatureFlag(UUID id) {
        requireAdmin();
        globalService.delete(id);
    }

    private void requireAdmin() {
        if (!currentUserResolver.resolve().isAdmin()) {
            throw new ForbiddenException("Admin role required");
        }
    }
}
