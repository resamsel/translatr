package com.translatr.controller;

import com.translatr.dto.AuthClientDto;
import com.translatr.service.AuthProviderStatusService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Returns the auth providers a visitor can actually use: named in
 * {@code translatr.auth.providers} AND backed by a configured client id/secret.
 * Each maps to {@code /login/{key}}, handled by {@link LoginResource} + Quarkus OIDC.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class AuthClientsResource {

    private final AuthProviderStatusService statusService;

    @Inject
    public AuthClientsResource(AuthProviderStatusService statusService) {
        this.statusService = statusService;
    }

    @GET
    @Path("/authclients")
    @PermitAll
    public List<AuthClientDto> find() {
        return statusService.active().stream()
                .map(s -> {
                    AuthClientDto dto = new AuthClientDto();
                    dto.key = s.key();
                    dto.url = "/login/" + s.key();
                    return dto;
                })
                .toList();
    }
}
