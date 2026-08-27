package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.dto.ActivityDto;
import com.translatr.dto.AggregateDto;
import com.translatr.dto.PagedList;
import com.translatr.service.ActivityService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class ActivityResource {

    private final ActivityService     activityService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public ActivityResource(ActivityService activityService, CurrentUserResolver currentUserResolver) {
        this.activityService     = activityService;
        this.currentUserResolver = currentUserResolver;
    }

    @GET @Path("/activities") @PermitAll
    public PagedList<ActivityDto> find(@QueryParam("offset") @DefaultValue("0") int offset,
                                       @QueryParam("limit")  @DefaultValue("20") int limit) {
        var user = currentUserResolver.resolve();
        return activityService.findByUser(user.id, offset, limit);
    }

    @GET @Path("/user/{userId}/activity") @PermitAll
    public PagedList<ActivityDto> byUser(@PathParam("userId") UUID userId,
                                         @QueryParam("offset") @DefaultValue("0") int offset,
                                         @QueryParam("limit")  @DefaultValue("20") int limit) {
        return activityService.findByUser(userId, offset, limit);
    }

    /**
     * Public aggregated activity (daily counts), used by the dashboard chart.
     * Optional filters: projectId, userId.
     */
    @GET @Path("/activities/aggregated") @PermitAll
    public PagedList<AggregateDto> aggregated(
            @QueryParam("projectId") UUID projectId,
            @QueryParam("userId")    UUID userId,
            @QueryParam("offset")    @DefaultValue("0")    int offset,
            @QueryParam("limit")     @DefaultValue("1000") int limit) {
        return activityService.getAggregates(projectId, userId, offset, limit);
    }
}
