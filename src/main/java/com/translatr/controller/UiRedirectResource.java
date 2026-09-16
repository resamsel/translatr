package com.translatr.controller;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.net.URI;

/**
 * {@code GET /ui} (no trailing slash) doesn't match Quinoa's static {@code index.html} at
 * {@code /ui/}, so — like {@code /}, see {@link RootResource} — it used to 404-then-500.
 * Redirect to the trailing-slash form Quinoa does serve.
 *
 * <p>Prod only: dev/test's counterpart is {@link UiLandingResource}, which bounces to the
 * separate SPA dev-server origin instead of redirecting within this one; the two are
 * mutually exclusive by build profile so they never both claim {@code @Path("/ui")}.
 * Redirecting to plain {@code /ui} here (matching this same path) would infinite-loop —
 * the target must be {@code /ui/}.
 */
@IfBuildProfile("prod")
@Path("/ui")
public class UiRedirectResource {

    @GET
    @PermitAll
    public Response toUiSlash() {
        return Response.seeOther(URI.create("/ui/")).build();
    }
}
