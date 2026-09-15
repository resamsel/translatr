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

/**
 * Restores the admin access token bootstrap that the pre-Quarkus (Play) app ran in
 * {@code utils.ApplicationStart} on every boot: when {@code translatr.admin-access-token}
 * (env {@code ADMIN_ACCESS_TOKEN}) is set, make sure that key is actually a persisted,
 * authenticate-able {@link AccessToken} row owned by an admin user.
 *
 * <p>The Quarkus migration carried the config property over but never re-implemented the
 * seeding step, so the key configured via {@code ADMIN_ACCESS_TOKEN} (used by
 * {@code docker-compose-loadtest.yml} and {@code k8s/manifest.yaml} to let the load generator
 * authenticate out of the box) was never written to the database — every request using it,
 * such as the load generator's default token, got a 401.
 */
@Singleton
public class AdminAccessTokenSeeder {

    private static final Logger LOG = Logger.getLogger(AdminAccessTokenSeeder.class);
    private static final String ADMIN_USERNAME = "translatr";

    // Mirrors the full scope list in ui/libs/translatr-model/src/lib/model/scope.ts.
    private static final String ALL_SCOPES = String.join(",",
            "read:user", "write:user",
            "read:accesstoken", "write:accesstoken",
            "read:project", "write:project",
            "read:locale", "write:locale",
            "read:key", "write:key",
            "read:message", "write:message",
            "read:member", "write:member",
            "read:notification", "write:notification",
            "read:featureflag", "write:featureflag");

    private final TranslatrConfig       config;
    private final UserRepository        userRepo;
    private final AccessTokenRepository tokenRepo;

    public AdminAccessTokenSeeder(TranslatrConfig config, UserRepository userRepo,
                                   AccessTokenRepository tokenRepo) {
        this.config    = config;
        this.userRepo  = userRepo;
        this.tokenRepo = tokenRepo;
    }

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        config.adminAccessToken()
                .filter(key -> !key.isBlank())
                .ifPresent(this::ensureAdminAccessToken);
    }

    private void ensureAdminAccessToken(String key) {
        User user = userRepo.findByUsername(ADMIN_USERNAME).orElseGet(this::createAdminUser);

        if (!tokenRepo.findByUser(user.id).isEmpty()) {
            // Admin already owns a token (from this seeder or created manually) — never
            // create a second one here. A unique (user_id, name) index would reject a
            // same-named duplicate anyway, and silently rotating an existing admin token
            // just because the configured key changed would be surprising.
            return;
        }

        AccessToken token = new AccessToken();
        token.user  = user;
        token.name  = "Admin Access Token";
        token.key   = key;
        token.scope = ALL_SCOPES;
        tokenRepo.persist(token);

        LOG.infof("Created access token '%s' for admin user '%s' with all scopes",
                token.name, ADMIN_USERNAME);
    }

    private User createAdminUser() {
        User user = new User();
        user.username = ADMIN_USERNAME;
        user.name = "Translatr";
        user.role = UserRole.Admin;
        userRepo.persist(user);

        LOG.infof("Created admin user '%s'", ADMIN_USERNAME);
        return user;
    }
}
