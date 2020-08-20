package services.api;

import com.google.inject.ImplementedBy;
import models.AuthClient;
import services.api.impl.AuthClientApiServiceImpl;

import java.util.Collection;

@ImplementedBy(AuthClientApiServiceImpl.class)
public interface AuthClientApiService {
  Collection<AuthClient> getClients();
}
