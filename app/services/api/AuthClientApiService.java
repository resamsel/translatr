package services.api;

import com.google.inject.ImplementedBy;
import models.AuthClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.credentials.Credentials;
import services.api.impl.AuthClientApiServiceImpl;

import java.util.Collection;
import java.util.Optional;

@ImplementedBy(AuthClientApiServiceImpl.class)
public interface AuthClientApiService {
  Collection<AuthClient> getClients();

  Optional<Client> findClient(String clientName);
}
