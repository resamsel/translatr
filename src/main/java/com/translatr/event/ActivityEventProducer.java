package com.translatr.event;

import com.translatr.model.ActionType;
import com.translatr.model.Project;
import com.translatr.model.User;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Replaces ActivityActorRef — publishes activity events to the Vert.x event bus.
 */
@ApplicationScoped
public class ActivityEventProducer {

    @Inject EventBus bus;

    public void publish(ActionType type, User user, Project project,
                        String contentType, Object before, Object after) {
        bus.publish("activity",
                new ActivityEvent(type, user, project, contentType, before, after));
    }
}
