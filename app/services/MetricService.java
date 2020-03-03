package services;

import com.google.inject.ImplementedBy;
import io.prometheus.client.Collector;
import services.impl.MetricServiceImpl;

import java.util.Enumeration;
import java.util.concurrent.Callable;

@ImplementedBy(MetricServiceImpl.class)
public interface MetricService {
  void consumeRequest(String method, int status);

  <T> T time(String method, Callable<T> callable);

  Enumeration<Collector.MetricFamilySamples> metricFamilySamples();
}
