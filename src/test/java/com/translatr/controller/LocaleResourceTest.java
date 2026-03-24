package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class LocaleResourceTest {

    @Test
    void testGetLocale_notFound() {
        given()
            .when().get("/api/locale/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }
}
