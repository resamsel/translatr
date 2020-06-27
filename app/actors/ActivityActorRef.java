package actors;

import actors.impl.ActivityActorRefImpl;
import akka.actor.ActorRef;
import com.google.inject.ImplementedBy;

@ImplementedBy(ActivityActorRefImpl.class)
public interface ActivityActorRef {
  void tell(Object msg, ActorRef sender);
}
