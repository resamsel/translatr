package controllers;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import commands.RevertDeleteLinkedAccountCommand;
import criterias.LinkedAccountCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import forms.SearchForm;
import forms.UserForm;
import models.LinkedAccount;
import models.LogEntry;
import models.Project;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.LinkedAccountService;
import services.UserService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 26 Sep 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Users extends AbstractController
{
	private final FormFactory formFactory;

	private final Configuration configuration;

	private final LinkedAccountService linkedAccountService;

	/**
	 * @param auth
	 * 
	 */
	@Inject
	public Users(Injector injector, CacheApi cache, FormFactory formFactory, Configuration configuration,
				PlayAuthenticate auth, UserService userService, LinkedAccountService linkedAccountService)
	{
		super(injector, cache, auth, userService);

		this.formFactory = formFactory;
		this.configuration = configuration;
		this.linkedAccountService = linkedAccountService;
	}

	public Result user(UUID id)
	{
		User user = User.byId(id);
		if(user == null)
			return redirect(routes.Application.index());

		return ok(
			views.html.users.user
				.render(createTemplate(), user, Project.findBy(new ProjectCriteria().withOwnerId(id).withOrder("name"))));
	}

	public Result activity(UUID id)
	{
		User user = User.byId(id);
		if(user == null)
			return redirect(routes.Application.index());

		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
		SearchForm search = form.get();

		List<LogEntry> activities =
					LogEntry.findBy(LogEntryCriteria.from(search).withUserId(id).withOrder("whenCreated desc"));

		search.pager(activities);

		return ok(views.html.users.activity.render(createTemplate(), user, activities, form));
	}

	public Result linkedAccounts(UUID id)
	{
		User user = User.byId(id);
		if(user == null)
			return redirect(routes.Application.index());

		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
		SearchForm search = form.get();

		List<LinkedAccount> accounts =
					LinkedAccount.findBy(LinkedAccountCriteria.from(search).withUserId(id).withOrder("providerKey"));

		search.pager(accounts);

		return ok(
			views.html.users.linkedAccounts.render(
				createTemplate(),
				user,
				accounts.stream().collect(groupingBy(a -> a.providerKey, reducing(null, a -> a, (a, b) -> b))),
				form));
	}

	public Result edit(UUID userId)
	{
		User user = User.byId(userId);

		if(user == null)
			return redirect(routes.Application.index());

		if("POST".equals(request().method()))
		{
			Form<UserForm> form = formFactory.form(UserForm.class).bindFromRequest();

			if(form.hasErrors())
				return badRequest(views.html.users.edit.render(createTemplate(), user, form));

			userService.save(form.get().into(user));

			return redirect(routes.Users.user(user.id));
		}

		return ok(
			views.html.users.edit
				.render(createTemplate(), user, formFactory.form(UserForm.class).fill(UserForm.from(user))));
	}

	public Result linkedAccountRemove(UUID userId, Long linkedAccountId)
	{
		LinkedAccount linkedAccount = LinkedAccount.byId(linkedAccountId);

		if(linkedAccount == null || !userId.equals(linkedAccount.user.id))
			return redirect(routes.Users.linkedAccounts(userId));

		undoCommand(injector.instanceOf(RevertDeleteLinkedAccountCommand.class).with(linkedAccount));

		linkedAccountService.delete(linkedAccount);

		return redirect(routes.Users.linkedAccounts(userId));
	}
}
