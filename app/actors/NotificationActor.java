package actors;

import actors.NotificationProtocol.FollowNotification;
import actors.NotificationProtocol.PublishNotification;
import akka.actor.UntypedActor;
import services.NotificationService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NotificationActor extends UntypedActor {

  public static final String NAME = "notification-actor";

  private final NotificationService notificationService;

  @Inject
  public NotificationActor(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override
  public void onReceive(Object msg) throws Throwable {
    if (msg instanceof PublishNotification) {
      PublishNotification t = (PublishNotification) msg;

      notificationService.publish(t.id, t.type, t.name, t.contentId, t.userId, t.projectId);
    } else if (msg instanceof FollowNotification) {
      FollowNotification t = (FollowNotification) msg;

      notificationService.follow(t.userId, t.projectId);
    }
  }
}
