package utils;

import play.Configuration;

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

  StreamIOSecret("translatr.stream.io.secret");

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

  public String getString(Configuration config) {
    return config.getString(key);
  }

  public Boolean getBoolean(Configuration config) {
    return config.getBoolean(key);
  }
}
