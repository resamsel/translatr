package controllers;

import criterias.NotificationCriteria;
import dto.NotificationsPaged;
import dto.PermissionException;
import io.getstream.client.exception.StreamClientException;
import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;
import io.getstream.client.model.beans.StreamResponse;
import mappers.AggregatedNotificationMapper;
import models.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Result;
import services.NotificationService;
import services.PermissionService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
//@io.swagger.annotations.Api(value = "Notifications", produces = "application/json")
public class NotificationsApi extends AbstractBaseApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsApi.class);

  private static final String FIND = "Find notifications";
  private static final String FIND_RESPONSE = "Found notifications";

  private final NotificationService notificationService;
  private final PermissionService permissionService;
  private final AggregatedNotificationMapper aggregatedNotificationMapper;

  @Inject
  public NotificationsApi(Injector injector, NotificationService notificationService, AggregatedNotificationMapper aggregatedNotificationMapper) {
    super(injector);

    this.notificationService = notificationService;
    this.permissionService = injector.instanceOf(PermissionService.class);
    this.aggregatedNotificationMapper = aggregatedNotificationMapper;
  }

//  @ApiOperation(value = FIND,
//          authorizations = @Authorization(value = AUTHORIZATION,
//                  scopes = {
//                          @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
//                          @AuthorizationScope(scope = MESSAGE_READ, description = MESSAGE_READ_DESCRIPTION)}))
//  @ApiResponses({
//          @ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.NotificationsPaged.class),
//          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
//          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
//  @ApiImplicitParams({
//          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
//                  dataType = "string", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query")})
  public CompletionStage<Result> find(Http.Request request) {
    StreamResponse<AggregatedActivity<SimpleActivity>> notifications;
    try {
      permissionService.checkPermissionAll(request, "Access token not allowed", Scope.NotificationRead);

      notifications = notificationService.find(NotificationCriteria.from(request));
    } catch (IOException | StreamClientException | PermissionException e) {
      LOGGER.error("Error while retrieving notifications", e);
      return CompletableFuture.completedFuture(handleException(e));
    }


    return toJsons(() -> new NotificationsPaged(notifications != null ? aggregatedNotificationMapper.toDto(notifications.getResults(), request) : null));
  }
}
