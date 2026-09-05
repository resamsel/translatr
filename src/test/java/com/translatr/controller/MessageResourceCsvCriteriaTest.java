package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class MessageResourceCsvCriteriaTest {

    @Test
    @TestSecurity(user = "csvswap", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "csvswap-sub"),
        @Claim(key = "name",  value = "Csv Swap"),
        @Claim(key = "email", value = "csvswap@example.com")
    })
    void findMessages_keyIdsAndLocaleIds_areNotSwapped() {
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"csv-swap-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        String keyId =
        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"name\": \"greeting\"}")
            .when().post("/api/key")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        String localeId =
        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"name\": \"en\"}")
            .when().post("/api/locale")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        given()
            .contentType("application/json")
            .body("{\"keyId\": \"" + keyId + "\", \"localeId\": \"" + localeId + "\", \"value\": \"Hello\"}")
            .when().post("/api/message")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        // The message's key id shows up under ?keyIds=, never under ?localeIds= — if these two
        // flat CSV parameters were ever swapped in the generated-interface binding, this second
        // assertion would flip from 0 to 1.
        given()
            .when().get("/api/messages?keyIds=" + keyId)
            .then()
            .statusCode(200)
            .body("total", is(1));

        given()
            .when().get("/api/messages?localeIds=" + keyId)
            .then()
            .statusCode(200)
            .body("total", is(0));

        // MessageResource.findMessagesByProject is a SEPARATE generated-interface method
        // (backing GET /api/project/{projectId}/messages) with its own independent
        // parameter order in its @Override declaration — the same keyIds/localeIds swap
        // could exist there without being caught by the assertions above.
        given()
            .when().get("/api/project/" + projectId + "/messages?keyIds=" + keyId)
            .then()
            .statusCode(200)
            .body("total", is(1));

        given()
            .when().get("/api/project/" + projectId + "/messages?localeIds=" + keyId)
            .then()
            .statusCode(200)
            .body("total", is(0));
    }
}
