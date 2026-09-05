package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasKey;

@QuarkusTest
class ProjectResourceTest {

    @Test
    void testFindProjects_returnsOk() {
        given()
            .when().get("/api/projects")
            .then()
            .statusCode(200)
            .body("list",     notNullValue())
            .body("limit",    notNullValue())
            // the Angular "Load more" pager keys off these — they must be on the wire
            .body("hasNext",  notNullValue())
            .body("hasPrev",  is(false));
    }

    @Test
    void testGetProject_notFound() {
        given()
            .when().get("/api/project/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    void getByOwnerAndName_anonymousWithFetchMyrole_isNotForcedToAuthenticate() {
        // ?fetch=myrole must resolve the caller *optionally* on this @PermitAll route:
        // an anonymous request still gets a plain 404 for the missing project, never a 401.
        given()
            .when().get("/api/nobody/no-such-project?fetch=myrole")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "roleowner", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "roleowner-sub"),
        @Claim(key = "name",  value = "Role Owner"),
        @Claim(key = "email", value = "roleowner@example.com")
    })
    void getByOwnerAndName_withFetchMyrole_reportsCallersRole() {
        String projectName = "myrole-project-" + System.currentTimeMillis();

        given()
            .contentType("application/json")
            .body("{\"name\": \"" + projectName + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        // creator is auto-added as an Owner member; the username is derived from the e-mail
        given()
            .when().get("/api/roleownerexample.com/" + projectName + "?fetch=myrole")
            .then()
            .statusCode(200)
            .body("myRole", is("Owner"));
    }

    @Test
    @TestSecurity(user = "norole", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "norole-sub"),
        @Claim(key = "name",  value = "No Role"),
        @Claim(key = "email", value = "norole@example.com")
    })
    void getByOwnerAndName_withoutFetch_omitsMyRole() {
        String projectName = "norole-project-" + System.currentTimeMillis();

        given()
            .contentType("application/json")
            .body("{\"name\": \"" + projectName + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        given()
            .when().get("/api/noroleexample.com/" + projectName)
            .then()
            .statusCode(200)
            .body("$", not(hasKey("myRole")));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "test-sub-001"),
        @Claim(key = "name",  value = "Test User"),
        @Claim(key = "email", value = "testuser@example.com")
    })
    void testCreateProject_authenticated() {
        given()
            .contentType("application/json")
            .body("{\"name\": \"test-project-" + System.currentTimeMillis() + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)));
    }

    @Test
    @TestSecurity(user = "fetchmembers", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "fetchmembers-sub"),
        @Claim(key = "name",  value = "Fetch Members"),
        @Claim(key = "email", value = "fetchmembers@example.com")
    })
    void findProjects_withFetchMembers_returnsMembersArray() {
        String projectName = "fetch-members-project-" + System.currentTimeMillis();
        given()
            .contentType("application/json")
            .body("{\"name\": \"" + projectName + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        given()
            .when().get("/api/projects?fetch=members&name=" + projectName)
            .then()
            .statusCode(200)
            .body("list[0].members", notNullValue())
            .body("list[0].members.size()", is(1))
            .body("list[0].members[0].role", is("Owner"))
            .body("list[0].members[0].userUsername", notNullValue());
    }

    @Test
    @TestSecurity(user = "nofetchlist", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "nofetchlist-sub"),
        @Claim(key = "name",  value = "No Fetch List"),
        @Claim(key = "email", value = "nofetchlist@example.com")
    })
    void findProjects_withoutFetch_omitsMembers() {
        String projectName = "no-fetch-list-project-" + System.currentTimeMillis();
        given()
            .contentType("application/json")
            .body("{\"name\": \"" + projectName + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        given()
            .when().get("/api/projects?name=" + projectName)
            .then()
            .statusCode(200)
            .body("list[0]", not(hasKey("members")));
    }

    @Test
    @TestSecurity(user = "ownerfilter", roles = "User")
    @JwtSecurity(claims = {
        @Claim(key = "sub",   value = "ownerfilter-sub"),
        @Claim(key = "name",  value = "Owner Filter"),
        @Claim(key = "email", value = "ownerfilter@example.com")
    })
    void findProjects_withOwnerUsername_filtersToThatOwner() {
        String projectName = "owner-filter-project-" + System.currentTimeMillis();
        given()
            .contentType("application/json")
            .body("{\"name\": \"" + projectName + "\"}")
            .when().post("/api/project")
            .then()
            .statusCode(anyOf(is(200), is(201)));

        given()
            .when().get("/api/projects?ownerUsername=ownerfilterexample.com&limit=50")
            .then()
            .statusCode(200)
            .body("list.findAll { it.ownerUsername != 'ownerfilterexample.com' }.size()", is(0))
            .body("list.find { it.name == '" + projectName + "' }", notNullValue());
    }
}
