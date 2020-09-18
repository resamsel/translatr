package controllers;

import criterias.UserCriteria;
import dto.User;
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
import org.apache.commons.lang3.StringUtils;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthProvider;
import services.api.UserApiService;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@io.swagger.annotations.Api(value = "Users", produces = "application/json")
public class UsersApi extends AbstractApi<User, UUID, UserCriteria, UserApiService> {

  private static final String TYPE = "User";

  private static final String FIND = "Find users";
  private static final String FIND_RESPONSE = "Found users";
  private static final String GET = "Get user by ID";
  private static final String GET_RESPONSE = "Found user";
  private static final String CREATE = "Create user";
  private static final String CREATE_RESPONSE = "Created user";
  private static final String CREATE_REQUEST = "The user to create";
  private static final String UPDATE = "Update user";
  private static final String UPDATE_RESPONSE = "Updated user";
  private static final String UPDATE_REQUEST = "The user to update";
  private static final String UPDATE_SETTINGS = "Update user settings";
  private static final String UPDATE_SETTINGS_RESPONSE = "Updated user settings";
  private static final String UPDATE_SETTINGS_REQUEST = "The user settings to update";
  private static final String PATCH_SETTINGS = "Patch user settings";
  private static final String PATCH_SETTINGS_RESPONSE = "Patch user settings";
  private static final String PATCH_SETTINGS_REQUEST = "The user settings to patch";
  private static final String DELETE = "Delete user";
  private static final String DELETE_RESPONSE = "Deleted user";

  private static final String SEARCH = "Part of the name of the user";
  private static final String NOT_FOUND_ERROR = "User not found";

  @Inject
  public UsersApi(Injector injector, AuthProvider authProvider, UserApiService userApiService) {
    super(injector, authProvider, userApiService);
  }

  @ApiOperation(value = FIND, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = @AuthorizationScope(scope = USER_READ, description = USER_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.UsersPaged.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
                  dataType = "string", paramType = "query"),
          @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
                  paramType = "query"),
          @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
          @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query")})
  public CompletionStage<Result> find(Http.Request request) {
    return toJsons(() -> api.find(UserCriteria.from(request)));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = GET, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = @AuthorizationScope(scope = USER_READ, description = USER_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.User.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> get(Http.Request request, @ApiParam(value = USER_ID) UUID id) {
    return toJson(() -> api.get(request, id));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = GET, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = @AuthorizationScope(scope = USER_READ, description = USER_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.User.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> byName(Http.Request request, @ApiParam(value = USER_USERNAME) String username,
                                        @ApiParam(value = FETCH) String fetch) {
    return toJson(() -> api.byUsername(request, username, StringUtils.split(fetch, ",")));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = GET, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = @AuthorizationScope(scope = USER_READ, description = USER_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.AggregatesPaged.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> activity(Http.Request request, @ApiParam(value = USER_ID) UUID id) {
    return toJsons(() -> api.activity(request, id));
  }

  public CompletionStage<Result> profile(Http.Request request) {
    return toJson(() -> api.profile(request));
  }

  @ApiOperation(value = GET, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = @AuthorizationScope(scope = USER_READ, description = USER_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.User.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> me(Http.Request request, @ApiParam(value = FETCH) String fetch) {
    return toJson(() -> api.me(request, StringUtils.split(fetch, ",")));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = CREATE, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {@AuthorizationScope(scope = USER_WRITE, description = USER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = CREATE_RESPONSE, response = dto.User.class),
          @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
          @ApiImplicitParam(name = "body", value = CREATE_REQUEST, required = true, dataType = TYPE,
                  paramType = "body"),
          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
                  dataType = "string", paramType = "query")})
  public CompletionStage<Result> create(Http.Request request) {
    return toJson(() -> api.create(request));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = UPDATE, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {@AuthorizationScope(scope = USER_WRITE, description = USER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = UPDATE_RESPONSE, response = dto.User.class),
          @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
          @ApiImplicitParam(name = "body", value = UPDATE_REQUEST, required = true, dataType = TYPE,
                  paramType = "body"),
          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
                  dataType = "string", paramType = "query")})
  public CompletionStage<Result> update(Http.Request request) {
    return toJson(() -> api.update(request, request.body().asJson()));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = DELETE, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {@AuthorizationScope(scope = USER_WRITE, description = USER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = DELETE_RESPONSE, response = dto.User.class),
          @ApiResponse(code = 403, message = INPUT_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> delete(Http.Request request, @ApiParam(value = PROJECT_ID) UUID id) {
    return toJson(() -> api.delete(request, id));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = UPDATE_SETTINGS, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {@AuthorizationScope(scope = USER_WRITE, description = USER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = UPDATE_SETTINGS_RESPONSE, response = dto.User.class),
          @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
          @ApiImplicitParam(name = "body", value = UPDATE_SETTINGS_REQUEST, required = true, dataType = TYPE,
                  paramType = "body"),
          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
                  dataType = "string", paramType = "query")})
  public CompletionStage<Result> saveSettings(Http.Request request, @ApiParam(value = USER_ID) UUID id) {
    return toJson(() -> api.saveSettings(id, request.body().asJson()));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = PATCH_SETTINGS, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {@AuthorizationScope(scope = USER_WRITE, description = USER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = PATCH_SETTINGS_RESPONSE, response = dto.User.class),
          @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
          @ApiImplicitParam(name = "body", value = PATCH_SETTINGS_REQUEST, required = true, dataType = TYPE,
                  paramType = "body"),
          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
                  dataType = "string", paramType = "query")})
  public CompletionStage<Result> updateSettings(Http.Request request, @ApiParam(value = USER_ID) UUID id) {
    return toJson(() -> api.updateSettings(id, request.body().asJson()));
  }
}
