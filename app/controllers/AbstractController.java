package controllers;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;

import commands.Command;
import dto.NotFoundException;
import dto.PermissionException;
import models.Project;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import services.LogEntryService;
import services.NotificationService;
import services.UserService;
import utils.ContextKey;
import utils.Template;

/**
 *
 * @author resamsel
 * @version 16 Sep 2016
 */
public abstract class AbstractController extends Controller {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

  private static final String FLASH_MESSAGE_KEY = "message";
  private static final String FLASH_ERROR_KEY = "error";

  private static final String COMMAND_FORMAT = "command:%s";

  public static final String DEFAULT_SEARCH = null;
  public static final String DEFAULT_ORDER = "name";
  public static final int DEFAULT_LIMIT = 20;
  public static final int DEFAULT_OFFSET = 0;

  public static final String SECTION_HOME = "home";
  public static final String SECTION_PROJECTS = "projects";
  public static final String SECTION_PROFILE = "profile";
  public static final String SECTION_COMMUNITY = "users";

  protected final Injector injector;

  protected final CacheApi cache;

  protected final PlayAuthenticate auth;

  protected final HttpExecutionContext executionContext;

  protected final UserService userService;

  protected final LogEntryService logEntryService;

  protected final NotificationService notificationService;

  /**
   * @param injector
   * @param userService
   * @param auth
   * 
   */
  protected AbstractController(Injector injector, CacheApi cache, PlayAuthenticate auth) {
    this.injector = injector;
    this.cache = cache;
    this.auth = auth;

    this.executionContext = injector.instanceOf(HttpExecutionContext.class);
    this.userService = injector.instanceOf(UserService.class);
    this.logEntryService = injector.instanceOf(LogEntryService.class);
    this.notificationService = injector.instanceOf(NotificationService.class);
  }

  protected <T> CompletionStage<T> tryCatch(Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier, executionContext.current());
  }

  public static String message(String key, Object... args) {
    return ctx().messages().at(key, args);
  }

  public static Result redirectWithError(Call call, String errorKey, Object... args) {
    addError(message(errorKey, args));
    return redirect(call);
  }

  public static Result redirectWithError(String url, String errorKey, Object... args) {
    addError(message(errorKey, args));
    return redirect(url);
  }

  public static Result redirectWithMessage(Call call, String key, Object... args) {
    addMessage(message(key, args));
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

    return template.withNotificationsEnabled(notificationService.isEnabled());
  }

  protected void select(Project project) {
    ContextKey.ProjectId.put(ctx(), project.id);
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

  protected CompletionStage<Result> loggedInUser(Function<User, Result> processor) {
    return tryCatch(() -> User.loggedInUser()).thenApply(processor);
  }

  protected CompletionStage<Result> user(String username, Function<User, Result> processor,
      String... fetches) {
    return user(username, processor, false, fetches);
  }

  protected CompletionStage<Result> user(String username, Function<User, Result> processor,
      boolean restrict, String... fetches) {
    return tryCatch(() -> {
      User user = userService.byUsername(username, fetches);
      if (user == null)
        throw new NotFoundException("User", username);

      if (restrict && !user.id.equals(User.loggedInUserId()))
        throw new PermissionException("user.notFound");

      return processor.apply(user);
    }).exceptionally(e -> {
      if (e.getCause() != null)
        e = e.getCause();
      if (e instanceof NotFoundException)
        return redirectWithError(controllers.routes.Application.index(), "user.notFound");
      if (e instanceof PermissionException)
        return redirectWithError(controllers.routes.Users.user(username), "access.denied");

      LOGGER.error("Error while processing request", e);
      throw new RuntimeException(e);
    });
  }

  /**
   * @param t
   * @return
   */
  protected Result handleException(Throwable t) {
    try {
      if (t.getCause() != null)
        throw t.getCause();

      throw t;
    } catch (PermissionException e) {
      addError("access.denied");
      return forbidden(views.html.errors.restricted.render(createTemplate()));
    } catch (Throwable e) {
      LoggerFactory.getLogger(AbstractApi.class).error("Error while processing request", e);
      return internalServerError(views.html.errors.restricted.render(createTemplate()));
    }
  }
}
