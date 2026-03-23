package com.translatr.repository;

import com.translatr.model.AccessToken;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AccessTokenRepository implements PanacheRepositoryBase<AccessToken, Long> {

    public Optional<AccessToken> findByKey(String key) {
        return find("key", key).firstResultOptional();
    }

    public List<AccessToken> findByUser(UUID userId) {
        return list("user.id", userId);
    }
}
