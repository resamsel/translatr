package com.translatr.auth;

import com.translatr.model.AccessToken;
import com.translatr.model.Project;
import com.translatr.model.User;
import com.translatr.model.UserRole;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * Drives the real {@link ScopeEnforcementFilter} over HTTP with seeded access tokens (#364):
 * scopes restrict tokens, while anonymous access to public reads is unchanged.
 */
@QuarkusTest
class ScopeEnforcementEndToEndTest {

    User user;
    UUID projectId;

    @BeforeEach
    @Transactional
    void seed() {
        user = new User();
        user.name = "scopes";
        user.username = "scopes-" + System.nanoTime();
        user.email = user.username + "@example.com";
        user.role = UserRole.Admin;
        user.persist();

        Project p = new Project();
        p.owner = user;
        p.name = "scopes-project-" + System.nanoTime();
        p.persist();
        projectId = p.id;
    }

    @Transactional
    String token(String scope) {
        AccessToken t = new AccessToken();
        t.user = User.findById(user.id);
        t.name = "scope-test";
        t.key = "set-" + System.nanoTime();
        t.scope = scope;
        t.persist();
        return t.key;
    }

    @Test
    void readOnlyToken_canListProjects() {
        given().header("X-Access-Token", token("read:project"))
                .when().get("/api/projects")
                .then().statusCode(200);
    }

    @Test
    void readOnlyToken_cannotCreateProject() {
        given().header("X-Access-Token", token("read:project"))
                .contentType("application/json")
                .body("{\"name\": \"denied-" + System.nanoTime() + "\"}")
                .when().post("/api/project")
                .then().statusCode(403)
                .header("WWW-Authenticate", "Bearer error=\"insufficient_scope\", scope=\"write:project\"")
                .body("error", is("insufficient_scope"))
                .body("required", contains("write:project"));
    }

    @Test
    void fullScopeToken_canCreateProject() {
        given().header("X-Access-Token", token(AdminAccessTokenSeeder.SCOPE))
                .contentType("application/json")
                .body("{\"name\": \"allowed-" + System.nanoTime() + "\"}")
                .when().post("/api/project")
                .then().statusCode(anyOf(is(200), is(201)));
    }

    @Test
    void anonymous_canReadProject() {
        given().when().get("/api/project/" + projectId)
                .then().statusCode(200);
    }

    @Test
    void tokenWithoutProjectRead_cannotReadProject() {
        given().header("X-Access-Token", token("read:user"))
                .when().get("/api/project/" + projectId)
                .then().statusCode(403)
                .body("required", contains("read:project"));
    }

    @Test
    void fetchMembersWithoutMemberRead_403() {
        given().header("X-Access-Token", token("read:project"))
                .when().get("/api/projects?fetch=members")
                .then().statusCode(403)
                .body("required", contains("read:member"));
    }

    @Test
    void readOnlyToken_cannotImportLocale() {
        given().header("X-Access-Token", token("read:locale,read:message"))
                .contentType("application/octet-stream")
                .body("{}".getBytes())
                .when().post("/api/locale/" + UUID.randomUUID() + "/import/json")
                .then().statusCode(403)
                .body("required", contains("write:locale", "write:message"));
    }

    @Test
    void tokenCannotUseOidcProviders() {
        given().header("X-Access-Token", token(AdminAccessTokenSeeder.SCOPE))
                .when().get("/api/oidc-providers")
                .then().statusCode(403);
    }
}
