package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;

@QuarkusTest
class UiLandingResourceTest {

    @Test
    void ui_redirectsToTheConfiguredUiBase() {
        given()
            .redirects().follow(false)
            .when().get("/ui")
            .then()
            .statusCode(303)
            .header("Location", endsWith("/ui"));
    }
}
