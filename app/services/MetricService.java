package services;

import com.google.inject.ImplementedBy;
import io.prometheus.client.Collector;
import models.ActionType;
import models.Model;
import services.impl.MetricServiceImpl;

import java.util.Enumeration;
import java.util.concurrent.Callable;

@ImplementedBy(MetricServiceImpl.class)
public interface MetricService {
  <T> T time(String method, Callable<T> callable);

  void consumeRequest(String method, int status);

  void logEvent(Class<? extends Model<?, ?>> subject, ActionType action);

  Enumeration<Collector.MetricFamilySamples> metricFamilySamples();
}
