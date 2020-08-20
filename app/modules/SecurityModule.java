package modules;

import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.models.Permission;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import controllers.CustomAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.matching.matcher.PathMatcher;
import org.pac4j.oauth.client.FacebookClient;
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
import play.libs.concurrent.HttpExecutionContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static play.mvc.Results.forbidden;
import static play.mvc.Results.unauthorized;

public class SecurityModule extends AbstractModule {

  private final com.typesafe.config.Config configuration;
  private final String baseUrl;

  private static class MyPac4jRoleHandler implements Pac4jRoleHandler {
    @Override
    public CompletionStage<List<? extends Permission>> getPermissionsForRole(String clients, String roleName, HttpExecutionContext httpExecutionContext) {
      return CompletableFuture.completedFuture(Collections.emptyList());
    }
  }

  public SecurityModule(final Environment environment, final com.typesafe.config.Config configuration) {
    this.configuration = configuration;
    this.baseUrl = configuration.getString("translatr.baseUrl");
  }

  @Override
  protected void configure() {
    bind(HandlerCache.class).to(Pac4jHandlerCache.class);

    bind(Pac4jRoleHandler.class).to(MyPac4jRoleHandler.class);
    bind(PlaySessionStore.class).to(PlayCacheSessionStore.class);
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

  protected FacebookClient provideFacebookClient() {
    if (!configuration.hasPath("pac4j.clients.facebook.clientId") || !configuration.hasPath("pac4j.clients.facebook.clientSecret")) {
      return null;
    }

    FacebookClient client = new FacebookClient(configuration.getString("pac4j.clients.facebook.clientId"), configuration.getString("pac4j.clients.facebook.clientSecret"));

    client.setName("facebook");

    return client;
  }

  protected TwitterClient provideTwitterClient() {
    if (!configuration.hasPath("pac4j.clients.twitter.consumerKey") || !configuration.hasPath("pac4j.clients.twitter.consumerSecret")) {
      return null;
    }

    TwitterClient client = new TwitterClient(configuration.getString("pac4j.clients.twitter.consumerKey"), "pac4j.clients.twitter.consumerSecret");

    client.setName("twitter");

    return client;
  }

  protected KeycloakOidcClient provideKeycloakClient() {
    if (!configuration.hasPath("pac4j.clients.keycloak.clientId") || !configuration.hasPath("pac4j.clients.keycloak.clientSecret")) {
      return null;
    }

    KeycloakOidcConfiguration config = new KeycloakOidcConfiguration();
    config.setBaseUri(configuration.getString("pac4j.clients.keycloak.baseUri"));
    config.setRealm(configuration.getString("pac4j.clients.keycloak.realm"));
    config.setClientId(configuration.getString("pac4j.clients.keycloak.clientId"));
    config.setSecret(configuration.getString("pac4j.clients.keycloak.clientSecret"));
    config.setWithState(false);

    KeycloakOidcClient client = new KeycloakOidcClient(config);

    client.setName("keycloak");

    return client;
  }

  @Provides
  protected Config provideConfig() {
    List<Client<?>> clientList = Arrays.asList(provideFacebookClient(), provideTwitterClient(), provideKeycloakClient());

    final Clients clients = new Clients(baseUrl + "/authenticate", clientList.stream().filter(Objects::nonNull).collect(Collectors.toList()));

    PlayHttpActionAdapter.INSTANCE.getResults().put(
            HttpConstants.UNAUTHORIZED,
            unauthorized(views.html.errors.unauthorized.render().toString())
                    .as(HttpConstants.APPLICATION_JSON)
    );
    PlayHttpActionAdapter.INSTANCE.getResults().put(
            HttpConstants.FORBIDDEN,
            forbidden(views.html.errors.unauthorized.render().toString())
                    .as((HttpConstants.HTML_CONTENT_TYPE))
    );

    final Config config = new Config(clients);
//    config.addAuthorizer("admin", new RequireAnyRoleAuthorizer<>("ROLE_ADMIN"));
//    config.addAuthorizer("custom", new CustomAuthorizer());
    config.addMatcher("excludedApiPath", new PathMatcher().excludePaths("/api/me", "/api/authclients"));
    config.setHttpActionAdapter(PlayHttpActionAdapter.INSTANCE);
    return config;
  }
}
