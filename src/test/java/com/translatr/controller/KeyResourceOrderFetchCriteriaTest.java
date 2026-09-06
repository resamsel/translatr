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
            .body("list[0].name", is("zzz"))
            // PagedKeyList's constructor takes 3 Integers and 2 Booleans positionally
            // (total, offset, limit, hasNext, hasPrev) — a transposition among them
            // (e.g. total<->offset, or hasNext<->hasPrev) would be silently masked by
            // any test whose result set is empty (total==offset==0, hasNext==hasPrev==
            // false trivially). With 2 keys and no limit param (default limit=20 per
            // LimitParam's OpenAPI default), offset=0 and offset+limit(20) >= total(2),
            // so hasNext is false and hasPrev (offset>0) is false too — but total:2
            // still distinguishes this from every other test's total:0.
            .body("total", is(2))
            .body("offset", is(0))
            .body("hasPrev", is(false));

        // A limit=1 request against the same 2-key result set forces hasNext:true
        // (offset=0, limit=1, total=2 => offset+limit(1) < total(2)), pinning the
        // one PagedKeyList field the assertions above leave at its default (false).
        given()
            .queryParam("limit", 1)
            .when().get("/api/project/" + projectId + "/keys")
            .then()
            .statusCode(200)
            .body("total", is(2))
            .body("hasNext", is(true));

        given()
            .queryParam("fetch", "progress")
            .when().get("/api/project/" + projectId + "/keys")
            .then()
            .statusCode(200)
            .body("list[0].progress", notNullValue());
    }
}
