package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.dto.GlobalFeatureFlagDto;
import com.translatr.dto.ResolvedFeatureDto;
import com.translatr.service.FeatureResolver;
import com.translatr.service.GlobalFeatureFlagService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GlobalFeatureFlagResource {

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

    /** override → global → default detail for the caller, one entry per feature. */
    @GET
    @Path("/featureflags/resolved")
    public List<ResolvedFeatureDto> resolved() {
        return featureResolver.resolveDetail(currentUserResolver.resolve().id);
    }

    @GET
    @Path("/featureflags/global")
    public List<GlobalFeatureFlagDto> listGlobal() {
        return globalService.list();
    }

    @POST
    @Path("/featureflag/global")
    public GlobalFeatureFlagDto setGlobal(GlobalFeatureFlagDto dto) {
        requireAdmin();
        return globalService.set(dto.feature, dto.enabled);
    }

    @DELETE
    @Path("/featureflag/global/{id}")
    public Response deleteGlobal(@PathParam("id") UUID id) {
        requireAdmin();
        globalService.delete(id);
        return Response.noContent().build();
    }

    private void requireAdmin() {
        if (!currentUserResolver.resolve().isAdmin()) {
            throw new ForbiddenException("Admin role required");
        }
    }
}
