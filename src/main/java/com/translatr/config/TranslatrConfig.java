package com.translatr.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Optional;

@ConfigMapping(prefix = "translatr")
public interface TranslatrConfig {

    String baseUrl();

    String redirectBase();

    Optional<String> admins();

    Optional<String> adminAccessToken();

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

