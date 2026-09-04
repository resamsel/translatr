package com.translatr.service;

import java.util.List;

/** Diagnostic view of one auth provider. {@code clientSecret} is already masked or null. */
public record OidcProviderStatus(
        String key,
        boolean listed,
        boolean active,
        String provider,
        String authServerUrl,
        String clientId,
        String clientSecret,
        List<String> scopes,
        List<String> errors) {
}
