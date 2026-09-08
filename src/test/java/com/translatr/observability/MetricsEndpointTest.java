package com.translatr.observability;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class MetricsEndpointTest {

    @Test
    void metricsEndpointExposesHttpServerRequestPercentiles() {
        // Generate at least one server request so the meter exists.
        given().when().get("/health").then().statusCode(200);

        given().when().get("/metrics")
                .then()
                .statusCode(200)
                // percentile-histogram=true + percentiles=... => a 0.95 quantile series
                .body(containsString("http_server_requests_seconds{"))
                .body(containsString("quantile=\"0.95\""));
    }
}
