package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class AccessTokenResourceTest {

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-token-sub"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testtoken@example.com")
    })
    void testFindAccessTokens_authenticated_returnsOk() {
        given()
            .when().get("/api/accesstokens")
            .then()
            .statusCode(200)
            .body("list",  notNullValue())
            .body("limit", notNullValue());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-token-sub"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testtoken@example.com")
    })
    void testGetAccessToken_notFound() {
        given()
            .when().get("/api/accesstoken/999999")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-token-sub"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testtoken@example.com")
    })
    void testCreateAccessToken_authenticated_returnsToken() {
        given()
            .contentType("application/json")
            .body("{\"name\": \"ci-token-" + System.currentTimeMillis() + "\", \"scope\": \"read\"}")
            .when().post("/api/accesstoken")
            .then()
            .statusCode(200)
            .body("name",  notNullValue())
            .body("key",   notNullValue());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-token-sub"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testtoken@example.com")
    })
    void testDeleteAccessToken_notFound() {
        given()
            .when().delete("/api/accesstoken/999999")
            .then()
            .statusCode(404);
    }
}

