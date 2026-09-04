package com.translatr.controller;

import com.translatr.config.TranslatrConfig;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

/**
 * Initiates the OIDC authorization-code login flow for the requested provider.
 *
 * <p>{@code @Authenticated} causes Quarkus OIDC (hybrid mode) to call
 * {@code HttpAuthenticationMechanism.sendChallenge()} when there is no session.
 * For browser requests ({@code Accept: text/html}) that issues a redirect to the
 * authorization endpoint of the provider that {@code TranslatrTenantConfigResolver}
 * resolved from the {@code /login/{provider}} path; for AJAX/API requests it returns
 * 401. The {@code ExceptionMappers.UnauthorizedMapper} has been intentionally removed
 * so it cannot intercept the exception before {@code sendChallenge()} runs.</p>
 *
 * <p>After the provider authenticates the user, Quarkus restores the original request
 * URI — {@code authentication.restore-path-after-redirect} is set on the per-tenant
 * {@code OidcTenantConfig} the resolver builds, not by a static property — and the
 * browser lands back here <em>with a session</em> and the original
 * {@code ?redirect_uri=}. This method then 303s to that route, so re-authenticating
 * mid-session returns the user to where they were (#245).</p>
 *
 * <p>{@code redirect_uri} is only honored when it is a site-relative path or an
 * absolute URL whose origin is trusted (the {@code redirect-base} origin or one of
 * {@code translatr.allowed-redirect-origins}); anything else falls back to {@code /ui}
 * so this endpoint cannot be used as an open redirector.</p>
 */
@Path("/login")
public class LoginResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class);

    private final TranslatrConfig config;

    @Inject
    public LoginResource(TranslatrConfig config) {
        this.config = config;
    }

    /**
     * Entry-point that the Angular login page navigates to.
     *
     * @param provider    the OIDC provider key (e.g. {@code keycloak})
     * @param redirectUri optional route to return to after login (e.g. {@code /ui/projects})
     */
    @GET
    @Path("/{provider}")
    @Authenticated
    public Response login(
            @PathParam("provider") String provider,
            @QueryParam("redirect_uri") String redirectUri) {

        TranslatrConfig.OidcProviderConfig p = config.auth().oidc().get(provider);
        if (p == null || p.clientId().map(String::isBlank).orElse(true)) {
            throw new jakarta.ws.rs.NotFoundException(
                    "unknown or unconfigured auth provider: " + provider);
        }

        return Response.seeOther(resolveTarget(redirectUri)).build();
    }

    private URI resolveTarget(String redirectUri) {
        URI target = safeTarget(redirectUri);
        return target != null ? target : URI.create(config.redirectBase() + "/ui");
    }

    /**
     * @return the redirect target for {@code redirectUri}, or {@code null} if it is absent,
     *         malformed, or points at an untrusted origin (caller falls back to {@code /ui}).
     */
    private URI safeTarget(String redirectUri) {
        if (redirectUri == null || redirectUri.isBlank()) {
            return null;
        }

        String candidate = redirectUri.trim();
        try {
            if (isSiteRelativePath(candidate)) {
                return URI.create(config.redirectBase() + candidate);
            }

            URI parsed = new URI(candidate);
            if (parsed.isAbsolute() && isTrustedOrigin(originOf(parsed))) {
                return parsed;
            }
        } catch (URISyntaxException | IllegalArgumentException e) {
            LOG.debugf("Ignoring unparseable redirect_uri '%s': %s", candidate, e.getMessage());
        }

        return null;
    }

    /** A path rooted at the backend origin: single leading slash, no scheme/authority, no backslash trick. */
    private static boolean isSiteRelativePath(String value) throws URISyntaxException {
        if (value.isEmpty() || value.charAt(0) != '/') {
            return false;
        }
        if (value.length() >= 2 && (value.charAt(1) == '/' || value.charAt(1) == '\\')) {
            return false;
        }
        URI parsed = new URI(value);
        return parsed.getScheme() == null && parsed.getAuthority() == null && parsed.getHost() == null;
    }

    private boolean isTrustedOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        if (origin.equals(originOf(safeUri(config.redirectBase())))) {
            return true;
        }
        return config.allowedRedirectOrigins().orElseGet(List::of).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(LoginResource::safeUri)
                .map(LoginResource::originOf)
                .anyMatch(origin::equals);
    }

    private static URI safeUri(String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /** {@code scheme://host[:port]}, lower-cased, or {@code null} if {@code uri} has no scheme/host. */
    private static String originOf(URI uri) {
        if (uri == null || uri.getScheme() == null || uri.getHost() == null) {
            return null;
        }
        String origin = uri.getScheme().toLowerCase(Locale.ROOT) + "://" + uri.getHost().toLowerCase(Locale.ROOT);
        return uri.getPort() != -1 ? origin + ":" + uri.getPort() : origin;
    }
}
