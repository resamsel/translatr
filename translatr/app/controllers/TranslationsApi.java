package controllers;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.MessageCriteria;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import models.Message;
import models.ProjectRole;
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.LogEntryService;
import services.MessageService;
import services.UserService;
import utils.JsonUtils;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@io.swagger.annotations.Api(value = "message", produces = "application/json")
@With(ApiAction.class)
public class TranslationsApi extends Api<Message, UUID, MessageCriteria, dto.Message> {
  /**
   * @param injector
   * @param cache
   * @param auth
   * @param userService
   * @param logEntryService
   */
  @Inject
  public TranslationsApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService, MessageService messageService) {
    super(injector, cache, auth, userService, logEntryService, messageService, Message::byId,
        Message::findBy, dto.Message.class, dto.Message::from, Message::from,
        new Scope[] {Scope.ProjectRead, Scope.MessageRead},
        new Scope[] {Scope.ProjectRead, Scope.MessageWrite});
  }

  @ApiOperation(value = "Find messages",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "message:read", description = "Read message")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Found messages", response = dto.Key[].class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "access_token", value = "The access token", required = true,
          dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = "localeId", value = "The locale ID", dataType = "java.util.UUID",
          paramType = "query"),
      @ApiImplicitParam(name = "keyName", value = "The name of the key", dataType = "string",
          paramType = "query"),
      @ApiImplicitParam(name = "search", value = "Part of the value of the messages",
          dataType = "string", paramType = "query")})
  public CompletionStage<Result> find(@ApiParam(value = "The project ID") UUID projectId) {
    return findBy(
        new MessageCriteria().withProjectId(projectId)
            .withLocaleId(JsonUtils.getUuid(request().getQueryString("localeId")))
            .withKeyName(request().getQueryString("keyName"))
            .withSearch(request().getQueryString("search")),
        criteria -> checkProjectRole(projectId, User.loggedInUser(), ProjectRole.Owner,
            ProjectRole.Translator, ProjectRole.Developer));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Get message by ID",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "message:read", description = "Read message")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Found message", response = dto.Key.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class),
      @ApiResponse(code = 404, message = "Project not found", response = dto.Error.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = "access_token", value = "The access token",
      required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> get(@ApiParam(value = "The message ID") UUID id) {
    return super.get(id);
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Create message",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "message:read", description = "Read message")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Created message", response = dto.Key.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = "The message to create", required = true,
          dataType = "dto.Project", paramType = "body"),
      @ApiImplicitParam(name = "access_token", value = "The access token", required = true,
          dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> create() {
    return super.create();
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Update message",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "message:write", description = "Write message")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Updated message", response = dto.Key.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = "The message to update", required = true,
          dataType = "dto.Project", paramType = "body"),
      @ApiImplicitParam(name = "access_token", value = "The access token", required = true,
          dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> update() {
    return super.update();
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Delete message",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "message:write", description = "Write message")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Deleted message", response = dto.Key.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = "access_token", value = "The access token",
      required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> delete(@ApiParam(value = "The message ID") UUID id) {
    return super.delete(id);
  }
}
