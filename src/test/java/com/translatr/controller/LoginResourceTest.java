package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * {@code redirect-base} in tests is {@code http://localhost:8081}
 * ({@code src/test/resources/application.properties}), and
 * {@code https://allowed.example.com} is configured as an allowed absolute redirect origin.
 */
@QuarkusTest
class LoginResourceTest {

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void relativeRedirectUri_redirectsToRedirectBasePlusPath() {
        given()
            .redirects().follow(false)
            .queryParam("redirect_uri", "/ui/projects/acme/foo")
            .when().get("/login/keycloak")
            .then()
            .statusCode(303)
            .header("Location", equalTo("http://localhost:8081/ui/projects/acme/foo"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void missingRedirectUri_redirectsToUi() {
        given()
            .redirects().follow(false)
            .when().get("/login/keycloak")
            .then()
            .statusCode(303)
            .header("Location", equalTo("http://localhost:8081/ui"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void blankRedirectUri_redirectsToUi() {
        given()
            .redirects().follow(false)
            .queryParam("redirect_uri", "")
            .when().get("/login/keycloak")
            .then()
            .statusCode(303)
            .header("Location", equalTo("http://localhost:8081/ui"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void disallowedAbsoluteRedirectUri_fallsBackToUi() {
        given()
            .redirects().follow(false)
            .queryParam("redirect_uri", "https://evil.example.com/steal")
            .when().get("/login/keycloak")
            .then()
            .statusCode(303)
            .header("Location", equalTo("http://localhost:8081/ui"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void protocolRelativeRedirectUri_fallsBackToUi() {
        given()
            .redirects().follow(false)
            .queryParam("redirect_uri", "//evil.example.com/steal")
            .when().get("/login/keycloak")
            .then()
            .statusCode(303)
            .header("Location", not(containsString("evil.example.com")))
            .header("Location", endsWith("/ui"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void backslashTrickRedirectUri_fallsBackToUi() {
        given()
            .redirects().follow(false)
            .queryParam("redirect_uri", "/\\evil.example.com/steal")
            .when().get("/login/keycloak")
            .then()
            .statusCode(303)
            .header("Location", not(containsString("evil.example.com")))
            .header("Location", endsWith("/ui"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void allowedAbsoluteRedirectUri_isHonored() {
        given()
            .redirects().follow(false)
            .queryParam("redirect_uri", "https://allowed.example.com/admin/dashboard")
            .when().get("/login/keycloak")
            .then()
            .statusCode(303)
            .header("Location", equalTo("https://allowed.example.com/admin/dashboard"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void unknownProvider_returns404() {
        given()
            .redirects().follow(false)
            .when().get("/login/nonesuch")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    void configuredProvider_stillRedirects() {
        // keycloak is configured in the default test profile (translatr.auth.providers=keycloak)
        given()
            .redirects().follow(false)
            .queryParam("redirect_uri", "/ui/x")
            .when().get("/login/keycloak")
            .then()
            .statusCode(303)
            .header("Location", equalTo("http://localhost:8081/ui/x"));
    }
}
