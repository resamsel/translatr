package controllers;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.KeyCriteria;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import models.Key;
import models.ProjectRole;
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.KeyService;
import services.LogEntryService;
import services.UserService;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@io.swagger.annotations.Api(value = "key", produces = "application/json")
@With(ApiAction.class)
public class KeysApi extends Api<Key, UUID, KeyCriteria, dto.Key> {
  @Inject
  public KeysApi(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService, KeyService keyService) {
    super(injector, cache, auth, userService, logEntryService, keyService, Key::byId, Key::findBy,
        dto.Key.class, dto.Key::from, Key::from, new Scope[] {Scope.ProjectRead, Scope.KeyRead},
        new Scope[] {Scope.ProjectRead, Scope.KeyWrite});
  }

  @ApiOperation(value = "Find keys",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "key:read", description = "Read key")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Found keys", response = dto.Key[].class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "access_token", value = "The access token", required = true,
          dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = "search", value = "Part of the name of the keys",
          dataType = "string", paramType = "query")})
  public CompletionStage<Result> find(@ApiParam(value = "The project ID") UUID projectId) {
    return findBy(
        new KeyCriteria().withProjectId(projectId).withSearch(request().getQueryString("search")),
        criteria -> checkProjectRole(projectId, User.loggedInUser(), ProjectRole.Owner,
            ProjectRole.Translator, ProjectRole.Developer));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Get key by ID",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "key:read", description = "Read key")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Found key", response = dto.Key.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class),
      @ApiResponse(code = 404, message = "Project not found", response = dto.Error.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = "access_token", value = "The access token",
      required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> get(@ApiParam(value = "The key ID") UUID id) {
    return super.get(id);
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Create key",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "key:read", description = "Read key")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Created key", response = dto.Key.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = "The key to create", required = true,
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
  @ApiOperation(value = "Create key",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "key:write", description = "Write key")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Updated key", response = dto.Key.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = "The key to update", required = true,
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
  @ApiOperation(value = "Delete key",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "key:write", description = "Write key")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Deleted key", response = dto.Key.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = "access_token", value = "The access token",
      required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> delete(@ApiParam(value = "The key ID") UUID id) {
    return super.delete(id);
  }
}
