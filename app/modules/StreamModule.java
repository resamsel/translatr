package modules;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import play.Environment;
import services.NotificationService;
import services.NotificationSync;
import services.impl.NotificationServiceDummy;
import services.impl.NotificationServiceImpl;
import services.impl.NotificationSyncImpl;
import utils.ConfigKey;

/**
 * @author resamsel
 * @version 19 May 2017
 */
public class StreamModule extends AbstractModule {
  private final Config config;

  public StreamModule(final Environment environment, final Config config) {
    this.config = config;
  }

  @Override
  protected void configure() {
    if (!ConfigKey.StreamIOKey.existsIn(config) || !ConfigKey.StreamIOSecret.existsIn(config)) {
      bind(NotificationService.class).to(NotificationServiceDummy.class);
      return;
    }

    bind(NotificationService.class).to(NotificationServiceImpl.class);
    bind(NotificationSync.class).to(NotificationSyncImpl.class);
  }
}
