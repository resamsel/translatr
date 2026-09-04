package com.translatr.testsupport;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/**
 * Reproduces the #255 boot scenario: the quarkus-oidc extension ENABLED with no
 * {@code quarkus.oidc.client-id} anywhere and no {@code translatr.auth.oidc.*} credentials.
 *
 * <p>Every other {@code @QuarkusTest} runs with {@code %test.quarkus.oidc.enabled=false}, which is
 * exactly the configuration in which the original crash cannot happen. Booting has to work because
 * the static default tenant is disabled and {@code TranslatrTenantConfigResolver} resolves tenants
 * lazily per request.</p>
 */
public class OidcEnabledNoCredentialsTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.ofEntries(
            // Build-time: turn the extension back on (application.properties disables it for %test).
            Map.entry("quarkus.oidc.enabled", "true"),
            // The static default tenant stays off ? the resolver supplies tenants per request.
            Map.entry("quarkus.oidc.tenant-enabled", "false"),
            Map.entry("quarkus.keycloak.devservices.enabled", "false"),
            // Two providers listed, deliberately NO translatr.auth.oidc.* credentials for either.
            Map.entry("translatr.auth.providers", "keycloak,google"),
            Map.entry("translatr.auth.oidc.keycloak.client-id", ""),
            Map.entry("translatr.auth.oidc.keycloak.client-secret", ""),
            Map.entry("translatr.auth.oidc.keycloak.auth-server-url", ""),
            Map.entry("translatr.auth.oidc.google.client-id", ""),
            Map.entry("translatr.auth.oidc.google.client-secret", "")
        );
    }
}
