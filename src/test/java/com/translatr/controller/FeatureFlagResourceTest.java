package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class FeatureFlagResourceTest {

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-ff-sub"),
        @Claim(key = "name",  value = "Test FF User"),
        @Claim(key = "email", value = "testff@example.com")
    })
    void findUserFeatureFlags_authenticated_returnsPagedShape() {
        given()
            .when().get("/api/featureflags")
            .then()
            .statusCode(200)
            .body("list",  notNullValue())
            .body("limit", notNullValue())
            .body("offset", notNullValue());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-ff-sub"),
        @Claim(key = "name",  value = "Test FF User"),
        @Claim(key = "email", value = "testff@example.com")
    })
    void findUserFeatureFlags_queryParams_reflectedInResponse() {
        given()
            .when().get("/api/featureflags?offset=0&limit=1&search=zzz&order=feature&feature=beta")
            .then()
            .statusCode(200)
            .body("offset", is(0))
            .body("limit",  is(1));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-ff-sub"),
        @Claim(key = "name",  value = "Test FF User"),
        @Claim(key = "email", value = "testff@example.com")
    })
    void getUserFeatureFlag_unknownId_returns404() {
        given()
            .when().get("/api/featureflag/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    void findUserFeatureFlags_anonymous_isUnauthorized() {
        given()
            .when().get("/api/featureflags")
            .then()
            .statusCode(401);
    }
}
