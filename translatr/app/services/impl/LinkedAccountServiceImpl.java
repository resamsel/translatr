package services.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.LinkedAccount;
import play.Configuration;
import services.LinkedAccountService;

/**
 * (c) 2016 Skiline Media GmbH
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
	public LinkedAccountServiceImpl(Configuration configuration)
	{
		super(configuration);
	}
}
