package com.translatr.controller;

import com.translatr.config.TranslatrConfig;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.net.URI;

/**
 * Receives the OIDC authorization-code callback from Keycloak.
 *
 * <p>Quarkus OIDC registers a Vert.x handler for the configured
 * {@code quarkus.oidc.authentication.redirect-path} ({@code /authenticate}).
 * When Keycloak redirects the browser back here with {@code ?code=…&state=…},
 * the OIDC mechanism intercepts the request at the Vert.x layer (before
 * JAX-RS), exchanges the code for tokens, writes the session cookie, and
 * issues a 302 to the original URL that was saved in the OAuth {@code state}
 * (typically {@code /login/{provider}?redirect_uri=…}).
 * <strong>The {@code callback()} method body is therefore never reached during
 * a normal OAuth flow.</strong></p>
 *
 * <p>The {@code @Authenticated} annotation is essential: it ensures Quarkus's
 * security infrastructure (and therefore the OIDC mechanism) is invoked for
 * this path even when proactive authentication is not otherwise active.
 * Without it the request falls straight through to JAX-RS, which has no
 * handler and returns 404.</p>
 *
 * <p>The method body acts as a fallback for direct browser navigation to
 * {@code /authenticate} (no OAuth parameters).  In that case {@code @Authenticated}
 * triggers a fresh Keycloak redirect; after login, OIDC redirects back here
 * with a valid session and the user is forwarded to the main UI.</p>
 */
@Path("/authenticate")
public class AuthenticateResource {

    private final TranslatrConfig config;

    @Inject
    public AuthenticateResource(TranslatrConfig config) {
        this.config = config;
    }

    @GET
    @Authenticated
    public Response callback() {
        // OIDC should have handled the response (redirect) before reaching here.
        // Fallback: send the authenticated user to the main UI.
        return Response.seeOther(URI.create(config.redirectBase() + "/ui")).build();
    }
}
