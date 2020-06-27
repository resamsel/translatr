package actors;

import actors.impl.NotificationActorRefImpl;
import akka.actor.ActorRef;
import com.google.inject.ImplementedBy;

@ImplementedBy(NotificationActorRefImpl.class)
public interface NotificationActorRef {
  void tell(Object msg, ActorRef sender);
}
