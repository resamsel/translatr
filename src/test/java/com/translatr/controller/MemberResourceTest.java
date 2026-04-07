package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class MemberResourceTest {

    @Test
    void testFindMembersByProject_returnsOk() {
        given()
            .when().get("/api/project/00000000-0000-0000-0000-000000000000/members")
            .then()
            .statusCode(200)
            .body("list",  notNullValue())
            .body("limit", notNullValue());
    }

    @Test
    void testGetMember_notFound() {
        given()
            .when().get("/api/member/999999")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-member-sub"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testmember@example.com")
    })
    void testCreateMember_missingProject_returns404() {
        given()
            .contentType("application/json")
            .body("{\"projectId\": \"00000000-0000-0000-0000-000000000000\", " +
                  "\"userId\": \"00000000-0000-0000-0000-000000000001\", " +
                  "\"role\": \"Translator\"}")
            .when().post("/api/member")
            .then()
            .statusCode(404);
    }
}

