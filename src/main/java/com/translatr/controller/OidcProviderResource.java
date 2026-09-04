package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.dto.OidcProviderStatus;
import com.translatr.generated.api.OidcProvidersApi;
import com.translatr.service.AuthProviderStatusService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.ForbiddenException;
import jakarta.inject.Inject;

import java.util.List;

/** Admin-only view of auth-provider configuration and per-provider errors. */
@Authenticated
public class OidcProviderResource implements OidcProvidersApi {

    private final AuthProviderStatusService statusService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public OidcProviderResource(AuthProviderStatusService statusService,
                                CurrentUserResolver currentUserResolver) {
        this.statusService = statusService;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    public List<OidcProviderStatus> listOidcProviders() {
        requireAdmin();
        return statusService.evaluateAll().stream().map(OidcProviderResource::toDto).toList();
    }

    private void requireAdmin() {
        if (!currentUserResolver.resolve().isAdmin()) {
            throw new ForbiddenException("Admin role required");
        }
    }

    private static OidcProviderStatus toDto(com.translatr.service.OidcProviderStatus s) {
        return new OidcProviderStatus()
                .key(s.key())
                .listed(s.listed())
                .active(s.active())
                .provider(s.provider())
                .authServerUrl(s.authServerUrl())
                .clientId(s.clientId())
                .clientSecret(s.clientSecret())
                .scopes(s.scopes())
                .errors(s.errors());
    }
}
