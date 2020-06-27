package modules;

import com.google.inject.AbstractModule;
import services.MetricService;
import services.impl.NoMetricServiceImpl;

public class NoMetricModule extends AbstractModule {

  /**
   * {@inheritDoc}
   */
  @Override
  protected void configure() {
    bind(MetricService.class).to(NoMetricServiceImpl.class);
  }
}
