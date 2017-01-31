package controllers;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.MessageCriteria;
import dto.Message;
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
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.With;
import services.LogEntryService;
import services.UserService;
import services.api.MessageApiService;
import utils.JsonUtils;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@io.swagger.annotations.Api(value = "Messages", produces = "application/json")
@With(ApiAction.class)
public class TranslationsApi extends AbstractApi<Message, UUID, MessageCriteria> {
  private static final String TYPE = "dto.Message";

  private static final String FIND = "Find messages";
  private static final String FIND_RESPONSE = "Found messages";
  private static final String GET = "Get message by ID";
  private static final String GET_RESPONSE = "Found message";
  private static final String CREATE = "Create message";
  private static final String CREATE_RESPONSE = "Created message";
  private static final String CREATE_REQUEST = "The message to create";
  private static final String UPDATE = "Update message";
  private static final String UPDATE_RESPONSE = "Updated message";
  private static final String UPDATE_REQUEST = "The message to update";
  private static final String DELETE = "Delete message";
  private static final String DELETE_RESPONSE = "Deleted message";

  private static final String SEARCH = "Part of the value of the message";
  private static final String NOT_FOUND_ERROR = "Message not found";
  private static final String KEY_NAME = "The name of the key";
  private static final String PARAM_KEY_NAME = "keyName";

  /**
   * @param injector
   * @param cache
   * @param auth
   * @param userService
   * @param logEntryService
   */
  @Inject
  public TranslationsApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService,
      MessageApiService messageApiService) {
    super(injector, cache, auth, userService, logEntryService, messageApiService);
  }

  @SuppressWarnings("unchecked")
  @ApiOperation(value = FIND,
      authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {
              @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
              @AuthorizationScope(scope = MESSAGE_READ, description = MESSAGE_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.Message[].class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
          dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = PARAM_LOCALE_ID, value = LOCALE_ID, dataType = "java.util.UUID",
          paramType = "query"),
      @ApiImplicitParam(name = PARAM_KEY_NAME, value = KEY_NAME, dataType = "string",
          paramType = "query"),
      @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
          paramType = "query"),
      @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
      @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query")})
  public CompletionStage<Result> find(@ApiParam(value = PROJECT_ID) UUID projectId) {
    return toJsons(() -> api.find(
        MessageCriteria.from(request()).withProjectId(projectId)
            .withLocaleId(JsonUtils.getUuid(request().getQueryString("localeId")))
            .withKeyName(request().getQueryString(PARAM_KEY_NAME)),
        criteria -> checkProjectRole(projectId, User.loggedInUser(), ProjectRole.Owner,
            ProjectRole.Translator, ProjectRole.Developer)));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = GET,
      authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {
              @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
              @AuthorizationScope(scope = MESSAGE_READ, description = MESSAGE_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.Message.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
      required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> get(@ApiParam(value = MESSAGE_ID) UUID id) {
    return toJson(() -> api.get(id));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = CREATE,
      authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {
              @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
              @AuthorizationScope(scope = MESSAGE_WRITE, description = MESSAGE_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = CREATE_RESPONSE, response = dto.Message.class),
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
              @AuthorizationScope(scope = MESSAGE_WRITE, description = MESSAGE_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = UPDATE_RESPONSE, response = dto.Message.class),
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
              @AuthorizationScope(scope = MESSAGE_WRITE, description = MESSAGE_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = DELETE_RESPONSE, response = dto.Message.class),
      @ApiResponse(code = 403, message = INPUT_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
      required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> delete(@ApiParam(value = MESSAGE_ID) UUID id) {
    return toJson(() -> api.delete(id));
  }
}
