package com.translatr.repository;

import com.translatr.model.LogEntry;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class LogEntryRepository implements PanacheRepositoryBase<LogEntry, UUID> {

    public List<LogEntry> findByUser(UUID userId, int page, int size) {
        return find("user.id = ?1 ORDER BY whenCreated DESC", userId)
                .page(page, size).list();
    }

    public long countByUser(UUID userId) {
        return count("user.id = ?1", userId);
    }

    public List<LogEntry> findByProject(UUID projectId, int page, int size) {
        return find("project.id = ?1 ORDER BY whenCreated DESC", projectId)
                .page(page, size).list();
    }

    public long countByProject(UUID projectId) {
        return count("project.id = ?1", projectId);
    }
}
