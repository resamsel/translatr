package com.translatr.controller;

import com.translatr.model.User;
import com.translatr.model.UserFeatureFlag;
import com.translatr.model.UserRole;
import com.translatr.repository.UserFeatureFlagRepository;
import com.translatr.repository.UserRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class FeatureFlagResourceTest {

    @Inject UserRepository            userRepo;
    @Inject UserFeatureFlagRepository featureFlagRepo;

    /** Persists a standalone user (not linked to any OIDC subject) to act as "someone else". */
    private UUID persistOtherUser(String usernamePrefix) {
        // User.username/name are capped at 32 chars — keep prefixes short and the suffix compact.
        String username = usernamePrefix + "-" + Long.toString(System.nanoTime(), 36);
        return QuarkusTransaction.requiringNew().call(() -> {
            User u = new User();
            u.username = username;
            u.name     = username;
            u.email    = username + "@example.com";
            u.role     = UserRole.User;
            userRepo.persist(u);
            return u.id;
        });
    }

    private UUID persistFlagForUser(UUID userId, String feature) {
        return QuarkusTransaction.requiringNew().call(() -> {
            User owner = userRepo.findByIdOptional(userId).orElseThrow();
            UserFeatureFlag flag = UserFeatureFlag.of(owner, feature, true);
            featureFlagRepo.persist(flag);
            return flag.id;
        });
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-ff-sub"),
        @Claim(key = "name",  value = "Test FF User"),
        @Claim(key = "email", value = "testff@example.com")
    })
    void findUserFeatureFlags_authenticated_returnsPagedShape() {
        given()
            .when().get("/api/featureflags")
            .then()
            .statusCode(200)
            .body("list",  notNullValue())
            .body("limit", notNullValue())
            .body("offset", notNullValue());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-ff-sub"),
        @Claim(key = "name",  value = "Test FF User"),
        @Claim(key = "email", value = "testff@example.com")
    })
    void findUserFeatureFlags_queryParams_reflectedInResponse() {
        given()
            .when().get("/api/featureflags?offset=0&limit=1&search=zzz&order=feature&feature=beta")
            .then()
            .statusCode(200)
            .body("offset", is(0))
            .body("limit",  is(1));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-ff-sub"),
        @Claim(key = "name",  value = "Test FF User"),
        @Claim(key = "email", value = "testff@example.com")
    })
    void getUserFeatureFlag_unknownId_returns404() {
        given()
            .when().get("/api/featureflag/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    void findUserFeatureFlags_anonymous_isUnauthorized() {
        given()
            .when().get("/api/featureflags")
            .then()
            .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-ff-sub"),
        @Claim(key = "name",  value = "Test FF User"),
        @Claim(key = "email", value = "testff@example.com")
    })
    void resolved_forOwnUserId_isAllowedWhenNotAdmin() {
        String ownId = given().when().get("/api/me").then().extract().path("id");

        given()
            .when().get("/api/featureflags/resolved?userId=" + ownId)
            .then()
            .statusCode(200)
            .body("size()", is(com.translatr.model.Feature.values().length));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-ff-sub"),
        @Claim(key = "name",  value = "Test FF User"),
        @Claim(key = "email", value = "testff@example.com")
    })
    void resolved_forAnotherUser_deniedWhenNotAdmin() {
        UUID otherId = persistOtherUser("ffres-oth");

        given()
            .when().get("/api/featureflags/resolved?userId=" + otherId)
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "ffadmin", roles = "translatr-admin")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "ff-admin-sub"),
        @Claim(key = "name",  value = "FF Admin"),
        @Claim(key = "email", value = "ff-admin@example.com")
    })
    void resolved_forAnotherUser_allowedWhenAdmin() {
        UUID otherId = persistOtherUser("ffres-adm");

        given()
            .when().get("/api/featureflags/resolved?userId=" + otherId)
            .then()
            .statusCode(200)
            .body("size()", is(com.translatr.model.Feature.values().length));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-ff-sub"),
        @Claim(key = "name",  value = "Test FF User"),
        @Claim(key = "email", value = "testff@example.com")
    })
    void createUserFeatureFlag_forAnotherUser_deniedWhenNotAdmin() {
        UUID otherId = persistOtherUser("ffcre-oth");

        given()
            .contentType("application/json")
            .body("{\"userId\":\"" + otherId + "\",\"feature\":\"beta-editor\",\"enabled\":true}")
            .when().post("/api/featureflag")
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-ff-sub"),
        @Claim(key = "name",  value = "Test FF User"),
        @Claim(key = "email", value = "testff@example.com")
    })
    void updateUserFeatureFlag_forAnotherUsersFlag_deniedWhenNotAdmin() {
        UUID otherId = persistOtherUser("ffupd-oth");
        UUID flagId  = persistFlagForUser(otherId, "beta-editor");

        given()
            .contentType("application/json")
            .body("{\"id\":\"" + flagId + "\",\"enabled\":false}")
            .when().put("/api/featureflag")
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-ff-sub"),
        @Claim(key = "name",  value = "Test FF User"),
        @Claim(key = "email", value = "testff@example.com")
    })
    void deleteUserFeatureFlag_forAnotherUsersFlag_deniedWhenNotAdmin() {
        UUID otherId = persistOtherUser("ffdel-oth");
        UUID flagId  = persistFlagForUser(otherId, "beta-editor");

        given()
            .when().delete("/api/featureflag/" + flagId)
            .then()
            .statusCode(403);
    }
}
