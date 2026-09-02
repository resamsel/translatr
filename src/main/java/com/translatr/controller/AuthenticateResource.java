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
 * JAX-RS), exchanges the code for tokens and writes the session cookie. With
 * {@code quarkus.oidc.authentication.restore-path-after-redirect=true} it then
 * issues a 302 to the original request URI that was saved in the OAuth
 * {@code state} (typically {@code /login/{provider}?redirect_uri=…}), so
 * {@link LoginResource} does the final redirect. <strong>The {@code callback()}
 * method body is therefore not reached during a normal OAuth flow.</strong></p>
 *
 * <p>The {@code @Authenticated} annotation is essential: it ensures Quarkus's
 * security infrastructure (and therefore the OIDC mechanism) is invoked for
 * this path even when proactive authentication is not otherwise active.
 * Without it the request falls straight through to JAX-RS, which has no
 * handler and returns 404.</p>
 *
 * <p>The method body is a fallback for bare browser navigation to
 * {@code /authenticate} (no OAuth parameters, no saved state).  In that case
 * {@code @Authenticated} triggers a fresh Keycloak redirect; after login the
 * user lands here with a valid session and is forwarded to the main UI.</p>
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
