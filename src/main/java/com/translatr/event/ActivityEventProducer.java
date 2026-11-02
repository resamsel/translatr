package com.translatr.event;

import com.translatr.model.ActionType;
import com.translatr.model.Project;
import com.translatr.model.User;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

/**
 * Replaces ActivityActorRef — publishes activity events to the Vert.x event bus.
 * {@link ActivityEventConsumer} persists them as {@code log_entry} rows asynchronously.
 *
 * <p>The consumer re-loads the user/project by id in its own transaction, so when called inside a
 * transaction the event is held back until that transaction commits (and dropped on rollback).
 * Publishing earlier races the commit and can persist a log entry with a null project.
 */
@ApplicationScoped
public class ActivityEventProducer {

    private final EventBus bus;
    private final TransactionSynchronizationRegistry txSync;

    public ActivityEventProducer(EventBus bus, TransactionSynchronizationRegistry txSync) {
        this.bus = bus;
        this.txSync = txSync;
    }

    /**
     * @param user    the acting user (required)
     * @param project the project the action relates to, or {@code null}
     * @param dtoType the DTO class whose instances are carried in {@code before}/{@code after}
     * @param before  DTO snapshot before the change (null for create)
     * @param after   DTO snapshot after the change (null for delete)
     */
    public void publish(ActionType type, User user, Project project, Class<?> dtoType,
                        Object before, Object after) {
        ActivityEvent event = new ActivityEvent(
                type,
                user    != null ? user.id    : null,
                project != null ? project.id : null,
                legacyContentType(dtoType),
                before,
                after);
        if (txSync.getTransactionStatus() != Status.STATUS_ACTIVE) {
            bus.publish("activity", event);
            return;
        }
        txSync.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {}

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    bus.publish("activity", event);
                }
            }
        });
    }

    /**
     * The Angular activity list keys off {@code "dto.<SimpleName>"} (e.g. {@code "dto.Project"}) —
     * the value the Play backend stored — not the fully-qualified DTO class name. Members were
     * {@code ProjectUser} back then, so keep that name for the translation/icon lookups.
     */
    private static String legacyContentType(Class<?> dtoType) {
        String simple = dtoType.getSimpleName();
        if (simple.endsWith("Dto")) {
            simple = simple.substring(0, simple.length() - "Dto".length());
        }
        if ("Member".equals(simple)) {
            simple = "ProjectUser";
        }
        return "dto." + simple;
    }
}
