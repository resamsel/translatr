package auth;

import models.AccessToken;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;
import play.inject.Injector;
import services.AccessTokenService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AccessTokenAuthenticator implements Authenticator<TokenCredentials> {

  private final Injector injector;
  private AccessTokenService accessTokenService;
  private Object lock = new Object();

  @Inject
  public AccessTokenAuthenticator(Injector injector) {
    this.injector = injector;
  }

  @Override
  public void validate(TokenCredentials credentials, WebContext context) {
    if (accessTokenService == null) {
      synchronized (lock) {
        if (accessTokenService == null) {
          accessTokenService = injector.instanceOf(AccessTokenService.class);
        }
      }
    }

    AccessToken accessToken = accessTokenService.byKey(credentials.getToken());

    if (accessToken == null) {
      throw new CredentialsException("Could not validate access token");
    }

    CommonProfile profile = new CommonProfile();
    profile.setClientName("parameter");
    profile.setId(credentials.getToken());
    credentials.setUserProfile(profile);
  }
}
