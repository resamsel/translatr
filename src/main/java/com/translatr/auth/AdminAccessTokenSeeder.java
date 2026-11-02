package com.translatr.auth;

import com.translatr.config.TranslatrConfig;
import com.translatr.model.AccessToken;
import com.translatr.model.User;
import com.translatr.model.UserRole;
import com.translatr.repository.AccessTokenRepository;
import com.translatr.repository.UserRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Seeds a well-known admin access token from {@code translatr.admin-access-token}
 * (env {@code ADMIN_ACCESS_TOKEN}) so tooling such as the load generator can
 * authenticate against a fresh database. No-op when the property is unset.
 */
@Singleton
public class AdminAccessTokenSeeder {

    private static final Logger LOG = Logger.getLogger(AdminAccessTokenSeeder.class);

    static final String ADMIN_USERNAME = "admin";
    static final String SCOPE = Arrays.stream(Scope.values())
            .map(Scope::value)
            .collect(Collectors.joining(","));

    private final TranslatrConfig config;
    private final UserRepository users;
    private final AccessTokenRepository tokens;

    public AdminAccessTokenSeeder(TranslatrConfig config, UserRepository users, AccessTokenRepository tokens) {
        this.config = config;
        this.users  = users;
        this.tokens = tokens;
    }

    void onStart(@Observes StartupEvent ev) {
        String key = config.adminAccessToken().map(String::trim).orElse("");
        if (key.isEmpty()) {
            return;
        }
        seed(key);
    }

    @Transactional
    void seed(String key) {
        if (tokens.findByKey(key).isPresent()) {
            return;
        }
        User admin = users.findByUsername(ADMIN_USERNAME).orElseGet(() -> {
            User u = new User();
            u.username = ADMIN_USERNAME;
            u.name     = "Admin";
            u.role     = UserRole.Admin;
            users.persist(u);
            return u;
        });
        AccessToken t = new AccessToken();
        t.user  = admin;
        t.name  = "admin-" + Integer.toHexString(key.hashCode()); // (user_id, name) is unique
        t.key   = key;
        t.scope = SCOPE;
        tokens.persist(t);
        LOG.info("Seeded admin access token from ADMIN_ACCESS_TOKEN");
    }
}
