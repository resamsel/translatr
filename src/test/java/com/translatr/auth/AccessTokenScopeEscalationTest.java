package com.translatr.auth;

import com.translatr.model.AccessToken;
import com.translatr.model.User;
import com.translatr.model.UserRole;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * A token holding {@code write:accesstoken} may only grant a subset of its own scopes when it
 * creates or updates a token; otherwise it could mint itself a more powerful one.
 */
@QuarkusTest
class AccessTokenScopeEscalationTest {

    static final String OWN = "read:accesstoken,write:accesstoken,read:project";

    User user;

    @BeforeEach
    @Transactional
    void seed() {
        user = new User();
        user.name = "escalation";
        user.username = "escalation-" + System.nanoTime();
        user.email = user.username + "@example.com";
        user.role = UserRole.User;
        user.persist();
    }

    @Transactional
    AccessToken token(String scope) {
        AccessToken t = new AccessToken();
        t.user = User.findById(user.id);
        t.name = "escalation-test-" + System.nanoTime();
        t.key = "ste-" + System.nanoTime();
        t.scope = scope;
        t.persist();
        return t;
    }

    @Test
    void create_withSubsetOfOwnScopes_isAllowed() {
        given().header("X-Access-Token", token(OWN).key)
                .contentType("application/json")
                .body("{\"name\": \"child\", \"scope\": \"read:project\"}")
                .when().post("/api/accesstoken")
                .then().statusCode(anyOf(is(200), is(201)))
                .body("scope", is("read:project"));
    }

    @Test
    void create_withScopeNotHeld_isForbidden() {
        given().header("X-Access-Token", token(OWN).key)
                .contentType("application/json")
                .body("{\"name\": \"child\", \"scope\": \"read:project,write:project\"}")
                .when().post("/api/accesstoken")
                .then().statusCode(403)
                .body("error", is("insufficient_scope"))
                .body("required", contains("write:project"));
    }

    @Test
    void update_toScopeNotHeld_isForbidden() {
        AccessToken target = token("read:project");
        given().header("X-Access-Token", token(OWN).key)
                .contentType("application/json")
                .body("{\"id\": " + target.id + ", \"scope\": \"write:user\"}")
                .when().put("/api/accesstoken")
                .then().statusCode(403)
                .body("required", contains("write:user"));
    }

    @Test
    void update_withSubsetOfOwnScopes_isAllowed() {
        AccessToken target = token("read:project");
        given().header("X-Access-Token", token(OWN).key)
                .contentType("application/json")
                .body("{\"id\": " + target.id + ", \"scope\": \"read:accesstoken\"}")
                .when().put("/api/accesstoken")
                .then().statusCode(200)
                .body("scope", is("read:accesstoken"));
    }
}
