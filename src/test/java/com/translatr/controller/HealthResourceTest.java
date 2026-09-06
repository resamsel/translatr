package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class HealthResourceTest {

    @Test
    void getHealth_returnsOk() {
        given()
            .when().get("/api/health")
            .then()
            .statusCode(200)
            .body("status", is("ok"));
    }
}
