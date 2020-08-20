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
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import services.CacheService;
import utils.Template;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class Application extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  private final Config configuration;
  private final org.pac4j.core.config.Config authConfig;
  private final Assets assets;
  private final PlaySessionStore playSessionStore;

  @Inject
  public Application(Injector injector, Config configuration, org.pac4j.core.config.Config authConfig,
                     CacheService cache, Assets assets, PlaySessionStore playSessionStore) {
    super(injector, cache);

    this.configuration = configuration;
    this.authConfig = authConfig;
    this.assets = assets;
    this.playSessionStore = playSessionStore;
  }

  public CompletionStage<Result> login(Http.Request request, String authClientName) {
    return tryCatch(() -> {
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

  public CompletionStage<Result> index() {
    return tryCatch(routes.Application::indexUi).thenApply(Results::redirect);
  }

  public Action<AnyContent> indexUi() {
    return assets.at("/public/ui", "index.html", false);
  }

  public Action<AnyContent> assetOrDefaultUi(String resource) {
    if (resource.matches("([^/]+|assets/i18n/[^\\.]+)(\\.[^/]+)+")) {
      return assets.at("/public/ui", resource, false);
    }

    LOGGER.debug("Asset ''{}'' does not match regex, loading /ui", resource);

    return indexUi();
  }

  public Action<AnyContent> indexAdmin() {
    return assets.at("/public/admin", "index.html", false);
  }

  public Action<AnyContent> assetOrDefaultAdmin(String resource) {
    if (resource.matches("([^/]+|assets/i18n/[^\\.]+)(\\.[^/]+)+")) {
      return assets.at("/public/admin", resource, false);
    }

    LOGGER.debug("Asset ''{}'' does not match regex, loading /admin", resource);

    return indexAdmin();
  }

  public CompletionStage<Result> activityCsv() {
    return tryCatch(() -> ok(
            new ActivityCsvConverter().apply(logEntryService.getAggregates(new LogEntryCriteria()).getList())));
  }

  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_HOME);
  }
}
