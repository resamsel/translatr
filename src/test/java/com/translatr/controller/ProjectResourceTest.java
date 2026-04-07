package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class ProjectResourceTest {

    @Test
    void testFindProjects_returnsOk() {
        given()
            .when().get("/api/projects")
            .then()
            .statusCode(200)
            .body("list",     notNullValue())
            .body("limit",    notNullValue());
    }

    @Test
    void testGetProject_notFound() {
        given()
            .when().get("/api/project/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-sub-001"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testuser@example.com")
    })
    void testCreateProject_authenticated() {
        given()
            .contentType("application/json")
            .body("{\"name\": \"test-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)));
    }
}
