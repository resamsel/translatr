package com.translatr.controller;

import com.translatr.model.LinkedAccount;
import com.translatr.model.User;
import com.translatr.model.UserRole;
import com.translatr.repository.UserRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class UserResourceTest {

    @Inject UserRepository userRepo;

    @Test
    void testFindUsers_returnsOk() {
        given()
            .when().get("/api/users")
            .then()
            .statusCode(200)
            .body("list",  notNullValue())
            .body("limit", notNullValue());
    }

    @Test
    void testGetUser_notFound() {
        given()
            .when().get("/api/user/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-user-sub"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testuser@example.com")
    })
    void testMe_authenticated_returnsUser() {
        given()
            .when().get("/api/me")
            .then()
            .statusCode(200)
            .body("username", notNullValue());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-user-sub"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testuser@example.com")
    })
    void testUpdateUser_missingId_returns404() {
        given()
            .contentType("application/json")
            .body("{\"id\": \"00000000-0000-0000-0000-000000000000\", \"name\": \"New Name\"}")
            .when().put("/api/user")
            .then()
            .statusCode(404);
    }

    // -------------------------------------------------------------------------
    // Keycloak-group-driven Admin role (synced on every OIDC login)
    // -------------------------------------------------------------------------

    @Test
    @TestSecurity(user = "grouptestadmin", roles = "translatr-admin")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "grouptest-admin-sub"),
        @Claim(key = "name",  value = "Group Test Admin"),
        @Claim(key = "email", value = "grouptest-admin@example.com")
    })
    void me_whenInAdminGroup_syncsRoleToAdmin() {
        given()
            .when().get("/api/me")
            .then()
            .statusCode(200)
            .body("role", is("Admin"));
    }

    @Test
    @TestSecurity(user = "grouptestdemote", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "grouptest-demote-sub"),
        @Claim(key = "name",  value = "Group Test Demote"),
        @Claim(key = "email", value = "grouptest-demote@example.com")
    })
    void me_whenNotInAdminGroup_demotesExistingAdmin() {
        // An existing Admin, linked to this OIDC subject, who no longer holds the group.
        QuarkusTransaction.requiringNew().run(() -> {
            User u = userRepo.findByUsername("grouptest-demoteexample.com").orElseGet(() -> {
                User nu = new User();
                nu.username = "grouptest-demoteexample.com";
                nu.name     = "Group Test Demote";
                nu.email    = "grouptest-demote@example.com";
                userRepo.persist(nu);
                return nu;
            });
            u.role = UserRole.Admin;
            if (userRepo.findByLinkedAccount("oidc", "grouptest-demote-sub").isEmpty()) {
                LinkedAccount la = new LinkedAccount();
                la.user           = u;
                la.providerKey    = "oidc";
                la.providerUserId = "grouptest-demote-sub";
                la.persist();
            }
        });

        given()
            .when().get("/api/me")
            .then()
            .statusCode(200)
            .body("role", is("User"));
    }
}
