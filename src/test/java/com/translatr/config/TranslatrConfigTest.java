package com.translatr.config;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class TranslatrConfigTest {

    @Inject TranslatrConfig config;

    @Test
    void testDefaults() {
        assertThat(config.auth().providers()).isNotBlank();
        assertThat(config.redirectBase()).isNotBlank();
    }

    @Test
    void keycloakProvider_requestsMicroprofileJwtScope_forTheGroupsClaim() {
        // Keycloak only emits the `groups` claim (realm roles) when this scope is
        // requested; syncOidcRole depends on it to grant/revoke Admin.
        assertThat(config.auth().oidc()).containsKey("keycloak");
        assertThat(config.auth().oidc().get("keycloak").scopes()).contains("microprofile-jwt");
    }

    @Test
    void rosterProviders_areAllPresent() {
        assertThat(config.auth().oidc().keySet())
            .contains("keycloak", "google", "github", "facebook", "twitter", "microsoft", "apple");
    }

    @Test
    void adminEmails_isEmpty_whenAdminsUnset() {
        assertThat(config.adminEmails()).isEmpty();
    }
}
