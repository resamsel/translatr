package controllers;

import actions.ApiAction;
import com.fasterxml.jackson.databind.node.NullNode;
import com.feth.play.module.pa.PlayAuthenticate;
import criterias.KeyCriteria;
import dto.Key;
import dto.Locale;
import dto.errors.ConstraintViolationError;
import dto.errors.GenericError;
import dto.errors.NotFoundError;
import dto.errors.PermissionError;
import io.swagger.annotations.*;
import models.ProjectRole;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.With;
import services.CacheService;
import services.api.KeyApiService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@io.swagger.annotations.Api(value = "Keys", produces = "application/json")
@With(ApiAction.class)
public class KeysApi extends AbstractApi<Key, UUID, KeyCriteria, KeyApiService> {

  private static final String TYPE = "dto.Key";

  private static final String FIND = "Find keys";
  private static final String FIND_RESPONSE = "Found keys";
  private static final String GET = "Get key by ID";
  private static final String GET_RESPONSE = "Found key";
  private static final String CREATE = "Create key";
  private static final String CREATE_RESPONSE = "Created key";
  private static final String CREATE_REQUEST = "The key to create";
  private static final String UPDATE = "Update key";
  private static final String UPDATE_RESPONSE = "Updated key";
  private static final String UPDATE_REQUEST = "The key to update";
  private static final String DELETE = "Delete key";
  private static final String DELETE_RESPONSE = "Deleted key";

  private static final String SEARCH = "Part of the name of the key";
  private static final String NOT_FOUND_ERROR = "Key not found";

  private static final String KEY_NAME = "The name of the key";

  @Inject
  public KeysApi(Injector injector, CacheService cache, PlayAuthenticate auth,
                 KeyApiService keyApiService) {
    super(injector, cache, auth, keyApiService);
  }

  @ApiOperation(value = FIND,
      authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {
              @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
              @AuthorizationScope(scope = KEY_READ, description = KEY_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.KeysPaged.class),
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
          paramType = "query")})
  public CompletionStage<Result> find(@ApiParam(value = PROJECT_ID) UUID projectId) {
    return toJsons(() -> api.find(
        KeyCriteria.from(request()).withProjectId(projectId),
        criteria -> checkProjectRole(
            projectId,
            User.loggedInUser(),
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
              @AuthorizationScope(scope = KEY_READ, description = KEY_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.Key.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
      required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> get(@ApiParam(value = KEY_ID) UUID id,
                                     @ApiParam(value = FETCH) String fetch) {
    return toJson(() -> api.get(id, StringUtils.split(fetch, ",")));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = GET,
      authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {
              @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
              @AuthorizationScope(scope = KEY_READ, description = KEY_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.Key.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
      required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> byOwnerAndProjectNameAndName(
      @ApiParam(value = USER_USERNAME) String username,
      @ApiParam(value = PROJECT_NAME) String projectName,
      @ApiParam(value = KEY_NAME) String keyName,
      @ApiParam(value = FETCH) String fetch) {
    return key(
        username,
        projectName,
        keyName,
        key -> ok(Optional.ofNullable(key).map(Json::toJson).orElse(NullNode.getInstance())),
        StringUtils.split(fetch, ","))
        .exceptionally(this::handleException);
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = CREATE,
      authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {
              @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
              @AuthorizationScope(scope = KEY_WRITE, description = KEY_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = CREATE_RESPONSE, response = dto.Key.class),
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
    return toJson(() -> api.create(request().body().asJson()));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = UPDATE,
      authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {
              @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
              @AuthorizationScope(scope = KEY_WRITE, description = KEY_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = UPDATE_RESPONSE, response = dto.Key.class),
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
    return toJson(() -> api.update(request().body().asJson()));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = DELETE,
      authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {
              @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
              @AuthorizationScope(scope = KEY_WRITE, description = KEY_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = DELETE_RESPONSE, response = dto.Key.class),
      @ApiResponse(code = 403, message = INPUT_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
      required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> delete(@ApiParam(value = KEY_ID) UUID id) {
    return toJson(() -> api.delete(id));
  }

  private CompletionStage<Result> key(String username, String projectName, String keyName,
                                      Function<Key, Result> processor, String... fetches) {
    return tryCatch(() -> {
      Key key = api.byOwnerAndProjectAndName(username, projectName, keyName, fetches);

      checkProjectRole(key.projectId, User.loggedInUser(), ProjectRole.Owner,
          ProjectRole.Manager, ProjectRole.Developer, ProjectRole.Translator);

      return processor.apply(key);
    });
  }
}
