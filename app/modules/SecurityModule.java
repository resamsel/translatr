package modules;

import be.objectify.deadbolt.java.cache.HandlerCache;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.matching.matcher.PathMatcher;
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
import utils.ConfigKey;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static play.mvc.Results.forbidden;
import static play.mvc.Results.unauthorized;
import static utils.ConfigKey.*;

public class SecurityModule extends AbstractModule {

  private static final String[] EXCLUDED_PATHS = new String[]{
          "/api/me",
          "/api/profile",
          "/api/register",
          "/api/user",
          "/api/authclients"
  };
  private final com.typesafe.config.Config configuration;
  private final String baseUrl;

  private static class MyPac4jRoleHandler implements Pac4jRoleHandler {
  }

  public SecurityModule(final Environment environment, final com.typesafe.config.Config configuration) {
    this.configuration = configuration;
    this.baseUrl = BaseUrl.get(configuration);
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

  @Provides
  protected Config provideConfig() {
    List<Client<?>> clientList = Arrays.asList(
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
//    config.addAuthorizer("admin", new RequireAnyRoleAuthorizer<>("ROLE_ADMIN"));
//    config.addAuthorizer("custom", new CustomAuthorizer());
    config.addMatcher("excludedApiPath", new PathMatcher().excludePaths(EXCLUDED_PATHS));
    config.setHttpActionAdapter(PlayHttpActionAdapter.INSTANCE);
    return config;
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
