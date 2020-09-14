package controllers;

import com.typesafe.config.Config;
import converters.ActivityCsvConverter;
import criterias.LogEntryCriteria;
import org.pac4j.core.client.Client;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.http.PlayHttpActionAdapter;
import org.pac4j.play.store.PlaySessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Environment;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthProvider;
import services.CacheService;
import utils.Template;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static utils.ConfigKey.RedirectBase;

public class Application extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  private final Environment environment;
  private final Config configuration;
  private final org.pac4j.core.config.Config authConfig;
  private final Assets assets;
  private final PlaySessionStore playSessionStore;
  private final AuthProvider authProvider;

  @Inject
  public Application(Environment environment, Injector injector, Config configuration, org.pac4j.core.config.Config authConfig,
                     CacheService cache, Assets assets, PlaySessionStore playSessionStore, AuthProvider authProvider) {
    super(injector, cache);

    this.environment = environment;
    this.configuration = configuration;
    this.authConfig = authConfig;
    this.assets = assets;
    this.playSessionStore = playSessionStore;
    this.authProvider = authProvider;
  }

  public CompletionStage<Result> login(Http.Request request, String authClientName) {
    return async(() -> {
      Optional<Client> client = authConfig.getClients().findClient(authClientName);

      if (client.isPresent()) {
        PlayWebContext webContext = new PlayWebContext(request, playSessionStore);
        Optional<RedirectionAction> action = client.get().getRedirectionAction(webContext);
        if (action.isPresent()) {
          return PlayHttpActionAdapter.INSTANCE.adapt(action.get(), webContext);
        }
      }

      return notFound("Auth client " + authClientName + " not found");
    });
  }

  public CompletionStage<Result> index(Http.Request request) {
    return async(() -> {
      if (authProvider.needsRegistration(request)) {
        return redirect(
                RedirectBase.getOrDefault(configuration, "")
                        + routes.Application.indexUi().url()
                        + "/register"
        );
      }

      String redirectBase = RedirectBase.getOrDefault(configuration, "");
      if (RedirectBase.existsIn(configuration) && !request.uri().startsWith(redirectBase)) {
        return redirect(redirectBase + routes.Application.indexUi().url());
      }

      return redirect(routes.Application.indexUi());
    });
  }

  public Action<AnyContent> indexUi() {
    return assets.at("/public/ui", "index.html", true);
  }

  public Action<AnyContent> assetOrDefaultUi(String resource) {
    if (resource.matches("([^/]+|assets/i18n/[^\\.]+)(\\.[^/]+)+")) {
      return assets.at("/public/ui", resource, true);
    }

    LOGGER.debug("Asset ''{}'' does not match regex, loading /ui", resource);

    return indexUi();
  }

  public Action<AnyContent> indexAdmin() {
    return assets.at("/public/admin", "index.html", true);
  }

  public Action<AnyContent> assetOrDefaultAdmin(String resource) {
    if (resource.matches("([^/]+|assets/i18n/[^\\.]+)(\\.[^/]+)+")) {
      return assets.at("/public/admin", resource, true);
    }

    LOGGER.debug("Asset ''{}'' does not match regex, loading /admin", resource);

    return indexAdmin();
  }

  public CompletionStage<Result> activityCsv() {
    return async(() -> ok(
            new ActivityCsvConverter().apply(logEntryService.getAggregates(new LogEntryCriteria()).getList())));
  }

  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_HOME);
  }
}
