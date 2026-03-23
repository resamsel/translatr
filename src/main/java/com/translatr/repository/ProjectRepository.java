package com.translatr.repository;

import com.translatr.model.Project;
import com.translatr.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProjectRepository implements PanacheRepositoryBase<Project, UUID> {

    public Optional<Project> findByOwnerUsernameAndName(String username, String name) {
        return find("owner.username = ?1 AND name = ?2", username, name).firstResultOptional();
    }

    public List<Project> findByOwner(User owner) {
        return list("owner", owner);
    }
}
