package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ActivityResourceAggregatedCriteriaTest {

    @Test
    @TestSecurity(user = "aggswaptest", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "aggswaptest-sub"),
        @Claim(key = "name",  value = "Aggregated Swap Test"),
        @Claim(key = "email", value = "aggswaptest@example.com")
    })
    void findAggregatedActivity_projectIdAndUserId_areNotSwapped() {
        // Creating a project publishes a real LogEntry row (ActionType.Create, content type
        // "dto.Project") tied to BOTH this project's id AND this caller's own user id — a
        // single real data point that both projectId and userId can independently, correctly
        // filter down to.
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"agg-swap-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        // If projectId/userId were swapped in the generated-interface binding, filtering by
        // this real projectId would either silently misroute into the userId slot (matching
        // nothing, since a project id is never a user id) or return unrelated rows.
        given()
            .queryParam("projectId", projectId)
            .when().get("/api/activities/aggregated")
            .then()
            .statusCode(200)
            .body("total", greaterThanOrEqualTo(1));

        // An unrelated random projectId must NOT match this caller's own activity — proving
        // the filter is genuinely scoped by project, not accidentally matching everything.
        given()
            .queryParam("projectId", "00000000-0000-0000-0000-000000000000")
            .when().get("/api/activities/aggregated")
            .then()
            .statusCode(200)
            .body("total", is(0));
    }
}
