package com.translatr.controller;

import com.translatr.model.User;
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
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class MemberResourceOrderSearchCriteriaTest {

    @Inject UserRepository userRepo;

    @Test
    @TestSecurity(user = "membersearchordertest", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "membersearchordertest-sub"),
        @Claim(key = "name",  value = "Member Search Order Test"),
        @Claim(key = "email", value = "membersearchordertest@example.com")
    })
    void findMembersByProject_searchAndOrder_areNotSwapped() {
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"member-swap-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        UUID secondUserId = QuarkusTransaction.requiringNew().call(() -> {
            User nu = new User();
            nu.username = "membertestsecond-" + System.currentTimeMillis();
            nu.name     = "distinctivesearchtarget";
            nu.email    = "membertestsecond-" + System.currentTimeMillis() + "@example.com";
            userRepo.persist(nu);
            return nu.id;
        });

        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"userId\": \"" + secondUserId + "\", " +
                  "\"role\": \"Translator\"}")
            .when().post("/api/member")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        // The project auto-adds its creator as an "Owner" member, so at this point there are
        // exactly 2 members: the auto-created Owner (created first) and the explicitly-created
        // Translator (created second).
        //
        // "order" and "search" are both String-typed params on findMembersByProject, sharing a
        // type with "fetch" too (Member's fetch is a genuine no-op, so it has no observable
        // effect on its own — but a swap involving it would still misroute "order" or "search").
        // If "order" landed in the wrong slot, sorting by role would silently fall back to the
        // default (ORDER BY whenCreated DESC) instead of the requested role ordering. We assert
        // "role asc" (Owner first, alphabetically before Translator) rather than "role desc"
        // specifically because the default fallback would ALSO put the more-recently-created
        // Translator first — a "role desc" assertion can't tell correct binding from silent
        // fallback, but "role asc" genuinely can.
        given()
            .queryParam("order", "role asc")
            .when().get("/api/project/" + projectId + "/members")
            .then()
            .statusCode(200)
            .body("list[0].role", is("Owner"))
            .body("total", is(2));

        // If "search" landed in the wrong slot (e.g. into "order"), it would never reach the
        // WHERE clause's LIKE filter, and the response would include ALL members instead of
        // just the one matching this substring.
        given()
            .queryParam("search", "distinctivesearchtarget")
            .when().get("/api/project/" + projectId + "/members")
            .then()
            .statusCode(200)
            .body("total", is(1))
            .body("list[0].role", is("Translator"));
    }

    @Test
    @TestSecurity(user = "memberlegacyswap", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "memberlegacyswap-sub"),
        @Claim(key = "name",  value = "Member Search Order Legacy Test"),
        @Claim(key = "email", value = "memberlegacyswap@example.com")
    })
    void findMembersByProjectLegacy_searchAndOrder_areNotSwapped() {
        // findMembersByProjectLegacy is a SEPARATELY-BOUND generated interface method from
        // findMembersByProject (different path, own independent parameter binding) even though
        // both eventually call the same toCriteria helper — a positional-argument transposition
        // in this method specifically would compile and pass every other test in this migration.
        String projectId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"member-swap-legacy-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().path("id");

        // Keep this username prefix short: nu.username's @Size(max = 32) constraint must fit the
        // prefix PLUS a 13-digit System.currentTimeMillis() suffix, or persist() throws
        // ConstraintViolationException. "mlegacysecond-" is 14 characters, well under budget.
        UUID secondUserId = QuarkusTransaction.requiringNew().call(() -> {
            User nu = new User();
            nu.username = "mlegacysecond-" + System.currentTimeMillis();
            nu.name     = "distinctivelegacysearchtarget";
            nu.email    = "mlegacysecond-" + System.currentTimeMillis() + "@example.com";
            userRepo.persist(nu);
            return nu.id;
        });

        given()
            .contentType("application/json")
            .body("{\"projectId\": \"" + projectId + "\", \"userId\": \"" + secondUserId + "\", " +
                  "\"role\": \"Translator\"}")
            .when().post("/api/member")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        given()
            .queryParam("order", "role asc")
            .when().get("/api/members/" + projectId)
            .then()
            .statusCode(200)
            .body("list[0].role", is("Owner"))
            .body("total", is(2));

        given()
            .queryParam("search", "distinctivelegacysearchtarget")
            .when().get("/api/members/" + projectId)
            .then()
            .statusCode(200)
            .body("total", is(1))
            .body("list[0].role", is("Translator"));
    }
}
