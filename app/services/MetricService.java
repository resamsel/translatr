package services;

import com.google.inject.ImplementedBy;
import io.prometheus.client.Collector;
import models.Project;
import models.User;
import services.impl.MetricServiceImpl;

import java.util.Enumeration;
import java.util.concurrent.Callable;

@ImplementedBy(MetricServiceImpl.class)
public interface MetricService {
  <T> T time(String method, Callable<T> callable);

  void consumeRequest(String method, int status);

  void consumeUser(User user);

  void consumeProject(Project project);

  Enumeration<Collector.MetricFamilySamples> metricFamilySamples();
}
