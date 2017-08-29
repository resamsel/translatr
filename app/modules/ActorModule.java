package modules;

import actors.ActivityActor;
import actors.KeyWordCountActor;
import actors.LocaleWordCountActor;
import actors.MessageWordCountActor;
import actors.NotificationActor;
import actors.ProjectWordCountActor;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
public class ActorModule extends AbstractModule implements AkkaGuiceSupport {

  /**
   * {@inheritDoc}
   */
  @Override
  protected void configure() {
    bindActor(MessageWordCountActor.class, MessageWordCountActor.NAME);
    bindActor(LocaleWordCountActor.class, LocaleWordCountActor.NAME);
    bindActor(KeyWordCountActor.class, KeyWordCountActor.NAME);
    bindActor(ProjectWordCountActor.class, ProjectWordCountActor.NAME);
    bindActor(NotificationActor.class, NotificationActor.NAME);
    bindActor(ActivityActor.class, ActivityActor.NAME);
  }
}
