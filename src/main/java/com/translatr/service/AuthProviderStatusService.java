package com.translatr.service;

import com.translatr.config.TranslatrConfig;
import com.translatr.config.TranslatrConfig.OidcProviderConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

@ApplicationScoped
public class AuthProviderStatusService {

    // Mirrors io.quarkus.oidc.OidcTenantConfig.Provider (kept as strings to avoid the OIDC runtime here).
    private static final Set<String> KNOWN_PRESETS = Set.of(
            "google", "github", "facebook", "twitter", "apple", "microsoft",
            "spotify", "discord", "twitch", "mastodon", "linkedin", "slack", "strava");

    private final TranslatrConfig config;

    @Inject
    public AuthProviderStatusService(TranslatrConfig config) {
        this.config = config;
    }

    public List<OidcProviderStatus> evaluateAll() {
        List<String> listed = Arrays.stream(config.auth().providers().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        Set<String> listedSet = new LinkedHashSet<>(listed);

        List<String> order = new ArrayList<>(listed);
        new TreeSet<>(config.auth().oidc().keySet()).stream()
                .filter(k -> !listedSet.contains(k))
                .forEach(order::add);

        List<OidcProviderStatus> out = new ArrayList<>();
        for (String key : order) {
            out.add(evaluate(key, listedSet.contains(key), config.auth().oidc().get(key)));
        }
        return out;
    }

    public List<OidcProviderStatus> active() {
        return evaluateAll().stream().filter(OidcProviderStatus::active).toList();
    }

    static String maskSecret(String raw) {
        return raw == null || raw.isBlank() ? null : "***len:" + raw.length() + "***";
    }

    private OidcProviderStatus evaluate(String key, boolean listed, OidcProviderConfig p) {
        List<String> errors = new ArrayList<>();
        String preset = p == null ? null : p.provider().filter(s -> !s.isBlank()).orElse(null);
        String authServerUrl = p == null ? null : p.authServerUrl().filter(s -> !s.isBlank()).orElse(null);
        String clientId = p == null ? null : p.clientId().filter(s -> !s.isBlank()).orElse(null);
        String secret = p == null ? null : p.clientSecret().filter(s -> !s.isBlank()).orElse(null);
        List<String> scopes = p == null ? List.of()
                : p.scopes().orElseGet(List::of).stream().filter(s -> !s.isBlank()).toList();

        if (p == null) {
            errors.add("no configuration block translatr.auth.oidc." + key);
        } else {
            if (clientId == null) errors.add("client-id is missing");
            if (secret == null)   errors.add("client-secret is missing");
            if (preset != null && !KNOWN_PRESETS.contains(preset.toLowerCase(Locale.ROOT))) {
                errors.add("unknown provider preset '" + preset + "'");
            }
            if (preset == null && authServerUrl == null) {
                errors.add("auth-server-url is required for a provider without a built-in preset");
            }
        }

        boolean active = listed && errors.isEmpty();
        return new OidcProviderStatus(key, listed, active, preset, authServerUrl,
                clientId, maskSecret(secret), scopes, List.copyOf(errors));
    }
}
