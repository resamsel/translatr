package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class LocaleResourceLocaleNameCriteriaTest {

    @Test
    @TestSecurity(user = "localenameswap", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "localenameswap-sub"),
        @Claim(key = "name",  value = "Locale Name Swap"),
        @Claim(key = "email", value = "localenameswap@example.com")
    })
    void findLocalesByProject_localeNameAndSearch_areNotSwapped() {
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"localename-swap-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"name\": \"de\"}")
            .when().post("/api/locale")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        // localeName does an EXACT match; search does a LIKE match. If these two String
        // params were ever swapped in the generated-interface binding, an exact-match
        // query sent through the "search" slot would still (wrongly) match via LIKE
        // semantics, or vice versa — so a value that matches under one but not the other
        // proves they're bound to the correct fields.
        given()
            .when().get("/api/project/" + projectId + "/locales?localeName=de")
            .then()
            .statusCode(200)
            .body("total", is(1));

        given()
            .when().get("/api/project/" + projectId + "/locales?localeName=d")
            .then()
            .statusCode(200)
            .body("total", is(0));

        given()
            .when().get("/api/project/" + projectId + "/locales?search=d")
            .then()
            .statusCode(200)
            .body("total", is(1));
    }
}
