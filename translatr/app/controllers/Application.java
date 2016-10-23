package controllers;

import java.util.Map.Entry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import commands.Command;
import converters.ActivityCsvConverter;
import criterias.LogEntryCriteria;
import models.Key;
import models.Locale;
import models.Message;
import models.Project;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import scala.collection.JavaConversions;
import services.KeyService;
import services.LocaleService;
import services.LogEntryService;
import services.MessageService;
import services.ProjectService;
import services.UserService;
import utils.ConfigKey;

/**
 * This controller contains an action to handle HTTP requests to the application's home page.
 */
@With(ContextAction.class)
public class Application extends AbstractController {
  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  public static final String USER_ROLE = "user";

  private final Configuration configuration;

  private final ProjectService projectService;

  private final LocaleService localeService;

  private final KeyService keyService;

  private final MessageService messageService;

  private final LogEntryService logEntryService;

  @Inject
  public Application(Injector injector, Configuration configuration, CacheApi cache,
      PlayAuthenticate auth, UserService userService, ProjectService projectService,
      LocaleService localeService, KeyService keyService, MessageService messageService,
      LogEntryService logEntryService) {
    super(injector, cache, auth, userService);

    this.configuration = configuration;
    this.projectService = projectService;
    this.localeService = localeService;
    this.keyService = keyService;
    this.messageService = messageService;
    this.logEntryService = logEntryService;
  }

  public Result index() {
    return ok(views.html.index.render(createTemplate()));
  }

  public Result login() {
    return ok(views.html.login.render(createTemplate(),
        configuration.getStringList(ConfigKey.AuthProviders.key())));
  }

  public Result logout() {
    userService.logout(auth.getUser(session()));
    return injector.instanceOf(com.feth.play.module.pa.controllers.Authenticate.class).logout();
  }

  public Result oAuthDenied(final String providerKey) {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    return redirectWithError(routes.Application.index(),
        "You need to accept the OAuth connection in order to use this website!");
  }

  public Result activityCsv() {
    return ok(
        new ActivityCsvConverter().apply(logEntryService.getAggregates(new LogEntryCriteria())));
  }

  public Result load() {
    String brand = ctx().messages().at("brand");
    User user = User.loggedInUser();
    if (user == null)
      user = User.byUsername("translatr");
    if (user == null)
      return redirectWithError(routes.Application.index(), ctx().messages().at("user.notFound"));

    Project project = Project.byOwnerAndName(user, brand);
    if (project == null)
      project = projectService.save(new Project(brand).withOwner(user));
    else if (project.deleted)
      projectService.save(project.withDeleted(false));

    select(project);

    for (Entry<String, scala.collection.immutable.Map<String, String>> bundle : JavaConversions
        .mapAsJavaMap(ctx().messages().messagesApi().scalaApi().messages()).entrySet()) {
      LOGGER.debug("Key: {}", bundle.getKey());
      if ("default.play".equals(bundle.getKey()))
        break;
      Locale locale = Locale.byProjectAndName(project, bundle.getKey());
      if (locale == null)
        locale = localeService.save(new Locale(project, bundle.getKey()));
      for (Entry<String, String> msg : JavaConversions.mapAsJavaMap(bundle.getValue()).entrySet()) {
        Key key = Key.byProjectAndName(project, msg.getKey());
        if (key == null)
          key = keyService.save(new Key(project, msg.getKey()));
        Message message = Message.byKeyAndLocale(key, locale);
        if (message == null)
          messageService.save(new Message(locale, key, msg.getValue()));
      }
    }

    return redirectWithMessage(routes.Projects.project(project.id),
        ctx().messages().at("project.created", project.name));
  }

  public Result commandExecute(String commandKey) {
    Command<?> command = getCommand(commandKey);

    if (command == null)
      notFound(Json.toJson("Command not found"));

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
        routes.javascript.Dashboards.search(), routes.javascript.Projects.search(),
        routes.javascript.Projects.activityCsv(), routes.javascript.Locales.locale(),
        routes.javascript.Keys.key(), routes.javascript.Keys.createImmediately(),
        routes.javascript.Keys.remove(), routes.javascript.Api.getMessage(),
        routes.javascript.Translations.create(), routes.javascript.Api.findMessages()));
  }
}
