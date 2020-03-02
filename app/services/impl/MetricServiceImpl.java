package services.impl;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import io.prometheus.client.hotspot.DefaultExports;
import services.MetricService;

import javax.inject.Singleton;
import java.util.concurrent.Callable;

@Singleton
public class MetricServiceImpl implements MetricService {
  private static final Gauge buildInfo = Gauge.build()
          .name("translatr_build_info")
          .help("Build information")
          .labelNames("version", "buildtime")
          .register();
  private static final Counter requests = Counter.build()
          .name("http_requests_total")
          .help("The total number of HTTP requests")
          .labelNames("method", "code")
          .register();
  private static final Summary requestTiming = Summary.build()
          .name("http_request_duration_seconds")
          .help("HTTP request duration in seconds")
          .quantile(0.5, 0.05)
          .quantile(0.9, 0.01)
          .register();

  public MetricServiceImpl() {
    DefaultExports.initialize();
    buildInfo.labels(buildinfo.BuildInfo.version(), buildinfo.BuildInfo.builtAtString()).set(1);
  }

  @Override
  public void consumeRequest(String method, int status) {
    requests.labels(method.toLowerCase(), String.valueOf(status)).inc();
  }

  @Override
  public <T> T time(Callable<T> callable) {
    return requestTiming.time(callable);
  }
}
