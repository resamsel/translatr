package services.impl;

import static utils.Stopwatch.log;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;

import models.ActionType;
import models.LinkedAccount;
import models.LogEntry;
import models.User;
import models.UserStats;
import play.Configuration;
import play.cache.CacheApi;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	private final CacheApi cache;

	private final LogEntryService logEntryService;

	private final LinkedAccountService linkedAccountService;

	/**
	 * @param configuration
	 */
	@Inject
	public UserServiceImpl(Configuration configuration, CacheApi cache, LinkedAccountService linkedAccountService,
				LogEntryService logEntryService)
	{
		super(configuration);
		this.cache = cache;
		this.linkedAccountService = linkedAccountService;
		this.logEntryService = logEntryService;
	}

	@Override
	public User create(final AuthUserIdentity authUser)
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
			if(!"null".equals(identity.getEmail()))
			{
				user.email = identity.getEmail();
				user.emailValidated = false;
			}
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
	public User addLinkedAccount(final AuthUserIdentity oldUser, final AuthUserIdentity newUser)
	{
		final User u = getLocalUser(oldUser);
		u.linkedAccounts.add(LinkedAccount.createFrom(newUser));
		return save(u);
	}

	@Override
	public User getLocalUser(final AuthUserIdentity authUser)
	{
		return log(
			() -> cache.getOrElse(
				String.format("%s:%s", authUser.getProvider(), authUser.getId()),
				() -> User.findByAuthUserIdentity(authUser),
				10 * 60),
			LOGGER,
			"getLocalUser");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLocalUser(AuthUserIdentity authUser)
	{
		return getLocalUser(authUser) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void logout(AuthUserIdentity authUser)
	{
		cache.remove(String.format("%s:%s", authUser.getProvider(), authUser.getId()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preSave(User t, boolean update)
	{
		if(t.email != null)
			t.email = t.email.toLowerCase();
		if(t.username == null && t.email != null)
			t.username = emailToUsername(t.email);
		if(update)
			logEntryService.save(LogEntry.from(ActionType.Update, null, dto.User.class, toDto(User.byId(t.id)), toDto(t)));
	}

	/**
	 * @param email
	 * @return
	 */
	private String emailToUsername(String email)
	{
		String username = email.toLowerCase().replaceAll("@", "").replaceAll("\\.", "");

		// TODO: potentially slow, replace with better variant (get all users with username like $username% and iterate
		// over them)
		String suffix = "";
		int retries = 10, i = 0;
		while(User.byUsername(String.format("%s%s", username, suffix)) != null && i++ < retries)
			suffix = Integer.toString(i);

		return String.format("%s%s", username, suffix);
	}

	@Override
	public User merge(final AuthUserIdentity oldUser, final AuthUserIdentity newUser)
	{
		return merge(getLocalUser(oldUser), getLocalUser(newUser));
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getByUsername(String username)
	{
		return log(
			() -> cache.getOrElse(String.format("username:%s", username), () -> User.byUsernameUncached(username), 60),
			LOGGER,
			"Retrieving user by username");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserStats getUserStats(UUID userId)
	{
		return log(
			() -> cache.getOrElse(String.format("user:stats:%s", userId), () -> User.userStatsUncached(userId), 60),
			LOGGER,
			"getUserStats");
	}

	protected dto.User toDto(User t)
	{
		dto.User out = dto.User.from(t);

		return out;
	}
}
