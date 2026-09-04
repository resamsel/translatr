package com.translatr.controller;

import com.translatr.testsupport.MultiProviderTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(MultiProviderTestProfile.class)
class AuthClientsResourceTest {

    @Test
    void returnsOnlyListedAndConfiguredProviders() {
        // profile lists keycloak,google,github but github has no client-secret
        given()
            .when().get("/api/authclients")
            .then()
            .statusCode(200)
            .body("key", containsInAnyOrder("keycloak", "google"))
            .body("find { it.key == 'google' }.url", is("/login/google"))
            .body("key", not(hasItem("github")));
    }
}
