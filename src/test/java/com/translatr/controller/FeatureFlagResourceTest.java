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
import io.restassured.path.json.JsonPath;
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

    /**
     * Triggers OIDC-backed user provisioning for the current @TestSecurity caller (a plain GET is
     * enough) and resolves the derived local user's id, so create-endpoint tests can pass the
     * required matching userId in the request body.
     */
    private UUID resolveCurrentUserId(String email) {
        given().when().get("/api/featureflags").then().statusCode(200);
        String derivedUsername = email.replaceAll("[^a-zA-Z0-9_.-]", "");
        return QuarkusTransaction.requiringNew().call(() ->
                userRepo.findByUsername(derivedUsername).orElseThrow().id);
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

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-idem-sub"),
        @Claim(key = "name",  value = "Test Idempotent User"),
        @Claim(key = "email", value = "testidem@example.com")
    })
    void createUserFeatureFlag_newFlag_returns201AndCreatesFlag() {
        UUID currentUserId = resolveCurrentUserId("testidem@example.com");
        String feature = "theme-switcher";
        String requestBody = "{\"userId\":\"" + currentUserId + "\",\"feature\":\"" + feature + "\",\"enabled\":true}";

        given()
            .contentType("application/json")
            .body(requestBody)
            .when().post("/api/featureflag")
            .then()
            .statusCode(201)
            .body("feature", is(feature))
            .body("enabled", is(true))
            .body("id", notNullValue())
            .body("userId", is(currentUserId.toString()));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-idem-dup-sub"),
        @Claim(key = "name",  value = "Test Idempotent Dup User"),
        @Claim(key = "email", value = "testidmdup@example.com")
    })
    void createUserFeatureFlag_existingFlag_returns200AndUpdatesExistingFlag() {
        UUID currentUserId = resolveCurrentUserId("testidmdup@example.com");
        String feature = "beta-editor";
        String requestBody = "{\"userId\":\"" + currentUserId + "\",\"feature\":\"" + feature + "\",\"enabled\":true}";

        // First create
        String response1 = given()
            .contentType("application/json")
            .body(requestBody)
            .when().post("/api/featureflag")
            .then()
            .statusCode(201)
            .extract().asString();

        // Extract the flagId from the first response
        UUID flagId = JsonPath.from(response1).getUUID("id");

        // Second create with same (user, feature) — should update, not fail
        given()
            .contentType("application/json")
            .body("{\"userId\":\"" + currentUserId + "\",\"feature\":\"" + feature + "\",\"enabled\":false}")
            .when().post("/api/featureflag")
            .then()
            .statusCode(200)
            .body("feature", is(feature))
            .body("enabled", is(false))
            .body("id", is(flagId.toString()))
            .body("userId", is(currentUserId.toString()));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-safe-retry-sub"),
        @Claim(key = "name",  value = "Test Safe Retry User"),
        @Claim(key = "email", value = "testsaferetry@example.com")
    })
    void createUserFeatureFlag_repeatedCreatesSamePOST_firstReturns201SecondReturns200() {
        UUID currentUserId = resolveCurrentUserId("testsaferetry@example.com");
        String feature = "advanced-search";
        String requestBody = "{\"userId\":\"" + currentUserId + "\",\"feature\":\"" + feature + "\",\"enabled\":true}";

        // First POST
        String response1 = given()
            .contentType("application/json")
            .body(requestBody)
            .when().post("/api/featureflag")
            .then()
            .statusCode(201)
            .extract().asString();

        UUID flagId = JsonPath.from(response1).getUUID("id");

        // Second identical POST — should return 200 and preserve ID
        given()
            .contentType("application/json")
            .body(requestBody)
            .when().post("/api/featureflag")
            .then()
            .statusCode(200)
            .body("feature", is(feature))
            .body("enabled", is(true))
            .body("id", is(flagId.toString()));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-upd-sub"),
        @Claim(key = "name",  value = "Test Update User"),
        @Claim(key = "email", value = "testupd@example.com")
    })
    void updateUserFeatureFlag_existingFlagById_returns200AndUpdatesFlag() {
        UUID currentUserId = resolveCurrentUserId("testupd@example.com");
        // First create a flag for the current user
        String createBody = "{\"userId\":\"" + currentUserId + "\",\"feature\":\"beta-editor\",\"enabled\":true}";
        String createResponse = given()
            .contentType("application/json")
            .body(createBody)
            .when().post("/api/featureflag")
            .then()
            .statusCode(201)
            .extract().asString();

        UUID flagId = JsonPath.from(createResponse).getUUID("id");

        // Now update it
        given()
            .contentType("application/json")
            .body("{\"id\":\"" + flagId + "\",\"enabled\":false}")
            .when().put("/api/featureflag")
            .then()
            .statusCode(200)
            .body("id", is(flagId.toString()))
            .body("enabled", is(false));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-upd-notfound-sub"),
        @Claim(key = "name",  value = "Test Update NotFound User"),
        @Claim(key = "email", value = "testupdnf@example.com")
    })
    void updateUserFeatureFlag_nonexistentFlagId_returns404() {
        UUID fakeFlagId = UUID.randomUUID();

        given()
            .contentType("application/json")
            .body("{\"id\":\"" + fakeFlagId + "\",\"enabled\":false}")
            .when().put("/api/featureflag")
            .then()
            .statusCode(404);
    }
}
