package com.translatr.controller;

import com.translatr.testsupport.OidcEnabledNoCredentialsTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * #255: the app must boot with quarkus-oidc ENABLED and no credentials configured at all.
 *
 * <p>Reaching an assertion at all proves the boot; the assertions themselves prove the app degrades
 * to "no login providers" instead of failing startup.</p>
 */
@QuarkusTest
@TestProfile(OidcEnabledNoCredentialsTestProfile.class)
class BootsWithoutOidcCredentialsTest {

    /**
     * Guards the guard: {@code quarkus.oidc.enabled} is build-time, so if the test profile ever
     * stopped overriding {@code %test.quarkus.oidc.enabled=false} the rest of this class would pass
     * vacuously. The OIDC runtime beans only exist when the extension is enabled.
     */
    @Test
    void oidcExtensionIsActuallyEnabled() {
        assertThat(io.quarkus.arc.Arc.container()
                        .instance(io.quarkus.oidc.runtime.DefaultTenantConfigResolver.class)
                        .isAvailable())
                .as("quarkus.oidc.enabled must be true for this test class to mean anything")
                .isTrue();
    }

    @Test
    void authClients_isEmpty_whenNoProviderHasCredentials() {
        given()
            .when().get("/api/authclients")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    /**
     * With OIDC enabled and the static tenant disabled, an anonymous call to an admin endpoint is a
     * plain 401 ? the dynamic resolver returns no tenant for a non-/login path with no session, so
     * there is nothing to challenge with.
     */
    @Test
    void oidcProviders_isUnauthorized_forAnonymousCaller() {
        given()
            .when().get("/api/oidc-providers")
            .then()
            .statusCode(401);
    }
}
