package com.translatr.repository;

import com.translatr.model.Locale;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class LocaleRepository implements PanacheRepositoryBase<Locale, UUID> {

    public Optional<Locale> findByProjectAndName(UUID projectId, String name) {
        return find("project.id = ?1 AND name = ?2", projectId, name).firstResultOptional();
    }

    public List<Locale> findByProject(UUID projectId) {
        return list("project.id", projectId);
    }
}
