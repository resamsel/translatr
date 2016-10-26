package commands;

import javax.inject.Inject;

import controllers.routes;
import dto.AccessToken;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.AccessTokenService;

public class RevertDeleteAccessTokenCommand implements Command<models.AccessToken> {
  private AccessToken accessToken;

  private final AccessTokenService accessTokenService;

  /**
   * 
   */
  @Inject
  public RevertDeleteAccessTokenCommand(AccessTokenService accessTokenService) {
    this.accessTokenService = accessTokenService;
  }

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
  public void execute() {
    models.AccessToken model = accessToken.toModel();
    accessTokenService.save(model);
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
}
