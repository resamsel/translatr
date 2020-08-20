package controllers;

import actions.ApiAction;
import com.fasterxml.jackson.databind.node.NullNode;
import criterias.LocaleCriteria;
import dto.Locale;
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
import org.apache.commons.lang3.StringUtils;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.With;
import services.AuthProvider;
import services.CacheService;
import services.api.LocaleApiService;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@io.swagger.annotations.Api(value = "Locales", produces = "application/json")
@With(ApiAction.class)
public class LocalesApi extends AbstractApi<Locale, UUID, LocaleCriteria, LocaleApiService> {

  private static final String TYPE = "Locale";

  private static final String FIND = "Find locales";
  private static final String FIND_RESPONSE = "Found locales";
  private static final String GET = "Get locale by ID";
  private static final String GET_RESPONSE = "Found locale";
  private static final String CREATE = "Create locale";
  private static final String CREATE_RESPONSE = "Created locale";
  private static final String CREATE_REQUEST = "The locale to create";
  private static final String UPDATE = "Update locale";
  private static final String UPDATE_RESPONSE = "Updated locale";
  private static final String UPDATE_REQUEST = "The locale to update";
  private static final String DELETE = "Delete locale";
  private static final String DELETE_RESPONSE = "Deleted locale";
  private static final String UPLOAD = "Upload messages to locale";
  private static final String UPLOAD_RESPONSE = "Uploaded locale";
  private static final String DOWNLOAD = "Download messages of locale";
  private static final String DOWNLOAD_RESPONSE = "Messages of locale";

  private static final String SEARCH = "Part of the name of the locale";
  private static final String NOT_FOUND_ERROR = "Locale not found";

  private static final String PARAM_LOCALE_NAME = LocaleCriteria.PARAM_LOCALE_NAME;
  private static final String LOCALE_NAME = "The name of the locale";
  private static final String PARAM_MESSAGES_KEY_NAME = LocaleCriteria.PARAM_MESSAGES_KEY_NAME;
  private static final String MESSAGES_KEY_NAME =
          "The name of the key of fetched messages - use in combination with fetch: messages";
  private static final String MISSING = "Whether or not locales with missing messages should be fetched - use in" +
          " combination with keyId to retrieve missing messages for a certain key";

  @Inject
  public LocalesApi(Injector injector, CacheService cache,
                    AuthProvider authProvider, LocaleApiService localeApiService) {
    super(injector, cache, authProvider, localeApiService);
  }

  @ApiOperation(value = FIND,
          authorizations = @Authorization(value = AUTHORIZATION,
                  scopes = {
                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
                          @AuthorizationScope(scope = LOCALE_READ, description = LOCALE_READ_DESCRIPTION)}))
  @ApiResponses({
          @ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.LocalesPaged.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
                  dataType = "string", paramType = "query"),
          @ApiImplicitParam(name = PARAM_LOCALE_NAME, value = LOCALE_NAME, dataType = "string",
                  paramType = "query"),
          @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
                  paramType = "query"),
          @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
          @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query"),
          @ApiImplicitParam(name = PARAM_FETCH, value = FETCH, dataType = "string",
                  paramType = "query"),
          @ApiImplicitParam(name = PARAM_MESSAGES_KEY_NAME, value = MESSAGES_KEY_NAME,
                  dataType = "string", paramType = "query"),
          @ApiImplicitParam(name = PARAM_KEY_ID, value = KEY_ID,
                  dataType = "string", paramType = "query"),
          @ApiImplicitParam(name = PARAM_MISSING, value = MISSING,
                  dataType = "string", paramType = "query")})
  public CompletionStage<Result> find(@ApiParam(value = "The project ID") UUID projectId) {
    return toJsons(() -> api.find(
            LocaleCriteria.from(request()).withProjectId(projectId),
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
                          @AuthorizationScope(scope = LOCALE_READ, description = LOCALE_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.Locale.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> get(@ApiParam(value = LOCALE_ID) UUID id) {
    return toJson(() -> api.get(id));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = GET,
          authorizations = @Authorization(value = AUTHORIZATION,
                  scopes = {
                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
                          @AuthorizationScope(scope = LOCALE_READ, description = LOCALE_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.Locale.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> byOwnerAndProjectNameAndName(
          @ApiParam(value = USER_USERNAME) String username,
          @ApiParam(value = PROJECT_NAME) String projectName,
          @ApiParam(value = LOCALE_NAME) String localeName,
          @ApiParam(value = FETCH) String fetch) {
    return locale(
            username,
            projectName,
            localeName,
            locale -> ok(Optional.ofNullable(locale).map(Json::toJson).orElse(NullNode.getInstance())),
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
                          @AuthorizationScope(scope = LOCALE_WRITE, description = LOCALE_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = CREATE_RESPONSE, response = dto.Locale.class),
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
                          @AuthorizationScope(scope = LOCALE_WRITE, description = LOCALE_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = UPDATE_RESPONSE, response = dto.Locale.class),
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
                          @AuthorizationScope(scope = LOCALE_WRITE, description = LOCALE_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = DELETE_RESPONSE, response = dto.Project.class),
          @ApiResponse(code = 403, message = INPUT_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> delete(@ApiParam(value = LOCALE_ID) UUID id) {
    return toJson(() -> api.delete(id));
  }

  @ApiOperation(value = UPLOAD, authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {@AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
                  @AuthorizationScope(scope = LOCALE_READ, description = LOCALE_READ_DESCRIPTION),
                  @AuthorizationScope(scope = MESSAGE_WRITE, description = MESSAGE_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = UPLOAD_RESPONSE, response = dto.Locale.class),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> upload(@ApiParam(value = LOCALE_ID) UUID localeId) {
    return toJson(() -> api.upload(localeId, request()));
  }

  @ApiOperation(value = DOWNLOAD, produces = "text/plain", authorizations = @Authorization(
          value = AUTHORIZATION,
          scopes = {@AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
                  @AuthorizationScope(scope = LOCALE_READ, description = LOCALE_READ_DESCRIPTION),
                  @AuthorizationScope(scope = MESSAGE_READ, description = MESSAGE_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = DOWNLOAD_RESPONSE),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> download(UUID localeId, String fileType) {
    return CompletableFuture
            .supplyAsync(() -> api.download(localeId, fileType, response()),
                    executionContext.current())
            .thenApply(data -> ok(new ByteArrayInputStream(data))).exceptionally(this::handleException);
  }

  @ApiOperation(value = DOWNLOAD, produces = "text/plain", authorizations = @Authorization(
          value = AUTHORIZATION,
          scopes = {@AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
                  @AuthorizationScope(scope = LOCALE_READ, description = LOCALE_READ_DESCRIPTION),
                  @AuthorizationScope(scope = MESSAGE_READ, description = MESSAGE_READ_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = DOWNLOAD_RESPONSE),
          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
          @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> downloadBy(String username, String projectName, String localeName,
                                            String fileType) {
    return locale(
            username,
            projectName,
            localeName,
            locale -> ok(
                    new ByteArrayInputStream(api.download(locale.id, fileType, response())))
    ).exceptionally(this::handleException);
  }

  private CompletionStage<Result> locale(String username, String projectName, String localeName,
                                         Function<Locale, Result> processor, String... fetches) {
    return tryCatch(() -> {
      Locale locale = api.byOwnerAndProjectAndName(username, projectName, localeName, fetches);

      checkProjectRole(locale.projectId, authProvider.loggedInUser(), ProjectRole.Owner,
              ProjectRole.Manager, ProjectRole.Developer, ProjectRole.Translator);

      return processor.apply(locale);
    });
  }
}
