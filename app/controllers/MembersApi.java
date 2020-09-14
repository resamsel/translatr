package controllers;

import criterias.ProjectUserCriteria;
import dto.ProjectUser;
import dto.errors.ConstraintViolationError;
import dto.errors.GenericError;
import dto.errors.NotFoundError;
import dto.errors.PermissionError;
import io.swagger.annotations.Api;
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
import play.inject.Injector;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthProvider;
import services.CacheService;
import services.ContextProvider;
import services.api.ProjectUserApiService;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@Api(value = "Members", produces = "application/json")
public class MembersApi extends AbstractApi<ProjectUser, Long, ProjectUserCriteria, ProjectUserApiService> {

  private static final String TYPE = "Member";

  private static final String FIND = "Find members";
  private static final String FIND_RESPONSE = "Found members";
  private static final String GET = "Get member by ID";
  private static final String GET_RESPONSE = "Found member";
  private static final String CREATE = "Create member";
  private static final String CREATE_RESPONSE = "Created member";
  private static final String CREATE_REQUEST = "The member to create";
  private static final String UPDATE = "Update member";
  private static final String UPDATE_RESPONSE = "Updated member";
  private static final String UPDATE_REQUEST = "The member to update";
  private static final String DELETE = "Delete member";
  private static final String DELETE_RESPONSE = "Deleted member";

  private static final String SEARCH = "Part of the name of the member";
  private static final String NOT_FOUND_ERROR = "Member not found";

  private static final String MEMBER_NAME = "The name of the member";
  private static final String MEMBER_ID = "The ID of the member";
  private static final String MEMBER_READ = "member:read";
  private static final String MEMBER_READ_DESCRIPTION = "Read member";
  private static final String MEMBER_WRITE = "member:write";
  private static final String MEMBER_WRITE_DESCRIPTION = "Write member";

  public static final String PARAM_ROLES = "roles";

  private final ContextProvider contextProvider;
  private final AuthProvider authProvider;

  @Inject
  public MembersApi(
          Injector injector, CacheService cache, AuthProvider authProvider,
          ProjectUserApiService projectUserApiService, ContextProvider contextProvider) {
    super(injector, cache, authProvider, projectUserApiService);
    this.contextProvider = contextProvider;
    this.authProvider = authProvider;
  }

  private Http.Request req() {
    return contextProvider.get().request();
  }

  @ApiOperation(value = FIND,
          authorizations = @Authorization(value = AUTHORIZATION,
                  scopes = {
                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
                          @AuthorizationScope(scope = MEMBER_READ, description = MEMBER_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.MembersPaged.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
                  dataType = "string", paramType = "query"),
          @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
                  paramType = "query"),
          @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
          @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query"),
          @ApiImplicitParam(name = PARAM_FETCH, value = FETCH, dataType = "string",
                  paramType = "query"),
          @ApiImplicitParam(name = PARAM_ROLES, value = FETCH, dataType = "string",
                  paramType = "query")})
  public CompletionStage<Result> find(@ApiParam(value = PROJECT_ID) UUID projectId) {
    return toJsons(() -> api.find(
            ProjectUserCriteria.from(req()).withProjectId(projectId),
            criteria -> checkProjectRole(
                    projectId,
                    authProvider.loggedInUser(),
                    ProjectRole.Owner,
                    ProjectRole.Manager,
                    ProjectRole.Translator,
                    ProjectRole.Developer
            ))
    );
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = GET,
          authorizations = @Authorization(value = AUTHORIZATION,
                  scopes = {
                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
                          @AuthorizationScope(scope = MEMBER_READ, description = MEMBER_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = ProjectUser.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> get(@ApiParam(value = MEMBER_ID) Long id,
                                     @ApiParam(value = FETCH) String fetch) {
    return toJson(() -> api.get(id, StringUtils.split(fetch, ",")));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = CREATE,
          authorizations = @Authorization(value = AUTHORIZATION,
                  scopes = {
                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
                          @AuthorizationScope(scope = MEMBER_WRITE, description = MEMBER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = CREATE_RESPONSE, response = ProjectUser.class),
          @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
          @ApiImplicitParam(name = "body", value = CREATE_REQUEST, required = true, dataType = TYPE,
                  paramType = "body"),
          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
                  dataType = "string", paramType = "query")})
  @BodyParser.Of(BodyParser.Json.class)
  public CompletionStage<Result> create() {
    return toJson(() -> api.create(req().body().asJson()));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = UPDATE,
          authorizations = @Authorization(value = AUTHORIZATION,
                  scopes = {
                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
                          @AuthorizationScope(scope = MEMBER_WRITE, description = MEMBER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = UPDATE_RESPONSE, response = ProjectUser.class),
          @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
          @ApiImplicitParam(name = "body", value = UPDATE_REQUEST, required = true, dataType = TYPE,
                  paramType = "body"),
          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
                  dataType = "string", paramType = "query")})
  @BodyParser.Of(BodyParser.Json.class)
  public CompletionStage<Result> update() {
    return toJson(() -> api.update(req().body().asJson()));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = DELETE,
          authorizations = @Authorization(value = AUTHORIZATION,
                  scopes = {
                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
                          @AuthorizationScope(scope = MEMBER_WRITE, description = MEMBER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = DELETE_RESPONSE, response = ProjectUser.class),
          @ApiResponse(code = 403, message = INPUT_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> delete(@ApiParam(value = MEMBER_ID) Long id) {
    return toJson(() -> api.delete(id));
  }
}
