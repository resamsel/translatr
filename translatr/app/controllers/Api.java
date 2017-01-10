package controllers;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

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

public abstract class Api<MODEL extends Model<MODEL>, DTO extends Dto, ID>
    extends AbstractController {
  protected final ModelService<MODEL> service;

  protected Api(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService, ModelService<MODEL> service) {
    super(injector, cache, auth, userService, logEntryService);

    this.service = service;
  }

  /**
   * @param errorMessage
   * @param scopes
   */
  protected void checkPermissionAll(String errorMessage, Scope... scopes) {
    if (!PermissionUtils.hasPermissionAll(scopes))
      throw new PermissionException(errorMessage);
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

  protected abstract Function<MODEL, DTO> dtoMapper();

  protected <T, CRITERIA extends AbstractSearchCriteria<CRITERIA>> Supplier<List<T>> finder(
      CRITERIA criteria, Function<CRITERIA, List<T>> finder, Scope... scopes) {
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

  protected abstract Function<ID, MODEL> getter();

  protected abstract Scope[] scopesGet();

  protected abstract Scope[] scopesCreate();

  protected abstract Scope[] scopesDelete();

  public Result get(ID id) {
    return toJson(dtoMapper(), () -> {
      checkPermissionAll("Access token not allowed", scopesGet());

      MODEL obj = getter().apply(id);

      if (obj == null)
        throw new NotFoundException(String.format("Entity not found: '%s'", String.valueOf(id)));

      return obj;
    });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result create() {
    return toJson(dtoMapper(), creator(request()));
  }

  protected void checkDelete(MODEL m) {}

  public Result delete(ID id) {
    return tryCatch(() -> {
      checkPermissionAll("Access token not allowed", scopesDelete());

      MODEL m = getter().apply(id);

      if (m == null)
        throw new NotFoundException(String.format("%s with ID '%s' not found",
            service.getClazz().getSimpleName(), String.valueOf(id)));

      service.delete(m);

      return ok(Json.newObject().put("message", String.format("%s with ID '%s' has been deleted",
          service.getClazz().getSimpleName(), id)));
    });
  }



  /**
   * @param request
   * @return
   */
  protected Supplier<MODEL> creator(Request request) {
    checkPermissionAll("Access token not allowed", scopesCreate());

    return () -> service.create(request.body().asJson());
  }
}
