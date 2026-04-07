package com.translatr.controller;

import com.translatr.config.TranslatrConfig;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.net.URI;

/**
 * Initiates the OIDC authorization-code login flow for the requested provider.
 *
 * <p>{@code @Authenticated} causes Quarkus OIDC (hybrid mode) to call
 * {@code HttpAuthenticationMechanism.sendChallenge()} when there is no session.
 * For browser requests ({@code Accept: text/html}) that issues a 302 redirect to
 * Keycloak; for AJAX/API requests it returns 401. The
 * {@code ExceptionMappers.UnauthorizedMapper} has been intentionally removed so
 * it cannot intercept the exception before {@code sendChallenge()} runs.</p>
 *
 * <p>After Keycloak authenticates the user, Quarkus handles the callback at
 * {@code /authenticate} (configured via
 * {@code quarkus.oidc.authentication.redirect-path}) and redirects back here.
 * The method then forwards the browser to the UI.</p>
 */
@Path("/login")
public class LoginResource {

    private final TranslatrConfig config;

    @Inject
    public LoginResource(TranslatrConfig config) {
        this.config = config;
    }

    /**
     * Entry-point that the Angular login page navigates to.
     *
     * @param provider    the OIDC provider key (e.g. {@code keycloak})
     * @param redirectUri optional Angular route to return to after login (e.g. {@code /ui/projects})
     */
    @GET
    @Path("/{provider}")
    @Authenticated
    public Response login(
            @PathParam("provider") String provider,
            @QueryParam("redirect_uri") String redirectUri) {

        String target;
        if (redirectUri != null && !redirectUri.isBlank()) {
            if (redirectUri.startsWith("http://") || redirectUri.startsWith("https://")) {
                // Absolute URL supplied by the caller – use as-is
                target = redirectUri;
            } else {
                // Relative Angular route (e.g. /ui/projects) – prefix with the configured base
                target = config.redirectBase() + redirectUri;
            }
        } else {
            // Default: send the user to the main UI
            target = config.redirectBase() + "/ui";
        }

        return Response.seeOther(URI.create(target)).build();
    }
}
