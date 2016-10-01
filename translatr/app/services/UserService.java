package services;

import com.feth.play.module.pa.user.AuthUser;
import com.google.inject.ImplementedBy;

import models.User;
import services.impl.UserServiceImpl;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 1 Oct 2016
 */
@ImplementedBy(UserServiceImpl.class)
public interface UserService
{
	/**
	 * @param oldUser
	 * @param newUser
	 * @return
	 */
	User addLinkedAccount(AuthUser oldUser, AuthUser newUser);

	/**
	 * @param authUser
	 * @return
	 */
	User create(AuthUser authUser);

	/**
	 * @param user
	 */
	User getLocalUser(AuthUser user);
}
