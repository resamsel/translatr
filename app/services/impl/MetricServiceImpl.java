package services.impl;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import io.prometheus.client.hotspot.DefaultExports;
import models.Project;
import models.User;
import services.MetricService;

import javax.inject.Singleton;
import java.util.Enumeration;
import java.util.concurrent.Callable;

@Singleton
public class MetricServiceImpl implements MetricService {
  private final CollectorRegistry registry = new CollectorRegistry();
  private final Counter requests = Counter.build()
      .name("http_requests_total")
      .help("The total number of HTTP requests")
      .labelNames("method", "code")
      .register(registry);
  private final Summary requestDuration = Summary.build()
      .name("http_request_duration_seconds")
      .help("HTTP request duration in seconds")
      .labelNames("method")
      .quantile(0.5, 0.05)
      .quantile(0.9, 0.02)
      .quantile(0.95, 0.01)
      .quantile(0.99, 0.001)
      .register(registry);

  private final Counter projects = Counter.build()
      .name("translatr_projects_total")
      .help("The total number of created projects")
      .register(registry);

  private final Counter users = Counter.build()
      .name("translatr_users_total")
      .help("The total number of created users")
      .register(registry);

  public MetricServiceImpl() {
    DefaultExports.register(registry);

    Gauge buildInfo = Gauge.build()
        .name("translatr_build_info")
        .help("Build information")
        .labelNames("version", "buildtime")
        .register(registry);
    buildInfo.labels(buildinfo.BuildInfo.version(), buildinfo.BuildInfo.builtAtString()).set(1);
  }

  @Override
  public <T> T time(String method, Callable<T> callable) {
    return requestDuration.labels(method.toLowerCase()).time(callable);
  }

  @Override
  public void consumeRequest(String method, int status) {
    requests.labels(method.toLowerCase(), String.valueOf(status)).inc();
  }

  @Override
  public void consumeUser(User user) {
    users.inc();
  }

  @Override
  public void consumeProject(Project project) {
    projects.inc();
  }

  @Override
  public Enumeration<Collector.MetricFamilySamples> metricFamilySamples() {
    return registry.metricFamilySamples();
  }
}
