package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class StatisticsResourceTest {

    @Test
    void getStatistics_returnsCounts() {
        given()
            .when().get("/api/statistics")
            .then()
            .statusCode(200)
            .body("userCount",     notNullValue())
            .body("projectCount",  notNullValue())
            .body("activityCount", notNullValue())
            .body("userCount",     greaterThanOrEqualTo(0))
            .body("projectCount",  greaterThanOrEqualTo(0))
            .body("activityCount", greaterThanOrEqualTo(0));
    }
}
