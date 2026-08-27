package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class ActivityResourceTest {

    @Test
    void find_withoutAuthOrUserId_returns401() {
        given()
            .when().get("/api/activities")
            .then()
            .statusCode(401);
    }

    @Test
    void find_withExplicitUserId_isPublic() {
        given()
            .when().get("/api/activities?userId=00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(200)
            .body("list", notNullValue());
    }

    @Test
    void aggregated_isPublic() {
        given()
            .when().get("/api/activities/aggregated")
            .then()
            .statusCode(200)
            .body("list", notNullValue());
    }

    @Test
    @TestSecurity(user = "activityuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "activity-sub-1"),
        @Claim(key = "name",  value = "Activity User"),
        @Claim(key = "email", value = "activityuser@example.com")
    })
    void find_authenticated_resolvesCurrentUser() {
        given()
            .when().get("/api/activities")
            .then()
            .statusCode(200)
            .body("list", notNullValue());
    }
}
