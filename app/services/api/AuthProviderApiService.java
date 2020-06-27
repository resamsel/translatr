package services.api;

import com.feth.play.module.pa.providers.AuthProvider;
import com.google.inject.ImplementedBy;
import services.api.impl.AuthProviderApiServiceImpl;

import java.util.Collection;

@ImplementedBy(AuthProviderApiServiceImpl.class)
public interface AuthProviderApiService {
  Collection<AuthProvider> getProviders();
}
