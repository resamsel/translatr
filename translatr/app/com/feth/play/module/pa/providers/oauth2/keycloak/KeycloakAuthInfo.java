package com.feth.play.module.pa.providers.oauth2.keycloak;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.Constants;

/**
 * @author resamsel
 * @version 4 Jan 2017
 */
public class KeycloakAuthInfo extends OAuth2AuthInfo {
  private static final long serialVersionUID = -9078177253298858910L;

  /**
   * @param token
   */
  public KeycloakAuthInfo(JsonNode node) {
    super(
        node.get(Constants.ACCESS_TOKEN) != null ? node.get(Constants.ACCESS_TOKEN).asText() : null,
        node.get(Constants.EXPIRES_IN) != null
            ? new Date().getTime() + node.get(Constants.EXPIRES_IN).asLong() * 1000 : -1,
        node.get(Constants.REFRESH_TOKEN) != null ? node.get(Constants.REFRESH_TOKEN).asText()
            : null);
  }
}
