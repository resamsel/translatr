package com.translatr.auth;

import com.translatr.model.AccessToken;
import com.translatr.model.User;
import com.translatr.model.UserRole;
import com.translatr.repository.AccessTokenRepository;
import com.translatr.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AdminAccessTokenSeederTest {

    @Inject AdminAccessTokenSeeder seeder;
    @Inject AccessTokenRepository tokens;
    @Inject UserRepository users;

    @Test
    void seedsAdminTokenThatAuthenticates() {
        String key = "seed-" + System.nanoTime();

        seeder.seed(key);

        AccessToken t = find(key);
        assertEquals(UserRole.Admin, t.user.role);
        assertEquals(AdminAccessTokenSeeder.ADMIN_USERNAME, t.user.username);
        assertTrue(t.scope.contains("read:accesstoken"));
        assertEquals(AdminAccessTokenSeeder.SCOPE, t.scope);
        assertTrue(t.scope.contains("write:featureflag"));

        given().header("X-Access-Token", key)
                .when().get("/api/me")
                .then().statusCode(200);
    }

    @Test
    void seedingTwiceIsIdempotent() {
        String key = "seed-" + System.nanoTime();

        seeder.seed(key);
        seeder.seed(key);

        assertEquals(1, count(key));
        assertEquals(1, adminUsers());
    }

    @Test
    void secondKeyReusesExistingAdminUser() {
        seeder.seed("seed-a-" + System.nanoTime());
        seeder.seed("seed-b-" + System.nanoTime());

        assertEquals(1, adminUsers());
    }

    @Transactional
    AccessToken find(String key) {
        AccessToken t = tokens.findByKey(key).orElseThrow();
        t.user.role.name(); // initialise lazy user
        return t;
    }

    @Transactional
    long count(String key) {
        return tokens.count("key", key);
    }

    @Transactional
    long adminUsers() {
        return users.count("username", AdminAccessTokenSeeder.ADMIN_USERNAME);
    }

    @Test
    void seededScopeCoversEveryScope() {
        org.assertj.core.api.Assertions.assertThat(Scope.parse(AdminAccessTokenSeeder.SCOPE))
                .containsExactlyInAnyOrder(Scope.values());
    }
}
