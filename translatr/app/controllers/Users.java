package controllers;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import actions.ContextAction;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import forms.SearchForm;
import models.LogEntry;
import models.Project;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import play.data.Form;
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
		return ok(
			views.html.users.user
				.render(User.byId(id), Project.findBy(new ProjectCriteria().withOwnerId(id).withOrder("name"))));
	}

	public Result userActivity(UUID id)
	{
		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
		SearchForm search = form.get();

		List<LogEntry> activities =
					LogEntry.findBy(LogEntryCriteria.from(search).withUserId(id).withOrder("whenCreated desc"));

		search.pager(activities);

		return ok(views.html.users.userActivity.render(User.byId(id), activities, form));
	}
}
