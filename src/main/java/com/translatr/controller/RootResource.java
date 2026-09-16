package com.translatr.controller;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.net.URI;

/**
 * {@code GET /} has no static resource of its own — Quinoa serves the SPA under
 * {@code /ui/**} (see {@code quarkus.quinoa.ui-root-path}) — so it used to fall through to a
 * 404 that, via {@link com.translatr.dto.ErrorResponse}'s missing reflection registration,
 * surfaced as a 500 in the native build instead. Redirect to the SPA's landing page.
 *
 * <p>Prod only: in dev/test the SPA lives on a separate dev-server origin, so there is
 * nothing on this origin's {@code /} to land on.
 */
@IfBuildProfile("prod")
@Path("/")
public class RootResource {

    @GET
    @PermitAll
    public Response toUi() {
        return Response.seeOther(URI.create("/ui/")).build();
    }
}
