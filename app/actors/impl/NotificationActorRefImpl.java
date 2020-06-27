package actors.impl;

import actors.NotificationActor;
import actors.NotificationActorRef;
import akka.actor.ActorRef;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class NotificationActorRefImpl implements NotificationActorRef {

  private final ActorRef actor;

  @Inject
  public NotificationActorRefImpl(@Named(NotificationActor.NAME) ActorRef actor) {
    this.actor = actor;
  }

  @Override
  public void tell(Object msg, ActorRef sender) {
    actor.tell(msg, sender);
  }
}
