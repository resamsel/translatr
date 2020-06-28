package services;

import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import controllers.routes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.mvc.Call;
import utils.ConfigKey;

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
  private static final Logger LOGGER = LoggerFactory.getLogger(OAuthResolver.class);

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
    String redirectBase = configuration.getString(REDIRECT_BASE, "");

    String redirectUri = request().getQueryString(OAuth2AuthProvider.Constants.REDIRECT_URI);
    if (StringUtils.isEmpty(redirectUri)) {
      return new play.api.mvc.Call(call.method(), adaptRedirectUri(redirectBase + call.url(), configuration), null);
    }

    if (redirectUri.startsWith("http://") || redirectUri.startsWith("https://")) {
      return new play.api.mvc.Call(call.method(), adaptRedirectUri(redirectUri, configuration), null);
    }

    return new play.api.mvc.Call(call.method(),
            adaptRedirectUri(redirectBase + call.url() + redirectUri, configuration), null);
  }

  @Override
  public Call afterLogout() {
    return routes.Application.index();
  }

  @Override
  public Call auth(final String provider) {
    // You can provide your own authentication implementation,
    // however the default should be sufficient for most cases
    play.api.mvc.Call redirectCall = adaptRedirectCall(
            com.feth.play.module.pa.controllers.routes.Authenticate.authenticate(provider), configuration);

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
      queryParams = "redirect_uri=" + adaptRedirectUri(redirectUri, configuration);
    } else {
      queryParams += "&redirect_uri=" + adaptRedirectUri(redirectUri, configuration);
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

  static String adaptRedirectUri(String redirectUri, Configuration configuration) {
    LOGGER.debug("Adapting redirectUri ''{}'' (forceSSL={})", redirectUri, ConfigKey.ForceSSL.getBoolean(configuration));
    if (ConfigKey.ForceSSL.getBoolean(configuration) && redirectUri.startsWith("http://")) {
      return redirectUri.replaceFirst("http://", "https://");
    }
    return redirectUri;
  }

  static play.api.mvc.Call adaptRedirectCall(play.api.mvc.Call redirectCall, Configuration configuration) {
    return new play.api.mvc.Call(redirectCall.method(), adaptRedirectUri(redirectCall.url(), configuration),
            redirectCall.fragment());
  }
}
