package services;

import com.google.inject.ImplementedBy;

import models.AccessToken;
import services.impl.AccessTokenServiceImpl;

/**
 *
 * @author resamsel
 * @version 19 Oct 2016
 */
@ImplementedBy(AccessTokenServiceImpl.class)
public interface AccessTokenService extends ModelService<AccessToken>
{
	/**
	 * @param accessTokenKey
	 * @return
	 */
	AccessToken getByKey(String accessTokenKey);
}
