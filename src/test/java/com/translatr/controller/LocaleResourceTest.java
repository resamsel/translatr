package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class LocaleResourceTest {

    @Test
    void testGetLocale_notFound() {
        given()
            .when().get("/api/locale/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    void findByProject_anonymous_stillResolvesAViewerLocale() {
        // viewerLocale() calls resolveOptional() on this @PermitAll route; an anonymous caller
        // must fall through to the English default, not trip a 401.
        given()
            .when().get("/api/project/00000000-0000-0000-0000-000000000000/locales")
            .then()
            .statusCode(200)
            .body("list", notNullValue());
    }

    @Test
    @TestSecurity(user = "displaynameowner", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "displayname-owner-sub"),
        @Claim(key = "name",  value = "Display Name Owner"),
        @Claim(key = "email", value = "displaynameowner@example.com")
    })
    void findByProject_stampsLocaleDisplayName() {
        String projectName = "displayname-project-" + System.currentTimeMillis();

        String projectId = given()
            .contentType("application/json")
            .body("{\"name\": \"" + projectName + "\"}")
            .when().post("/api/project")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"name\": \"de\"}")
            .when().post("/api/locale")
            .then().statusCode(anyOf(is(200), is(201)));

        // no preferredLocale on this user -> English display names
        given()
            .when().get("/api/project/" + projectId + "/locales")
            .then()
            .statusCode(200)
            .body("list[0].name",        is("de"))
            .body("list[0].displayName", is("German"));
    }
}
