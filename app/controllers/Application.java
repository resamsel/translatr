package controllers;

import com.typesafe.config.Config;
import converters.ActivityCsvConverter;
import criterias.LogEntryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthProvider;
import services.LogEntryService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static utils.ConfigKey.RedirectBase;

public class Application extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  private final Config configuration;
  private final Assets assets;
  private final AuthProvider authProvider;
  private final LogEntryService logEntryService;

  @Inject
  public Application(Injector injector, Config configuration, Assets assets,
                     AuthProvider authProvider, LogEntryService logEntryService) {
    super(injector);

    this.configuration = configuration;
    this.assets = assets;
    this.authProvider = authProvider;
    this.logEntryService = logEntryService;
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
}
