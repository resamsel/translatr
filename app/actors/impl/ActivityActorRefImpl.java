package actors.impl;

import actors.ActivityActor;
import actors.ActivityActorRef;
import akka.actor.ActorRef;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class ActivityActorRefImpl implements ActivityActorRef {

  private final ActorRef actor;

  @Inject
  public ActivityActorRefImpl(@Named(ActivityActor.NAME) ActorRef actor) {
    this.actor = actor;
  }

  @Override
  public void tell(Object msg, ActorRef sender) {
    actor.tell(msg, sender);
  }
}
