package controllers;

import actions.ApiAction;
import com.feth.play.module.pa.PlayAuthenticate;
import criterias.LogEntryCriteria;
import criterias.UserCriteria;
import dto.Activity;
import dto.errors.GenericError;
import dto.errors.PermissionError;
import io.swagger.annotations.*;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.CacheService;
import services.api.ActivityApiService;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@io.swagger.annotations.Api(value = "Activities", produces = "application/json")
@With(ApiAction.class)
public class ActivitiesApi extends AbstractApi<Activity, UUID, LogEntryCriteria, ActivityApiService> {
  private static final String FIND = "Find activites";
  private static final String FIND_RESPONSE = "Found activities";

  private static final String SEARCH = "Part of the contents of the activity";

  @Inject
  protected ActivitiesApi(
      Injector injector, CacheService cache, PlayAuthenticate auth, ActivityApiService activityApiService) {
    super(injector, cache, auth, activityApiService);
  }

  @ApiOperation(value = FIND, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.ActivitiesPaged.class),
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
    return toJsons(() -> api.find(LogEntryCriteria.from(request())));
  }
}
