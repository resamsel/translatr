package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.dto.ActivityDto;
import com.translatr.dto.AggregateDto;
import com.translatr.dto.PagedActivityList;
import com.translatr.dto.PagedAggregateList;
import com.translatr.dto.PagedList;
import com.translatr.service.ActivityService;
import com.translatr.generated.api.ActivitiesApi;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.UUID;

public class ActivityResource implements ActivitiesApi {

    private final ActivityService     activityService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public ActivityResource(ActivityService activityService, CurrentUserResolver currentUserResolver) {
        this.activityService     = activityService;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    @PermitAll
    public PagedActivityList findActivities(UUID userId, Integer offset, Integer limit) {
        // An explicit userId (e.g. viewing another user's activity feed) never needs the
        // current-user lookup — and this endpoint is @PermitAll, so resolving "me" would
        // blow up for anonymous callers that don't pass one.
        UUID targetUserId = userId != null ? userId : currentUserResolver.resolve().id;
        return toPagedActivityDto(activityService.findByUser(targetUserId, offset, limit));
    }

    @Override
    @PermitAll
    public PagedActivityList findActivitiesByUser(UUID userId, Integer offset, Integer limit) {
        return toPagedActivityDto(activityService.findByUser(userId, offset, limit));
    }

    @Override
    @PermitAll
    public PagedAggregateList findAggregatedActivity(UUID projectId, UUID userId, Integer offset, Integer limit) {
        return toPagedAggregateDto(activityService.getAggregates(projectId, userId, offset, limit));
    }

    private static PagedActivityList toPagedActivityDto(PagedList<ActivityDto> src) {
        return new PagedActivityList(src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }

    private static PagedAggregateList toPagedAggregateDto(PagedList<AggregateDto> src) {
        return new PagedAggregateList(src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
