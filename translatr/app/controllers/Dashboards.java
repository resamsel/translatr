package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import actions.ContextAction;
import criterias.ProjectCriteria;
import dto.SearchResponse;
import dto.Suggestion;
import forms.SearchForm;
import models.Project;
import models.Suggestable;
import play.Configuration;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
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
public class Dashboards extends AbstractController
{
	private final FormFactory formFactory;

	private final Configuration configuration;

	/**
	 * 
	 */
	@Inject
	public Dashboards(FormFactory formFactory, Configuration configuration)
	{
		this.formFactory = formFactory;
		this.configuration = configuration;
	}

	public Result dashboard()
	{
		return ok(views.html.dashboard.render(Project.all(), SearchForm.bindFromRequest(formFactory, configuration)));
	}

	public Result search()
	{
		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
		SearchForm search = form.get();

		List<Suggestable> suggestions = new ArrayList<>();

		Collection<? extends Suggestable> projects = Project.findBy(ProjectCriteria.from(search));
		if(!projects.isEmpty())
			suggestions.addAll(projects);
		// else
		// suggestions.add(
		// Suggestable.DefaultSuggestable.from(
		// ctx().messages().at("project.create", search.search),
		// Data.from(
		// Project.class,
		// null,
		// "+++",
		// controllers.routes.Application.projectCreateImmediately(search.search).url())));

		return ok(Json.toJson(SearchResponse.from(Suggestion.from(suggestions))));
	}
}
