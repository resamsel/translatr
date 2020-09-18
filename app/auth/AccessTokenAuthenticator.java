package auth;

import models.AccessToken;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import play.inject.Injector;
import services.AccessTokenService;

import javax.inject.Inject;
import javax.inject.Singleton;

public class AccessTokenAuthenticator implements Authenticator<TokenCredentials> {

  private final Injector injector;
  private final String clientName;
  private AccessTokenService accessTokenService;

  public AccessTokenAuthenticator(Injector injector, String clientName) {
    this.injector = injector;
    this.clientName = clientName;
  }

  @Override
  public void validate(TokenCredentials credentials, WebContext context) {
    init();

    AccessToken accessToken = accessTokenService.byKey(credentials.getToken());

    if (accessToken == null) {
      throw new CredentialsException("Could not validate access token");
    }

    AccessTokenProfile profile = new AccessTokenProfile();
    profile.setClientName(clientName);
    profile.setId(credentials.getToken());
    profile.setScope(accessToken.scope);
    credentials.setUserProfile(profile);
  }

  private void init() {
    if (accessTokenService == null) {
      synchronized (this) {
        if (accessTokenService == null) {
          accessTokenService = injector.instanceOf(AccessTokenService.class);
        }
      }
    }
  }
}
