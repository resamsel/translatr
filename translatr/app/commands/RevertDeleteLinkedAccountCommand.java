package commands;

import javax.inject.Inject;

import controllers.routes;
import dto.LinkedAccount;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.LinkedAccountService;

public class RevertDeleteLinkedAccountCommand implements Command<models.LinkedAccount>
{
	private LinkedAccount linkedAccount;

	private final LinkedAccountService linkedAccountService;

	/**
	 * 
	 */
	@Inject
	public RevertDeleteLinkedAccountCommand(LinkedAccountService linkedAccountService)
	{
		this.linkedAccountService = linkedAccountService;
	}

	/**
	 * @param key
	 * @return
	 */
	@Override
	public RevertDeleteLinkedAccountCommand with(models.LinkedAccount linkedAccount)
	{
		this.linkedAccount = LinkedAccount.from(linkedAccount);
		return this;
	}

	@Override
	public void execute()
	{
		models.LinkedAccount model = linkedAccount.toModel();
		linkedAccountService.save(model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage()
	{
		return Context.current().messages().at("linkedAccount.deleted", linkedAccount.providerKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Call redirect()
	{
		return routes.Users.linkedAccounts(linkedAccount.userId);
	}
}
