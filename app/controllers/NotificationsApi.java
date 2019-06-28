package controllers;

import actions.ApiAction;
import com.feth.play.module.pa.PlayAuthenticate;
import criterias.NotificationCriteria;
import dto.NotificationsPaged;
import dto.PermissionException;
import dto.errors.GenericError;
import dto.errors.PermissionError;
import io.getstream.client.exception.StreamClientException;
import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;
import io.getstream.client.model.beans.StreamResponse;
import io.swagger.annotations.*;
import models.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.CacheService;
import services.NotificationService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@io.swagger.annotations.Api(value = "Notifications", produces = "application/json")
@With(ApiAction.class)
public class NotificationsApi extends AbstractBaseApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsApi.class);

  private static final String FIND = "Find notifications";
  private static final String FIND_RESPONSE = "Found notifications";

  private NotificationService notificationService;

  @Inject
  public NotificationsApi(Injector injector, CacheService cache, PlayAuthenticate auth,
      NotificationService notificationService) {
    super(injector, cache, auth);

    this.notificationService = notificationService;
  }

  @ApiOperation(value = FIND,
      authorizations = @Authorization(value = AUTHORIZATION,
          scopes = {
              @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION),
              @AuthorizationScope(scope = MESSAGE_READ, description = MESSAGE_READ_DESCRIPTION)}))
  @ApiResponses({
      @ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.NotificationsPaged.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
          dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
      @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query")})
  public CompletionStage<Result> find() {
    StreamResponse<AggregatedActivity<SimpleActivity>> notifications;
    try {
      permissionService.checkPermissionAll("Access token not allowed", Scope.NotificationRead);

      notifications = notificationService.find(NotificationCriteria.from(request()));
    } catch (IOException | StreamClientException | PermissionException e) {
      LOGGER.error("Error while retrieving notifications", e);
      return CompletableFuture.completedFuture(handleException(e));
    }

    return toJsons(() -> new NotificationsPaged(notifications));
  }
}
