package controllers;

import com.fasterxml.jackson.databind.node.NullNode;
import criterias.KeyCriteria;
import dto.Key;
import io.swagger.v3.oas.annotations.Parameter;
import models.ProjectRole;
import org.apache.commons.lang3.StringUtils;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthProvider;
import services.api.KeyApiService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

///**
// * @author resamsel
// * @version 10 Jan 2017
// */
////@io.swagger.annotations.Api(value = "Keys", produces = "application/json")
public class KeysApi extends AbstractApi<Key, UUID, KeyCriteria, KeyApiService> {

  private static final String TYPE = "Key";

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
  private static final String MISSING = "Whether or not keys with missing messages should be fetched - use in" +
          " combination with localeId to retrieve missing messages for a certain locale";

  @Inject
  public KeysApi(Injector injector, AuthProvider authProvider, KeyApiService keyApiService) {
    super(injector, authProvider, keyApiService);
  }

//  @ApiOperation(value = FIND,
//          authorizations = @Authorization(value = AUTHORIZATION,
//                  scopes = {
//                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
//                          @AuthorizationScope(scope = KEY_READ, description = KEY_READ_DESCRIPTION)}))
//  @ApiResponses({@ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.KeysPaged.class),
//          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
//          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
//  @ApiImplicitParams({
//          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
//                  dataType = "string", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
//                  paramType = "query"),
//          @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_FETCH, value = FETCH, dataType = "string",
//                  paramType = "query"),
//          @ApiImplicitParam(name = PARAM_LOCALE_ID, value = LOCALE_ID,
//                  dataType = "string", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_MISSING, value = MISSING,
//                  dataType = "string", paramType = "query")})
  public CompletionStage<Result> find(Http.Request request, @Parameter(name = PROJECT_ID) UUID projectId) {
    return toJsons(() -> api.find(
            KeyCriteria.from(request).withProjectId(projectId),
            criteria -> checkProjectRole(
                    projectId,
                    authProvider.loggedInUser(request),
                    ProjectRole.Owner,
                    ProjectRole.Manager,
                    ProjectRole.Translator,
                    ProjectRole.Developer
            ))
    );
  }

//  /**
//   * {@inheritDoc}
//   */
//  @ApiOperation(value = GET,
//          authorizations = @Authorization(value = AUTHORIZATION,
//                  scopes = {
//                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
//                          @AuthorizationScope(scope = KEY_READ, description = KEY_READ_DESCRIPTION)}))
//  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.Key.class),
//          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
//          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
//          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
//  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
//          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> get(Http.Request request, @Parameter(name = KEY_ID) UUID id,
                                     @Parameter(name = FETCH) String fetch) {
    return toJson(() -> api.get(request, id, StringUtils.split(fetch, ",")));
  }

//  /**
//   * {@inheritDoc}
//   */
//  @ApiOperation(value = GET,
//          authorizations = @Authorization(value = AUTHORIZATION,
//                  scopes = {
//                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
//                          @AuthorizationScope(scope = KEY_READ, description = KEY_READ_DESCRIPTION)}))
//  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.Key.class),
//          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
//          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
//          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
//  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
//          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> byOwnerAndProjectNameAndName(
          Http.Request request,
          @Parameter(name = USER_USERNAME) String username,
          @Parameter(name = PROJECT_NAME) String projectName,
          @Parameter(name = KEY_NAME) String keyName,
          @Parameter(name = FETCH) String fetch) {
    return key(
            request,
            username,
            projectName,
            keyName,
            key -> ok(Optional.ofNullable(key).map(Json::toJson).orElse(NullNode.getInstance())),
            StringUtils.split(fetch, ","))
            .exceptionally(this::handleException);
  }

//  /**
//   * {@inheritDoc}
//   */
//  @ApiOperation(value = CREATE,
//          authorizations = @Authorization(value = AUTHORIZATION,
//                  scopes = {
//                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
//                          @AuthorizationScope(scope = KEY_WRITE, description = KEY_WRITE_DESCRIPTION)}))
//  @ApiResponses({@ApiResponse(code = 200, message = CREATE_RESPONSE, response = dto.Key.class),
//          @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
//          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
//          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
//  @ApiImplicitParams({
//          @ApiImplicitParam(name = "body", value = CREATE_REQUEST, required = true, dataType = TYPE,
//                  paramType = "body"),
//          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
//                  dataType = "string", paramType = "query")})
  @BodyParser.Of(BodyParser.Json.class)
  public CompletionStage<Result> create(Http.Request request) {
    return toJson(() -> api.create(request, request.body().asJson()));
  }

//  /**
//   * {@inheritDoc}
//   */
//  @ApiOperation(value = UPDATE,
//          authorizations = @Authorization(value = AUTHORIZATION,
//                  scopes = {
//                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
//                          @AuthorizationScope(scope = KEY_WRITE, description = KEY_WRITE_DESCRIPTION)}))
//  @ApiResponses({@ApiResponse(code = 200, message = UPDATE_RESPONSE, response = dto.Key.class),
//          @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
//          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
//          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
//          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
//  @ApiImplicitParams({
//          @ApiImplicitParam(name = "body", value = UPDATE_REQUEST, required = true, dataType = TYPE,
//                  paramType = "body"),
//          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
//                  dataType = "string", paramType = "query")})
  @BodyParser.Of(BodyParser.Json.class)
  public CompletionStage<Result> update(Http.Request request) {
    return toJson(() -> api.update(request, request.body().asJson()));
  }

//  /**
//   * {@inheritDoc}
//   */
//  @ApiOperation(value = DELETE,
//          authorizations = @Authorization(value = AUTHORIZATION,
//                  scopes = {
//                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
//                          @AuthorizationScope(scope = KEY_WRITE, description = KEY_WRITE_DESCRIPTION)}))
//  @ApiResponses({@ApiResponse(code = 200, message = DELETE_RESPONSE, response = dto.Key.class),
//          @ApiResponse(code = 403, message = INPUT_ERROR, response = PermissionError.class),
//          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
//          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
//  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
//          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> delete(Http.Request request, @Parameter(name = KEY_ID) UUID id) {
    return toJson(() -> api.delete(request, id));
  }

  private CompletionStage<Result> key(Http.Request request, String username, String projectName, String keyName,
                                      Function<Key, Result> processor, String... fetches) {
    return async(() -> {
      Key key = api.byOwnerAndProjectAndName(request, username, projectName, keyName, fetches);

      checkProjectRole(key.projectId, authProvider.loggedInUser(request), ProjectRole.Owner,
              ProjectRole.Manager, ProjectRole.Developer, ProjectRole.Translator);

      return processor.apply(key);
    });
  }
}
