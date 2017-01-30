package controllers;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.ProjectCriteria;
import dto.Project;
import dto.errors.ConstraintViolationError;
import dto.errors.GenericError;
import dto.errors.NotFoundError;
import dto.errors.PermissionError;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.LogEntryService;
import services.UserService;
import services.api.ProjectApiService;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@io.swagger.annotations.Api(value = "Projects", produces = "application/json")
@With(ApiAction.class)
public class ProjectsApi extends Api<Project, UUID, ProjectCriteria> {
  private static final String TYPE = "dto.Project";

  private static final String FIND = "Find projects";
  private static final String FIND_RESPONSE = "Found projects";
  private static final String GET = "Get project by ID";
  private static final String GET_RESPONSE = "Found project";
  private static final String CREATE = "Create project";
  private static final String CREATE_RESPONSE = "Created project";
  private static final String CREATE_REQUEST = "The project to create";
  private static final String UPDATE = "Update project";
  private static final String UPDATE_RESPONSE = "Updated project";
  private static final String UPDATE_REQUEST = "The project to update";
  private static final String DELETE = "Delete project";
  private static final String DELETE_RESPONSE = "Deleted project";

  private static final String SEARCH = "Part of the name of the project";
  private static final String NOT_FOUND_ERROR = "Project not found";

  @Inject
  public ProjectsApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService,
      ProjectApiService projectApiService) {
    super(injector, cache, auth, userService, logEntryService, projectApiService);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @ApiOperation(value = FIND, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = {@AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.Project[].class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
          dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
          paramType = "query"),
      @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
      @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query")})
  public CompletionStage<Result> find() {
    return toJsons(
        () -> api.find(ProjectCriteria.from(request()).withMemberId(User.loggedInUserId())));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = GET, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = {@AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.Project.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
      required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> get(@ApiParam(value = PROJECT_ID) UUID id) {
    return toJson(() -> api.get(id));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = CREATE, authorizations = @Authorization(value = AUTHORIZATION, scopes = {
      @AuthorizationScope(scope = PROJECT_WRITE, description = PROJECT_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = CREATE_RESPONSE, response = dto.Project.class),
      @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = CREATE_REQUEST, required = true, dataType = TYPE,
          paramType = "body"),
      @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
          dataType = "string", paramType = "query")})
  public CompletionStage<Result> create() {
    return toJson(() -> api.create(request().body().asJson()));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = UPDATE, authorizations = @Authorization(value = AUTHORIZATION, scopes = {
      @AuthorizationScope(scope = PROJECT_WRITE, description = PROJECT_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = UPDATE_RESPONSE, response = dto.Project.class),
      @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = UPDATE_REQUEST, required = true, dataType = TYPE,
          paramType = "body"),
      @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
          dataType = "string", paramType = "query")})
  public CompletionStage<Result> update() {
    return toJson(() -> api.update(request().body().asJson()));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = DELETE, authorizations = @Authorization(value = AUTHORIZATION, scopes = {
      @AuthorizationScope(scope = PROJECT_WRITE, description = PROJECT_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = DELETE_RESPONSE, response = dto.Project.class),
      @ApiResponse(code = 403, message = INPUT_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
      required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> delete(@ApiParam(value = PROJECT_ID) UUID id) {
    return toJson(() -> api.delete(id));
  }
}
