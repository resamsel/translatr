package com.translatr.controller;

import com.translatr.config.TranslatrConfig;
import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.net.URI;

/**
 * Landing point for {@code GET /ui} on the backend origin.
 *
 * <p>Two flows land here on the backend origin: the OIDC login redirect (post-authentication),
 * and {@link LogoutResource}'s local-session-clear redirect. In dev the SPA lives on a separate
 * dev-server origin ({@link TranslatrConfig#redirectBase()}{@code + "/ui"}), so bounce there.
 *
 * <p>Excluded from the {@code prod} build: there the SPA is served from this same origin by
 * Quinoa under {@code /ui}, so a JAX-RS resource on {@code @Path("/ui")} would both shadow
 * Quinoa's SPA routing for {@code /ui/**} and 303-loop {@code <backend>/ui} onto itself.
 * Active in {@code dev} and {@code test}.
 */
@UnlessBuildProfile("prod")
@Path("/ui")
public class UiLandingResource {

    private final TranslatrConfig config;

    @Inject
    public UiLandingResource(TranslatrConfig config) {
        this.config = config;
    }

    @GET
    @PermitAll
    public Response toUi() {
        return Response.seeOther(URI.create(config.redirectBase() + "/ui")).build();
    }
}
