package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class MessageResourceTest {

    @Test
    void testFindMessages_returnsOk() {
        given()
            .when().get("/api/messages")
            .then()
            .statusCode(200)
            .body("list",  notNullValue())
            .body("limit", notNullValue());
    }

    @Test
    void testFindMessagesByProject_returnsOk() {
        given()
            .when().get("/api/project/00000000-0000-0000-0000-000000000000/messages")
            .then()
            .statusCode(200)
            .body("list", notNullValue());
    }

    @Test
    void testGetMessage_notFound() {
        given()
            .when().get("/api/message/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-msg-sub"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testmsg@example.com")
    })
    void testCreateMessage_missingLocale_returns404() {
        given()
            .contentType("application/json")
            .body("{\"localeId\": \"00000000-0000-0000-0000-000000000000\", " +
                  "\"keyId\": \"00000000-0000-0000-0000-000000000001\", " +
                  "\"value\": \"Hello\"}")
            .when().post("/api/message")
            .then()
            .statusCode(404);
    }
}

