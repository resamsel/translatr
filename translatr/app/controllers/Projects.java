package controllers;

import static utils.Stopwatch.log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.ContextAction;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.LogEntryCriteria;
import dto.SearchResponse;
import dto.Suggestion;
import forms.SearchForm;
import models.Key;
import models.Locale;
import models.Project;
import models.Suggestable;
import play.Configuration;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import services.LogEntryService;

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

	private final LogEntryService logEntryService;

	private final FormFactory formFactory;

	private final Configuration configuration;

	/**
	 * 
	 */
	@Inject
	public Projects(FormFactory formFactory, LogEntryService logEntryService, Configuration configuration)
	{
		this.formFactory = formFactory;
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
				() -> views.html.project
					.render(project, logEntryService.getStats(new LogEntryCriteria().withProjectId(project.id)), form),
				LOGGER,
				"Rendering project"));
	}

	public Result projectSearch(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
		SearchForm search = form.get();

		List<Suggestable> suggestions = new ArrayList<>();

		suggestions.addAll(
			Locale.findBy(
				new LocaleCriteria()
					.withProjectId(project.id)
					.withSearch(search.search)
					.withLimit(4)
					.withOrder("whenUpdated desc")));
		suggestions.addAll(
			Key.findBy(
				new KeyCriteria()
					.withProjectId(project.id)
					.withSearch(search.search)
					.withLimit(4)
					.withOrder("whenUpdated desc")));

		return ok(Json.toJson(SearchResponse.from(Suggestion.from(suggestions))));
	}
}
