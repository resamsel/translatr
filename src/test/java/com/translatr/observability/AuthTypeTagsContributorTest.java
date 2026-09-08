package com.translatr.observability;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

/**
 * HTTP-level coverage of the real wiring: {@link AuthTypeTagsContributor} is picked up as a
 * {@code HttpServerMetricsTagsContributor} CDI bean and every {@code http_server_requests}
 * sample ends up with an {@code auth_type} label.
 */
@QuarkusTest
class AuthTypeTagsContributorTest {

    @Test
    void anonymousRequestIsTaggedAnonymous() {
        given().when().get("/health").then().statusCode(200);

        given().when().get("/metrics")
                .then().statusCode(200)
                .body(containsString("auth_type=\"anonymous\""));
    }

    @Test
    void invalidAccessTokenRequestStillGetsAnAuthTypeTag() {
        // A bogus token drives resolveAuthType() down its non-anonymous branch and never
        // resolves to an identity. The real AccessTokenAuthMechanism must reject it cleanly
        // with 401 (not surface the lookup as a 500), and the resulting
        // http_server_requests sample still carries the auth_type label.
        given().header("X-Access-Token", "definitely-not-a-real-key")
                .when().get("/api/me")
                .then().statusCode(401);

        given().when().get("/metrics")
                .then().statusCode(200)
                .body(containsString("auth_type="));
    }
}
