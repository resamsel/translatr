package com.translatr.auth;

import com.translatr.config.TranslatrConfig;
import com.translatr.config.TranslatrConfig.OidcProviderConfig;
import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.OidcTenantConfigBuilder;
import io.quarkus.oidc.TenantConfigResolver;
import io.quarkus.oidc.runtime.OidcTenantConfig.ApplicationType;
import io.quarkus.oidc.runtime.OidcTenantConfig.Provider;
import io.quarkus.oidc.runtime.OidcUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import org.jboss.logging.Logger;

/**
 * Resolves a per-request OIDC tenant from the {@code /login/{provider}} path (initial challenge) or
 * from the tenant id Quarkus already stashed on the {@link RoutingContext} from the
 * authorization-code state / session cookie (callback + later requests).
 *
 * <p>Returns {@code null} when the request is not an OIDC login, or names a provider that is absent
 * / has no {@code client-id} / has an unknown {@code provider} preset. With the static default
 * tenant disabled ({@code quarkus.oidc.tenant-enabled=false}) that yields a plain 401 for anonymous
 * callers.
 */
@ApplicationScoped
public class TranslatrTenantConfigResolver implements TenantConfigResolver {

    private static final Logger LOG = Logger.getLogger(TranslatrTenantConfigResolver.class);
    private static final Duration CONNECTION_DELAY = Duration.ofSeconds(5);
    private static final String LOGIN_PREFIX = "/login/";
    private static final String REDIRECT_PATH = "/authenticate";

    private final TranslatrConfig config;

    @Inject
    public TranslatrTenantConfigResolver(TranslatrConfig config) {
        this.config = config;
    }

    @Override
    public Uni<OidcTenantConfig> resolve(
            RoutingContext context, OidcRequestContext<OidcTenantConfig> requestContext) {
        String tenantId = context.get(OidcUtils.TENANT_ID_ATTRIBUTE);
        if (tenantId == null) {
            tenantId = tenantIdFromPath(context.normalizedPath());
        }
        if (tenantId == null) {
            return Uni.createFrom().nullItem();
        }

        OidcProviderConfig provider = config.auth().oidc().get(tenantId);
        if (provider == null || provider.clientId().map(String::isBlank).orElse(true)) {
            return Uni.createFrom().nullItem();
        }

        try {
            return Uni.createFrom().item(build(tenantId, provider));
        } catch (IllegalArgumentException e) {
            LOG.warnf("Ignoring auth provider '%s': %s", tenantId, e.getMessage());
            return Uni.createFrom().nullItem();
        }
    }

    private OidcTenantConfig build(String tenantId, OidcProviderConfig p) {
        OidcTenantConfigBuilder b = OidcTenantConfig.builder()
                .tenantId(tenantId)
                .applicationType(ApplicationType.HYBRID)
                .connectionDelay(CONNECTION_DELAY)
                .clientId(p.clientId().orElseThrow());

        var authentication = b.authentication().redirectPath(REDIRECT_PATH).restorePathAfterRedirect(true);
        List<String> scopes = effectiveScopes(p);
        if (!scopes.isEmpty()) {
            // Only set when configured: an explicit empty list would suppress the scopes a
            // built-in `provider` preset contributes (OidcUtils#resolveProviderConfig only fills
            // in fields the resolved config leaves unset).
            authentication.scopes(scopes);
        }
        authentication.end();

        p.clientSecret()
                .filter(s -> !s.isBlank())
                .ifPresent(s -> b.credentials().clientSecret(s).end());

        // Throws IllegalArgumentException for an unknown preset name; caught by resolve().
        p.provider()
                .filter(s -> !s.isBlank())
                .map(s -> Provider.valueOf(s.toUpperCase(Locale.ROOT)))
                .ifPresent(b::provider);
        p.authServerUrl().filter(s -> !s.isBlank()).ifPresent(b::authServerUrl);

        return b.build();
    }

    private static List<String> effectiveScopes(OidcProviderConfig p) {
        return p.scopes().orElseGet(List::of).stream()
                .filter(s -> !s.isBlank())
                .toList();
    }

    /** @return the segment after {@code /login/}, or {@code null}. */
    static String tenantIdFromPath(String normalizedPath) {
        if (normalizedPath == null || !normalizedPath.startsWith(LOGIN_PREFIX)) {
            return null;
        }
        String rest = normalizedPath.substring(LOGIN_PREFIX.length());
        int slash = rest.indexOf('/');
        if (slash >= 0) {
            rest = rest.substring(0, slash);
        }
        return rest.isBlank() ? null : rest;
    }
}
