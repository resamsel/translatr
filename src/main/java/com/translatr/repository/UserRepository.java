package com.translatr.repository;

import com.translatr.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<User> findByLinkedAccount(String providerKey, String providerUserId) {
        return find("SELECT u FROM User u JOIN u.linkedAccounts la " +
                    "WHERE la.providerKey = ?1 AND la.providerUserId = ?2",
                    providerKey, providerUserId).firstResultOptional();
    }
}
