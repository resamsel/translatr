package modules;

import auth.AccessTokenAuthenticator;
import auth.CustomAuthorizer;
import auth.CustomCallbackLogic;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.credentials.authenticator.LocalCachingAuthenticator;
import org.pac4j.core.matching.matcher.PathMatcher;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oidc.client.KeycloakOidcClient;
import org.pac4j.oidc.config.KeycloakOidcConfiguration;
import org.pac4j.play.CallbackController;
import org.pac4j.play.LogoutController;
import org.pac4j.play.deadbolt2.Pac4jHandlerCache;
import org.pac4j.play.deadbolt2.Pac4jRoleHandler;
import org.pac4j.play.http.PlayHttpActionAdapter;
import org.pac4j.play.store.PlayCacheSessionStore;
import org.pac4j.play.store.PlaySessionStore;
import play.Environment;
import play.inject.Injector;
import utils.ConfigKey;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static play.mvc.Results.forbidden;
import static play.mvc.Results.unauthorized;
import static utils.ConfigKey.*;

public class SecurityModule extends AbstractModule {

  private final com.typesafe.config.Config configuration;
  private final String baseUrl;
  private final List<String> excludePaths;

  private static class MyPac4jRoleHandler implements Pac4jRoleHandler {
  }

  public SecurityModule(final Environment environment, final com.typesafe.config.Config configuration) {
    this.configuration = configuration;
    this.baseUrl = BaseUrl.get(configuration);
    this.excludePaths = Pac4jSecurityExcludePaths.getStringList(configuration);
  }

  @Override
  protected void configure() {
    bind(HandlerCache.class).to(Pac4jHandlerCache.class);

    bind(Pac4jRoleHandler.class).to(MyPac4jRoleHandler.class);
    bind(PlaySessionStore.class).to(PlayCacheSessionStore.class);
    // com.nimbusds.oauth2.sdk.pkce.CodeVerifier cannot be cast to java.io.Serializable
//    bind(PlaySessionStore.class).to(PlayCookieSessionStore.class);

    // callback
    final CallbackController callbackController = new CallbackController();
    callbackController.setDefaultUrl("/");
    callbackController.setMultiProfile(true);
    bind(CallbackController.class).toInstance(callbackController);

    // logout
    final LogoutController logoutController = new LogoutController();
    logoutController.setDefaultUrl("/?defaulturlafterlogout");
    //logoutController.setDestroySession(true);
    bind(LogoutController.class).toInstance(logoutController);
  }

  @Provides
  protected Config provideConfig(Injector injector) {
    List<Client<?>> clientList = Arrays.asList(
            provideAnonymousClient(),
            provideParameterClient(injector),
            provideHeaderClient(injector),
            provideGoogleClient(),
            provideKeycloakClient(),
            provideGitHubClient(),
            provideFacebookClient(),
            provideTwitterClient()
    );

    final Clients clients = new Clients(
            baseUrl + "/authenticate", clientList.stream().filter(Objects::nonNull).collect(Collectors.toList()));

    PlayHttpActionAdapter.INSTANCE.getResults().put(
            HttpConstants.UNAUTHORIZED,
            unauthorized(views.html.errors.unauthorized.render().toString())
                    .as(HttpConstants.APPLICATION_JSON)
    );
    PlayHttpActionAdapter.INSTANCE.getResults().put(
            HttpConstants.FORBIDDEN,
            forbidden(views.html.errors.unauthorized.render().toString())
                    .as((HttpConstants.APPLICATION_JSON))
    );

    final Config config = new Config(clients);

    config.addAuthorizer("custom", new CustomAuthorizer());
    config.addMatcher("excludePaths", new PathMatcher().excludePaths(excludePaths.toArray(new String[0])));
    config.setHttpActionAdapter(PlayHttpActionAdapter.INSTANCE);
    config.setCallbackLogic(new CustomCallbackLogic<>(injector));

    return config;
  }

  protected AnonymousClient provideAnonymousClient() {
    AnonymousClient client = new AnonymousClient();

    client.setName("anonymous");

    return client;
  }

  protected ParameterClient provideParameterClient(Injector injector) {
    ParameterClient client = new ParameterClient("access_token",
            new LocalCachingAuthenticator<>(new AccessTokenAuthenticator(injector), 10000, 15, TimeUnit.MINUTES));

    client.setName("parameter");
    client.setSupportGetRequest(true);

    return client;
  }

  protected HeaderClient provideHeaderClient(Injector injector) {
    HeaderClient client = new HeaderClient("x-access-token",
            new LocalCachingAuthenticator<>(new AccessTokenAuthenticator(injector), 10000, 15, TimeUnit.MINUTES));

    client.setName("header");

    return client;
  }

  protected Google2Client provideGoogleClient() {
    return createClient("google", GoogleClientId, GoogleClientSecret, Google2Client::new);
  }

  protected KeycloakOidcClient provideKeycloakClient() {
    return createClient("keycloak", KeycloakClientId, KeycloakClientSecret, (id, secret) -> {
      KeycloakOidcConfiguration config = new KeycloakOidcConfiguration();
      config.setClientId(id);
      config.setSecret(secret);
      config.setBaseUri(KeycloakBaseUri.get(configuration));
      config.setRealm(KeycloakRealm.get(configuration));
      config.setWithState(false);

      return new KeycloakOidcClient(config);
    });
  }

  protected GitHubClient provideGitHubClient() {
    return createClient("github", GitHubClientId, GitHubClientSecret, GitHubClient::new);
  }

  protected FacebookClient provideFacebookClient() {
    return createClient("facebook", FacebookClientId, FacebookClientSecret, FacebookClient::new);
  }

  protected TwitterClient provideTwitterClient() {
    return createClient("twitter", TwitterClientId, TwitterClientSecret, TwitterClient::new);
  }

  private <T extends BaseClient<?>> T createClient(String name, ConfigKey id, ConfigKey secret, BiFunction<String, String, T> creator) {
    if (!id.existsIn(configuration) || !secret.existsIn(configuration)) {
      return null;
    }

    T client = creator.apply(id.get(configuration), secret.get(configuration));

    client.setName(name);

    return client;
  }
}
