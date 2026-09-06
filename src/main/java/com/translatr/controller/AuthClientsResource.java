package com.translatr.controller;

import com.translatr.dto.AuthClientDto;
import com.translatr.generated.api.AuthclientsApi;
import com.translatr.service.AuthProviderStatusService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;

import java.util.List;

public class AuthClientsResource implements AuthclientsApi {

    private final AuthProviderStatusService statusService;

    @Inject
    public AuthClientsResource(AuthProviderStatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    @PermitAll
    public List<AuthClientDto> listAuthClients() {
        return statusService.active().stream()
                .map(s -> new AuthClientDto()
                        .key(s.key())
                        .url("/login/" + s.key()))
                .toList();
    }
}
