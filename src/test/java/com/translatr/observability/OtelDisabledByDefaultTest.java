package com.translatr.observability;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class OtelDisabledByDefaultTest {

    @Test
    void otelSdkIsDisabledUnderTheDefaultProfile() {
        boolean disabled = ConfigProvider.getConfig()
                .getValue("quarkus.otel.sdk.disabled", Boolean.class);
        assertThat(disabled)
                .as("OTel SDK must stay dormant unless an overlay enables it")
                .isTrue();
    }
}
