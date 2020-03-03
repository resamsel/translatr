package services.impl;

import io.prometheus.client.Collector;
import models.Project;
import models.User;
import services.MetricService;

import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.Callable;

public class NoMetricServiceImpl implements MetricService {
  @Override
  public <T> T time(String method, Callable<T> callable) {
    try {
      return callable.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void consumeRequest(String method, int status) {
  }

  @Override
  public void consumeUser(User user) {
  }

  @Override
  public void consumeProject(Project project) {
  }

  @Override
  public Enumeration<Collector.MetricFamilySamples> metricFamilySamples() {
    return Collections.emptyEnumeration();
  }
}
