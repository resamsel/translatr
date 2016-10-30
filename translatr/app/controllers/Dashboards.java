package controllers;

import static utils.Stopwatch.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import criterias.ProjectCriteria;
import dto.SearchResponse;
import dto.Suggestion;
import forms.ProjectForm;
import forms.SearchForm;
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
import services.LogEntryService;
import services.UserService;

/**
 *
 * @author resamsel
 * @version 26 Sep 2016
 */
@With(ContextAction.class)
public class Dashboards extends AbstractController {
  private static final Logger LOGGER = LoggerFactory.getLogger(Dashboards.class);

  private final FormFactory formFactory;

  private final Configuration configuration;

  /**
   * 
   */
  @Inject
  public Dashboards(Injector injector, CacheApi cache, FormFactory formFactory,
      Configuration configuration, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService) {
    super(injector, cache, auth, userService, logEntryService);

    this.formFactory = formFactory;
    this.configuration = configuration;
  }

  @SubjectPresent(forceBeforeAuthCheck = true)
  public Result dashboard() {
    return loggedInUser(user -> {
      Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();

      List<Project> projects =
          Project.findBy(ProjectCriteria.from(search).withMemberId(User.loggedInUserId()));

      return log(() -> ok(views.html.dashboards.dashboard.render(createTemplate(), projects,
          SearchForm.bindFromRequest(formFactory, configuration), ProjectForm.form(formFactory))),
          LOGGER, "Rendering dashboard");
    });
  }

  @SubjectPresent
  public Result search() {
    Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
    SearchForm search = form.get();

    List<Suggestable> suggestions = new ArrayList<>();

    Collection<? extends Suggestable> projects = Project.findBy(ProjectCriteria.from(search));
    if (!projects.isEmpty())
      suggestions.addAll(projects);
    else
      suggestions.add(Suggestable.DefaultSuggestable
          .from(ctx().messages().at("project.create", search.search), Data.from(Project.class, null,
              "+++", controllers.routes.Projects.createImmediately(search.search).url())));

    return ok(Json.toJson(SearchResponse.from(Suggestion.from(suggestions))));
  }
}
