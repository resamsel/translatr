package services;

import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import controllers.routes;
import play.mvc.Call;

import javax.inject.Singleton;

/**
 * Concrete Resolver implementation.
 */
@Singleton
public class OAuthResolver extends Resolver {
  @Override
  public Call login() {
    // Your login page
    return routes.Application.login();
  }

  @Override
  public Call afterAuth() {
    // The user will be redirected to this page after authentication
    // if no original URL was saved
    return routes.Application.indexUi();
  }

  @Override
  public Call afterLogout() {
    return routes.Application.index();
  }

  @Override
  public Call auth(final String provider) {
    // You can provide your own authentication implementation,
    // however the default should be sufficient for most cases
    return com.feth.play.module.pa.controllers.routes.Authenticate.authenticate(provider);
  }

  @Override
  public Call onException(final AuthException e) {
    if (e instanceof AccessDeniedException)
      return routes.Application.oAuthDenied(((AccessDeniedException) e).getProviderKey());

    // more custom problem handling here...

    return super.onException(e);
  }

  @Override
  public Call askLink() {
    return routes.Profiles.askLink();
  }

  @Override
  public Call askMerge() {
    return routes.Profiles.askMerge();
  }
}
