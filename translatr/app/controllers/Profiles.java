package controllers;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import commands.RevertDeleteLinkedAccountCommand;
import criterias.LinkedAccountCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import forms.Accept;
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
import services.LogEntryService;
import services.UserService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 6 Oct 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Profiles extends AbstractController
{
	private final FormFactory formFactory;

	private final Configuration configuration;

	private final LinkedAccountService linkedAccountService;

	private final LogEntryService logEntryService;

	/**
	 * @param injector
	 * @param cache
	 * @param auth
	 * @param userService
	 */
	@Inject
	public Profiles(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
				FormFactory formFactory, Configuration configuration, LinkedAccountService linkedAccountService,
				LogEntryService logEntryService)
	{
		super(injector, cache, auth, userService);
		this.formFactory = formFactory;
		this.configuration = configuration;
		this.linkedAccountService = linkedAccountService;
		this.logEntryService = logEntryService;
	}

	public Result profile()
	{
		return loggedInUser(
			user -> ok(
				views.html.users.user.render(
					createTemplate(),
					user,
					logEntryService.getAggregates(new LogEntryCriteria().withUserId(user.id)))));
	}

	public Result projects()
	{
		return loggedInUser(
			user -> ok(
				views.html.users.projects.render(
					createTemplate(),
					user,
					Project.findBy(new ProjectCriteria().withMemberId(user.id).withOrder("name")))));
	}

	public Result activity()
	{
		return loggedInUser(user -> {
			Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
			SearchForm search = form.get();

			List<LogEntry> activities =
						LogEntry.findBy(LogEntryCriteria.from(search).withUserId(user.id).withOrder("whenCreated desc"));

			search.pager(activities);

			return ok(views.html.users.activity.render(createTemplate(), user, activities, form));
		});
	}

	public Result edit()
	{
		return loggedInUser(user -> {
			return ok(
				views.html.users.edit
					.render(createTemplate(), user, formFactory.form(UserForm.class).fill(UserForm.from(user))));
		});
	}

	public Result doEdit()
	{
		return loggedInUser(user -> {
			Form<UserForm> form = formFactory.form(UserForm.class).bindFromRequest();

			if(form.hasErrors())
				return badRequest(views.html.users.edit.render(createTemplate(), user, form));

			userService.save(form.get().into(user));

			return redirect(routes.Users.user(user.id));
		});
	}

	public Result linkedAccounts()
	{
		return loggedInUser(user -> {
			Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
			SearchForm search = form.get();

			List<LinkedAccount> accounts =
						LinkedAccount.findBy(LinkedAccountCriteria.from(search).withUserId(user.id).withOrder("providerKey"));

			search.pager(accounts);

			return ok(
				views.html.users.linkedAccounts.render(
					createTemplate(),
					user,
					accounts.stream().collect(groupingBy(a -> a.providerKey, reducing(null, a -> a, (a, b) -> b))),
					form));
		});
	}

	public Result linkedAccountRemove(Long linkedAccountId)
	{
		return loggedInUser(user -> {
			LinkedAccount linkedAccount = LinkedAccount.byId(linkedAccountId);

			if(linkedAccount == null || !user.id.equals(linkedAccount.user.id))
				return redirect(routes.Profiles.linkedAccounts());

			undoCommand(injector.instanceOf(RevertDeleteLinkedAccountCommand.class).with(linkedAccount));

			linkedAccountService.delete(linkedAccount);

			return redirect(routes.Profiles.linkedAccounts());
		});
	}

	@SubjectPresent
	public Result askLink()
	{
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());

		final AuthUser newAccount = this.auth.getLinkUser(session());
		if(newAccount == null)
		{
			// account to link could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}

		return ok(views.html.profiles.askLink.render(createTemplate(), formFactory.form(Accept.class), newAccount));
	}

	public Result doLink()
	{
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final AuthUser newAccount = auth.getLinkUser(session());
		if(newAccount == null)
		{
			// account to link could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}

		final Form<Accept> filledForm = formFactory.form(Accept.class).bindFromRequest();
		if(filledForm.hasErrors())
		{
			// User did not select whether to link or not link
			return badRequest(views.html.profiles.askLink.render(createTemplate(), filledForm, newAccount));
		}
		else
		{
			// User made a choice :)
			final boolean link = filledForm.get().accept;
			if(link)
				addMessage(ctx().messages().at("playauthenticate.accounts.link.success"));

			return auth.link(ctx(), link);
		}
	}

	@SubjectPresent
	public Result askMerge()
	{
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());

		// this is the currently logged in user
		final AuthUser aUser = auth.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = auth.getMergeUser(session());
		if(bUser == null)
			// user to merge with could not be found, silently redirect to login
			return redirect(routes.Application.index());

		// You could also get the local user object here via
		// User.findByAuthUserIdentity(newUser)
		return ok(views.html.profiles.askMerge.render(createTemplate(), formFactory.form(Accept.class), aUser, bUser));
	}

	@SubjectPresent
	public Result doMerge()
	{
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());

		// this is the currently logged in user
		final AuthUser aUser = this.auth.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = this.auth.getMergeUser(session());
		if(bUser == null)
			// user to merge with could not be found, silently redirect to login
			return redirect(routes.Application.index());

		final Form<Accept> filledForm = formFactory.form(Accept.class).bindFromRequest();
		if(filledForm.hasErrors())
		{
			// User did not select whether to merge or not merge
			return badRequest(views.html.profiles.askMerge.render(createTemplate(), filledForm, aUser, bUser));
		}
		else
		{
			// User made a choice :)
			final boolean merge = filledForm.get().accept;
			if(merge)
				addMessage(ctx().messages().at("playauthenticate.accounts.merge.success"));

			return auth.merge(ctx(), merge);
		}
	}

	private Result loggedInUser(Function<User, Result> processor)
	{
		return processor.apply(User.loggedInUser());
	}
}
