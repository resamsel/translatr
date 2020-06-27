package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import commands.Command;
import dto.NotFoundException;
import dto.PermissionException;
import exceptions.KeyNotFoundException;
import exceptions.LocaleNotFoundException;
import exceptions.ProjectNotFoundException;
import exceptions.UserNotFoundException;
import models.Project;
import models.Scope;
import models.User;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.Injector;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Call;
import play.mvc.Controller;
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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
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
  static final String SECTION_PROJECTS = "projects";
  static final String SECTION_PROFILE = "profile";
  static final String SECTION_COMMUNITY = "users";

  protected final Injector injector;

  protected final CacheService cache;

  protected final PlayAuthenticate auth;

  final HttpExecutionContext executionContext;

  protected final UserService userService;

  protected final ProjectService projectService;

  protected final LogEntryService logEntryService;

  private final NotificationService notificationService;

  protected final PermissionService permissionService;

  private final AuthProvider authProvider;

  protected AbstractController(Injector injector, CacheService cache, PlayAuthenticate auth) {
    this.injector = injector;
    this.cache = cache;
    this.auth = auth;

    this.executionContext = injector.instanceOf(HttpExecutionContext.class);
    this.userService = injector.instanceOf(UserService.class);
    this.projectService = injector.instanceOf(ProjectService.class);
    this.logEntryService = injector.instanceOf(LogEntryService.class);
    this.notificationService = injector.instanceOf(NotificationService.class);
    this.permissionService = injector.instanceOf(PermissionService.class);
    this.authProvider = injector.instanceOf(AuthProvider.class);
  }

  <T> CompletionStage<T> tryCatch(CheckedSupplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier::wrap, executionContext.current());
  }

  private Result userNotFound(String username) {
    return redirectWithError(Projects.indexRoute(), "user.notFoundBy", username);
  }

  private Result projectNotFound(String username, String projectName) {
    try {
      return redirectWithError(
          user(username, false).route(),
          "project.notFoundBy", username, projectName
      );
    } catch (UserNotFoundException e) {
      return userNotFound(e.getUsername());
    }
  }

  private Result localeNotFound(String username, String projectName, String localeName) {
    try {
      return redirectWithError(
          project(username, projectName, false).route(),
          "locale.notFoundBy", username, projectName, localeName
      );
    } catch (ProjectNotFoundException e) {
      return projectNotFound(username, projectName);
    }
  }

  private Result keyNotFound(String username, String projectName, String keyName) {
    try {
      return redirectWithError(
          project(username, projectName, false).route(),
          "key.notFoundBy", username, projectName, keyName
      );
    } catch (ProjectNotFoundException e) {
      return projectNotFound(username, projectName);
    }
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

  static Result redirectWithError(String url, String errorKey, Object... args) {
    String message = message(errorKey, args);

    LOGGER.debug("Redirecting with error message: {}", message);

    addError(message);
    return redirect(url);
  }

  static Result redirectWithMessage(Call call, String key, Object... args) {
    addMessage(message(key, args));
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

    Template template = Template.create(auth, user);

    if (user == null) {
      return template;
    }

    return template.withNotificationsEnabled(notificationService.isEnabled());
  }

  protected Project select(Project project) {
    ContextKey.ProjectId.put(ctx(), project.id);
    return project;
  }

  Command<?> getCommand(String key) {
    return cache.get(key);
  }

  void undoCommand(Command<?> command) {
    String undoKey = String.format(COMMAND_FORMAT, UUID.randomUUID());

    cache.set(undoKey, command, 120);

    flash("undo", undoKey);
  }

  protected CompletionStage<Result> loggedInUser(Function<User, Result> processor) {
    return tryCatch(authProvider::loggedInUser).thenApply(processor);
  }

  protected CompletionStage<Result> user(String username, Function<User, Result> processor,
                                         String... fetches) {
    return user(username, processor, false, fetches);
  }

  protected CompletionStage<Result> user(String username, Function<User, Result> processor,
                                         boolean restrict, String... fetches) {
    return tryCatch(() -> processor.apply(user(username, restrict, fetches)))
        .exceptionally(e -> {
          e = ExceptionUtils.getRootCause(e);
          if (e instanceof NotFoundException) {
            return redirectWithError(Projects.indexRoute(), "user.notFound");
          }
          if (e instanceof PermissionException) {
            return redirectWithError(controllers.routes.Users.user(username), "access.denied");
          }

          LOGGER.error("Error while processing request", e);
          throw new RuntimeException(e);
        });
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

  protected CompletionStage<Result> project(String username, String projectName,
                                            BiFunction<User, Project, Result> processor, String... fetches) {
    return project(username, projectName, processor, false, fetches);
  }

  protected CompletionStage<Result> project(String username, String projectName,
                                            BiFunction<User, Project, Result> processor, boolean restrict, String... fetches) {
    return tryCatch(() -> {
      Project project = project(username, projectName, restrict, fetches);

      return processor.apply(select(project).owner, project);
    }).exceptionally(t -> {
      if (t instanceof ProjectNotFoundException) {
        return redirectWithError(Projects.indexRoute(), t.getMessage());
      }

      return redirect(Projects.indexRoute());
    });
  }

  protected Project project(String username, String projectName, boolean restrict,
                            String... fetches) throws ProjectNotFoundException {
    Project project = projectService.byOwnerAndName(username, projectName, fetches);
    if (project == null) {
      throw new ProjectNotFoundException(message("project.notFound", username, projectName),
          username, projectName);
    }

    if (restrict && !project.owner.id.equals(authProvider.loggedInUserId())) {
      throw new PermissionException(message("access.denied"), Scope.ProjectRead.name());
    }

    return project;
  }

  protected Result handleException(Throwable t) {
    try {
      throw ExceptionUtils.getRootCause(t);
    } catch (UserNotFoundException e) {
      return userNotFound(e.getUsername());
    } catch (ProjectNotFoundException e) {
      return projectNotFound(e.getUsername(), e.getProjectName());
    } catch (LocaleNotFoundException e) {
      return localeNotFound(e.getUsername(), e.getProjectName(), e.getLocaleName());
    } catch (KeyNotFoundException e) {
      return keyNotFound(e.getUsername(), e.getProjectName(), e.getKeyName());
    } catch (PermissionException e) {
      addError(message("access.denied"));
      return forbidden(views.html.errors.restricted.render(createTemplate()));
    } catch (Throwable e) {
      LoggerFactory.getLogger(AbstractApi.class).error("Error while processing request", e);
      return internalServerError(views.html.errors.restricted.render(createTemplate()));
    }
  }
}
