package utils;

import com.typesafe.config.Config;

import java.util.List;

/**
 * @author resamsel
 * @version 30 Sep 2016
 */
public enum ConfigKey {
  /* Whether or not to force SSL */
  ForceSSL("translatr.forceSSL"),

  /* The list of available auth providers */
  AuthProviders("translatr.auth.providers"),

  StreamIOKey("translatr.stream.io.key"),

  StreamIOSecret("translatr.stream.io.secret"),

  BaseUrl("translatr.baseUrl"),
  RedirectBase("translatr.redirectBase"),

  Pac4jSecurityExcludePaths("pac4j.security.excludePaths"),

  GoogleClientId("pac4j.clients.google.id"),
  GoogleClientSecret("pac4j.clients.google.secret"),

  KeycloakClientId("pac4j.clients.keycloak.id"),
  KeycloakClientSecret("pac4j.clients.keycloak.secret"),
  KeycloakBaseUri("pac4j.clients.keycloak.baseUri"),
  KeycloakRealm("pac4j.clients.keycloak.realm"),

  GitHubClientId("pac4j.clients.github.id"),
  GitHubClientSecret("pac4j.clients.github.secret"),

  TwitterClientId("pac4j.clients.twitter.id"),
  TwitterClientSecret("pac4j.clients.twitter.secret"),

  FacebookClientId("pac4j.clients.facebook.id"),
  FacebookClientSecret("pac4j.clients.facebook.secret");

  private String key;

  ConfigKey(String key) {
    this.key = key;
  }

  public String key() {
    return key;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return key;
  }

  public boolean existsIn(Config config) {
    return config.hasPath(key);
  }

  public String get(Config config) {
    return config.getString(key);
  }

  public String getOrDefault(Config config, String defaultValue) {
    if (!existsIn(config)) {
      return defaultValue;
    }

    return config.getString(key);
  }

  public Boolean getBoolean(Config config) {
    return config.getBoolean(key);
  }

  public List<String> getStringList(Config config) {
    return config.getStringList(key);
  }
}
