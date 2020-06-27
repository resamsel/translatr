package services.impl;

import play.mvc.Http;
import services.ContextProvider;

public class ContextProviderImpl implements ContextProvider {
  @Override
  public Http.Context get() {
    return Http.Context.current();
  }

  @Override
  public Http.Context getOrNull() {
    Http.Context ctx = Http.Context.current.get();

    if (ctx != null) {
      return ctx;
    }

    return null;
  }
}
