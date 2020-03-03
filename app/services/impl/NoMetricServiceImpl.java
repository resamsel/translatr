package services.impl;

import io.prometheus.client.Collector;
import services.MetricService;

import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.Callable;

public class NoMetricServiceImpl implements MetricService {
  @Override
  public void consumeRequest(String method, int status) {
  }

  @Override
  public <T> T time(Callable<T> callable) {
    try {
      return callable.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Enumeration<Collector.MetricFamilySamples> metricFamilySamples() {
    return Collections.emptyEnumeration();
  }
}
