package services.api.impl;

import com.feth.play.module.pa.providers.AuthProvider;
import services.api.AuthProviderApiService;

import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class AuthProviderApiServiceImpl implements AuthProviderApiService {
  @Override
  public Collection<AuthProvider> getProviders() {
    return AuthProvider.Registry.getProviders();
  }
}
