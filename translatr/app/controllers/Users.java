package controllers;

import java.util.UUID;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import criterias.LogEntryCriteria;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.LogEntryService;
import services.UserService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 26 Sep 2016
 */
@With(ContextAction.class)
public class Users extends AbstractController
{
	private final LogEntryService logEntryService;

	/**
	 * @param auth
	 * 
	 */
	@Inject
	public Users(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
				LogEntryService logEntryService)
	{
		super(injector, cache, auth, userService);
		this.logEntryService = logEntryService;
	}

	public Result user(UUID id)
	{
		User user = User.byId(id);
		if(user == null)
			return redirectWithError(routes.Application.index(), ctx().messages().at("user.notFound"));

		if(user.id.equals(User.loggedInUserId()))
			return redirect(routes.Profiles.profile());

		return ok(
			views.html.users.user
				.render(createTemplate(), user, logEntryService.getStats(new LogEntryCriteria().withUserId(user.id))));
	}
}
