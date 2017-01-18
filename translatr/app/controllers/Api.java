package controllers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;

import criterias.AbstractSearchCriteria;
import dto.Dto;
import dto.NotFoundException;
import dto.PermissionException;
import models.Model;
import models.Project;
import models.ProjectRole;
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.BodyParser;
import play.mvc.Http.Request;
import play.mvc.Result;
import services.LogEntryService;
import services.ModelService;
import services.UserService;
import utils.ErrorUtils;
import utils.PermissionUtils;

public abstract class Api<MODEL extends Model<MODEL, ID>, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>, DTO extends Dto>
    extends AbstractController {
  protected final HttpExecutionContext executionContext;

  protected final ModelService<MODEL> service;

  protected final Class<DTO> dtoClass;

  protected final Function<MODEL, DTO> dtoMapper;

  protected final Function<JsonNode, MODEL> modelMapper;

  protected final Scope[] readScopes;

  protected final Scope[] writeScopes;

  protected final Function<ID, MODEL> getter;

  protected final Function<CRITERIA, List<MODEL>> finder;

  protected Api(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService, ModelService<MODEL> service, Function<ID, MODEL> getter,
      Function<CRITERIA, List<MODEL>> finder, Class<DTO> dtoClass, Function<MODEL, DTO> dtoMapper,
      Function<JsonNode, MODEL> modelMapper, Scope[] readScopes, Scope[] writeScopes) {
    super(injector, cache, auth, userService, logEntryService);

    this.executionContext = injector.instanceOf(HttpExecutionContext.class);
    this.service = service;
    this.getter = getter;
    this.finder = finder;
    this.dtoClass = dtoClass;
    this.dtoMapper = dtoMapper;
    this.modelMapper = modelMapper;
    this.readScopes = readScopes;
    this.writeScopes = writeScopes;
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
      return forbidden(e.toJson());
    } catch (NotFoundException e) {
      return notFound(e.toJson());
    } catch (ConstraintViolationException e) {
      return badRequest(ErrorUtils.toJson(e));
    } catch (ValidationException e) {
      return badRequest(ErrorUtils.toJson(e));
    } catch (Throwable e) {
      LoggerFactory.getLogger(Api.class).error("Error while processing API request", e);
      return badRequest(ErrorUtils.toJson(e));
    }
  }

  protected <IN, OUT> CompletionStage<Result> toJson(Function<IN, OUT> mapper,
      Supplier<IN> supplier) {
    return CompletableFuture.supplyAsync(supplier, executionContext.current())
        .thenApply(out -> ok(Json.toJson(mapper.apply(out)))).exceptionally(Api::handleException);
  }

  protected <IN, OUT> CompletionStage<Result> toJsons(Function<IN, OUT> mapper,
      Supplier<List<IN>> supplier) {
    return CompletableFuture.supplyAsync(supplier, executionContext.current())
        .thenApply(out -> ok(Json.toJson(out.stream().map(mapper).collect(Collectors.toList()))))
        .exceptionally(Api::handleException);
  }

  public static interface Validator {
    void validate() throws ValidationException;
  }

  @SafeVarargs
  protected final CompletionStage<Result> findBy(CRITERIA criteria,
      Consumer<CRITERIA>... validators) {
    return toJsons(dtoMapper, () -> {
      for (Consumer<CRITERIA> validator : validators)
        validator.accept(criteria);

      checkPermissionAll("Access token not allowed", readScopes);

      return finder.apply(criteria);
    });
  }

  public CompletionStage<Result> get(ID id) {
    return toJson(dtoMapper, () -> {
      checkPermissionAll("Access token not allowed", readScopes);

      MODEL obj = getter.apply(id);

      if (obj == null)
        throw new NotFoundException(String.format("%s with ID '%s' not found",
            dtoClass.getSimpleName(), String.valueOf(id)));

      return obj;
    });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public CompletionStage<Result> create() {
    return toJson(dtoMapper, creator(request()));
  }

  @BodyParser.Of(BodyParser.Json.class)
  public CompletionStage<Result> update() {
    return toJson(dtoMapper, updater(request()));
  }

  protected void checkDelete(MODEL m) {}

  public CompletionStage<Result> delete(ID id) {
    return CompletableFuture.supplyAsync(() -> {
      checkPermissionAll("Access token not allowed", writeScopes);

      MODEL m = getter.apply(id);

      if (m == null)
        throw new NotFoundException(String.format("%s with ID '%s' not found",
            dtoClass.getSimpleName(), String.valueOf(id)));

      service.delete(m);

      return true;
    }, executionContext.current())
        .thenApply(success -> ok(Json.newObject().put("message",
            String.format("%s with ID '%s' has been deleted", dtoClass.getSimpleName(), id))))
        .exceptionally(Api::handleException);
  }

  /**
   * @param request
   * @return
   */
  protected Supplier<MODEL> creator(Request request) {
    return () -> {
      checkPermissionAll("Access token not allowed", writeScopes);

      return service.create(modelMapper.apply(request.body().asJson()));
    };
  }

  /**
   * @param request
   * @return
   */
  protected Supplier<MODEL> updater(Request request) {
    return () -> {
      checkPermissionAll("Access token not allowed", writeScopes);

      return service.update(modelMapper.apply(request.body().asJson()));
    };
  }
}
