package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.dto.OidcProviderStatusDto;
import com.translatr.service.AuthProviderStatusService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.ForbiddenException;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

/** Admin-only view of auth-provider configuration and per-provider errors. */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class OidcProviderResource {

    private final AuthProviderStatusService statusService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public OidcProviderResource(AuthProviderStatusService statusService,
                                CurrentUserResolver currentUserResolver) {
        this.statusService = statusService;
        this.currentUserResolver = currentUserResolver;
    }

    @GET
    @Path("/oidc-providers")
    @Authenticated
    @Operation(summary = "List OIDC provider configuration and diagnostics (admin).")
    @APIResponse(responseCode = "200", description = "Provider status list; client secret masked.")
    @APIResponse(responseCode = "403", description = "Caller is not an admin.")
    public List<OidcProviderStatusDto> list() {
        requireAdmin();
        return statusService.evaluateAll().stream().map(OidcProviderStatusDto::from).toList();
    }

    private void requireAdmin() {
        if (!currentUserResolver.resolve().isAdmin()) {
            throw new ForbiddenException("Admin role required");
        }
    }
}
