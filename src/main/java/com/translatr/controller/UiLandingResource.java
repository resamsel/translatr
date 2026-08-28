package com.translatr.controller;

import com.translatr.config.TranslatrConfig;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.net.URI;

/**
 * Landing point for {@code GET /ui} on the backend origin.
 *
 * <p>Quarkus OIDC's RP-initiated logout ({@code quarkus.oidc.logout.post-logout-path=/ui})
 * builds {@code post_logout_redirect_uri} relative to the backend origin, so after Keycloak
 * logout the browser lands on {@code <backend>/ui}. The SPA is actually reached at
 * {@link TranslatrConfig#redirectBase()}{@code + "/ui"} (a separate dev-server origin in dev,
 * the same origin in prod), so forward there — the same target the login flow uses.</p>
 */
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
