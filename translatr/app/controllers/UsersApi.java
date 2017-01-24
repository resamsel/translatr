package controllers;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.UserCriteria;
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
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.LogEntryService;
import services.UserService;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@io.swagger.annotations.Api(value = "Users", produces = "application/json")
@With(ApiAction.class)
public class UsersApi extends Api<User, UUID, UserCriteria, dto.User> {
  private static final String TYPE = "dto.User";

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
  private static final String DELETE = "Delete user";
  private static final String DELETE_RESPONSE = "Deleted user";

  private static final String SEARCH = "Part of the name of the user";
  private static final String NOT_FOUND = "User not found";

  @Inject
  public UsersApi(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService) {
    super(injector, cache, auth, userService, logEntryService, userService, User::byId,
        User::findBy, dto.User.class, dto.User::from, User::from, new Scope[] {Scope.UserRead},
        new Scope[] {Scope.UserWrite});
  }

  @ApiOperation(value = FIND, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = @AuthorizationScope(scope = USER_READ, description = USER_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.User[].class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN_DESCRIPTION,
          required = true, dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
          paramType = "query"),
      @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
      @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query")})
  public CompletionStage<Result> find() {
    return findBy(UserCriteria.from(request()));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = GET, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = @AuthorizationScope(scope = USER_READ, description = USER_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.User.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN_DESCRIPTION,
      required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> get(@ApiParam(value = PROJECT_ID) UUID id) {
    return super.get(id);
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
      @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN_DESCRIPTION,
          required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> create() {
    return super.create();
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = UPDATE, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = {@AuthorizationScope(scope = USER_WRITE, description = USER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = UPDATE_RESPONSE, response = dto.User.class),
      @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = UPDATE_REQUEST, required = true, dataType = TYPE,
          paramType = "body"),
      @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN_DESCRIPTION,
          required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> update() {
    return super.update();
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = DELETE, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = {@AuthorizationScope(scope = USER_WRITE, description = USER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = DELETE_RESPONSE, response = dto.User.class),
      @ApiResponse(code = 403, message = INPUT_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN_DESCRIPTION,
      required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> delete(@ApiParam(value = PROJECT_ID) UUID id) {
    return super.delete(id);
  }
}
