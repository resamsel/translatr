package com.translatr.observability;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

/** Enables a percentile histogram + p50..p99 on http.server.requests so SigNoz can query quantiles. */
public class HttpServerHistogramConfig {

    @Produces
    @Singleton
    public MeterFilter httpServerRequestsHistogram() {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                if ("http.server.requests".equals(id.getName())) {
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
