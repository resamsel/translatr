package com.translatr.config;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class TranslatrConfigTest {

    @Inject TranslatrConfig config;

    // Keycloak only emits the `groups` claim (realm roles) when this scope is requested;
    // syncOidcRole depends on it to grant/revoke Admin.
    @ConfigProperty(name = "quarkus.oidc.authentication.scopes")
    List<String> oidcScopes;

    // After the Keycloak code exchange Quarkus lands on redirect-path (/authenticate) and
    // would drop the original ?redirect_uri=. Restoring the original request URI is what
    // lets LoginResource.login() re-run authenticated and 303 to the requested route (#245).
    @ConfigProperty(name = "quarkus.oidc.authentication.restore-path-after-redirect")
    boolean restorePathAfterRedirect;

    @Test
    void testDefaults() {
        assertThat(config.auth().providers()).isNotBlank();
        assertThat(config.redirectBase()).isNotBlank();
    }

    @Test
    void requestsMicroprofileJwtScope_forTheGroupsClaim() {
        assertThat(oidcScopes).contains("microprofile-jwt");
    }

    @Test
    void restoresOriginalRequestUri_afterLoginRedirect() {
        assertThat(restorePathAfterRedirect).isTrue();
    }
}
