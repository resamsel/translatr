package com.translatr.auth;

import com.translatr.service.AccessTokenService;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

/**
 * Handles API access token authentication via:
 *   - Header:       Authorization: Bearer <token>  OR  X-Access-Token: <token>
 *   - Query param:  ?access_token=<token>
 *
 * Runs alongside the Quarkus OIDC mechanism (do NOT use @Alternative here –
 * that would replace OIDC entirely).  Returns null from both authenticate() and
 * getChallenge() when no access token is present, so the OIDC mechanism can
 * handle browser/OAuth flows and issue the Keycloak redirect.
 */
@ApplicationScoped
public class AccessTokenAuthMechanism implements HttpAuthenticationMechanism {

    static final String HEADER_NAME = "X-Access-Token";
    static final String QUERY_PARAM = "access_token";

    private final AccessTokenService tokenService;
    private final io.micrometer.core.instrument.MeterRegistry registry;

    public AccessTokenAuthMechanism(AccessTokenService tokenService,
                                     io.micrometer.core.instrument.MeterRegistry registry) {
        this.tokenService = tokenService;
        this.registry     = registry;
    }

    /**
     * Master gate for the branch's custom instrumentation — see
     * {@code com.translatr.config.TranslatrConfig.ObservabilityConfig#metricsEnabled}. When
     * {@code false} (the default outside the SigNoz overlay and {@code @QuarkusTest}), the
     * {@code translatr.apikey.auth.failures} counter is not touched.
     */
    @ConfigProperty(name = "translatr.observability.metrics-enabled", defaultValue = "false")
    boolean metricsEnabled;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
                                              IdentityProviderManager identityProviderManager) {
        String rawHeader = context.request().getHeader(HEADER_NAME);
        String token = extractToken(context);
        if (token == null) {
            // A fully token-less request is a normal OIDC/browser flow — not an API-key
            // failure. Only an explicit but blank X-Access-Token header counts as "missing".
            if (metricsEnabled && rawHeader != null && rawHeader.isBlank()) {
                registry.counter("translatr.apikey.auth.failures", "reason", "missing").increment();
            }
            return Uni.createFrom().nullItem();
        }

        // tokenService.findByKey() runs a blocking Hibernate/Panache query, but
        // HttpAuthenticationMechanism#authenticate() is invoked on the Vert.x IO thread.
        // Offload to the worker pool to avoid BlockingOperationNotAllowedException.
        return Uni.createFrom().item(() -> tokenService.findByKey(token))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .map(tokenOpt -> tokenOpt
                        .<SecurityIdentity>map(AccessTokenSecurityIdentity::new)
                        .orElseGet(() -> {
                            // A token string was supplied but matched no AccessToken entity.
                            if (metricsEnabled) {
                                registry.counter("translatr.apikey.auth.failures", "reason", "invalid").increment();
                            }
                            return null;
                        }));
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        if (extractToken(context) == null) {
            // No access token in this request – defer the challenge to the OIDC mechanism
            // so it can issue the browser redirect to Keycloak instead of a bare 401.
            return Uni.createFrom().nullItem();
        }
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
