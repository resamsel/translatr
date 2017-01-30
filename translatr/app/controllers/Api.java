package controllers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;

import criterias.AbstractSearchCriteria;
import dto.Dto;
import dto.NotFoundException;
import dto.PermissionException;
import models.Project;
import models.ProjectRole;
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import services.LogEntryService;
import services.UserService;
import services.api.ApiService;
import utils.ErrorUtils;
import utils.PermissionUtils;

public abstract class Api<DTO extends Dto, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>>
    extends AbstractController {
  protected static final String PERMISSION_ERROR = "Invalid access token";
  protected static final String INTERNAL_SERVER_ERROR = "Internal server error";
  protected static final String INPUT_ERROR = "Bad request";

  protected static final String ACCESS_TOKEN = "The access token";
  protected static final String PARAM_ACCESS_TOKEN = "access_token";
  protected static final String PARAM_SEARCH = "search";
  protected static final String OFFSET = "The first row of the paged result list";
  protected static final String PARAM_OFFSET = "offset";
  protected static final String LIMIT = "The page size of the paged result list";
  protected static final String PARAM_LIMIT = "limit";
  protected static final String PROJECT_ID = "The project ID";
  protected static final String LOCALE_ID = "The locale ID";
  protected static final String PARAM_LOCALE_ID = "localeId";
  protected static final String KEY_ID = "The key ID";
  protected static final String MESSAGE_ID = "The message ID";
  protected static final String USER_ID = "The user ID";

  protected static final String AUTHORIZATION = "scopes";

  protected static final String PROJECT_READ = "project:read";
  protected static final String PROJECT_READ_DESCRIPTION = "Read project";
  protected static final String PROJECT_WRITE = "project:write";
  protected static final String PROJECT_WRITE_DESCRIPTION = "Write project";
  protected static final String LOCALE_READ = "locale:read";
  protected static final String LOCALE_READ_DESCRIPTION = "Read locale";
  protected static final String LOCALE_WRITE = "locale:write";
  protected static final String LOCALE_WRITE_DESCRIPTION = "Write locale";
  protected static final String KEY_READ = "key:read";
  protected static final String KEY_READ_DESCRIPTION = "Read key";
  protected static final String KEY_WRITE = "key:write";
  protected static final String KEY_WRITE_DESCRIPTION = "Write key";
  protected static final String MESSAGE_READ = "message:read";
  protected static final String MESSAGE_READ_DESCRIPTION = "Read message";
  protected static final String MESSAGE_WRITE = "message:write";
  protected static final String MESSAGE_WRITE_DESCRIPTION = "Write message";
  protected static final String USER_READ = "user:read";
  protected static final String USER_READ_DESCRIPTION = "Read user";
  protected static final String USER_WRITE = "user:write";
  protected static final String USER_WRITE_DESCRIPTION = "Write user";

  protected final HttpExecutionContext executionContext;

  protected final ApiService<DTO, ID, CRITERIA> api;

  protected Api(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService, ApiService<DTO, ID, CRITERIA> api) {
    super(injector, cache, auth, userService, logEntryService);

    this.executionContext = injector.instanceOf(HttpExecutionContext.class);
    this.api = api;
  }

  /**
   * @param errorMessage
   * @param scopes
   */
  protected void checkPermissionAll(String errorMessage, Scope... scopes) {
    if (!PermissionUtils.hasPermissionAll(scopes))
      throw new PermissionException(errorMessage, scopes);
  }

  protected void checkProjectRole(Project project, User user, ProjectRole... roles) {
    checkProjectRole(project.id, user, roles);
  }

  protected void checkProjectRole(UUID projectId, User user, ProjectRole... roles) {
    if (!PermissionUtils.hasPermissionAny(projectId, user, roles))
      throw new PermissionException("User not allowed in project");
  }

  @Override
  protected Result tryCatch(Supplier<Result> supplier) {
    try {
      return supplier.get();
    } catch (Throwable t) {
      return handleException(t);
    }
  }

  protected static Result handleException(Throwable t) {
    try {
      if (t.getCause() != null)
        throw t.getCause();

      throw t;
    } catch (PermissionException e) {
      return forbidden(ErrorUtils.toJson(e));
    } catch (NotFoundException e) {
      return notFound(ErrorUtils.toJson(e));
    } catch (ConstraintViolationException e) {
      return badRequest(ErrorUtils.toJson(e));
    } catch (ValidationException e) {
      return badRequest(ErrorUtils.toJson(e));
    } catch (Throwable e) {
      LoggerFactory.getLogger(Api.class).error("Error while processing API request", e);
      return internalServerError(ErrorUtils.toJson(e));
    }
  }

  protected <IN, OUT> CompletionStage<Result> toJson(Supplier<IN> supplier) {
    return CompletableFuture.supplyAsync(supplier, executionContext.current())
        .thenApply(out -> ok(Json.toJson(out))).exceptionally(Api::handleException);
  }

  protected <T> CompletionStage<Result> toJsons(Supplier<List<T>> supplier) {
    return CompletableFuture.supplyAsync(supplier, executionContext.current())
        .thenApply(out -> ok(Json.toJson(out))).exceptionally(Api::handleException);
  }
}
