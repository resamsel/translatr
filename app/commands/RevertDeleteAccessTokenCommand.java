package commands;

import controllers.routes;
import dto.AccessToken;
import play.inject.Injector;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.AccessTokenService;

public class RevertDeleteAccessTokenCommand implements Command<models.AccessToken> {
  private static final long serialVersionUID = 3620886642910047163L;

  private AccessToken accessToken;

  /**
   * @param key
   * @return
   */
  @Override
  public RevertDeleteAccessTokenCommand with(models.AccessToken accessToken) {
    this.accessToken = AccessToken.from(accessToken);
    return this;
  }

  @Override
  public void execute(Injector injector) {
    injector.instanceOf(AccessTokenService.class).update(accessToken.toModel());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMessage() {
    return Context.current().messages().at("accessToken.deleted", accessToken.name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Call redirect() {
    return routes.Profiles.accessTokens();
  }

  /**
   * @param accessToken
   * @return
   */
  public static RevertDeleteAccessTokenCommand from(models.AccessToken accessToken) {
    return new RevertDeleteAccessTokenCommand().with(accessToken);
  }
}
