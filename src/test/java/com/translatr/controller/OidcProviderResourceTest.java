package com.translatr.controller;

import com.translatr.testsupport.MultiProviderTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(MultiProviderTestProfile.class)
class OidcProviderResourceTest {

    @Test
    @TestSecurity(user = "oidcadmin", roles = "translatr-admin")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "oidc-admin-sub"),
        @Claim(key = "name",  value = "OIDC Admin"),
        @Claim(key = "email", value = "oidc-admin@example.com")
    })
    void admin_getsMaskedProviderStatus() {
        given()
            .when().get("/api/oidc-providers")
            .then()
            .statusCode(200)
            .body("find { it.key == 'google' }.active", is(true))
            .body("find { it.key == 'google' }.clientId", is("google-id"))
            .body("find { it.key == 'google' }.clientSecret", is("***len:24***"))
            .body("find { it.key == 'github' }.active", is(false))
            .body("find { it.key == 'github' }.errors", hasItem("client-secret is missing"))
            .body("toString()", not(containsString("google-secret-abcdefghij")))
            .body("toString()", not(containsString("kc-secret-1234567890")));
    }

    @Test
    @TestSecurity(user = "oidcuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "oidc-user-sub"),
        @Claim(key = "name",  value = "OIDC User"),
        @Claim(key = "email", value = "oidc-user@example.com")
    })
    void nonAdmin_getsForbidden() {
        given().when().get("/api/oidc-providers").then().statusCode(403);
    }
}
