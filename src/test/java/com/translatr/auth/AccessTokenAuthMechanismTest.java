package com.translatr.auth;

import com.translatr.model.AccessToken;
import com.translatr.model.User;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * Exercises the REAL {@link AccessTokenAuthMechanism} over HTTP (no {@code @TestSecurity}
 * mock). Guards against the regression where {@code AccessTokenService.findByKey} ran its
 * Panache lookup on a raw worker thread with no Hibernate session, so every access-key
 * request — valid or bogus — surfaced as HTTP 500 instead of 401 / 200.
 *
 * <p>{@code GET /api/me} ({@code UsersApi.getCurrentUser}) is {@code @Authenticated}, so the
 * mechanism runs and an unresolved identity yields a genuine 401.
 */
@QuarkusTest
class AccessTokenAuthMechanismTest {

    String rawKey;

    @BeforeEach
    @Transactional
    void seedToken() {
        User u = new User();
        u.name = "authmech";
        u.username = "authmech-" + System.nanoTime();
        u.email = u.username + "@example.com";
        u.persist();

        AccessToken t = new AccessToken();
        t.user = u;
        t.name = "test";
        t.key = "amt-" + System.nanoTime();
        t.scope = "";
        t.persist();

        this.rawKey = t.key;
    }

    @Test
    void bogusAccessTokenIsRejectedWith401() {
        given().header("X-Access-Token", "definitely-not-a-real-key")
                .when().get("/api/me")
                .then().statusCode(401);
    }

    @Test
    void validSeededAccessTokenAuthenticatesWith200() {
        given().header("X-Access-Token", rawKey)
                .when().get("/api/me")
                .then().statusCode(200);
    }
}
