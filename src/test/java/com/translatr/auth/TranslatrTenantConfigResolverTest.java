package com.translatr.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.translatr.config.TranslatrConfig;
import com.translatr.config.TranslatrConfig.OidcProviderConfig;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.runtime.OidcUtils;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranslatrTenantConfigResolverTest {

    @Mock TranslatrConfig config;
    @Mock TranslatrConfig.AuthConfig authConfig;
    @Mock RoutingContext ctx;

    private TranslatrTenantConfigResolver newResolver(Map<String, OidcProviderConfig> providers) {
        lenient().when(config.auth()).thenReturn(authConfig);
        lenient().when(authConfig.oidc()).thenReturn(providers);
        return new TranslatrTenantConfigResolver(config);
    }

    private static OidcProviderConfig provider(
            String preset, String authServerUrl, String clientId, String clientSecret, List<String> scopes) {
        return new OidcProviderConfig() {
            @Override
            public Optional<String> provider() {
                return Optional.ofNullable(preset);
            }

            @Override
            public Optional<String> authServerUrl() {
                return Optional.ofNullable(authServerUrl);
            }

            @Override
            public Optional<String> clientId() {
                return Optional.ofNullable(clientId);
            }

            @Override
            public Optional<String> clientSecret() {
                return Optional.ofNullable(clientSecret);
            }

            @Override
            public Optional<List<String>> scopes() {
                return Optional.ofNullable(scopes);
            }
        };
    }

    @Test
    void tenantIdFromPath_extractsTheProviderSegment() {
        assertThat(TranslatrTenantConfigResolver.tenantIdFromPath("/login/google")).isEqualTo("google");
        assertThat(TranslatrTenantConfigResolver.tenantIdFromPath("/login/google/")).isEqualTo("google");
        assertThat(TranslatrTenantConfigResolver.tenantIdFromPath("/login/")).isNull();
        assertThat(TranslatrTenantConfigResolver.tenantIdFromPath("/api/projects")).isNull();
        assertThat(TranslatrTenantConfigResolver.tenantIdFromPath("/authenticate")).isNull();
        assertThat(TranslatrTenantConfigResolver.tenantIdFromPath(null)).isNull();
    }

    @Test
    void resolve_returnsNull_forNonLoginPathWithNoTenantAttribute() {
        var resolver = newResolver(Map.of());
        when(ctx.get(OidcUtils.TENANT_ID_ATTRIBUTE)).thenReturn(null);
        when(ctx.normalizedPath()).thenReturn("/api/projects");

        assertThat(resolver.resolve(ctx, null).await().indefinitely()).isNull();
    }

    @Test
    void resolve_returnsNull_forUnknownProvider() {
        var resolver = newResolver(Map.of());
        when(ctx.normalizedPath()).thenReturn("/login/nonesuch");

        assertThat(resolver.resolve(ctx, null).await().indefinitely()).isNull();
    }

    @Test
    void resolve_returnsNull_forUnknownPresetName() {
        var resolver =
                newResolver(Map.of("bogus", provider("nosuchpreset", null, "cid", "csecret", List.of("email"))));
        when(ctx.normalizedPath()).thenReturn("/login/bogus");

        assertThat(resolver.resolve(ctx, null).await().indefinitely()).isNull();
    }

    @Test
    void resolve_buildsPresetConfig_fromPath() {
        var resolver = newResolver(Map.of("google", provider("google", null, "gid", "gsecret", List.of("email", "profile"))));
        when(ctx.normalizedPath()).thenReturn("/login/google");

        OidcTenantConfig built = resolver.resolve(ctx, null).await().indefinitely();

        assertThat(built).isNotNull();
        assertThat(built.tenantId()).contains("google");
        assertThat(built.clientId()).contains("gid");
        assertThat(built.credentials().clientSecret().value()).contains("gsecret");
        assertThat(built.provider()).contains(io.quarkus.oidc.runtime.OidcTenantConfig.Provider.GOOGLE);
        assertThat(built.applicationType())
                .contains(io.quarkus.oidc.runtime.OidcTenantConfig.ApplicationType.HYBRID);
        assertThat(built.authentication().scopes()).contains(List.of("email", "profile"));
        // #245: the login round-trip must come back to /authenticate and restore the original path.
        assertThat(built.authentication().redirectPath()).contains("/authenticate");
        assertThat(built.authentication().restorePathAfterRedirect()).isTrue();
    }

    /**
     * OidcAuthenticationMechanism#setTenantIdAttribute stamps TENANT_ID_ATTRIBUTE from the first
     * {@code q_session} / {@code q_auth} cookie on every request, so a user who already has a
     * Keycloak session carries "keycloak" into GET /login/google. The path has to win, or provider
     * switching is impossible for anyone with a session.
     */
    @Test
    void resolve_prefersPath_overConflictingAttribute() {
        var resolver = newResolver(Map.of(
                "google", provider("google", null, "gid", "gsecret", List.of("email")),
                "keycloak",
                provider(null, "http://kc/realms/Translatr", "kid", "ksecret", List.of("microprofile-jwt"))));
        lenient().when(ctx.get(OidcUtils.TENANT_ID_ATTRIBUTE)).thenReturn("keycloak");
        when(ctx.normalizedPath()).thenReturn("/login/google");

        OidcTenantConfig built = resolver.resolve(ctx, null).await().indefinitely();

        assertThat(built).isNotNull();
        assertThat(built.tenantId()).contains("google");
        assertThat(built.clientId()).contains("gid");
    }

    /** /authenticate (and every other non-/login path) has no provider segment, so the attribute wins. */
    @Test
    void resolve_fallsBackToAttribute_whenPathNamesNoProvider() {
        var resolver = newResolver(Map.of(
                "keycloak",
                provider(null, "http://kc/realms/Translatr", "kid", "ksecret", List.of("microprofile-jwt"))));
        when(ctx.normalizedPath()).thenReturn("/authenticate");
        when(ctx.get(OidcUtils.TENANT_ID_ATTRIBUTE)).thenReturn("keycloak");

        OidcTenantConfig built = resolver.resolve(ctx, null).await().indefinitely();

        assertThat(built).isNotNull();
        assertThat(built.tenantId()).contains("keycloak");
        assertThat(built.authServerUrl()).contains("http://kc/realms/Translatr");
        assertThat(built.provider()).isEmpty();
        // #245: same guarantees on the tenant Quarkus resolved from the state / session cookie.
        assertThat(built.authentication().redirectPath()).contains("/authenticate");
        assertThat(built.authentication().restorePathAfterRedirect()).isTrue();
    }

    /**
     * Quarkus' RP-initiated logout would redirect to the provider's end_session_endpoint, which the
     * social presets do not publish; GET /logout is served by LogoutResource instead.
     */
    @Test
    void resolve_leavesLogoutPathUnset() {
        var resolver = newResolver(Map.of("google", provider("google", null, "gid", "gsecret", List.of())));
        when(ctx.normalizedPath()).thenReturn("/login/google");

        OidcTenantConfig built = resolver.resolve(ctx, null).await().indefinitely();

        assertThat(built).isNotNull();
        assertThat(built.logout().path()).isEmpty();
    }

    @Test
    void resolve_returnsNull_whenClientIdMissing() {
        var resolver = newResolver(Map.of("google", provider("google", null, null, null, List.of())));
        when(ctx.normalizedPath()).thenReturn("/login/google");

        assertThat(resolver.resolve(ctx, null).await().indefinitely()).isNull();
    }

    @Test
    void resolve_leavesScopesUnset_whenNoneConfigured() {
        var resolver = newResolver(Map.of("google", provider("google", null, "gid", "gsecret", null)));
        when(ctx.normalizedPath()).thenReturn("/login/google");

        OidcTenantConfig built = resolver.resolve(ctx, null).await().indefinitely();

        assertThat(built).isNotNull();
        assertThat(built.authentication().scopes()).isEmpty();
    }
}
