package com.translatr.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;
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

    /**
     * A parametrised route ({@code GET /api/user/{id}}) must record its counter under the JAX-RS
     * <em>template</em> — the {@code {id}} segment, not the raw UUID from the request line.
     */
    @Test
    void parametrisedRouteRecordsTheTemplateNotTheRawId() {
        String someId = UUID.randomUUID().toString();

        given().header("X-Access-Token", rawKey)
                .when().get("/api/user/" + someId)
                .then().statusCode(anyOf(is(200), is(403), is(404)));

        Counter c = registry.find("translatr.apikey.requests")
                .tag("endpoint", "/api/user/{id}")
                .counter();
        assertThat(c)
                .as("param route recorded under its JAX-RS template, not /api/user/" + someId)
                .isNotNull();
        String endpoint = c.getId().getTag("endpoint");
        assertThat(endpoint).isEqualTo("/api/user/{id}");
        assertThat(endpoint).contains("{");
        assertThat(endpoint).doesNotContain(someId);
        assertThat(endpoint).doesNotMatch(".*/[0-9a-fA-F-]{8,}.*");
    }

    /**
     * A key-authenticated request that matches <em>no</em> route (any 404 under {@code /api/**})
     * must collapse to the single constant {@code /api/{unmatched}} — one valid key must not be
     * able to mint an unbounded number of {@code endpoint} label values with junk paths.
     */
    @Test
    void unmatchedApiPathCollapsesToASingleConstant() {
        given().header("X-Access-Token", rawKey)
                .when().get("/api/definitely-not-a-route/x/y/z")
                .then().statusCode(is(404));

        Counter c = registry.find("translatr.apikey.requests")
                .tag("endpoint", "/api/{unmatched}")
                .counter();
        assertThat(c)
                .as("unmatched /api path collapsed to the /api/{unmatched} constant")
                .isNotNull();
        assertThat(c.getId().getTag("endpoint")).isEqualTo("/api/{unmatched}");
        // the junk segments from the request line must not have leaked into the label
        registry.find("translatr.apikey.requests").counters()
                .forEach(other -> assertThat(other.getId().getTag("endpoint"))
                        .doesNotContain("definitely-not-a-route"));
    }
}
