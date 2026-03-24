package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class KeyResourceTest {

    @Test
    void testGetKey_notFound() {
        given()
            .when().get("/api/key/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }
}
