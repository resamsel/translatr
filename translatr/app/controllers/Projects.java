package controllers;

import static utils.FormatUtils.formatLocale;
import static utils.Stopwatch.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.collect.ImmutableMap;

import actions.ContextAction;
import commands.RevertDeleteProjectCommand;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.LogEntryCriteria;
import dto.SearchResponse;
import dto.Suggestion;
import forms.ProjectForm;
import forms.SearchForm;
import models.Key;
import models.Locale;
import models.Project;
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
import services.UserService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 16 Sep 2016
 */
@With(ContextAction.class)
public class Projects extends AbstractController
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Projects.class);

	private final ProjectService projectService;

	private final LocaleService localeService;

	private final KeyService keyService;

	private final LogEntryService logEntryService;

	private final FormFactory formFactory;

	private final Configuration configuration;

	/**
	 * 
	 */
	@Inject
	public Projects(Injector injector, CacheApi cache, FormFactory formFactory, PlayAuthenticate auth,
				UserService userService, ProjectService projectService, LocaleService localeService, KeyService keyService,
				LogEntryService logEntryService, Configuration configuration)
	{
		super(injector, cache, auth, userService);

		this.formFactory = formFactory;
		this.projectService = projectService;
		this.localeService = localeService;
		this.keyService = keyService;
		this.logEntryService = logEntryService;
		this.configuration = configuration;
	}

	public Result project(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);

		return ok(
			log(
				() -> views.html.projects.project.render(
					createTemplate(),
					project,
					logEntryService.getStats(new LogEntryCriteria().withProjectId(project.id)),
					form),
				LOGGER,
				"Rendering project"));
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
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
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
				Data.from(
					Locale.class,
					null,
					"+++",
					routes.Application.localeCreateImmediately(project.id, search.search).url())));

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
				Data.from(
					Key.class,
					null,
					"+++",
					routes.Application.keyCreateImmediately(project.id, search.search).url())));

		return ok(Json.toJson(SearchResponse.from(Suggestion.from(suggestions))));
	}

	public Result locales(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
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
	}

	public Result keys(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
		SearchForm search = form.get();

		List<Key> keys = Key.findBy(KeyCriteria.from(search).withProjectId(project.id));

		search.pager(keys);

		Map<UUID, Double> progress =
					keyService.progress(keys.stream().map(k -> k.id).collect(Collectors.toList()), Locale.countBy(project));

		return ok(views.html.projects.keys.render(createTemplate(), project, keys, progress, form));
	}

	public Result keysSearch(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
		SearchForm search = form.get();

		List<Key> keys = Key.findBy(KeyCriteria.from(search).withProjectId(project.id));

		search.pager(keys);

		Map<UUID, Double> progress =
					keyService.progress(keys.stream().map(k -> k.id).collect(Collectors.toList()), Locale.countBy(project));

		return ok(views.html.tags.keyRows.render(keys, progress, form));
	}
}
