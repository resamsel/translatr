package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class UserResourceTest {

    @Test
    void testFindUsers_returnsOk() {
        given()
            .when().get("/api/users")
            .then()
            .statusCode(200)
            .body("list",  notNullValue())
            .body("limit", notNullValue());
    }

    @Test
    void testGetUser_notFound() {
        given()
            .when().get("/api/user/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-user-sub"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testuser@example.com")
    })
    void testMe_authenticated_returnsUser() {
        given()
            .when().get("/api/me")
            .then()
            .statusCode(200)
            .body("username", notNullValue());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-user-sub"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testuser@example.com")
    })
    void testUpdateUser_missingId_returns404() {
        given()
            .contentType("application/json")
            .body("{\"id\": \"00000000-0000-0000-0000-000000000000\", \"name\": \"New Name\"}")
            .when().put("/api/user")
            .then()
            .statusCode(404);
    }
}

