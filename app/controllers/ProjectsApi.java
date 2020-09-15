package controllers;

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
import models.ProjectRole;
import org.apache.commons.lang3.StringUtils;
import play.data.FormFactory;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthProvider;
import services.api.ProjectApiService;
import utils.FormUtils;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@io.swagger.annotations.Api(value = "Projects", produces = "application/json")
public class ProjectsApi extends AbstractApi<Project, UUID, ProjectCriteria, ProjectApiService> {

  private static final String TYPE = "Project";

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
  private static final String SEARCH = "Search the contents of the project";
  private static final String SEARCH_RESPONSE = "Suggestions for contents";

  private static final String SEARCH_FIELD = "The search term";
  private static final String NOT_FOUND_ERROR = "Project not found";

  private final Injector injector;
  private final ProjectApiService projectApiService;

  @Inject
  public ProjectsApi(Injector injector, AuthProvider authProvider, ProjectApiService projectApiService) {
    super(injector, authProvider, projectApiService);
    this.injector = injector;

    this.projectApiService = projectApiService;
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = FIND,
          authorizations = @Authorization(value = AUTHORIZATION, scopes = {
                  @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION)}))
  @ApiResponses({
          @ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.ProjectsPaged.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)
  })
  @ApiImplicitParams({
          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
                  dataType = "string", paramType = "query"),
          @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH_FIELD, dataType = "string",
                  paramType = "query"),
          @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
          @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query"),
          @ApiImplicitParam(name = PARAM_FETCH, value = FETCH, dataType = "string", paramType = "query")
  })
  public CompletionStage<Result> find() {
    return toJsons(() -> api.find(ProjectCriteria.from(request()).withFetches()));
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
  public CompletionStage<Result> get(@ApiParam(value = PROJECT_ID) UUID id,
                                     @ApiParam(value = FETCH) String fetch) {
    return toJson(() -> api.get(id, StringUtils.split(fetch, ",")));
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
  public CompletionStage<Result> byOwnerAndName(
          @ApiParam(value = USER_USERNAME) String username,
          @ApiParam(value = PROJECT_NAME) String projectName,
          @ApiParam(value = FETCH) String fetch) {
    return toJson(() -> api.byOwnerAndName(
            username,
            projectName,
            project -> checkProjectRole(project, authProvider.loggedInUser(), ProjectRole.values()),
            StringUtils.split(fetch, ","))
    );
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = GET, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.AggregatesPaged.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> activity(@ApiParam(value = PROJECT_ID) UUID id) {
    return toJsons(() -> api.activity(id));
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
  public CompletionStage<Result> create(Http.Request request) {
    return toJson(() -> api.create(request.body().asJson()));
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

  @ApiOperation(value = SEARCH, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {@AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION)}))
  @ApiResponses({
          @ApiResponse(code = 200, message = SEARCH_RESPONSE, response = dto.SearchResponse.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
                  dataType = "string", paramType = "query"),
          @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH_FIELD, dataType = "string",
                  paramType = "query"),
          @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
          @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query")})
  public CompletionStage<Result> search(UUID projectId) {
    return toJsonSearch(() -> projectApiService.search(
            projectId,
            FormUtils.Search.bindFromRequest(injector.instanceOf(FormFactory.class), configuration)
                    .get()
    ));
  }
}
