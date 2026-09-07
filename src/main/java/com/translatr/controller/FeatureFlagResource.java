package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.FeatureFlagCriteria;
import com.translatr.dto.FeatureFlagDto;
import com.translatr.dto.PagedFeatureFlagList;
import com.translatr.dto.PagedList;
import com.translatr.generated.api.UserFeatureFlagsApi;
import com.translatr.service.FeatureFlagService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;

import java.util.UUID;

@Authenticated
public class FeatureFlagResource implements UserFeatureFlagsApi {

    private final FeatureFlagService  featureFlagService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public FeatureFlagResource(FeatureFlagService featureFlagService, CurrentUserResolver currentUserResolver) {
        this.featureFlagService  = featureFlagService;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    public PagedFeatureFlagList findUserFeatureFlags(String search, Integer offset, Integer limit, String order,
                                                     String fetch, UUID userId, String feature) {
        var user     = currentUserResolver.resolve();
        var criteria = toCriteria(search, offset, limit, order, fetch, userId, feature);
        return toPagedDto(featureFlagService.find(criteria, user.id));
    }

    @Override
    public FeatureFlagDto getUserFeatureFlag(UUID id) {
        return featureFlagService.get(id);
    }

    @Override
    public FeatureFlagDto createUserFeatureFlag(FeatureFlagDto featureFlagDto) {
        return featureFlagService.create(featureFlagDto);
    }

    @Override
    public FeatureFlagDto updateUserFeatureFlag(FeatureFlagDto featureFlagDto) {
        return featureFlagService.update(featureFlagDto);
    }

    @Override
    public FeatureFlagDto deleteUserFeatureFlag(UUID id) {
        return featureFlagService.delete(id);
    }

    static FeatureFlagCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                          UUID userId, String feature) {
        FeatureFlagCriteria c = new FeatureFlagCriteria();
        c.search  = search;
        if (offset != null) c.offset = offset;
        if (limit  != null) c.limit  = limit;
        c.order   = order;
        c.fetch   = fetch;
        c.userId  = userId;
        c.feature = feature;
        return c;
    }

    private static PagedFeatureFlagList toPagedDto(PagedList<FeatureFlagDto> src) {
        return new PagedFeatureFlagList(
                src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
