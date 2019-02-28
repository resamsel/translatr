package controllers;

import actions.ContextAction;
import com.feth.play.module.pa.PlayAuthenticate;
import commands.Command;
import converters.ActivityCsvConverter;
import criterias.LogEntryCriteria;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import services.CacheService;
import utils.ConfigKey;
import utils.Template;

/**
 * This controller contains an action to handle HTTP requests to the application's home page.
 */
@With(ContextAction.class)
public class Application extends AbstractController {

  public static final String USER_ROLE = "user";

  private final Configuration configuration;
  private final Assets assets;

  @Inject
  public Application(Injector injector, Configuration configuration, CacheService cache,
      PlayAuthenticate auth, Assets assets) {
    super(injector, cache, auth);

    this.configuration = configuration;
    this.assets = assets;
  }

  public CompletionStage<Result> index() {
    return tryCatch(() -> ok(views.html.index.render(createTemplate())));
  }

  public Action<AnyContent> indexUi() {
    return assets.at("/public", "index.html", false);
  }

  public Action<AnyContent> assetOrDefault(String resource) {
    if (resource.contains(".")) {
      return assets.at("/public", resource, false);
    }
    return indexUi();
  }

  public CompletionStage<Result> login() {
    return tryCatch(() -> {
      List<String> providers = Arrays
          .asList(StringUtils.split(configuration.getString(ConfigKey.AuthProviders.key()), ","));

      if (providers.size() == 1) {
        return redirect(
            com.feth.play.module.pa.controllers.routes.Authenticate.authenticate(providers.get(0)));
      }

      return ok(views.html.login.render(createTemplate(), providers));
    });
  }

  public CompletionStage<Result> logout() {
    return tryCatch(() -> {
      userService.logout(auth.getUser(session()));
      return injector.instanceOf(com.feth.play.module.pa.controllers.Authenticate.class).logout();
    });
  }

  public CompletionStage<Result> oAuthDenied(final String providerKey) {
    return tryCatch(() -> {
      com.feth.play.module.pa.controllers.Authenticate.noCache(response());

      return redirectWithError(routes.Application.index(), "error.access.denied");
    });
  }

  public CompletionStage<Result> activityCsv() {
    return tryCatch(() -> ok(
        new ActivityCsvConverter().apply(logEntryService.getAggregates(new LogEntryCriteria()))));
  }

  public CompletionStage<Result> commandExecute(String commandKey) {
    return tryCatch(() -> {
      Command<?> command = getCommand(commandKey);

      String referer = request().getHeader(Http.HeaderNames.REFERER);

      if (command == null) {
        if (referer == null) {
          return redirectWithError(Projects.indexRoute(), "command.notFound");
        }

        return redirectWithError(referer, "command.notFound");
      }

      command.execute(injector);

      Call call = command.redirect();

      if (call != null) {
        return redirect(call);
      }

      if (referer == null) {
        return redirect(Projects.indexRoute());
      }

      return redirect(referer);
    });
  }

  public CompletionStage<Result> javascriptRoutes() {
    return tryCatch(() -> ok(
        JavaScriptReverseRouter.create("jsRoutes", routes.javascript.Application.activityCsv(),
            routes.javascript.Users.activityCsv(), routes.javascript.Users.activity(),
            routes.javascript.Profiles.resetNotifications(), routes.javascript.Projects.search(),
            routes.javascript.ProjectsApi.search(), routes.javascript.Locales.localeBy(),
            routes.javascript.Keys.keyBy(), routes.javascript.Keys.createImmediatelyBy(),
            routes.javascript.LocalesApi.find(), routes.javascript.KeysApi.find(),
            routes.javascript.TranslationsApi.create(), routes.javascript.TranslationsApi.update(),
            routes.javascript.TranslationsApi.find(), routes.javascript.NotificationsApi.find())));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_HOME);
  }
}
