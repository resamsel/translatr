package com.translatr.config;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class TranslatrConfigTest {

    @Inject TranslatrConfig config;

    @Test
    void testDefaults() {
        assertThat(config.auth().providers()).isNotBlank();
        assertThat(config.redirectBase()).isNotBlank();
    }
}
