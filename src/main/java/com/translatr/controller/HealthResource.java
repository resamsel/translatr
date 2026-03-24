package com.translatr.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/api")
public class HealthResource {

    @GET
    @Path("/authclients")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> authClients() {
        // Returns configured auth providers — public endpoint
        return Map.of("providers", System.getenv().getOrDefault("AUTH_PROVIDERS", "keycloak"));
    }
}
