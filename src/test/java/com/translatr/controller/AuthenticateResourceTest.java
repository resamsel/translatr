package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;

/**
 * With OIDC handling the code exchange this endpoint is only reached by bare browser
 * navigation to {@code /authenticate}; the fallback sends the authenticated user to the UI.
 */
@QuarkusTest
class AuthenticateResourceTest {

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void bareAuthenticate_redirectsToUi() {
        given()
            .redirects().follow(false)
            .when().get("/authenticate")
            .then()
            .statusCode(303)
            .header("Location", endsWith("/ui"));
    }
}
