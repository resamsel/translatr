package controllers;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.joda.time.DateTime;

import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;

import commands.Command;
import criterias.LogEntryCriteria;
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

  protected Result tryCatch(Supplier<Result> supplier) {
    return supplier.get();
  }

  public static Result redirectWithError(Call call, String errorKey, Object... args) {
    addError(ctx().messages().at(errorKey, args));
    return redirect(call);
  }

  public static Result redirectWithMessage(Call call, String key, Object... args) {
    addMessage(ctx().messages().at(key, args));
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

    Template template = Template.create(auth, user);

    if (user == null)
      return template;

    return template.withNotifications(notificationsOf(user));
  }

  private PagedList<LogEntry> notificationsOf(User user) {
    if (user == null)
      return null;

    DateTime whenCreatedMin;
    String sessionLastAcknowledged = session(SessionKey.LastAcknowledged.key());
    String sessionLastLogin = session(SessionKey.LastLogin.key());
    if (sessionLastAcknowledged != null)
      whenCreatedMin = DateTime.parse(sessionLastAcknowledged);
    else if (sessionLastLogin != null)
      whenCreatedMin = DateTime.parse(sessionLastLogin);
    else
      whenCreatedMin = user.whenCreated;

    PagedList<LogEntry> paged = logEntryService.findBy(new LogEntryCriteria().withLimit(10)
        .withUserIdExcluded(user.id).withProjectUserId(user.id));

    paged.getList().stream().forEach(l -> l.withUnread(l.whenCreated.isAfter(whenCreatedMin)));

    return paged;
  }

  protected void select(Project project) {
    ctx().args.put("projectId", project.id);
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
    return tryCatch(() -> processor.apply(User.loggedInUser()));
  }
}
