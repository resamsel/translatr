package services.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.service.AbstractUserService;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;

import models.User;
import services.UserService;

@Singleton
public class AuthenticateServiceImpl extends AbstractUserService
{
	private final UserService userService;

	@Inject
	public AuthenticateServiceImpl(final PlayAuthenticate auth, final UserService userService)
	{
		super(auth);
		this.userService = userService;
	}

	@Override
	public Object save(final AuthUser authUser)
	{
		final boolean isLinked = User.existsByAuthUserIdentity(authUser);
		if(!isLinked)
		{
			return userService.create(authUser).id;
		}
		else
		{
			// we have this user already, so return null
			return null;
		}
	}

	@Override
	public Object getLocalIdentity(final AuthUserIdentity identity)
	{
		// For production: Caching might be a good idea here...
		// ...and dont forget to sync the cache when users get deactivated/deleted
		final User u = User.findByAuthUserIdentity(identity);
		if(u != null)
		{
			return u.id;
		}
		else
		{
			return null;
		}
	}

	@Override
	public AuthUser merge(final AuthUser newUser, final AuthUser oldUser)
	{
		if(!oldUser.equals(newUser))
			User.merge(oldUser, newUser);

		return oldUser;
	}

	@Override
	public AuthUser link(final AuthUser oldUser, final AuthUser newUser)
	{
		userService.addLinkedAccount(oldUser, newUser);
		return oldUser;
	}
}
