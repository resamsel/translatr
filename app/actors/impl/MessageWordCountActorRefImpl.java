package actors.impl;

import actors.MessageWordCountActor;
import actors.MessageWordCountActorRef;
import akka.actor.ActorRef;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class MessageWordCountActorRefImpl implements MessageWordCountActorRef {
  private final ActorRef actor;

  @Inject
  public MessageWordCountActorRefImpl(@Named(MessageWordCountActor.NAME) ActorRef actor) {
    this.actor = actor;
  }

  @Override
  public void tell(Object msg, ActorRef sender) {
    actor.tell(msg, sender);
  }
}
