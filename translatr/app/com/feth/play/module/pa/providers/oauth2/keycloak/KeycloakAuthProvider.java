package com.feth.play.module.pa.providers.oauth2.keycloak;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;

import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

/**
 * @author resamsel
 * @version 4 Jan 2017
 */
@Singleton
public class KeycloakAuthProvider extends OAuth2AuthProvider<KeycloakAuthUser, KeycloakAuthInfo> {
  public static final String PROVIDER_KEY = "keycloak";

  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakAuthProvider.class);

  public static abstract class SettingKeys extends OAuth2AuthProvider.SettingKeys {
    public static final String USER_INFO_URL = "userInfoUrl";
  }

  /**
   * @param auth
   * @param lifecycle
   * @param wsClient
   */
  @Inject
  public KeycloakAuthProvider(PlayAuthenticate auth, ApplicationLifecycle lifecycle,
      WSClient wsClient) {
    super(auth, lifecycle, wsClient);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected KeycloakAuthInfo buildInfo(WSResponse r) throws AccessTokenException {
    final JsonNode n = r.asJson();
    LOGGER.debug(n.toString());

    if (n.get(OAuth2AuthProvider.Constants.ERROR) != null) {
      throw new AccessTokenException(n.get(OAuth2AuthProvider.Constants.ERROR).asText());
    } else {
      return new KeycloakAuthInfo(n);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected AuthUserIdentity transform(KeycloakAuthInfo info, String state) throws AuthException {
    final String url = getConfiguration().getString(SettingKeys.USER_INFO_URL);

    final WSResponse r = fetchAuthResponse(url, info.getAccessToken());

    final JsonNode result = r.asJson();
    if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
      throw new AuthException(result.get(OAuth2AuthProvider.Constants.ERROR).asText());
    } else {
      LOGGER.debug(result.toString());
      return new KeycloakAuthUser(result, info, state);
    }
  }

  protected WSResponse fetchAuthResponse(String url, String accessToken) throws AuthException {
    final WSRequest request = wsClient.url(url);
    request.setHeader("Authorization", "Bearer " + accessToken);

    try {
      return request.get().toCompletableFuture().get(getTimeout(), MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new AuthException(e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getKey() {
    return PROVIDER_KEY;
  }
}
