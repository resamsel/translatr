package services.impl;

import play.mvc.Http;
import services.ContextProvider;

public class ContextProviderImpl implements ContextProvider {
  @Override
  public Http.Context get() {
    return Http.Context.current();
  }
}
