package services.impl;

import com.google.common.collect.ImmutableMap;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import io.prometheus.client.hotspot.DefaultExports;
import models.AccessToken;
import models.ActionType;
import models.LinkedAccount;
import models.Message;
import models.Model;
import models.ProjectUser;
import services.MetricService;

import javax.inject.Singleton;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.Callable;

@Singleton
public class MetricServiceImpl implements MetricService {
  private static final Map<Class<? extends Model<?, ?>>, String> SUBJECT_MAP = ImmutableMap.<Class<? extends Model<?, ?>>, String>builder()
          .put(ProjectUser.class, "member")
          .put(AccessToken.class, "access-token")
          .put(LinkedAccount.class, "linked-account")
          .put(Message.class, "translation")
          .build();

  private final CollectorRegistry registry = new CollectorRegistry();
  private final Counter requests = Counter.build()
          .name("http_requests_total")
          .help("The total number of HTTP requests")
          .labelNames("method", "code", "host")
          .register(registry);
  private final Summary requestDuration = Summary.build()
          .name("http_request_duration_seconds")
          .help("HTTP request duration in seconds")
          .labelNames("method", "host")
          .quantile(0.5, 0.05)
          .quantile(0.9, 0.02)
          .quantile(0.95, 0.01)
          .quantile(0.99, 0.001)
          .register(registry);

  private final Counter events = Counter.build()
          .name("translatr_events_total")
          .help("The total number of events")
          .labelNames("subject", "verb", "status", "host")
          .register(registry);

  private final String hostname;

  public MetricServiceImpl() {
    DefaultExports.register(registry);

    Gauge buildInfo = Gauge.build()
            .name("translatr_build_info")
            .help("Build information")
            .labelNames("version", "buildtime")
            .register(registry);
    buildInfo.labels(buildinfo.BuildInfo.version(), buildinfo.BuildInfo.builtAtString()).set(1);

    InetAddress host = null;
    try {
      host = InetAddress.getLocalHost();
    } catch (UnknownHostException ignored) {
    }

    if (host != null) {
      hostname = host.getHostName();
    } else {
      hostname = "unknown";
    }
  }

  @Override
  public <T> T time(String method, Callable<T> callable) {
    return requestDuration.labels(method.toLowerCase(), hostname).time(callable);
  }

  @Override
  public void consumeRequest(String method, int status) {
    requests.labels(method.toLowerCase(), String.valueOf(status), hostname).inc();
  }

  @Override
  public void logEvent(Class<? extends Model<?, ?>> subject, ActionType verb) {
    events.labels(
            SUBJECT_MAP.getOrDefault(subject, subject.getSimpleName().toLowerCase()),
            verb.name().toLowerCase(),
            "OK",
            hostname
    ).inc();
  }

  @Override
  public Enumeration<Collector.MetricFamilySamples> metricFamilySamples() {
    return registry.metricFamilySamples();
  }
}
