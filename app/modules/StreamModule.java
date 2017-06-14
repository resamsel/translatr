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
    Option<String> streamIoKey =
        configuration.getString(ConfigKey.StreamIOKey.key(), Option.empty());
    Option<String> streamIoSecret =
        configuration.getString(ConfigKey.StreamIOSecret.key(), Option.empty());

    if (!streamIoKey.isDefined() || !streamIoSecret.isDefined())
      return seq(bind(NotificationService.class).to(NotificationServiceDummy.class));

    String streamIoKeyValue = streamIoKey.get();
    String streamIoSecretValue = streamIoSecret.get();

    if (StringUtils.isEmpty(streamIoKeyValue) || StringUtils.isEmpty(streamIoSecretValue))
      return seq(bind(NotificationService.class).to(NotificationServiceDummy.class));

    return seq(bind(NotificationService.class).to(NotificationServiceImpl.class),
        bind(NotificationSync.class).to(NotificationSyncImpl.class));
  }
}
