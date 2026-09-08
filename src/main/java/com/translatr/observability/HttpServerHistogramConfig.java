package com.translatr.observability;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Enables a percentile histogram + p50..p99 on http.server.requests so SigNoz can query quantiles.
 *
 * <p>Gated by the {@code translatr.observability.metrics-enabled} master switch (default
 * {@code false}): {@code percentilesHistogram(true)} explodes {@code http.server.requests} into
 * ~66-70 {@code _bucket} series per tag combination, so it must stay dormant unless the SigNoz
 * load-test overlay (or a {@code @QuarkusTest}) opts in. When disabled the {@link MeterFilter}
 * returns the incoming {@link DistributionStatisticConfig} untouched.
 */
@Singleton
public class HttpServerHistogramConfig {

    @ConfigProperty(name = "translatr.observability.metrics-enabled", defaultValue = "false")
    boolean metricsEnabled;

    @Produces
    @Singleton
    public MeterFilter httpServerRequestsHistogram() {
        boolean enabled = metricsEnabled;
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                if (enabled && "http.server.requests".equals(id.getName())) {
                    return DistributionStatisticConfig.builder()
                            .percentilesHistogram(true)
                            .percentiles(0.5, 0.9, 0.95, 0.99)
                            .build()
                            .merge(config);
                }
                return config;
            }
        };
    }
}
