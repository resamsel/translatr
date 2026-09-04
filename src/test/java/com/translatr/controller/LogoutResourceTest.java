package com.translatr.controller;

import com.translatr.testsupport.OidcEnabledNoCredentialsTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.matcher.RestAssuredMatchers.detailedCookie;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;

/**
 * {@code GET /logout} has to exist and end the local session. It used to be registered by the static
 * {@code quarkus.oidc.logout.*} properties, which the multi-tenant migration removed; the Angular
 * navbar links to it unconditionally.
 *
 * <p>Runs with OIDC ENABLED so the route is exercised in the configuration that actually ships (a
 * purely dynamic {@code TenantConfigResolver}, no static tenant).</p>
 */
@QuarkusTest
@TestProfile(OidcEnabledNoCredentialsTestProfile.class)
class LogoutResourceTest {

    @Test
    void expiresOidcCookies_andRedirectsToTheSpa() {
        var cookies = given()
            .redirects().follow(false)
            .cookie("q_session_google", "opaque-session")
            .cookie("q_session_google_chunk_1", "opaque-chunk")
            .cookie("q_auth_google", "opaque-state")
            .cookie("unrelated", "keep-me")
            .when().get("/logout")
            .then()
            .statusCode(303)
            .header("Location", endsWith("/ui"))
            .cookie("q_session_google", detailedCookie().value("").maxAge(0))
            .cookie("q_session_google_chunk_1", detailedCookie().maxAge(0))
            .cookie("q_auth_google", detailedCookie().maxAge(0))
            .extract().detailedCookies();

        assertThat(cookies.hasCookieWithName("unrelated")).isFalse();
    }

    @Test
    void isNotFourOhFour_withoutASession() {
        given()
            .redirects().follow(false)
            .when().get("/logout")
            .then()
            .statusCode(303)
            .header("Location", endsWith("/ui"));
    }
}
