package controllers;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Result;
import services.api.StatisticApiService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

@OpenAPIDefinition(info = @Info(title = "Statistics", version = "1.0"))
public class StatisticsApi extends AbstractBaseApi {
  private static final String FIND = "Find statistics";
  private static final String FIND_RESPONSE = "Found statistics";

  public static final String PARAM_TYPES = "types";

  private final StatisticApiService statisticApiService;

  @Inject
  protected StatisticsApi(Injector injector, StatisticApiService statisticApiService) {
    super(injector);
    this.statisticApiService = statisticApiService;
  }

//  @Operation(summary = FIND,parameters = {
//          @Parameter(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
//          dataType = "string", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
//                  paramType = "query"),
//          @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_TYPES, value = TYPES, dataType = "string",
//                  paramType = "query")
//  }
//          authorizations = @Authorization(value = AUTHORIZATION,
//          scopes = @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION)))
//  @ApiResponses({@ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.ActivitiesPaged.class),
//          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
//          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
//  @ApiImplicitParams({
//          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
//                  dataType = "string", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
//                  paramType = "query"),
//          @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_TYPES, value = TYPES, dataType = "string",
//                  paramType = "query")})
  public CompletionStage<Result> find(Http.Request request) {
    return toJson(() -> statisticApiService.find(request));
  }
}
