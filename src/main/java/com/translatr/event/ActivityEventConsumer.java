package com.translatr.event;

import com.translatr.model.LogEntry;
import com.translatr.repository.LogEntryRepository;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces ActivityActor (Akka) — persists activity log entries asynchronously.
 */
@ApplicationScoped
public class ActivityEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityEventConsumer.class);

    @Inject LogEntryRepository logRepo;

    @ConsumeEvent("activity")
    @Transactional
    @SuppressWarnings("unchecked")
    public void onActivity(ActivityEvent event) {
        LOG.debug("onActivity: type={} user={}", event.type, event.user != null ? event.user.username : null);
        try {
            @SuppressWarnings("rawtypes")
            Class clazz = event.contentType != null ? Class.forName(event.contentType) : Object.class;
            LogEntry entry = LogEntry.from(event.type, event.user, event.project, clazz,
                    event.before, event.after);
            logRepo.persist(entry);
        } catch (ClassNotFoundException e) {
            LOG.warn("Unknown contentType class: {}", event.contentType);
        }
    }
}
