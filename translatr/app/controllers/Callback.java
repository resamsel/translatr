package controllers;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.auth0.Auth0Client;
import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;

import play.api.libs.json.JsValue;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http.HeaderNames;
import play.mvc.Http.MimeTypes;
import play.mvc.Result;
import services.LogEntryService;
import services.UserService;
import utils.Auth0Config;

/**
 * @author resamsel
 * @version 11 Jan 2017
 */
@Singleton
public class Callback extends AbstractController {
  private final Auth0Config config;
  private final WSClient wsClient;

  /**
   * 
   */
  @Inject
  public Callback(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService, Auth0Config config, WSClient wsClient) {
    super(injector, cache, auth, userService, logEntryService);
    this.config = config;
    this.wsClient = wsClient;
  }

  public Result index() {
    return ok(views.html.auth.index.render(createTemplate(), config));
  }

  public Result callback() {
    JsonNode json = getToken(request().getQueryString("code"));

    String idToken = json.get("id_token").asText();
    String accessToken = json.get("access_token").asText();

    JsonNode user = getUser(accessToken);

    cache.set("profile:" + idToken, user);
    session("idToken", idToken);
    session("accessToken", accessToken);

    return redirect(routes.Callback.user());
  }

  public Result user() {
    JsonNode user = cache.get("profile:" + session("idToken"));
    return ok(views.html.auth.user.render(createTemplate(), user));
  }

  public JsonNode getToken(String code) {
    CompletionStage<WSResponse> response =
        wsClient.url(String.format("https://%s/oauth/token", config.domain))
            .setHeader(HeaderNames.ACCEPT, MimeTypes.JSON)
            .post(Json.newObject().put("client_id", config.clientId)
                .put("client_secret", config.clientSecret).put("redirect_uri", config.callbackURL)
                .put("code", code).put("grant_type", "authorization_code"));

    try {
      return response.toCompletableFuture().get(3000, TimeUnit.MILLISECONDS).asJson();
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      return null;
    }
  }

  public JsonNode getUser(String accessToken) {
    CompletionStage<WSResponse> response =
        wsClient.url(String.format("https://%s/userinfo", config.domain))
            .setQueryParameter("access_token", accessToken).get();

    try {
      return response.toCompletableFuture().get(3000, TimeUnit.MILLISECONDS).asJson();
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      return null;
    }
  }
}
