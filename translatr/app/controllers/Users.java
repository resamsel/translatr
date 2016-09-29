package controllers;

import java.util.UUID;

import javax.inject.Inject;

import actions.ContextAction;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import play.data.FormFactory;
import play.mvc.Result;
import play.mvc.With;

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
	private final FormFactory formFactory;

	private final Configuration configuration;

	/**
	 * 
	 */
	@Inject
	public Users(CacheApi cache, FormFactory formFactory, Configuration configuration)
	{
		super(cache);

		this.formFactory = formFactory;
		this.configuration = configuration;
	}

	public Result user(UUID id)
	{
		return ok(views.html.users.user.render(User.byId(id)));
	}
}
