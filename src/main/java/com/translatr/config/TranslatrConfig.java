package com.translatr.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "translatr")
public interface TranslatrConfig {

    String baseUrl();

    String redirectBase();

    /**
     * Absolute origins ({@code scheme://host[:port]}) that {@link com.translatr.controller.LoginResource}
     * accepts as an absolute {@code ?redirect_uri=} after login, on top of the {@link #redirectBase()}
     * origin (always allowed). Relative paths are always allowed; any other absolute URL falls back to
     * {@code /ui}. Needed when the admin SPA is served from a different origin than {@code redirectBase}.
     * Env: {@code ALLOWED_REDIRECT_ORIGINS} (comma-separated).
     */
    Optional<List<String>> allowedRedirectOrigins();

    Optional<String> admins();

    Optional<String> adminAccessToken();

    /**
     * Keycloak realm role (surfaced in the OIDC {@code groups} claim) that grants the
     * Translatr {@code Admin} role. Synced on every OIDC login — losing the role demotes
     * the user back to {@code User}. Env: {@code TRANSLATR_ADMIN_GROUP}.
     */
    @WithDefault("translatr-admin")
    String adminGroup();

    @WithDefault("false")
    boolean forceSSL();

    AuthConfig auth();

    SearchConfig search();

    interface AuthConfig {
        @WithDefault("keycloak")
        String providers();
    }

    interface SearchConfig {
        @WithDefault("false")
        boolean missing();

        @WithDefault("20")
        int limit();

        AutocompleteConfig autocomplete();

        interface AutocompleteConfig {
            @WithDefault("3")
            int limit();
        }
    }
}

