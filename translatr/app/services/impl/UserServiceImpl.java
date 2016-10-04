package services.impl;

import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;

import models.ActionType;
import models.LinkedAccount;
import models.LogEntry;
import models.User;
import play.Configuration;
import services.LinkedAccountService;
import services.LogEntryService;
import services.UserService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 1 Oct 2016
 */
@Singleton
public class UserServiceImpl extends AbstractModelService<User> implements UserService
{
	private final LogEntryService logEntryService;

	private final LinkedAccountService linkedAccountService;

	/**
	 * @param configuration
	 */
	@Inject
	public UserServiceImpl(Configuration configuration, LinkedAccountService linkedAccountService,
				LogEntryService logEntryService)
	{
		super(configuration);
		this.linkedAccountService = linkedAccountService;
		this.logEntryService = logEntryService;
	}

	@Override
	public User create(final AuthUser authUser)
	{
		final User user = new User();
		user.active = true;
		user.linkedAccounts = Collections.singletonList(LinkedAccount.createFrom(authUser));

		if(authUser instanceof EmailIdentity)
		{
			final EmailIdentity identity = (EmailIdentity)authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			user.email = identity.getEmail();
			user.emailValidated = false;
		}

		if(authUser instanceof NameIdentity)
		{
			final NameIdentity identity = (NameIdentity)authUser;
			final String name = identity.getName();
			if(name != null)
				user.name = name;
		}

		return save(user);
	}

	@Override
	public User addLinkedAccount(final AuthUser oldUser, final AuthUser newUser)
	{
		final User u = User.findByAuthUserIdentity(oldUser);
		u.linkedAccounts.add(LinkedAccount.createFrom(newUser));
		return save(u);
	}

	@Override
	public User getLocalUser(final AuthUser authUser)
	{
		return User.findByAuthUserIdentity(authUser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preSave(User t, boolean update)
	{
		if(t.email != null)
			t.email = t.email.toLowerCase();
		if(update)
			logEntryService.save(LogEntry.from(ActionType.Update, null, dto.User.class, toDto(User.byId(t.id)), toDto(t)));
	}

	@Override
	public User merge(final AuthUser oldUser, final AuthUser newUser)
	{
		return merge(User.findByAuthUserIdentity(oldUser), User.findByAuthUserIdentity(newUser));
	}

	@Override
	public User merge(final User user, final User otherUser)
	{
		for(final LinkedAccount acc : otherUser.linkedAccounts)
			user.linkedAccounts.add(linkedAccountService.create(acc));
		// do all other merging stuff here - like resources, etc.

		// deactivate the merged user that got added to this one
		otherUser.active = false;
		save(Arrays.asList(new User[]{otherUser, user}));

		return user;
	}

	protected dto.User toDto(User t)
	{
		dto.User out = dto.User.from(t);

		return out;
	}
}
