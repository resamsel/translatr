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
public interface UserService extends ModelService<User>
{
	/**
	 * @param user
	 * @return
	 */
	User create(AuthUser user);

	/**
	 * @param user
	 * @param otherUser
	 * @return
	 */
	User addLinkedAccount(AuthUser user, AuthUser otherUser);

	/**
	 * @param user
	 */
	User getLocalUser(AuthUser user);

	/**
	 * @param user
	 * @param otherUser
	 */
	User merge(AuthUser user, AuthUser otherUser);

	/**
	 * @param user
	 * @param otherUser
	 * @return
	 */
	User merge(User user, User otherUser);
}
