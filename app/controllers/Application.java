package controllers;

import java.util.List;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import commands.Command;
import converters.ActivityCsvConverter;
import criterias.LogEntryCriteria;
import play.Configuration;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import services.LogEntryService;
import services.UserService;
import utils.ConfigKey;

/**
 * This controller contains an action to handle HTTP requests to the application's home page.
 */
@With(ContextAction.class)
public class Application extends AbstractController {
  public static final String USER_ROLE = "user";

  private final Configuration configuration;

  @Inject
  public Application(Injector injector, Configuration configuration, CacheApi cache,
      PlayAuthenticate auth, UserService userService, LogEntryService logEntryService) {
    super(injector, cache, auth, userService, logEntryService);

    this.configuration = configuration;
  }

  public Result index() {
    return ok(views.html.index.render(createTemplate()));
  }

  public Result login() {
    List<String> providers = configuration.getStringList(ConfigKey.AuthProviders.key());

    if (providers.size() == 1)
      return redirect(
          com.feth.play.module.pa.controllers.routes.Authenticate.authenticate(providers.get(0)));

    return ok(views.html.login.render(createTemplate(), providers));
  }

  public Result logout() {
    userService.logout(auth.getUser(session()));
    return injector.instanceOf(com.feth.play.module.pa.controllers.Authenticate.class).logout();
  }

  public Result oAuthDenied(final String providerKey) {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    return redirectWithError(routes.Application.index(), "error.access.denied");
  }

  public Result activityCsv() {
    return ok(
        new ActivityCsvConverter().apply(logEntryService.getAggregates(new LogEntryCriteria())));
  }

  public Result commandExecute(String commandKey) {
    Command<?> command = getCommand(commandKey);

    if (command == null)
      return notFound(Json.toJson("Command not found"));

    command.execute();

    Call call = command.redirect();

    if (call != null)
      return redirect(call);

    String referer = request().getHeader("Referer");

    if (referer == null)
      return redirect(routes.Application.index());

    return redirect(referer);
  }

  public Result javascriptRoutes() {
    return ok(JavaScriptReverseRouter.create("jsRoutes",
        routes.javascript.Application.activityCsv(), routes.javascript.Users.activityCsv(),
        routes.javascript.Profiles.activity(), routes.javascript.Profiles.resetNotifications(),
        routes.javascript.Dashboards.search(), routes.javascript.ProjectsApi.search(),
        routes.javascript.Projects.activity(), routes.javascript.Projects.activityCsv(),
        routes.javascript.Locales.locale(), routes.javascript.Keys.key(),
        routes.javascript.Keys.createImmediately(), routes.javascript.LocalesApi.find(),
        routes.javascript.KeysApi.find(), routes.javascript.TranslationsApi.create(),
        routes.javascript.TranslationsApi.update(), routes.javascript.TranslationsApi.find()));
  }
}
