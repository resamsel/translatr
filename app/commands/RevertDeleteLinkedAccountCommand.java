package commands;

import controllers.routes;
import dto.LinkedAccount;
import play.inject.Injector;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.LinkedAccountService;

public class RevertDeleteLinkedAccountCommand implements Command<models.LinkedAccount> {
  private static final long serialVersionUID = 7856521291080668659L;

  private LinkedAccount linkedAccount;

  /**
   * @param key
   * @return
   */
  @Override
  public RevertDeleteLinkedAccountCommand with(models.LinkedAccount linkedAccount) {
    this.linkedAccount = LinkedAccount.from(linkedAccount);
    return this;
  }

  @Override
  public void execute(Injector injector) {
    injector.instanceOf(LinkedAccountService.class).update(linkedAccount.toModel());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMessage() {
    return Context.current().messages().at("linkedAccount.deleted", linkedAccount.providerKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Call redirect() {
    return routes.Profiles.linkedAccounts();
  }

  /**
   * @param linkedAccount
   * @return
   */
  public static RevertDeleteLinkedAccountCommand from(models.LinkedAccount linkedAccount) {
    return new RevertDeleteLinkedAccountCommand().with(linkedAccount);
  }
}
