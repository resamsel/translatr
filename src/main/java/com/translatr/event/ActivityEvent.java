package com.translatr.event;

import com.translatr.model.ActionType;
import com.translatr.model.Project;
import com.translatr.model.User;

public class ActivityEvent {
    public final ActionType type;
    public final User       user;
    public final Project    project;
    public final String     contentType;
    public final Object     before;
    public final Object     after;

    public ActivityEvent(ActionType type, User user, Project project,
                         String contentType, Object before, Object after) {
        this.type        = type;
        this.user        = user;
        this.project     = project;
        this.contentType = contentType;
        this.before      = before;
        this.after       = after;
    }
}
