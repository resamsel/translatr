package controllers;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import commands.Command;
import converters.ActivityCsvConverter;
import criterias.LogEntryCriteria;
import play.Configuration;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import utils.ConfigKey;
import utils.Template;

/**
 * This controller contains an action to handle HTTP requests to the application's home page.
 */
@With(ContextAction.class)
public class Application extends AbstractController {
  public static final String USER_ROLE = "user";

  private final Configuration configuration;

  @Inject
  public Application(Injector injector, Configuration configuration, CacheApi cache,
      PlayAuthenticate auth) {
    super(injector, cache, auth);

    this.configuration = configuration;
  }

  public Result index() {
    return ok(views.html.index.render(createTemplate()));
  }

  public Result login() {
    List<String> providers = Arrays
        .asList(StringUtils.split(configuration.getString(ConfigKey.AuthProviders.key()), ","));

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

    String referer = request().getHeader(Http.HeaderNames.REFERER);

    if (command == null) {
      if (referer == null)
        return redirectWithError(routes.Projects.index(), "command.notFound");

      return redirectWithError(referer, "command.notFound");
    }

    command.execute(injector);

    Call call = command.redirect();

    if (call != null)
      return redirect(call);

    if (referer == null)
      return redirect(routes.Projects.index());

    return redirect(referer);
  }

  public Result javascriptRoutes() {
    return ok(
        JavaScriptReverseRouter.create("jsRoutes", routes.javascript.Application.activityCsv(),
            routes.javascript.Users.activityCsv(), routes.javascript.Users.activity(),
            routes.javascript.Profiles.resetNotifications(), routes.javascript.Projects.search(),
            routes.javascript.ProjectsApi.search(), routes.javascript.Projects.activity(),
            routes.javascript.Projects.activityCsv(), routes.javascript.Locales.localeBy(),
            routes.javascript.Keys.keyBy(), routes.javascript.Keys.createImmediately(),
            routes.javascript.LocalesApi.find(), routes.javascript.KeysApi.find(),
            routes.javascript.TranslationsApi.create(), routes.javascript.TranslationsApi.update(),
            routes.javascript.TranslationsApi.find(), routes.javascript.NotificationsApi.find()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_HOME);
  }
}
