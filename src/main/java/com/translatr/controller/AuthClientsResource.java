package com.translatr.controller;

import com.translatr.config.TranslatrConfig;
import com.translatr.dto.AuthClientDto;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns the list of configured authentication providers.
 * The provider names come from {@code translatr.auth.providers} (comma-separated).
 * Each entry maps to the {@code /login/{provider}} URL handled by Quarkus OIDC.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class AuthClientsResource {

    @Inject TranslatrConfig config;

    @GET
    @Path("/authclients")
    @PermitAll
    public List<AuthClientDto> find() {
        return Arrays.stream(config.auth().providers().split(","))
                .map(String::trim)
                .filter(p -> !p.isBlank())
                .map(p -> {
                    AuthClientDto dto = new AuthClientDto();
                    dto.key = p;
                    dto.url = "/login/" + p;
                    return dto;
                })
                .collect(Collectors.toList());
    }
}

