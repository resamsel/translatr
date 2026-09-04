package com.translatr.service;

import com.translatr.config.TranslatrConfig;
import com.translatr.config.TranslatrConfig.OidcProviderConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthProviderStatusServiceTest {

    @Mock TranslatrConfig config;
    @Mock TranslatrConfig.AuthConfig authConfig;

    private AuthProviderStatusService svc(String providers, Map<String, OidcProviderConfig> oidc) {
        lenient().when(config.auth()).thenReturn(authConfig);
        lenient().when(authConfig.providers()).thenReturn(providers);
        lenient().when(authConfig.oidc()).thenReturn(oidc);
        return new AuthProviderStatusService(config);
    }

    private static OidcProviderConfig p(String preset, String authServerUrl, String clientId, String secret) {
        return new OidcProviderConfig() {
            public Optional<String> provider()      { return Optional.ofNullable(preset); }
            public Optional<String> authServerUrl() { return Optional.ofNullable(authServerUrl); }
            public Optional<String> clientId()      { return Optional.ofNullable(clientId); }
            public Optional<String> clientSecret()  { return Optional.ofNullable(secret); }
            public Optional<List<String>> scopes()  { return Optional.of(List.of()); }
        };
    }

    @Test
    void maskSecret_showsLengthOnly() {
        assertThat(AuthProviderStatusService.maskSecret("abcdef")).isEqualTo("***len:6***");
        assertThat(AuthProviderStatusService.maskSecret("")).isNull();
        assertThat(AuthProviderStatusService.maskSecret(null)).isNull();
    }

    @Test
    void configuredAndListed_isActive_andSecretMasked() {
        var s = svc("keycloak,google",
                Map.of("keycloak", p(null, "http://kc/realms/Translatr", "kid", "kc-secret-value"),
                       "google",   p("google", null, "gid", "gsecret")))
                .evaluateAll();

        var google = s.stream().filter(x -> x.key().equals("google")).findFirst().orElseThrow();
        assertThat(google.listed()).isTrue();
        assertThat(google.active()).isTrue();
        assertThat(google.errors()).isEmpty();
        assertThat(google.clientId()).isEqualTo("gid");
        assertThat(google.clientSecret()).isEqualTo("***len:7***");
        assertThat(s.toString()).doesNotContain("gsecret").doesNotContain("kc-secret-value");
    }

    @Test
    void listedButNoSecret_isNotActive_andReportsError() {
        var google = svc("google", Map.of("google", p("google", null, "gid", null)))
                .evaluateAll().get(0);
        assertThat(google.active()).isFalse();
        assertThat(google.clientSecret()).isNull();
        assertThat(google.errors()).contains("client-secret is missing");
    }

    @Test
    void configuredButNotListed_hasListedFalse() {
        var s = svc("keycloak",
                Map.of("keycloak", p(null, "http://kc", "kid", "ksecret"),
                       "github",   p("github", null, "ghid", "ghsecret")))
                .evaluateAll();
        var github = s.stream().filter(x -> x.key().equals("github")).findFirst().orElseThrow();
        assertThat(github.listed()).isFalse();
        assertThat(github.active()).isFalse();
    }

    @Test
    void unknownPreset_reportsError() {
        var x = svc("typo", Map.of("typo", p("gooogle", null, "id", "secret"))).evaluateAll().get(0);
        assertThat(x.errors()).contains("unknown provider preset 'gooogle'");
        assertThat(x.active()).isFalse();
    }

    /**
     * KNOWN_PRESETS is derived from OidcTenantConfig.Provider, so every preset the resolver accepts
     * is reported as valid here — including `x`, which a hardcoded copy of the list had missed.
     */
    @Test
    void everyQuarkusPresetIsKnown() {
        for (var preset : new String[] {"google", "github", "microsoft", "apple", "x"}) {
            var s = svc(preset, Map.of(preset, p(preset, null, "id", "secret"))).evaluateAll().get(0);
            assertThat(s.errors()).as("preset %s", preset).isEmpty();
            assertThat(s.active()).as("preset %s", preset).isTrue();
        }
    }

    /** AUTH_PROVIDERS=google,google must not render two identical login buttons. */
    @Test
    void duplicateListedProvider_isReportedOnce() {
        var s = svc("google,google", Map.of("google", p("google", null, "gid", "gsecret"))).evaluateAll();
        assertThat(s).extracting(OidcProviderStatus::key).containsExactly("google");
    }

    @Test
    void noPresetAndNoAuthServerUrl_reportsError() {
        var x = svc("x", Map.of("x", p(null, null, "id", "secret"))).evaluateAll().get(0);
        assertThat(x.errors()).contains("auth-server-url is required for a provider without a built-in preset");
    }

    @Test
    void active_isTheActiveSubset() {
        var svc = svc("keycloak,google",
                Map.of("keycloak", p(null, "http://kc", "kid", "ksecret"),
                       "google",   p("google", null, "gid", null)));
        assertThat(svc.active()).extracting(OidcProviderStatus::key).containsExactly("keycloak");
    }
}
