package com.translatr.event;

import com.translatr.model.LogEntry;
import com.translatr.repository.LogEntryRepository;
import com.translatr.repository.ProjectRepository;
import com.translatr.repository.UserRepository;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces ActivityActor (Akka) — persists activity log entries asynchronously.
 * The user/project are re-loaded here (in this consumer's own transaction) from the
 * ids carried by {@link ActivityEvent}, rather than passed as detached entities.
 */
@ApplicationScoped
public class ActivityEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityEventConsumer.class);

    @Inject LogEntryRepository logRepo;
    @Inject UserRepository     userRepo;
    @Inject ProjectRepository  projectRepo;

    @ConsumeEvent("activity")
    @Blocking
    @Transactional
    public void onActivity(ActivityEvent event) {
        LOG.debug("onActivity: type={} userId={} contentType={}",
                event.type, event.userId, event.contentType);
        var user    = event.userId    != null ? userRepo.findById(event.userId)       : null;
        var project = event.projectId != null ? projectRepo.findById(event.projectId) : null;
        LogEntry entry = LogEntry.from(event.type, user, project, event.contentType,
                event.before, event.after);
        logRepo.persist(entry);
    }
}
