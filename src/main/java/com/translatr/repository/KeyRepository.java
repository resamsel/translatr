package com.translatr.repository;

import com.translatr.model.Key;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class KeyRepository implements PanacheRepositoryBase<Key, UUID> {

    public Optional<Key> findByProjectAndName(UUID projectId, String name) {
        return find("project.id = ?1 AND name = ?2", projectId, name).firstResultOptional();
    }

    public List<Key> findByProject(UUID projectId) {
        return list("project.id", projectId);
    }
}
