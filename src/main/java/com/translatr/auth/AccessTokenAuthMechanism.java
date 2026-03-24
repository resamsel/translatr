package com.translatr.auth;

import com.translatr.service.AccessTokenService;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.Set;

/**
 * Handles API access token authentication via:
 *   - Header:       Authorization: Bearer <token>  OR  X-Access-Token: <token>
 *   - Query param:  ?access_token=<token>
 *
 * Falls through (returns null) when no access token is present, letting
 * Quarkus OIDC handle browser/OAuth flows.
 */
@ApplicationScoped
@Alternative
@Priority(1)
public class AccessTokenAuthMechanism implements HttpAuthenticationMechanism {

    static final String HEADER_NAME = "X-Access-Token";
    static final String QUERY_PARAM = "access_token";

    @Inject AccessTokenService tokenService;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
                                              IdentityProviderManager identityProviderManager) {
        String token = extractToken(context);
        if (token == null) return Uni.createFrom().nullItem();

        return tokenService.findUserByKey(token)
                .map(user -> (SecurityIdentity) new AccessTokenSecurityIdentity(user, token))
                .map(Uni.createFrom()::item)
                .orElseGet(() -> Uni.createFrom().nullItem());
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(
                new ChallengeData(401, "WWW-Authenticate", "Bearer realm=\"Translatr\""));
    }

    @Override
    public Set<Class<? extends io.quarkus.security.identity.request.AuthenticationRequest>> getCredentialTypes() {
        return Set.of(TokenAuthenticationRequest.class);
    }

    private String extractToken(RoutingContext ctx) {
        // Check X-Access-Token header
        String header = ctx.request().getHeader(HEADER_NAME);
        if (header != null && !header.isBlank()) return header.trim();

        // Check Authorization: Bearer <token>
        String auth = ctx.request().getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String bearer = auth.substring(7).trim();
            // If it looks like a UUID / opaque token (not a JWT), treat as access token
            if (!bearer.contains(".")) return bearer;
        }

        // Check query param
        String query = ctx.request().getParam(QUERY_PARAM);
        if (query != null && !query.isBlank()) return query.trim();

        return null;
    }
}
