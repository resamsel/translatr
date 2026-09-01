package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GlobalFeatureFlagResourceTest {

    private static final String ADMIN_SUB = "gff-admin-sub";
    private static final String USER_SUB  = "gff-user-sub";

    @Test
    @TestSecurity(user = "gffadmin", roles = "translatr-admin")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = ADMIN_SUB),
        @Claim(key = "name",  value = "GFF Admin"),
        @Claim(key = "email", value = "gff-admin@example.com")
    })
    void resolved_returnsOneEntryPerFeature() {
        given()
            .when().get("/api/featureflags/resolved")
            .then()
            .statusCode(200)
            .body("feature", hasItem("language-switcher"))
            .body("find { it.feature == 'language-switcher' }.effective", is(false))
            .body("find { it.feature == 'language-switcher' }.defaultEnabled", is(false));
    }

    @Test
    @TestSecurity(user = "gffadmin", roles = "translatr-admin")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = ADMIN_SUB),
        @Claim(key = "name",  value = "GFF Admin"),
        @Claim(key = "email", value = "gff-admin@example.com")
    })
    void admin_canSetAndListAndDeleteGlobalFlag() {
        String id =
        given()
            .contentType("application/json")
            .body("{\"feature\":\"header-graphic\",\"enabled\":true}")
            .when().post("/api/featureflag/global")
            .then()
            .statusCode(200)
            .body("feature", is("header-graphic"))
            .body("enabled", is(true))
            .extract().path("id");

        given()
            .when().get("/api/featureflags/global")
            .then()
            .statusCode(200)
            .body("feature", hasItem("header-graphic"));

        given()
            .when().delete("/api/featureflag/global/" + id)
            .then()
            .statusCode(204);
    }

    @Test
    @TestSecurity(user = "gffadmin", roles = "translatr-admin")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = ADMIN_SUB),
        @Claim(key = "name",  value = "GFF Admin"),
        @Claim(key = "email", value = "gff-admin@example.com")
    })
    void set_unknownFeature_returns400() {
        given()
            .contentType("application/json")
            .body("{\"feature\":\"no-such-feature\",\"enabled\":true}")
            .when().post("/api/featureflag/global")
            .then()
            .statusCode(400);
    }

    @Test
    @TestSecurity(user = "gffuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = USER_SUB),
        @Claim(key = "name",  value = "GFF User"),
        @Claim(key = "email", value = "gff-user@example.com")
    })
    void nonAdmin_cannotSetGlobalFlag_returns403() {
        given()
            .contentType("application/json")
            .body("{\"feature\":\"header-graphic\",\"enabled\":true}")
            .when().post("/api/featureflag/global")
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "gffuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = USER_SUB),
        @Claim(key = "name",  value = "GFF User"),
        @Claim(key = "email", value = "gff-user@example.com")
    })
    void nonAdmin_cannotDeleteGlobalFlag_returns403() {
        given()
            .when().delete("/api/featureflag/global/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(403);
    }

    @Test
    void anonymous_isUnauthorized() {
        given()
            .when().get("/api/featureflags/global")
            .then()
            .statusCode(401);
    }
}
