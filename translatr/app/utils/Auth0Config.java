package utils;

import javax.inject.Inject;
import javax.inject.Singleton;

import play.Application;
import play.Configuration;
import play.inject.Injector;

/**
 * @author resamsel
 * @version 11 Jan 2017
 */
@Singleton
public class Auth0Config {
  public String clientId;
  public String clientSecret;
  public String callbackURL;
  public String domain;

  @Inject
  public Auth0Config(Injector injector) {
    Configuration configuration = injector.instanceOf(Configuration.class);
    clientId = configuration.getString("auth0.clientId");
    clientSecret = configuration.getString("auth0.clientSecret");
    callbackURL = configuration.getString("auth0.callbackURL");
    domain = configuration.getString("auth0.domain");
  }
}
