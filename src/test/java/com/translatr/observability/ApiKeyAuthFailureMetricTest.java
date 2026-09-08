package com.translatr.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the real {@link com.translatr.auth.AccessTokenAuthMechanism} reject path and
 * asserts it feeds the {@code translatr.apikey.auth.failures} Micrometer counter.
 *
 * <p>{@code GET /api/me} is {@code @Authenticated} and routes through the mechanism (same
 * route as {@code AccessTokenAuthMechanismTest}).
 *
 * <p>No {@code reason=expired} case: the {@code AccessToken} entity has no expiry field.
 * A fully token-less request is a normal OIDC/browser flow and increments nothing.
 */
@QuarkusTest
class ApiKeyAuthFailureMetricTest {

    @Inject
    MeterRegistry registry;

    @Test
    void invalidTokenIncrementsFailureCounterWithReasonInvalid() {
        double before = count("invalid");

        given().header("X-Access-Token", "not-a-real-key-" + System.nanoTime())
                .when().get("/api/me")
                .then().statusCode(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is(401), org.hamcrest.Matchers.is(404)));

        assertThat(count("invalid")).isGreaterThan(before);
    }

    @Test
    void blankAccessTokenHeaderIncrementsFailureCounterWithReasonMissing() {
        double before = count("missing");

        // RestAssured drops a truly empty header value, so send a single space — still
        // blank per String#isBlank(), which is exactly the "missing" branch's guard.
        given().header("X-Access-Token", " ")
                .when().get("/api/me")
                .then().statusCode(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is(401), org.hamcrest.Matchers.is(404)));

        assertThat(count("missing")).isGreaterThan(before);
    }

    private double count(String reason) {
        var c = registry.find("translatr.apikey.auth.failures").tag("reason", reason).counter();
        return c == null ? 0d : c.count();
    }
}
