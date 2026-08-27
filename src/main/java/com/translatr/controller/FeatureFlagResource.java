package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.FeatureFlagCriteria;
import com.translatr.dto.FeatureFlagDto;
import com.translatr.dto.PagedList;
import com.translatr.service.FeatureFlagService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/api")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeatureFlagResource {

    private final FeatureFlagService  featureFlagService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public FeatureFlagResource(FeatureFlagService featureFlagService, CurrentUserResolver currentUserResolver) {
        this.featureFlagService  = featureFlagService;
        this.currentUserResolver = currentUserResolver;
    }

    @GET
    @Path("/featureflags")
    public PagedList<FeatureFlagDto> find(@BeanParam FeatureFlagCriteria criteria) {
        var user = currentUserResolver.resolve();
        return featureFlagService.find(criteria, user.id);
    }

    @GET
    @Path("/featureflag/{id}")
    public FeatureFlagDto get(@PathParam("id") UUID id) {
        return featureFlagService.get(id);
    }

    @POST
    @Path("/featureflag")
    public FeatureFlagDto create(FeatureFlagDto dto) {
        return featureFlagService.create(dto);
    }

    @PUT
    @Path("/featureflag")
    public FeatureFlagDto update(FeatureFlagDto dto) {
        return featureFlagService.update(dto);
    }

    @DELETE
    @Path("/featureflag/{id}")
    public FeatureFlagDto delete(@PathParam("id") UUID id) {
        return featureFlagService.delete(id);
    }
}
