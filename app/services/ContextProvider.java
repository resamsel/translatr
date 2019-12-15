package services;

import com.google.inject.ImplementedBy;
import play.mvc.Http;
import services.impl.ContextProviderImpl;

@ImplementedBy(ContextProviderImpl.class)
public interface ContextProvider {
  Http.Context get();

  Http.Context getOrNull();
}
