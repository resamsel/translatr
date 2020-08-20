package controllers;

import dto.PermissionException;
import exceptions.KeyNotFoundException;
import exceptions.LocaleNotFoundException;
import exceptions.ProjectNotFoundException;
import exceptions.UserNotFoundException;
import models.Project;
import models.User;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.Injector;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthProvider;
import services.CacheService;
import services.LogEntryService;
import services.NotificationService;
import services.PermissionService;
import services.ProjectService;
import services.UserService;
import utils.CheckedSupplier;
import utils.ContextKey;
import utils.Template;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
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

  static final String SECTION_HOME = "home";

  protected final Injector injector;

  protected final CacheService cache;

  final HttpExecutionContext executionContext;

  protected final UserService userService;

  protected final ProjectService projectService;

  protected final LogEntryService logEntryService;

  private final NotificationService notificationService;

  protected final PermissionService permissionService;

  private final AuthProvider authProvider;

  protected AbstractController(Injector injector, CacheService cache) {
    this.injector = injector;
    this.cache = cache;

    this.executionContext = injector.instanceOf(HttpExecutionContext.class);
    this.userService = injector.instanceOf(UserService.class);
    this.projectService = injector.instanceOf(ProjectService.class);
    this.logEntryService = injector.instanceOf(LogEntryService.class);
    this.notificationService = injector.instanceOf(NotificationService.class);
    this.permissionService = injector.instanceOf(PermissionService.class);
    this.authProvider = injector.instanceOf(AuthProvider.class);
  }

  public static void noCache(final Http.Response response) {
    // http://stackoverflow.com/questions/49547/making-sure-a-web-page-is-not-cached-across-all-browsers
    response.setHeader(Http.Response.CACHE_CONTROL, "no-cache, no-store, must-revalidate");  // HTTP 1.1
    response.setHeader(Http.Response.PRAGMA, "no-cache");  // HTTP 1.0.
    response.setHeader(Http.Response.EXPIRES, "0");  // Proxies.
  }

  <T> CompletionStage<T> tryCatch(CheckedSupplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier::wrap, executionContext.current());
  }

  public static String message(String key, Object... args) {
    return ctx().messages().at(key, args);
  }

  public static Result redirectWithError(Call call, String errorKey, Object... args) {
    String message = message(errorKey, args);

    LOGGER.debug("Redirecting with error message: {}", message);

    addError(message);
    return redirect(call);
  }

  static void addError(String error) {
    flash(FLASH_ERROR_KEY, error);
  }

  public static void addMessage(String message) {
    flash(FLASH_MESSAGE_KEY, message);
  }

  protected Template createTemplate() {
    User user = authProvider.loggedInUser();

    Template template = Template.create(user);

    if (user == null) {
      return template;
    }

    return template.withNotificationsEnabled(notificationService.isEnabled());
  }

  protected Project select(Project project) {
    ContextKey.ProjectId.put(ctx(), project.id);
    return project;
  }

  protected CompletionStage<Result> loggedInUser(Function<User, Result> processor) {
    return tryCatch(authProvider::loggedInUser).thenApply(processor);
  }

  protected User user(String username, boolean restrict, String... fetches)
          throws UserNotFoundException {
    User user = userService.byUsername(username, fetches);
    if (user == null) {
      throw new UserNotFoundException(message("user.notFound", username), username);
    }

    if (restrict && !user.id.equals(authProvider.loggedInUserId())) {
      throw new PermissionException("user.notFound");
    }

    return user;
  }

  protected Result handleException(Throwable t) {
    try {
      throw ExceptionUtils.getRootCause(t);
    } catch (UserNotFoundException e) {
      return notFound(e.getUsername());
    } catch (ProjectNotFoundException e) {
      return notFound(e.getUsername(), e.getProjectName());
    } catch (LocaleNotFoundException e) {
      return notFound(String.format("%s/%s/%s", e.getUsername(), e.getProjectName(), e.getLocaleName()));
    } catch (KeyNotFoundException e) {
      return notFound(String.format("%s/%s/%s", e.getUsername(), e.getProjectName(), e.getKeyName()));
    } catch (PermissionException e) {
      addError(message("access.denied"));
      return forbidden(views.html.errors.restricted.render());
    } catch (Throwable e) {
      LoggerFactory.getLogger(AbstractApi.class).error("Error while processing request", e);
      return internalServerError(views.html.errors.restricted.render());
    }
  }
}
