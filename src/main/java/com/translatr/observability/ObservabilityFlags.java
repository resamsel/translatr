package com.translatr.observability;

import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Live reads of the observability switches. Deliberately not {@code @ConfigProperty} fields:
 * in a native image, JAX-RS provider and meter-filter instances are built during Quarkus'
 * STATIC_INIT phase (at image-build time), so an injected field would freeze the build-time
 * value and Quarkus aborts startup when the runtime env var differs. Reading at call time
 * always reflects the running process's config.
 */
final class ObservabilityFlags {

    private ObservabilityFlags() {}

    static boolean metricsEnabled() {
        return ConfigProvider.getConfig()
                .getOptionalValue("translatr.observability.metrics-enabled", Boolean.class)
                .orElse(false);
    }

    static boolean keyIdLabel() {
        return ConfigProvider.getConfig()
                .getOptionalValue("translatr.observability.apikey-metrics.key-id-label", Boolean.class)
                .orElse(true);
    }
}
