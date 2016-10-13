package controllers;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import criterias.LogEntryCriteria;
import models.Aggregate;
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
		return user(id, user -> {
			if(user.id.equals(User.loggedInUserId()))
				return redirect(routes.Profiles.profile());

			return ok(views.html.users.user.render(createTemplate(), user));
		});
	}

	public Result activityCsv(UUID userId)
	{
		return user(userId, user -> {
			List<Aggregate> activity = logEntryService.getAggregates(new LogEntryCriteria().withUserId(user.id));

			int max = activity.stream().mapToInt(a -> a.value).reduce(0, Math::max);

			String csv = "Date,Value\n" + activity
				.stream()
				.map(a -> String.format("%s,%.2f\n", a.date.toString("yyyy-MM-dd"), Math.sqrt(a.value) / Math.sqrt(max)))
				.reduce("", (a, b) -> a.concat(b));
			return ok(csv);
		});
	}

	/**
	 * @param userId
	 * @param object
	 * @return
	 */
	private Result user(UUID userId, Function<User, Result> processor)
	{
		User user = User.byId(userId);
		if(user == null)
			return redirectWithError(routes.Application.index(), ctx().messages().at("user.notFound"));

		return processor.apply(user);
	}
}
