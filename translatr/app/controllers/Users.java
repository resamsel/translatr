package controllers;

import java.util.UUID;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import criterias.ProjectCriteria;
import models.Project;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
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
	/**
	 * @param auth
	 * 
	 */
	@Inject
	public Users(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService)
	{
		super(injector, cache, auth, userService);
	}

	public Result user(UUID id)
	{
		User user = User.byId(id);
		if(user == null)
			return redirect(routes.Application.index());

		return ok(
			views.html.users.user
				.render(createTemplate(), user, Project.findBy(new ProjectCriteria().withMemberId(id).withOrder("name"))));
	}
}
