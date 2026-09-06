package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class NotificationResourceTest {

    @Test
    void findNotifications_alwaysReturnsEmpty() {
        given()
            .when().get("/api/notifications")
            .then()
            .statusCode(200)
            .body("list", notNullValue())
            .body("list.size()", is(0))
            .body("total", is(0))
            .body("hasNext", is(false));
    }

    @Test
    void findNotifications_reflectsRealOffsetAndLimit() {
        given()
            .queryParam("offset", 5)
            .queryParam("limit", 10)
            .when().get("/api/notifications")
            .then()
            .statusCode(200)
            .body("offset", is(5))
            .body("limit", is(10))
            .body("hasPrev", is(true));
    }
}
