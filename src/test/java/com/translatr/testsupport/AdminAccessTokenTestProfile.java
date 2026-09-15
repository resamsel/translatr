package com.translatr.testsupport;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/** Configures a fixed admin access token so AdminAccessTokenSeederTest can exercise the seeding. */
public class AdminAccessTokenTestProfile implements QuarkusTestProfile {
    public static final String TOKEN = "test-admin-access-token-1234567890";

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("translatr.admin-access-token", TOKEN);
    }
}
