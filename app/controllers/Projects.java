package controllers;

import static utils.Stopwatch.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.collect.ImmutableMap;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import commands.RevertDeleteProjectCommand;
import commands.RevertDeleteProjectUserCommand;
import converters.ActivityCsvConverter;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import criterias.ProjectUserCriteria;
import dto.SearchResponse;
import dto.Suggestion;
import forms.ActivitySearchForm;
import forms.KeySearchForm;
import forms.LocaleSearchForm;
import forms.ProjectForm;
import forms.ProjectOwnerForm;
import forms.ProjectUserForm;
import forms.SearchForm;
import models.Key;
import models.Locale;
import models.LogEntry;
import models.Project;
import models.ProjectRole;
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
import utils.FormUtils;
import utils.PermissionUtils;
import utils.Template;

/**
 *
 * @author resamsel
 * @version 16 Sep 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Projects extends AbstractController {
  private static final Logger LOGGER = LoggerFactory.getLogger(Projects.class);

  private final ProjectService projectService;

  private final LocaleService localeService;

  private final KeyService keyService;

  private final FormFactory formFactory;

  private final ProjectUserService projectUserService;

  private final Configuration configuration;

  /**
   * 
   */
  @Inject
  public Projects(Injector injector, CacheApi cache, FormFactory formFactory, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService, ProjectService projectService,
      LocaleService localeService, KeyService keyService, ProjectUserService projectUserService,
      Configuration configuration) {
    super(injector, cache, auth);

    this.formFactory = formFactory;
    this.projectService = projectService;
    this.localeService = localeService;
    this.keyService = keyService;
    this.projectUserService = projectUserService;
    this.configuration = configuration;
  }

  @SubjectPresent(forceBeforeAuthCheck = true)
  public CompletionStage<Result> index() {
    return loggedInUser(user -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();
      if (search.order == null)
        search.order = "name";

      PagedList<Project> projects =
          projectService.findBy(ProjectCriteria.from(search).withMemberId(User.loggedInUserId()));

      return log(() -> ok(views.html.projects.index.render(createTemplate(), projects.getList(),
          FormUtils.Search.bindFromRequest(formFactory, configuration),
          ProjectForm.form(formFactory))), LOGGER, "Rendering projects");
    });
  }

  @SubjectPresent
  public Result search() {
    Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
    SearchForm search = form.get();

    List<Suggestable> suggestions = new ArrayList<>();

    PagedList<? extends Suggestable> projects = projectService.findBy(ProjectCriteria.from(search));
    if (!projects.getList().isEmpty())
      suggestions.addAll(projects.getList());
    else
      suggestions.add(Suggestable.DefaultSuggestable
          .from(ctx().messages().at("project.create", search.search), Data.from(Project.class, null,
              "+++", controllers.routes.Projects.createImmediately(search.search).url())));

    return ok(Json.toJson(SearchResponse.from(Suggestion.from(suggestions))));
  }

  public CompletionStage<Result> projectBy(String username, String projectName) {
    return user(username, user -> project(user, projectName, project -> {
      if (!PermissionUtils.hasPermissionAny(project, ProjectRole.values()))
        return redirectWithError(routes.Projects.index(), "project.access.denied", project.name);

      return ok(views.html.projects.project.render(createTemplate(), project,
          FormUtils.Search.bindFromRequest(formFactory, configuration)));
    }), User.FETCH_PROJECTS);
  }

  public CompletionStage<Result> project(UUID projectId) {
    return project(projectId, project -> redirect(
        controllers.routes.Projects.projectBy(project.owner.username, project.name)));
    // return searchForm(projectId, (project, form) -> {
    // if (!PermissionUtils.hasPermissionAny(project, ProjectRole.values()))
    // return redirectWithError(routes.Projects.index(), "project.access.denied", project.name);
    //
    // return ok(log(() -> views.html.projects.project.render(createTemplate(), project, form),
    // LOGGER, "Rendering project"));
    // }, Project.FETCH_MEMBERS);
  }

  public Result create() {
    return ok(views.html.projects.create.render(createTemplate(),
        ProjectForm.form(formFactory).bindFromRequest()));
  }

  public CompletionStage<Result> doCreate() {
    return tryCatch(() -> {
      Form<ProjectForm> form = ProjectForm.form(formFactory).bindFromRequest();
      if (form.hasErrors())
        throw new ConstraintViolationException(Collections.emptySet());

      LOGGER.debug("Project: {}", Json.toJson(form));

      User owner = User.loggedInUser();
      Project project = Project.byOwnerAndName(owner, form.get().getName());
      if (project != null)
        form.get().fill(project).withDeleted(false);
      else
        project = form.get().fill(new Project()).withOwner(owner);

      projectService.save(project);

      select(project);

      return redirect(routes.Projects.projectBy(project.owner.username, project.path));
    }).exceptionally(t -> {
      Throwable cause = t.getCause();
      if (cause instanceof ConstraintViolationException)
        return badRequest(views.html.projects.create.render(createTemplate(),
            FormUtils.include(ProjectForm.form(formFactory).bindFromRequest(), cause)));

      return handleException(cause);
    });
  }

  public Result createImmediately(String projectName) {
    Form<ProjectForm> form =
        ProjectForm.form(formFactory).bind(ImmutableMap.of("name", projectName));
    if (form.hasErrors())
      return badRequest(views.html.projects.create.render(createTemplate(), form));

    User owner = User.loggedInUser();
    Project project = Project.byOwnerAndName(owner, projectName);
    if (project == null) {
      project = new Project(projectName).withOwner(owner);

      LOGGER.debug("Project: {}", Json.toJson(project));

      try {
        projectService.save(project);
      } catch (ConstraintViolationException e) {
        return badRequest(
            views.html.projects.create.render(createTemplate(), FormUtils.include(form, e)));
      }
    }

    return redirect(routes.Projects.projectBy(project.owner.username, project.path));
  }

  public CompletionStage<Result> edit(UUID projectId) {
    return project(projectId, project -> {
      if (!PermissionUtils.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager))
        return redirectWithError(routes.Projects.projectBy(project.owner.username, project.path),
            "project.edit.denied", project.name);

      select(project);

      return ok(views.html.projects.edit.render(createTemplate(), project,
          formFactory.form(ProjectForm.class).fill(ProjectForm.from(project))));
    });
  }

  public CompletionStage<Result> doEdit(UUID projectId) {
    return project(projectId, project -> {
      if (!PermissionUtils.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager))
        return redirectWithError(routes.Projects.projectBy(project.owner.username, project.path),
            "project.edit.denied", project.name);

      select(project);

      Form<ProjectForm> form = formFactory.form(ProjectForm.class).bindFromRequest();

      if (form.hasErrors())
        return badRequest(views.html.projects.edit.render(createTemplate(), project, form));

      projectService.save(form.get().fill(project));

      return redirect(routes.Projects.projectBy(project.owner.username, project.path));
    });
  }

  public Result remove(UUID projectId) {
    Project project = projectService.byId(projectId);

    LOGGER.debug("Key: {}", Json.toJson(project));

    if (project == null)
      return redirect(routes.Application.index());

    if (!PermissionUtils.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager))
      return redirectWithError(routes.Projects.projectBy(project.owner.username, project.path),
          "project.delete.denied", project.name);

    select(project);

    undoCommand(RevertDeleteProjectCommand.from(project));

    projectService.delete(project);

    return redirect(routes.Projects.index());
  }

  public CompletionStage<Result> localesBy(String username, String projectPath, String s,
      String order, int limit, int offset) {
    return user(username, user -> project(user, projectPath, project -> {
      Form<LocaleSearchForm> form =
          FormUtils.LocaleSearch.bindFromRequest(formFactory, configuration);
      LocaleSearchForm search = form.get();
      search.update(s, order, limit, offset);

      PagedList<Locale> locales =
          Locale.findBy(LocaleCriteria.from(search).withProjectId(project.id));

      search.pager(locales);

      // java.util.Locale locale = ctx().lang().locale();
      // Collections.sort(locales,
      // (a, b) -> formatLocale(locale, a).compareTo(formatLocale(locale, b)));

      return ok(views.html.projects.locales.render(createTemplate(), project, locales,
          localeService.progress(
              locales.getList().stream().map(l -> l.id).collect(Collectors.toList()),
              project.keys.size()),
          form));
    }));
  }

  public CompletionStage<Result> keysBy(String username, String projectPath, String s, String order,
      int limit, int offset) {
    return user(username, user -> project(user, projectPath, project -> {
      Form<KeySearchForm> form = FormUtils.KeySearch.bindFromRequest(formFactory, configuration);
      KeySearchForm search = form.get();
      search.update(s, order, limit, offset);

      PagedList<Key> keys = Key.findBy(KeyCriteria.from(search).withProjectId(project.id));

      search.pager(keys);

      Map<UUID, Double> progress =
          keyService.progress(keys.getList().stream().map(k -> k.id).collect(Collectors.toList()),
              project.locales.size());

      return ok(views.html.projects.keys.render(createTemplate(), project, keys, progress, form));
    }));
  }

  public Result keys(UUID id, String s, String order, int limit, int offset) {
    return keySearchForm(id, (project, form) -> {
      KeySearchForm search = form.get();
      search.update(s, order, limit, offset);

      PagedList<Key> keys = Key.findBy(KeyCriteria.from(search).withProjectId(project.id));

      search.pager(keys);

      Map<UUID, Double> progress =
          keyService.progress(keys.getList().stream().map(k -> k.id).collect(Collectors.toList()),
              project.locales.size());

      return ok(views.html.projects.keys.render(createTemplate(), project, keys, progress, form));
    });
  }

  public Result members(UUID projectId) {
    return searchForm(projectId, (project, form) -> {
      SearchForm search = form.get();

      PagedList<ProjectUser> list =
          ProjectUser.findBy(ProjectUserCriteria.from(search).withProjectId(project.id));

      search.pager(list);

      return ok(views.html.projects.members.render(createTemplate(), project, list, form,
          ProjectUserForm.form(formFactory)));
    });
  }

  public Result memberAdd(UUID projectId) {
    return projectLegacy(projectId, project -> {
      Form<ProjectUserForm> form = ProjectUserForm.form(formFactory).bindFromRequest();

      return ok(views.html.projects.memberAdd.render(createTemplate(), project, form));
    });
  }

  public Result doMemberAdd(UUID projectId) {
    return projectLegacy(projectId, project -> {
      Form<ProjectUserForm> form = ProjectUserForm.form(formFactory).bindFromRequest();

      if (form.hasErrors())
        return badRequest(views.html.projects.memberAdd.render(createTemplate(), project, form));

      User user = userService.byUsername(form.get().getUsername());

      projectUserService
          .save(form.get().fill(new ProjectUser()).withProject(project).withUser(user));

      return redirect(routes.Projects.members(project.id));
    });
  }

  public Result memberRemove(UUID projectId, Long memberId) {
    return projectLegacy(projectId, project -> {
      ProjectUser member = projectUserService.byId(memberId);

      if (member == null || !project.id.equals(member.project.id))
        return redirectWithError(routes.Projects.members(project.id), "project.member.notFound");

      undoCommand(RevertDeleteProjectUserCommand.from(member));

      projectUserService.delete(member);

      return redirect(routes.Projects.members(project.id));
    });
  }

  public Result doOwnerChange(UUID projectId) {
    return projectLegacy(projectId, project -> {
      Form<ProjectOwnerForm> form = formFactory.form(ProjectOwnerForm.class).bindFromRequest();

      if (!PermissionUtils.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager))
        return redirectWithError(routes.Projects.members(project.id), "project.owner.change.denied",
            project.name);

      // if (form.hasErrors())
      // return badRequest(views.html.projects.ownerChange.render(createTemplate(), project, form));

      ProjectOwnerForm val = form.get();
      // Make old owner a member of type Manager
      project.members.stream().filter(m -> m.role == ProjectRole.Owner)
          .forEach(m -> m.role = ProjectRole.Manager);
      // Make new owner a member of type Owner
      project.members.stream().filter(m -> m.user.id.equals(val.getOwnerId()))
          .forEach(m -> m.role = ProjectRole.Owner);
      projectService.save(project.withOwner(userService.byId(val.getOwnerId())));

      return redirect(routes.Projects.members(project.id));
    });
  }

  public Result activity(UUID projectId) {
    return projectLegacy(projectId, project -> {
      Form<ActivitySearchForm> form =
          FormUtils.ActivitySearch.bindFromRequest(formFactory, configuration);
      ActivitySearchForm search = form.get();

      PagedList<LogEntry> activities = logEntryService.findBy(
          LogEntryCriteria.from(search).withProjectId(project.id).withOrder("whenCreated desc"));

      search.pager(activities);

      return ok(views.html.projects.activity.render(createTemplate(), project, activities, form));
    });
  }

  public Result activityCsv(UUID projectId) {
    return projectLegacy(projectId, project -> {
      return ok(new ActivityCsvConverter()
          .apply(logEntryService.getAggregates(new LogEntryCriteria().withProjectId(project.id))));
    });
  }

  public CompletionStage<Result> wordCountReset(UUID projectId) {
    return project(projectId, project -> {
      if (!PermissionUtils.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager))
        return redirectWithError(routes.Projects.projectBy(project.owner.username, project.path),
            "project.edit.denied", project.name);

      select(project);

      projectService.resetWordCount(projectId);

      return redirectWithMessage(routes.Projects.projectBy(project.owner.username, project.path),
          "project.wordCount.reset");
    });
  }

  private CompletionStage<Result> project(UUID projectId, Function<Project, Result> processor,
      String... propertiesToFetch) {
    return tryCatch(() -> {
      Project project = projectService.byId(projectId, propertiesToFetch);

      if (project == null)
        return redirectWithError(routes.Projects.index(), "project.notFound", projectId);

      select(project);

      return processor.apply(project);
    });
  }

  /**
   * 
   * @param projectId
   * @param processor
   * @param propertiesToFetch
   * @return
   * @deprecated Use {@link Projects#project(UUID, Function, String...)} instead!
   */
  @Deprecated
  private Result projectLegacy(UUID projectId, Function<Project, Result> processor,
      String... propertiesToFetch) {
    try {
      return project(projectId, processor, propertiesToFetch).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Error while getting result of future", e);
      return internalServerError(e.getMessage());
    }
  }

  private <T extends Form<SearchForm>> Result searchForm(UUID projectId,
      BiFunction<Project, Form<SearchForm>, Result> processor, String... propertiesToFetch) {
    return projectLegacy(projectId, project -> processor.apply(project,
        FormUtils.Search.bindFromRequest(formFactory, configuration)), propertiesToFetch);
  }

  private <T extends Form<KeySearchForm>> Result keySearchForm(UUID projectId,
      BiFunction<Project, Form<KeySearchForm>, Result> processor) {
    return projectLegacy(projectId, project -> processor.apply(project,
        FormUtils.KeySearch.bindFromRequest(formFactory, configuration)));
  }

  private Result project(User user, String projectPath, Function<Project, Result> processor) {
    if (user.projects != null) {
      Optional<Project> project =
          user.projects.stream().filter(p -> p.path.equals(projectPath)).findFirst();
      if (project.isPresent())
        return processor.apply(project.get());
    }

    Project project = projectService.byOwnerAndPath(user, projectPath);
    if (project == null)
      return redirectWithError(routes.Application.index(), "project.notFound");

    return processor.apply(project);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_PROJECTS);
  }
}
