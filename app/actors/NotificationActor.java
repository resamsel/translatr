package actors;

import actors.NotificationProtocol.Notification;
import akka.actor.UntypedActor;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.LogEntry;
import services.NotificationService;

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
    if (msg instanceof Notification) {
      Notification t = (Notification) msg;

      notificationService.publish(t.id, t.type, t.name, t.contentId, t.userId, t.projectId);
    }
  }
}
