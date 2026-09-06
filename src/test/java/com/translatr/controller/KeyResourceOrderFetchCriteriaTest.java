package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class KeyResourceOrderFetchCriteriaTest {

    @Test
    @TestSecurity(user = "orderfetchswap", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "orderfetchswap-sub"),
        @Claim(key = "name",  value = "Order Fetch Swap"),
        @Claim(key = "email", value = "orderfetchswap@example.com")
    })
    void findKeysByProject_orderAndFetch_areNotSwapped() {
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"orderfetch-swap-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"name\": \"aaa\"}")
            .when().post("/api/key")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"name\": \"zzz\"}")
            .when().post("/api/key")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        // If order/fetch were ever swapped in the generated-interface binding, ?fetch=progress
        // routed into the order slot would silently fall through QuerySupport.orderBy's
        // whitelist (name/whenCreated/whenUpdated/wordCount — "progress" isn't a real column)
        // to the default ORDER BY name, while ?order=name desc routed into the fetch slot
        // would never trigger the real progress expansion. Both failures are silent, not
        // errors, so this test proves the two are bound to the correct fields.
        given()
            .queryParam("order", "name desc")
            .when().get("/api/project/" + projectId + "/keys")
            .then()
            .statusCode(200)
            .body("list[0].name", is("zzz"));

        given()
            .queryParam("fetch", "progress")
            .when().get("/api/project/" + projectId + "/keys")
            .then()
            .statusCode(200)
            .body("list[0].progress", notNullValue());
    }
}
