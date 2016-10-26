package services.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.LinkedAccount;
import play.Configuration;
import services.LinkedAccountService;
import services.LogEntryService;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 2 Oct 2016
 */
@Singleton
public class LinkedAccountServiceImpl extends AbstractModelService<LinkedAccount> implements LinkedAccountService
{
	/**
	 * @param configuration
	 */
	@Inject
	public LinkedAccountServiceImpl(Configuration configuration, LogEntryService logEntryService)
	{
		super(configuration, logEntryService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LinkedAccount create(LinkedAccount linkedAccount)
	{
		final LinkedAccount ret = new LinkedAccount();

		ret.providerKey = linkedAccount.providerKey;
		ret.providerUserId = linkedAccount.providerUserId;

		return ret;
	}
}
