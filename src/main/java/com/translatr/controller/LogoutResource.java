package com.translatr.controller;

import com.translatr.config.TranslatrConfig;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * Local logout: expires every Quarkus OIDC session/state cookie the browser sent and 303s back to
 * the SPA.
 *
 * <p>Quarkus' own RP-initiated logout ({@code quarkus.oidc.logout.path}) is deliberately <em>not</em>
 * configured on the tenants built by {@code TranslatrTenantConfigResolver}: it redirects to the
 * provider's {@code end_session_endpoint}, which Google, GitHub, Facebook, Apple and X do not
 * publish, so it would fail for most of the roster. Ending the local session is what the navbar's
 * logout link needs; signing the user out at the identity provider is tracked as
 * resamsel/translatr#258.</p>
 *
 * <p>{@code @PermitAll} so that hitting {@code /logout} without (or with a stale) session is a
 * harmless redirect rather than a 401 or a login challenge.</p>
 */
@Path("/logout")
public class LogoutResource {

    /** Covers q_session, q_session_&lt;tenant&gt;[_chunk_N], q_session_at/_rt, q_auth_*, q_post_logout. */
    private static final List<String> COOKIE_PREFIXES = List.of("q_session", "q_auth", "q_post_logout");

    private final TranslatrConfig config;

    @Inject
    public LogoutResource(TranslatrConfig config) {
        this.config = config;
    }

    @GET
    @PermitAll
    public Response logout(@Context HttpHeaders headers) {
        Response.ResponseBuilder response =
                Response.seeOther(URI.create(config.redirectBase() + "/ui"));

        headers.getCookies().keySet().stream()
                .filter(LogoutResource::isOidcCookie)
                .map(LogoutResource::expired)
                .forEach(response::cookie);

        return response.build();
    }

    static boolean isOidcCookie(String name) {
        return name != null && COOKIE_PREFIXES.stream().anyMatch(name::startsWith);
    }

    private static NewCookie expired(String name) {
        return new NewCookie.Builder(name)
                .value("")
                .path("/")
                .maxAge(0)
                .expiry(new Date(0))
                .httpOnly(true)
                .build();
    }
}
