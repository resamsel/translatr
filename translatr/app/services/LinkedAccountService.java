package services;

import com.google.inject.ImplementedBy;

import models.LinkedAccount;
import services.impl.LinkedAccountServiceImpl;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(LinkedAccountServiceImpl.class)
public interface LinkedAccountService extends ModelService<LinkedAccount>
{
	/**
	 * @param linkedAccount
	 * @return
	 */
	LinkedAccount create(LinkedAccount linkedAccount);
}
