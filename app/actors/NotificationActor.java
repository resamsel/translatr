package actors;

import actors.NotificationProtocol.FollowNotification;
import actors.NotificationProtocol.PublishNotification;
import akka.actor.AbstractActor;
import services.NotificationService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NotificationActor extends AbstractActor {

  public static final String NAME = "notification-actor";

  private final NotificationService notificationService;

  @Inject
  public NotificationActor(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
            .match(PublishNotification.class,
                    t -> notificationService.publish(t.id, t.type, t.name, t.contentId, t.userId, t.projectId))
            .match(FollowNotification.class,
                    t -> notificationService.follow(t.userId, t.projectId))
            .build();
  }
}
