package controllers;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import play.mvc.BodyParser;
import play.mvc.Http.Request;
import play.mvc.Result;
import services.LogEntryService;
import services.ModelService;
import services.UserService;
import utils.ErrorUtils;
import utils.PermissionUtils;

public abstract class Api<MODEL extends Model<MODEL, ID>, DTO extends Dto, ID>
    extends AbstractController {
  protected final ModelService<MODEL> service;

  protected final Class<DTO> dtoClass;

  protected final Function<MODEL, DTO> dtoMapper;

  protected final Function<JsonNode, MODEL> modelMapper;

  protected final Scope[] readScopes;

  protected final Scope[] writeScopes;

  protected final Function<ID, MODEL> getter;

  protected Api(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService, ModelService<MODEL> service, Function<ID, MODEL> getter,
      Class<DTO> dtoClass, Function<MODEL, DTO> dtoMapper, Function<JsonNode, MODEL> modelMapper,
      Scope[] readScopes, Scope[] writeScopes) {
    super(injector, cache, auth, userService, logEntryService);

    this.service = service;
    this.getter = getter;
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
    if (!project.hasPermissionAny(user, roles))
      throw new PermissionException("User not allowed in project");
  }

  @Override
  protected Result tryCatch(Supplier<Result> supplier) {
    try {
      return supplier.get();
    } catch (PermissionException e) {
      return forbidden(e.toJson());
    } catch (NotFoundException e) {
      return notFound(e.toJson());
    } catch (ValidationException e) {
      return badRequest(ErrorUtils.toJson(e));
    } catch (Exception e) {
      LoggerFactory.getLogger(getClass()).error("Error while processing API request", e);
      return badRequest(ErrorUtils.toJson(e));
    }
  }

  protected <IN, OUT> Result toJson(Function<IN, OUT> mapper, Supplier<IN> supplier) {
    return tryCatch(() -> ok(Json.toJson(mapper.apply(supplier.get()))));
  }

  protected <IN, OUT> Result toJsons(Function<IN, OUT> mapper, Supplier<List<IN>> supplier) {
    return tryCatch(
        () -> ok(Json.toJson(supplier.get().stream().map(mapper).collect(Collectors.toList()))));
  }

  protected <T, CRITERIA extends AbstractSearchCriteria<CRITERIA>> Supplier<List<T>> finder(
      Function<CRITERIA, List<T>> finder, CRITERIA criteria) {
    checkPermissionAll("Access token not allowed", readScopes);

    return () -> finder.apply(criteria);
  }

  protected <T> T project(UUID projectId, Function<Project, T> processort) {
    Project project = Project.byId(projectId);
    if (project == null || project.deleted)
      throw new NotFoundException(String.format("Project not found: '%s'", projectId));

    return processort.apply(project);
  }

  protected Result projectCatch(UUID projectId, Function<Project, Result> processor) {
    return tryCatch(() -> project(projectId, processor));
  }

  public Result get(ID id) {
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
  public Result create() {
    return toJson(dtoMapper, creator(request()));
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result update() {
    return toJson(dtoMapper, updater(request()));
  }

  protected void checkDelete(MODEL m) {}

  public Result delete(ID id) {
    return tryCatch(() -> {
      checkPermissionAll("Access token not allowed", writeScopes);

      MODEL m = getter.apply(id);

      if (m == null)
        throw new NotFoundException(String.format("%s with ID '%s' not found",
            dtoClass.getSimpleName(), String.valueOf(id)));

      service.delete(m);

      return ok(Json.newObject().put("message",
          String.format("%s with ID '%s' has been deleted", dtoClass.getSimpleName(), id)));
    });
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
