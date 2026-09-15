package com.translatr.testsupport;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;

/**
 * Deletes any pre-existing "translatr" admin user (and its access tokens) before Quarkus boots.
 *
 * <p>{@code AdminAccessTokenSeeder} only ever seeds the admin's first access token — like the
 * pre-Quarkus {@code ApplicationStart} it restores, it never rotates an existing one. Without
 * this cleanup, {@code AdminAccessTokenSeederTest} would be at the mercy of whatever a previous
 * run (or a manually seeded admin token) already left in the shared dev Postgres instance:
 * with an admin user already owning a token under a different key, the seeder correctly leaves
 * it alone, and the test's own configured key would never become valid.
 */
public class CleanAdminUserResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/translatr", "postgres", "translatr");
             Statement statement = connection.createStatement()) {
            statement.execute(
                    "DELETE FROM access_token WHERE user_id IN (SELECT id FROM user_ WHERE username = 'translatr')");
            statement.execute("DELETE FROM user_ WHERE username = 'translatr'");
        } catch (Exception e) {
            throw new RuntimeException("Failed to clean up pre-existing 'translatr' admin user", e);
        }
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        // Nothing to tear down — the seeded row is left in place so it can be inspected after
        // the test run, and the next run's start() cleans up regardless.
    }
}
