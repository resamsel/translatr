package controllers;

import actions.ApiAction;
import criterias.UserFeatureFlagCriteria;
import dto.UserFeatureFlag;
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
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.AuthProvider;
import services.CacheService;
import services.api.UserFeatureFlagApiService;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@io.swagger.annotations.Api(value = "Feature Flags", produces = "application/json")
@With(ApiAction.class)
public class FeatureFlagsApi extends AbstractApi<UserFeatureFlag, UUID, UserFeatureFlagCriteria, UserFeatureFlagApiService> {

  private static final String TYPE = "User";

  private static final String FIND = "Find feature flags";
  private static final String FIND_RESPONSE = "Found feature flags";
  private static final String GET = "Get feature flag by ID";
  private static final String GET_RESPONSE = "Found feature flag";
  private static final String CREATE = "Create feature flag";
  private static final String CREATE_RESPONSE = "Created feature flag";
  private static final String CREATE_REQUEST = "The feature flag to create";
  private static final String UPDATE = "Update feature flag";
  private static final String UPDATE_RESPONSE = "Updated feature flag";
  private static final String UPDATE_REQUEST = "The feature flag to update";
  private static final String DELETE = "Delete feature flag";
  private static final String DELETE_RESPONSE = "Deleted feature flag";

  private static final String SEARCH = "Part of the name of the feature flag";
  private static final String NOT_FOUND_ERROR = "Feature flag not found";
  private static final String FEATURE_FLAG_ID = "The feature flag id";

  @Inject
  public FeatureFlagsApi(
          Injector injector, CacheService cache, AuthProvider authProvider,
          UserFeatureFlagApiService userFeatureFlagApiService) {
    super(injector, cache, authProvider, userFeatureFlagApiService);
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
  public CompletionStage<Result> find() {
    return toJsons(() -> api.find(UserFeatureFlagCriteria.from(request())));
  }

  @ApiOperation(value = GET, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = @AuthorizationScope(scope = USER_READ, description = USER_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.User.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> get(@ApiParam(value = FEATURE_FLAG_ID) UUID id) {
    return toJson(() -> api.get(id));
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
  public CompletionStage<Result> create() {
    return toJson(() -> api.create(request().body().asJson()));
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
  public CompletionStage<Result> update() {
    return toJson(() -> api.update(request().body().asJson()));
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
  public CompletionStage<Result> delete(@ApiParam(value = FEATURE_FLAG_ID) UUID id) {
    return toJson(() -> api.delete(id));
  }
}
