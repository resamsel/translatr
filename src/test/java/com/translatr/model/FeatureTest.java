package com.translatr.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureTest {

    @Test
    void of_returnsMatchingValue_forKnownKey() {
        assertThat(Feature.of("language-switcher")).contains(Feature.LanguageSwitcher);
    }

    @Test
    void of_returnsEmpty_forUnknownKey() {
        assertThat(Feature.of("no-such-feature")).isEmpty();
    }

    @Test
    void everyValueHasAKeyAndDefaultsToFalse() {
        for (Feature f : Feature.values()) {
            assertThat(f.key).isNotBlank();
            assertThat(f.defaultEnabled).isFalse();
        }
    }

    @Test
    void keysMatchTheFrontendEnum() {
        assertThat(java.util.Arrays.stream(Feature.values()).map(f -> f.key))
            .containsExactlyInAnyOrder(
                "project-cli-card", "project-infographic", "header-graphic", "language-switcher");
    }
}
