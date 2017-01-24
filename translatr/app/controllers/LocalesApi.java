package controllers;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.LocaleCriteria;
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
import models.Locale;
import models.ProjectRole;
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import services.LocaleService;
import services.LogEntryService;
import services.UserService;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@io.swagger.annotations.Api(value = "Locales", produces = "application/json")
@With(ApiAction.class)
public class LocalesApi extends Api<Locale, UUID, LocaleCriteria, dto.Locale> {
  @Inject
  public LocalesApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService, LocaleService localeService) {
    super(injector, cache, auth, userService, logEntryService, localeService, Locale::byId,
        Locale::findBy, dto.Locale.class, dto.Locale::from, Locale::from,
        new Scope[] {Scope.ProjectRead, Scope.LocaleRead},
        new Scope[] {Scope.ProjectRead, Scope.LocaleWrite});
  }

  @ApiOperation(value = "Find locales",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "locale:read", description = "Read locale")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Found locales", response = dto.Locale[].class),
      @ApiResponse(code = 403, message = "Invalid access token", response = PermissionError.class),
      @ApiResponse(code = 500, message = "Internal server error", response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "access_token", value = "The access token", required = true,
          dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = "localeName", value = "The name of the locale", dataType = "string",
          paramType = "query"),
      @ApiImplicitParam(name = "search", value = "Part of the name of the locales",
          dataType = "string", paramType = "query")})
  public CompletionStage<Result> find(@ApiParam(value = "The project ID") UUID projectId) {
    return findBy(
        LocaleCriteria.from(request()).withProjectId(projectId)
            .withLocaleName(request().getQueryString("localeName")),
        criteria -> checkProjectRole(projectId, User.loggedInUser(), ProjectRole.Owner,
            ProjectRole.Translator, ProjectRole.Developer));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Get locale by ID",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "locale:read", description = "Read locale")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Found locale", response = dto.Locale.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = PermissionError.class),
      @ApiResponse(code = 404, message = "Locale not found", response = NotFoundError.class),
      @ApiResponse(code = 500, message = "Internal server error", response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = "access_token", value = "The access token",
      required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> get(@ApiParam(value = "The locale ID") UUID id) {
    return super.get(id);
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Create locale",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "locale:write", description = "Write locale")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Created locale", response = dto.Locale.class),
      @ApiResponse(code = 400, message = "Bad request", response = ConstraintViolationError.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = PermissionError.class),
      @ApiResponse(code = 500, message = "Internal server error", response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = "The locale to create", required = true,
          dataType = "dto.Locale", paramType = "body"),
      @ApiImplicitParam(name = "access_token", value = "The access token", required = true,
          dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> create() {
    return super.create();
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Update locale",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "locale:write", description = "Write locale")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Updated locale", response = dto.Locale.class),
      @ApiResponse(code = 400, message = "Bad request", response = ConstraintViolationError.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = PermissionError.class),
      @ApiResponse(code = 404, message = "Locale not found", response = NotFoundError.class),
      @ApiResponse(code = 500, message = "Internal server error", response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = "The locale to update", required = true,
          dataType = "dto.Locale", paramType = "body"),
      @ApiImplicitParam(name = "access_token", value = "The access token", required = true,
          dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> update() {
    return super.update();
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Delete locale",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "locale:write", description = "Write locale")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Deleted locale", response = dto.Locale.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = PermissionError.class),
      @ApiResponse(code = 404, message = "Locale not found", response = NotFoundError.class),
      @ApiResponse(code = 500, message = "Internal server error", response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = "access_token", value = "The access token",
      required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> delete(@ApiParam(value = "The locale ID") UUID id) {
    return super.delete(id);
  }

  @ApiOperation(value = "Upload messages to locale",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "locale:read", description = "Read locale"),
              @AuthorizationScope(scope = "message:write", description = "Write message")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Uploaded locale", response = dto.Locale.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = PermissionError.class),
      @ApiResponse(code = 404, message = "Locale not found", response = NotFoundError.class),
      @ApiResponse(code = 500, message = "Internal server error", response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = "access_token", value = "The access token",
      required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> upload(@ApiParam(value = "The locale ID") UUID localeId,
      @ApiParam(value = "The file type") String fileType) {
    return CompletableFuture.supplyAsync(() -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
          Scope.MessageWrite);

      return injector.instanceOf(Locales.class).importLocale(Locale.byId(localeId), request());
    }, executionContext.current())
        .thenApply(success -> ok(Json.newObject().put("status", success)));
  }

  @ApiOperation(value = "Download messages of locale", produces = "text/plain",
      authorizations = @Authorization(value = "scopes",
          scopes = {@AuthorizationScope(scope = "project:read", description = "Read project"),
              @AuthorizationScope(scope = "locale:read", description = "Read locale"),
              @AuthorizationScope(scope = "message:read", description = "Read message")}))
  @ApiResponses({@ApiResponse(code = 200, message = "Messages of locale"),
      @ApiResponse(code = 403, message = "Invalid access token", response = PermissionError.class),
      @ApiResponse(code = 404, message = "Locale not found", response = NotFoundError.class),
      @ApiResponse(code = 500, message = "Internal server error", response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = "access_token", value = "The access token",
      required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> download(UUID localeId, String fileType) {
    return CompletableFuture.supplyAsync(() -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
          Scope.MessageRead);

      return injector.instanceOf(Locales.class).download(localeId, fileType);
    }, executionContext.current()).thenApply(data -> ok(new ByteArrayInputStream(data)))
        .exceptionally(Api::handleException);
  }
}
