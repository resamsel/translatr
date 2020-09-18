package controllers;

import converters.ActivityCsvConverter;
import criterias.LogEntryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Result;
import services.LogEntryService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class Application extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  private final Assets assets;
  private final LogEntryService logEntryService;

  @Inject
  public Application(Injector injector, Assets assets, LogEntryService logEntryService) {
    super(injector);

    this.assets = assets;
    this.logEntryService = logEntryService;
  }

  public CompletionStage<Result> index() {
    return async(() -> redirect(routes.Application.indexUi()));
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

  public CompletionStage<Result> activityCsv(Http.Request request) {
    return async(() -> ok(
            new ActivityCsvConverter().apply(logEntryService.getAggregates(LogEntryCriteria.from(request)).getList())));
  }
}
