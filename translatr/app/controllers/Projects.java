package controllers;

import static utils.FormatUtils.formatLocale;
import static utils.Stopwatch.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.collect.ImmutableMap;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import commands.RevertDeleteProjectCommand;
import commands.RevertDeleteProjectUserCommand;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectUserCriteria;
import dto.SearchResponse;
import dto.Suggestion;
import forms.ProjectForm;
import forms.ProjectUserForm;
import forms.SearchForm;
import models.Aggregate;
import models.Key;
import models.Locale;
import models.LogEntry;
import models.Project;
import models.ProjectUser;
import models.Suggestable;
import models.Suggestable.Data;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import services.KeyService;
import services.LocaleService;
import services.LogEntryService;
import services.ProjectService;
import services.ProjectUserService;
import services.UserService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 16 Sep 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Projects extends AbstractController
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Projects.class);

	private final ProjectService projectService;

	private final LocaleService localeService;

	private final KeyService keyService;

	private final LogEntryService logEntryService;

	private final FormFactory formFactory;

	private final ProjectUserService projectUserService;

	private final Configuration configuration;

	/**
	 * 
	 */
	@Inject
	public Projects(Injector injector, CacheApi cache, FormFactory formFactory, PlayAuthenticate auth,
				UserService userService, ProjectService projectService, LocaleService localeService, KeyService keyService,
				LogEntryService logEntryService, ProjectUserService projectUserService, Configuration configuration)
	{
		super(injector, cache, auth, userService);

		this.formFactory = formFactory;
		this.projectService = projectService;
		this.localeService = localeService;
		this.keyService = keyService;
		this.logEntryService = logEntryService;
		this.projectUserService = projectUserService;
		this.configuration = configuration;
	}

	public Result project(UUID projectId)
	{
		return searchForm(projectId, (project, form) -> {
			return ok(
				log(
					() -> views.html.projects.project.render(createTemplate(), project, form),
					LOGGER,
					"Rendering project"));
		});
	}

	public Result create()
	{
		Form<ProjectForm> form = ProjectForm.form(formFactory).bindFromRequest();

		if(form.hasErrors())
			return badRequest(views.html.projects.create.render(createTemplate(), form));

		LOGGER.debug("Project: {}", Json.toJson(form));

		User owner = User.loggedInUser();
		Project project = Project.byOwnerAndName(owner, form.get().getName());
		if(project != null)
			form.get().fill(project).withDeleted(false);
		else
			project = form.get().fill(new Project()).withOwner(owner);
		projectService.save(project);

		select(project);

		return redirect(routes.Projects.project(project.id));
	}

	public Result createImmediately(String projectName)
	{
		if(projectName.length() > Project.NAME_LENGTH)
			return badRequest(
				views.html.projects.create
					.render(createTemplate(), ProjectForm.form(formFactory).bind(ImmutableMap.of("name", projectName))));

		User owner = User.loggedInUser();
		Project project = Project.byOwnerAndName(owner, projectName);
		if(project == null)
		{
			project = new Project(projectName).withOwner(owner);

			LOGGER.debug("Project: {}", Json.toJson(project));

			projectService.save(project);
		}

		return redirect(routes.Projects.project(project.id));
	}

	public Result edit(UUID projectId)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		if("POST".equals(request().method()))
		{
			Form<ProjectForm> form = formFactory.form(ProjectForm.class).bindFromRequest();

			if(form.hasErrors())
				return badRequest(views.html.projects.edit.render(createTemplate(), project, form));

			projectService.save(form.get().fill(project));

			return redirect(routes.Projects.project(project.id));
		}

		return ok(
			views.html.projects.edit
				.render(createTemplate(), project, formFactory.form(ProjectForm.class).fill(ProjectForm.from(project))));
	}

	public Result remove(UUID projectId)
	{
		Project project = Project.byId(projectId);

		LOGGER.debug("Key: {}", Json.toJson(project));

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		undoCommand(injector.instanceOf(RevertDeleteProjectCommand.class).with(project));

		projectService.delete(project);

		return redirect(routes.Dashboards.dashboard());
	}

	public Result search(UUID id)
	{
		return searchForm(id, (project, form) -> {
			SearchForm search = form.get();
			search.setLimit(configuration.getInt("translatr.search.autocomplete.limit", 3));

			List<Suggestable> suggestions = new ArrayList<>();

			List<? extends Suggestable> locales = Locale.findBy(
				new LocaleCriteria().withProjectId(project.id).withSearch(search.search).withOrder("whenUpdated desc"));

			search.pager(locales);
			if(!locales.isEmpty())
				suggestions.addAll(locales);
			if(search.hasMore)
				suggestions.add(
					Suggestable.DefaultSuggestable.from(
						ctx().messages().at("locale.search", search.search),
						Data.from(Locale.class, null, "???", search.urlWithOffset(routes.Projects.locales(project.id), 0))));
			suggestions.add(
				Suggestable.DefaultSuggestable.from(
					ctx().messages().at("locale.create", search.search),
					Data
						.from(Locale.class, null, "+++", routes.Locales.createImmediately(project.id, search.search).url())));

			List<? extends Suggestable> keys =
						Key.findBy(KeyCriteria.from(search).withProjectId(project.id).withOrder("whenUpdated desc"));

			search.pager(keys);

			if(!keys.isEmpty())
				suggestions.addAll(keys);
			if(search.hasMore)
				suggestions.add(
					Suggestable.DefaultSuggestable.from(
						ctx().messages().at("key.search", search.search),
						Data.from(Key.class, null, "???", search.urlWithOffset(routes.Projects.keys(project.id), 0))));
			suggestions.add(
				Suggestable.DefaultSuggestable.from(
					ctx().messages().at("key.create", search.search),
					Data.from(Key.class, null, "+++", routes.Keys.createImmediately(project.id, search.search).url())));

			return ok(Json.toJson(SearchResponse.from(Suggestion.from(suggestions))));
		});
	}

	public Result locales(UUID id)
	{
		return searchForm(id, (project, form) -> {
			SearchForm search = form.get();

			List<Locale> locales = Locale.findBy(LocaleCriteria.from(search).withProjectId(project.id));

			search.pager(locales);

			java.util.Locale locale = ctx().lang().locale();
			Collections.sort(locales, (a, b) -> formatLocale(locale, a).compareTo(formatLocale(locale, b)));

			return ok(
				log(
					() -> views.html.projects.locales.render(
						createTemplate(),
						project,
						locales,
						localeService
							.progress(locales.stream().map(l -> l.id).collect(Collectors.toList()), Key.countBy(project)),
						form),
					LOGGER,
					"Rendering projects.locales"));
		});
	}

	public Result keys(UUID id)
	{
		return searchForm(id, (project, form) -> {
			SearchForm search = form.get();

			List<Key> keys = Key.findBy(KeyCriteria.from(search).withProjectId(project.id));

			search.pager(keys);

			Map<UUID, Double> progress = keyService
				.progress(keys.stream().map(k -> k.id).collect(Collectors.toList()), Locale.countBy(project));

			return ok(views.html.projects.keys.render(createTemplate(), project, keys, progress, form));
		});
	}

	public Result keysSearch(UUID id)
	{
		return searchForm(id, (project, form) -> {
			SearchForm search = form.get();

			List<Key> keys = Key.findBy(KeyCriteria.from(search).withProjectId(project.id));

			search.pager(keys);

			Map<UUID, Double> progress = keyService
				.progress(keys.stream().map(k -> k.id).collect(Collectors.toList()), Locale.countBy(project));

			return ok(views.html.tags.keyRows.render(keys, progress, form));
		});
	}

	public Result members(UUID projectId)
	{
		return searchForm(projectId, (project, form) -> {
			SearchForm search = form.get();

			List<ProjectUser> list = ProjectUser.findBy(ProjectUserCriteria.from(search).withProjectId(project.id));

			search.pager(list);

			return ok(
				views.html.projects.members
					.render(createTemplate(), project, list, form, ProjectUserForm.form(formFactory)));
		});
	}

	public Result memberAdd(UUID projectId)
	{
		return project(projectId, project -> {
			Form<ProjectUserForm> form = ProjectUserForm.form(formFactory).bindFromRequest();

			// TODO: Enable GET/POST switch
			if(form.hasErrors())
				return badRequest(views.html.projects.memberAdd.render(createTemplate(), project, form));

			User user = User.byUsername(form.get().getUsername());

			projectUserService.save(form.get().fill(new ProjectUser()).withProject(project).withUser(user));

			return redirect(routes.Projects.members(project.id));
		});
	}

	public Result memberRemove(UUID projectId, Long memberId)
	{
		return project(projectId, project -> {
			ProjectUser member = ProjectUser.byId(memberId);

			if(member == null || !project.id.equals(member.project.id))
			{
				flash("error", ctx().messages().at("project.member.notFound"));

				return redirect(routes.Projects.members(project.id));
			}

			undoCommand(injector.instanceOf(RevertDeleteProjectUserCommand.class).with(member));

			projectUserService.delete(member);

			return redirect(routes.Projects.members(project.id));
		});
	}

	public Result activity(UUID projectId)
	{
		return project(projectId, project -> {
			Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
			SearchForm search = form.get();

			List<LogEntry> activities = LogEntry
				.findBy(LogEntryCriteria.from(search).withProjectId(project.id).withOrder("whenCreated desc"));

			search.pager(activities);

			return ok(views.html.projects.activity.render(createTemplate(), project, activities, form));
		});
	}

	public Result activityCsv(UUID projectId)
	{
		return project(projectId, project -> {
			List<Aggregate> activity = logEntryService.getAggregates(new LogEntryCriteria().withProjectId(project.id));

			int max = activity.stream().mapToInt(a -> a.value).reduce(0, Math::max);

			String csv = "Date,Value\n" + activity
				.stream()
				.map(a -> String.format("%s,%.2f\n", a.date.toString("yyyy-MM-dd"), Math.log(a.value) / Math.log(max)))
				.reduce("", (a, b) -> a.concat(b));
			return ok(csv);
		});
	}

	private Result project(UUID projectId, Function<Project, Result> processor)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirectWithError(routes.Dashboards.dashboard(), ctx().messages().at("project.notFound", projectId));

		select(project);

		return processor.apply(project);
	}

	private <T extends Form<SearchForm>> Result searchForm(UUID projectId,
		BiFunction<Project, Form<SearchForm>, Result> processor)
	{
		return project(
			projectId,
			project -> processor.apply(project, SearchForm.bindFromRequest(formFactory, configuration)));
	}
}
