package com.translatr.event;

import com.translatr.model.ActionType;
import java.util.UUID;

/**
 * Payload for the "activity" event-bus address. Carries only the user/project ids
 * (not entity references) so the asynchronous {@link ActivityEventConsumer} can
 * re-load them in its own transaction — mirrors how {@code WordCountEvent} works.
 */
public class ActivityEvent {
    public final ActionType type;
    public final UUID       userId;
    public final UUID       projectId;
    public final String     contentType;
    public final Object     before;
    public final Object     after;

    public ActivityEvent(ActionType type, UUID userId, UUID projectId,
                         String contentType, Object before, Object after) {
        this.type        = type;
        this.userId      = userId;
        this.projectId   = projectId;
        this.contentType = contentType;
        this.before      = before;
        this.after       = after;
    }
}
