package actions;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import dto.PermissionException;
import models.AccessToken;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;
import services.AccessTokenService;
import utils.ErrorUtils;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public class ApiAction extends Action.Simple {
  private static final String ACCESS_TOKEN_PARAM = "access_token";

  private final AccessTokenService accessTokenService;

  /**
   * 
   */
  @Inject
  public ApiAction(AccessTokenService accessTokenService) {
    this.accessTokenService = accessTokenService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CompletionStage<Result> call(Context ctx) {
    Request req = ctx.request();

    String accessToken = null;
    switch (req.method()) {
      case "POST":
      case "PUT":
        JsonNode json = req.body().asJson();
        Map<String, String[]> form = req.body().asFormUrlEncoded();
        if (json != null && json.hasNonNull(ACCESS_TOKEN_PARAM))
          accessToken = json.get(ACCESS_TOKEN_PARAM).asText();
        else if (form != null && form.containsKey(ACCESS_TOKEN_PARAM))
          accessToken = form.get(ACCESS_TOKEN_PARAM)[0];
        else
          accessToken = req.getQueryString(ACCESS_TOKEN_PARAM);
        break;
      case "GET":
      case "DELETE":
        accessToken = req.getQueryString(ACCESS_TOKEN_PARAM);
        break;
      default:
        break;
    }

    if (accessToken != null) {
      AccessToken token = accessTokenService.byKey(accessToken);
      if (token == null)
        return CompletableFuture.completedFuture(
            forbidden(ErrorUtils.toJson(new PermissionException("Invalid access_token"))));

      ctx.args.put("accessToken", token);
    }

    return delegate.call(ctx);
  }
}
