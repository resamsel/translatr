package actors;

import actors.impl.MessageWordCountActorRefImpl;
import akka.actor.ActorRef;
import com.google.inject.ImplementedBy;

@ImplementedBy(MessageWordCountActorRefImpl.class)
public interface MessageWordCountActorRef {
  void tell(Object msg, ActorRef sender);
}
