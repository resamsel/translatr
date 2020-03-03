package services;

import com.google.inject.ImplementedBy;
import services.impl.MetricServiceImpl;

import java.util.concurrent.Callable;

@ImplementedBy(MetricServiceImpl.class)
public interface MetricService {
  void consumeRequest(String method, int status);

  <T> T time(Callable<T> callable);
}
