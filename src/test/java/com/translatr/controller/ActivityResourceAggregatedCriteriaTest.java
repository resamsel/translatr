package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ActivityResourceAggregatedCriteriaTest {

    // Activity logging is asynchronous (ActivityEventProducer publishes onto the Vert.x event
    // bus and ActivityEventConsumer persists the LogEntry on a @Blocking worker thread), and
    // under CI load the consumer can lag well past a couple of seconds. Every assertion below
    // that depends on that write landing polls via awaitTotal() instead of reading once, so a
    // slow-but-eventual write doesn't fail the test.
    private static final int MAX_ATTEMPTS = 100;
    private static final long POLL_INTERVAL_MILLIS = 100;

    @Test
    @TestSecurity(user = "aggswaptest", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "aggswaptest-sub"),
        @Claim(key = "name",  value = "Aggregated Swap Test"),
        @Claim(key = "email", value = "aggswaptest@example.com")
    })
    void findAggregatedActivity_projectIdAndUserId_areNotSwapped() throws InterruptedException {
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
        // nothing, since a project id is never a user id) or return unrelated rows. Poll until
        // the aggregated total reflects the project-creation activity rather than asserting on
        // a single, possibly-premature read.
        awaitTotal(projectId, greaterThanOrEqualTo(1));

        // An unrelated random projectId must NOT match this caller's own activity — proving
        // the filter is genuinely scoped by project, not accidentally matching everything.
        awaitTotal("00000000-0000-0000-0000-000000000000", is(0));
    }

    private void awaitTotal(String projectId, Matcher<Integer> matcher) throws InterruptedException {
        int total = 0;
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            total = given()
                .queryParam("projectId", projectId)
                .when().get("/api/activities/aggregated")
                .then()
                .statusCode(200)
                .extract().path("total");
            if (matcher.matches(total)) {
                return;
            }
            Thread.sleep(POLL_INTERVAL_MILLIS);
        }
        assertThat(total, matcher);
    }
}
