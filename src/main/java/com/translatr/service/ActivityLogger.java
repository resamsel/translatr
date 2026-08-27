package com.translatr.service;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.event.ActivityEventProducer;
import com.translatr.model.ActionType;
import com.translatr.model.Project;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Records domain-model changes as activity log entries. Replaces the per-service
 * {@code activityActor.tell(...)} calls of the old {@code AbstractModelService}:
 * resolves the acting user and hands the event to {@link ActivityEventProducer},
 * which persists it asynchronously as a {@code log_entry} row.
 */
@ApplicationScoped
public class ActivityLogger {

    private final ActivityEventProducer events;
    private final CurrentUserResolver   currentUser;

    @Inject
    public ActivityLogger(ActivityEventProducer events, CurrentUserResolver currentUser) {
        this.events      = events;
        this.currentUser = currentUser;
    }

    /**
     * @param project the project the change relates to, or {@code null}
     * @param dtoType the DTO type carried in {@code before}/{@code after}
     * @param before  DTO snapshot before the change ({@code null} for {@link ActionType#Create})
     * @param after   DTO snapshot after the change ({@code null} for {@link ActionType#Delete})
     */
    public void publish(ActionType type, Project project, Class<?> dtoType,
                        Object before, Object after) {
        events.publish(type, currentUser.resolve(), project, dtoType, before, after);
    }
}
