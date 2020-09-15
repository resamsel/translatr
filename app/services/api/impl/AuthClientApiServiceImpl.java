package services.api.impl;

import controllers.routes;
import models.AuthClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import services.api.AuthClientApiService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class AuthClientApiServiceImpl implements AuthClientApiService {

  private final Config authConfig;

  @Inject
  public AuthClientApiServiceImpl(Config authConfig) {
    this.authConfig = authConfig;
  }

  @Override
  public Collection<AuthClient> getClients() {
    return authConfig.getClients()
            .findAllClients()
            .stream()
            .map(client -> AuthClient.of(client.getName(), routes.Auth.login(client.getName()).toString()))
            .collect(Collectors.toList());
  }

  @Override
  public Optional<Client> findClient(String clientName) {
    return authConfig.getClients().findClient(clientName);
  }
}
