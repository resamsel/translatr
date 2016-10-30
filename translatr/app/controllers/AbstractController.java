package controllers;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.joda.time.DateTime;

import com.feth.play.module.pa.PlayAuthenticate;

import commands.Command;
import criterias.LogEntryCriteria;
import models.Locale;
import models.LogEntry;
import models.Project;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import services.LogEntryService;
import services.UserService;
import utils.SessionKey;
import utils.Template;

/**
 *
 * @author resamsel
 * @version 16 Sep 2016
 */
public abstract class AbstractController extends Controller {
  private static final String FLASH_MESSAGE_KEY = "message";

  private static final String FLASH_ERROR_KEY = "error";

  private static final String COMMAND_FORMAT = "command:%s";

  protected final Injector injector;

  protected final CacheApi cache;

  protected final PlayAuthenticate auth;

  protected final UserService userService;

  protected final LogEntryService logEntryService;

  /**
   * @param injector
   * @param userService
   * @param auth
   * 
   */
  protected AbstractController(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService) {
    this.injector = injector;
    this.cache = cache;
    this.auth = auth;
    this.userService = userService;
    this.logEntryService = logEntryService;
  }

  public static Result redirectWithError(Call call, String errorMessage) {
    addError(errorMessage);
    return redirect(call);
  }

  public static Result redirectWithMessage(Call call, String message) {
    addMessage(message);
    return redirect(call);
  }

  public static void addError(String error) {
    flash(FLASH_ERROR_KEY, error);
  }

  public static void addMessage(String message) {
    flash(FLASH_MESSAGE_KEY, message);
  }

  /**
   * @return
   */
  protected Template createTemplate() {
    User user = User.loggedInUser();

    return Template.create(auth, user).withNotifications(notificationsOf(user));
  }

  private List<LogEntry> notificationsOf(User user) {
    if (user == null)
      return null;

    DateTime whenCreatedMin = null;
    String sessionLastAcknowledged = session(SessionKey.LastAcknowledged.key());
    String sessionLastLogin = session(SessionKey.LastLogin.key());
    if (sessionLastAcknowledged != null)
      whenCreatedMin = DateTime.parse(sessionLastAcknowledged);
    else if (sessionLastLogin != null)
      whenCreatedMin = DateTime.parse(sessionLastLogin);
    else
      whenCreatedMin = user.whenCreated;

    return logEntryService.findBy(new LogEntryCriteria().withUserIdExcluded(user.id)
        .withWhenCreatedMin(whenCreatedMin).withProjectUserId(user.id));
  }

  protected void select(Project project) {
    // session("projectId", project.id.toString());
    ctx().args.put("projectId", project.id);
  }

  protected void select(Locale locale) {
    // session("localeId", locale.id.toString());
    // session("localeName", locale.name);
  }

  protected Command<?> getCommand(String key) {
    return cache.get(key);
  }

  /**
   * @param command
   */
  protected String undoCommand(Command<?> command) {
    String undoKey = String.format(COMMAND_FORMAT, UUID.randomUUID());

    cache.set(undoKey, command, 120);

    flash("undo", undoKey);

    return undoKey;
  }


  protected Result loggedInUser(Function<User, Result> processor) {
    return processor.apply(User.loggedInUser());
  }

  protected Result locale(UUID localeId, Function<Locale, Result> processor) {
    Locale locale = Locale.byId(localeId);
    if (locale == null)
      return redirect(routes.Dashboards.dashboard());

    select(locale.project);

    return processor.apply(locale);
  }
}
