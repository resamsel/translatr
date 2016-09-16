package controllers;

import static utils.Stopwatch.log;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.ContextAction;
import criterias.LogEntryCriteria;
import forms.SearchForm;
import models.Project;
import play.data.Form;
import play.data.FormFactory;
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

	/**
	 * 
	 */
	@Inject
	public Projects(FormFactory formFactory, LogEntryService logEntryService)
	{
		this.formFactory = formFactory;
		this.logEntryService = logEntryService;
	}

	public Result project(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		Form<SearchForm> form = formFactory.form(SearchForm.class).bindFromRequest();

		return ok(
			log(
				() -> views.html.project
					.render(project, logEntryService.getStats(new LogEntryCriteria().withProjectId(project.id)), form),
				LOGGER,
				"Rendering project"));
	}
}
