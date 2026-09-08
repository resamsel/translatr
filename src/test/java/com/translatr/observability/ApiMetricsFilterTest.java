package com.translatr.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.translatr.model.AccessToken;
import com.translatr.model.User;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

/**
 * Exercises {@link ApiMetricsFilter} end to end over HTTP: a key-authenticated {@code /api}
 * call must land in {@code translatr.apikey.requests} with a non-blank {@code key_id} and an
 * {@code endpoint} tag that is the JAX-RS <em>template</em> (starts with {@code /api}, contains
 * no raw id/UUID). Span attributes are not asserted — {@code @QuarkusTest} leaves the OTel SDK
 * disabled, so {@code Span.current()} is invalid and the request filter is a deliberate no-op
 * there; the Micrometer counter does not need OTel.
 *
 * <p>Token seeding mirrors {@code AccessTokenAuthMechanismTest} (Task 4): a real {@link User} +
 * {@link AccessToken} persisted with {@link System#nanoTime()}-suffixed unique fields.
 */
@QuarkusTest
class ApiMetricsFilterTest {

    @Inject MeterRegistry registry;

    String rawKey;

    @BeforeEach
    @Transactional
    void seedToken() {
        User u = new User();
        u.name = "apimetrics";
        u.username = "apimetrics-" + System.nanoTime();
        u.email = u.username + "@example.com";
        u.persist();

        AccessToken t = new AccessToken();
        t.user = u;
        t.name = "test";
        t.key = "amf-" + System.nanoTime();
        t.scope = "";
        t.persist();

        this.rawKey = t.key;
    }

    @Test
    void keyAuthenticatedApiCallIncrementsPerKeyCounterWithTemplatedEndpoint() {
        given().header("X-Access-Token", rawKey)
                .when().get("/api/me")
                .then().statusCode(anyOf(is(200), is(404)));

        Search s = registry.find("translatr.apikey.requests");
        assertThat(s.counters())
                .as("per-key counter recorded")
                .isNotEmpty();
        s.counters().forEach(c -> {
            assertThat(c.getId().getTag("key_id")).isNotBlank();
            String endpoint = c.getId().getTag("endpoint");
            assertThat(endpoint).startsWith("/api");
            // templated: no bare numeric/UUID segment
            assertThat(endpoint).doesNotMatch(".*/[0-9a-fA-F-]{8,}.*");
        });
    }
}
