package modules;

import org.apache.commons.lang3.StringUtils;

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.Option;
import scala.collection.Seq;
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
public class StreamModule extends Module {
  /**
   * {@inheritDoc}
   */
  @Override
  public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
    String streamIoKey = configuration.getString(ConfigKey.StreamIOKey.key(), Option.empty()).get();
    String streamIoSecret =
        configuration.getString(ConfigKey.StreamIOSecret.key(), Option.empty()).get();

    if (StringUtils.isEmpty(streamIoKey) || StringUtils.isEmpty(streamIoSecret))
      return seq(bind(NotificationService.class).to(NotificationServiceDummy.class));

    return seq(bind(NotificationService.class).to(NotificationServiceImpl.class),
        bind(NotificationSync.class).to(NotificationSyncImpl.class));
  }
}
