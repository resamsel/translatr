package com.translatr.repository;

import com.translatr.model.Message;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MessageRepository implements PanacheRepositoryBase<Message, UUID> {

    public Optional<Message> findByLocaleAndKey(UUID localeId, UUID keyId) {
        return find("locale.id = ?1 AND key.id = ?2", localeId, keyId).firstResultOptional();
    }

    public List<Message> findByProject(UUID projectId) {
        return list("locale.project.id", projectId);
    }
}
