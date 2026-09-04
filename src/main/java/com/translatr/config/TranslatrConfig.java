package com.translatr.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    /** Comma-separated admin e-mail list (env ADMINS); lower-cased, trimmed, blanks dropped. */
    default Set<String> adminEmails() {
        return admins()
                .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)
                        .filter(v -> !v.isEmpty())
                        .map(v -> v.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toUnmodifiableSet()))
                .orElseGet(Set::of);
    }

    interface AuthConfig {
        @WithDefault("keycloak")
        String providers();

        /** Keyed by provider name: translatr.auth.oidc.<name>.* */
        Map<String, OidcProviderConfig> oidc();
    }

    interface OidcProviderConfig {
        /** Quarkus built-in preset (google|github|facebook|twitter|microsoft|apple|…); empty for Keycloak. */
        Optional<String> provider();
        /** Required when there is no built-in preset. */
        Optional<String> authServerUrl();
        Optional<String> clientId();
        Optional<String> clientSecret();
        @WithDefault("")
        List<String> scopes();
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

