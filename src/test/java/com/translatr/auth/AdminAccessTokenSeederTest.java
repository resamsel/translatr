package com.translatr.auth;

import com.translatr.testsupport.AdminAccessTokenTestProfile;
import com.translatr.testsupport.CleanAdminUserResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * Reproduces the load generator crash: with {@code translatr.admin-access-token} (env
 * {@code ADMIN_ACCESS_TOKEN}) configured, a request authenticated with that exact key must
 * succeed. Before {@link AdminAccessTokenSeeder} existed, the key was never persisted as an
 * {@code AccessToken} row, so every such request got a 401 — which is what crashed the load
 * generator once its retries were exhausted (see docker-compose-loadtest.yml).
 */
@QuarkusTest
@QuarkusTestResource(CleanAdminUserResource.class)
@TestProfile(AdminAccessTokenTestProfile.class)
class AdminAccessTokenSeederTest {

    @Test
    void configuredAdminAccessToken_authenticatesSuccessfully() {
        given()
            .queryParam("access_token", AdminAccessTokenTestProfile.TOKEN)
            .when().get("/api/accesstokens")
            .then()
            .statusCode(200);
    }
}
