package com.translatr.repository;

import com.translatr.model.ProjectUser;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProjectUserRepository implements PanacheRepositoryBase<ProjectUser, Long> {

    public Optional<ProjectUser> findByProjectAndUser(UUID projectId, UUID userId) {
        return find("project.id = ?1 AND user.id = ?2", projectId, userId)
                .firstResultOptional();
    }
}
