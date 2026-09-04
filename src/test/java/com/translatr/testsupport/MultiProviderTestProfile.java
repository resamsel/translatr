package com.translatr.testsupport;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/** Mixed provider config for AuthClientsResourceTest and OidcProviderResourceTest. */
public class MultiProviderTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.ofEntries(
            Map.entry("translatr.auth.providers", "keycloak,google,github"),
            Map.entry("translatr.auth.oidc.keycloak.auth-server-url", "http://localhost:9999/realms/Translatr"),
            Map.entry("translatr.auth.oidc.keycloak.client-id", "kc-id"),
            Map.entry("translatr.auth.oidc.keycloak.client-secret", "kc-secret-1234567890"),
            Map.entry("translatr.auth.oidc.keycloak.scopes", "microprofile-jwt"),
            Map.entry("translatr.auth.oidc.google.provider", "google"),
            Map.entry("translatr.auth.oidc.google.client-id", "google-id"),
            Map.entry("translatr.auth.oidc.google.client-secret", "google-secret-abcdefghij"),
            Map.entry("translatr.auth.oidc.github.provider", "github"),
            Map.entry("translatr.auth.oidc.github.client-id", "github-id")
        );
    }
}
