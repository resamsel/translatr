package auth;

import controllers.routes;
import dto.UserUnregisteredException;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.exception.http.SeeOtherAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import play.inject.Injector;
import services.AuthProvider;

public class CustomCallbackLogic<R, C extends WebContext> implements CallbackLogic<R, C> {
  private final DefaultCallbackLogic<R, C> delegate = DefaultCallbackLogic.INSTANCE;

  private final Injector injector;

  private AuthProvider authProvider;

  public CustomCallbackLogic(Injector injector) {
    this.injector = injector;
  }

  @Override
  public R perform(C context, Config config, HttpActionAdapter<R, C> httpActionAdapter, String defaultUrl, Boolean saveInSession, Boolean multiProfile, Boolean renewSession, String client) {
    init();

    R result = delegate.perform(context, config, httpActionAdapter, defaultUrl, saveInSession, multiProfile, renewSession, client);

    try {
      // INFO: providing null for request in this context is okay
      authProvider.loggedInProfile(context)
              .ifPresent(profile -> authProvider.updateUser(profile, null));
    } catch (UserUnregisteredException e) {
      return httpActionAdapter.adapt(new SeeOtherAction(routes.Application.indexUi().url() + "/register"), context);
    }

    return result;
  }

  private void init() {
    if (authProvider == null) {
      synchronized (this) {
        if (authProvider == null) {
          authProvider = injector.instanceOf(AuthProvider.class);
        }
      }
    }
  }
}
