package services;

import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import controllers.routes;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.mvc.Call;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;

import static play.mvc.Http.Context.Implicit.request;

/**
 * Concrete Resolver implementation.
 */
@Singleton
public class OAuthResolver extends Resolver {
  private static final String REDIRECT_BASE = "translatr.redirectBase";

  private final Configuration configuration;

  @Inject
  public OAuthResolver(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public Call login() {
    // Your login page
    return routes.Application.login();
  }

  @Override
  public Call afterAuth() {
    // The user will be redirected to this page after authentication
    // if no original URL was saved
    play.api.mvc.Call call = routes.Application.indexUi();

    String redirectUri = request().getQueryString(OAuth2AuthProvider.Constants.REDIRECT_URI);
    if (StringUtils.isEmpty(redirectUri)) {
      return call;
    }

    if (redirectUri.startsWith("http://") || redirectUri.startsWith("https://")) {
      return new play.api.mvc.Call(call.method(), redirectUri, null);
    }

    String redirectBase = configuration.getString(REDIRECT_BASE, "");
    return new play.api.mvc.Call(call.method(), redirectBase + call.url() + redirectUri, null);
  }

  @Override
  public Call afterLogout() {
    return routes.Application.index();
  }

  @Override
  public Call auth(final String provider) {
    // You can provide your own authentication implementation,
    // however the default should be sufficient for most cases
    play.api.mvc.Call redirectCall = com.feth.play.module.pa.controllers.routes.Authenticate.authenticate(provider);

    String redirectUri = request().getQueryString(OAuth2AuthProvider.Constants.REDIRECT_URI);
    if (StringUtils.isEmpty(redirectUri)) {
      return redirectCall;
    }

    URI uri;
    try {
      uri = new URI(redirectCall.url());
    } catch (URISyntaxException e) {
      return redirectCall;
    }
    String queryParams = uri.getQuery();
    if (queryParams == null) {
      queryParams = "redirect_uri=" + redirectUri;
    } else {
      queryParams += "&redirect_uri=" + redirectUri;
    }

    try {
      return new play.api.mvc.Call(
          redirectCall.method(),
          new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), queryParams, uri.getFragment()).toString(),
          null
      );
    } catch (URISyntaxException e) {
      return redirectCall;
    }
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
