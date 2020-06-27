package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import io.prometheus.client.Collector;
import io.prometheus.client.exporter.common.TextFormat;
import play.inject.Injector;
import play.mvc.Result;
import services.CacheService;
import services.MetricService;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.concurrent.CompletionStage;

public class Metrics extends AbstractController {
  private final MetricService metricService;

  @Inject
  public Metrics(Injector injector, CacheService cache, PlayAuthenticate auth, MetricService metricService) {
    super(injector, cache, auth);
    this.metricService = metricService;
  }

  public CompletionStage<Result> metrics() {
    return tryCatch(() -> ok(format(metricService.metricFamilySamples())));
  }

  private String format(Enumeration<Collector.MetricFamilySamples> samples) throws IOException {
    StringWriter writer = new StringWriter();

    TextFormat.write004(writer, samples);

    return writer.toString();
  }
}
