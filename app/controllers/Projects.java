package controllers;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.collect.ImmutableMap;
import commands.RevertDeleteProjectCommand;
import commands.RevertDeleteProjectUserCommand;
import converters.ActivityCsvConverter;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import criterias.ProjectUserCriteria;
import dto.SearchResponse;
import forms.ActivitySearchForm;
import forms.KeySearchForm;
import forms.LocaleSearchForm;
import forms.ProjectForm;
import forms.ProjectOwnerForm;
import forms.ProjectUserForm;
import forms.SearchForm;
import mappers.SuggestionMapper;
import models.Key;
import models.Locale;
import models.LogEntry;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.Suggestable;
import models.Suggestable.Data;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.data.Form;
import play.data.FormFactory;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Result;
import play.mvc.With;
import services.AuthProvider;
import services.CacheService;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.ProjectService;
import services.ProjectUserService;
import utils.FormUtils;
import utils.FormUtils.Search;
import utils.Template;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author resamsel
 * @version 16 Sep 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Projects extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(Projects.class);

  private final AuthProvider authProvider;
  private final ProjectService projectService;
  private final LocaleService localeService;
  private final KeyService keyService;
  private final FormFactory formFactory;
  private final MessageService messageService;
  private final ProjectUserService projectUserService;
  private final Configuration configuration;

  /**
   *
   */
  @Inject
  public Projects(Injector injector, CacheService cache, FormFactory formFactory,
                  PlayAuthenticate auth, AuthProvider authProvider,
                  ProjectService projectService, LocaleService localeService, KeyService keyService,
                  MessageService messageService, ProjectUserService projectUserService,
                  Configuration configuration) {
    super(injector, cache, auth);

    this.formFactory = formFactory;
    this.authProvider = authProvider;
    this.projectService = projectService;
    this.localeService = localeService;
    this.keyService = keyService;
    this.messageService = messageService;
    this.projectUserService = projectUserService;
    this.configuration = configuration;
  }

  public CompletionStage<Result> index(String s, String order, int limit, int offset) {
    return loggedInUser(user -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();
      search.update(s, order, limit, offset);

      PagedList<Project> projects =
          projectService.findBy(ProjectCriteria.from(search).withMemberId(authProvider.loggedInUserId()));

      search.pager(projects);

      return ok(views.html.projects.index.render(createTemplate(), projects, form,
          ProjectForm.form(formFactory)));
    });
  }

  @SubjectPresent
  public Result search() {
    Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
    SearchForm search = form.get();

    List<Suggestable> suggestions = new ArrayList<>();

    PagedList<? extends Suggestable> projects = projectService.findBy(ProjectCriteria.from(search));
    if (!projects.getList().isEmpty()) {
      suggestions.addAll(projects.getList());
    } else {
      suggestions.add(Suggestable.DefaultSuggestable
          .from(ctx().messages().at("project.create", search.search), Data.from(Project.class, null,
              "+++", controllers.routes.Projects.createImmediately(search.search).url())));
    }

    return ok(Json.toJson(SearchResponse.from(SuggestionMapper.toDto(suggestions))));
  }

  public CompletionStage<Result> projectBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      if (!permissionService.hasPermissionAny(project, ProjectRole.values())) {
        return redirectWithError(user.route(), "project.access.denied", project.name);
      }

      return ok(views.html.projects.project.render(
          createTemplate(),
          project,
          localeService.latest(project, 3),
          keyService.latest(project, 3),
          messageService.latest(project, 3),
          FormUtils.Search.bindFromRequest(formFactory, configuration)));
    });
  }

  public CompletionStage<Result> createBy(String username) {
    return user(username, user -> ok(views.html.projects.create
        .render(createTemplate(), ProjectForm.form(formFactory).bindFromRequest())));
  }

  public CompletionStage<Result> doCreateBy(String username) {
    return user(username, user -> {
      Form<ProjectForm> form = ProjectForm.form(formFactory).bindFromRequest();
      if (form.hasErrors()) {
        return badRequest(views.html.projects.create.render(createTemplate(), form));
      }

      Project project = projectService.byOwnerAndName(user.username, form.get().getName());
      if (project != null) {
        // Revive project
        form.get().fill(project).withDeleted(false);
      } else {
        project = form.get().fill(new Project()).withOwner(user);
      }

      try {
        project = projectService.create(project);
      } catch (ConstraintViolationException e) {
        return badRequest(
            views.html.projects.create.render(createTemplate(), FormUtils.include(form, e)));
      }

      return redirectWithMessage(select(project).route(), "project.created", project.name);
    });
  }

  public CompletionStage<Result> createImmediately(String projectName) {
    return loggedInUser(user -> {
      Form<ProjectForm> form =
          ProjectForm.form(formFactory).bind(ImmutableMap.of("name", projectName));
      if (form.hasErrors()) {
        return badRequest(views.html.projects.create.render(createTemplate(), form));
      }

      Project project = projectService.byOwnerAndName(user.username, projectName);
      if (project == null) {
        try {
          project = projectService.create(new Project(projectName).withOwner(user));
        } catch (ConstraintViolationException e) {
          return badRequest(
              views.html.projects.create.render(createTemplate(), FormUtils
                  .include(form, e)));
        }

        addMessage(message("project.created", project.name));
      }

      select(project);

      return redirect(project.route());
    });
  }

  public CompletionStage<Result> editBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      if (!permissionService.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager)) {
        return redirectWithError(project.route(), "project.edit.denied", project.name);
      }

      return ok(views.html.projects.edit.render(createTemplate(), project,
          formFactory.form(ProjectForm.class).fill(ProjectForm.from(project))));
    });
  }

  public CompletionStage<Result> doEditBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      if (!permissionService.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager)) {
        return redirectWithError(project.route(), "project.edit.denied", project.name);
      }

      Form<ProjectForm> form = formFactory.form(ProjectForm.class).bindFromRequest();
      if (form.hasErrors()) {
        return badRequest(views.html.projects.edit.render(createTemplate(), project, form));
      }

      try {
        project = projectService.update(form.get().fill(project));
      } catch (ConstraintViolationException e) {
        return badRequest(
            views.html.projects.edit.render(createTemplate(), project, FormUtils.include(form, e)));
      }

      return redirect(project.route());
    });
  }

  public CompletionStage<Result> removeBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      if (!permissionService.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager)) {
        return redirectWithError(project.route(), "project.delete.denied", project.name);
      }

      select(project);

      undoCommand(injector.instanceOf(RevertDeleteProjectCommand.class).with(project));

      projectService.delete(project);

      return redirect(user.route());
    });
  }

  public CompletionStage<Result> localesBy(String username, String projectName, String s,
                                           String order, int limit, int offset) {
    return project(username, projectName, (user, project) -> {
      Form<LocaleSearchForm> form =
          FormUtils.LocaleSearch.bindFromRequest(formFactory, configuration);
      LocaleSearchForm search = form.get();
      search.update(s, order, limit, offset);

      PagedList<Locale> locales =
          localeService.findBy(LocaleCriteria.from(search).withProjectId(project.id));

      search.pager(locales);

      return ok(views.html.projects.locales.render(
          createTemplate(), project, locales, localeService.progress(project.id), form));
    });
  }

  public CompletionStage<Result> keysBy(String username, String projectName, String s, String order,
                                        int limit, int offset) {
    return project(username, projectName, (user, project) -> {
      Form<KeySearchForm> form = FormUtils.KeySearch.bindFromRequest(formFactory, configuration);
      KeySearchForm search = form.get();
      search.update(s, order, limit, offset);

      PagedList<Key> keys = keyService.findBy(KeyCriteria.from(search).withProjectId(project.id));

      search.pager(keys);

      return ok(views.html.projects.keys.render(
          createTemplate(), project, keys, keyService.progress(project.id), form));
    });
  }

  public CompletionStage<Result> membersBy(String username, String projectName, String s,
                                           String order, int limit, int offset) {
    return project(username, projectName, (user, project) -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();
      Form<ProjectOwnerForm> projectOwnerForm = ProjectOwnerForm.form(formFactory).fill(
          new ProjectOwnerForm().withProjectId(project.id).withProjectName(project.name)
      );

      return ok(views.html.projects.members.render(
          createTemplate(),
          project,
          search.pager(projectUserService
              .findBy(ProjectUserCriteria.from(search).withProjectId(project.id))),
          form,
          ProjectUserForm.form(formFactory),
          projectOwnerForm));
    });
  }

  public CompletionStage<Result> memberAddBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> ok(views.html.projects.memberAdd
        .render(createTemplate(), project, ProjectUserForm.form(formFactory).bindFromRequest())));
  }

  public CompletionStage<Result> doMemberAddBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      Form<ProjectUserForm> form = ProjectUserForm.form(formFactory).bindFromRequest();

      if (form.hasErrors()) {
        return badRequest(views.html.projects.memberAdd.render(createTemplate(), project, form));
      }

      User member = userService.byUsername(form.get().getUsername());

      projectUserService
          .create(form.get().fill(new ProjectUser()).withProject(project).withUser(member));

      return redirect(project.membersRoute());
    });
  }

  public CompletionStage<Result> memberRemoveBy(String username, String projectName,
                                                Long memberId) {
    return project(username, projectName, (user, project) -> {
      ProjectUser member = projectUserService.byId(memberId);

      if (member == null || !project.id.equals(member.project.id)) {
        return redirectWithError(project.membersRoute(), "project.member.notFound");
      }

      undoCommand(RevertDeleteProjectUserCommand.from(member));

      projectUserService.delete(member);

      return redirect(project.membersRoute());
    });
  }

  public CompletionStage<Result> doOwnerChangeBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      if (!permissionService.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager)) {
        return redirectWithError(project.membersRoute(), "project.owner.change.denied",
            project.name);
      }

      Form<SearchForm> searchForm = Search.bindFromRequest(formFactory, configuration);
      SearchForm search = searchForm.get();

      Form<ProjectOwnerForm> form = ProjectOwnerForm.form(formFactory).bindFromRequest();
      if (form.hasErrors()) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("doOwnerChange: form errors: {}", form.errorsAsJson());
        }

        return badRequest(views.html.projects.ownerChange.render(
            createTemplate(),
            project,
            projectUserService.findBy(ProjectUserCriteria.from(search).withProjectId(project.id)),
            form
        ));
      }
      ProjectOwnerForm projectOwner = form.get();

      try {
        projectService.changeOwner(
            project.withName(projectOwner.getProjectName()),
            userService.byId(projectOwner.getOwnerId())
        );
      } catch (ConstraintViolationException e) {
        return badRequest(views.html.projects.ownerChange.render(
            createTemplate(),
            project,
            projectUserService.findBy(ProjectUserCriteria.from(search).withProjectId(project.id)),
            FormUtils.include(form, e)
        ));
      }

      return redirect(project.membersRoute());
    });
  }

  public CompletionStage<Result> activityBy(String username, String projectName, String s,
                                            String order, int limit, int offset) {
    return project(username, projectName, (user, project) -> {
      Form<ActivitySearchForm> form =
          FormUtils.ActivitySearch.bindFromRequest(formFactory, configuration);
      ActivitySearchForm search = form.get();

      PagedList<LogEntry> activities = logEntryService.findBy(
          LogEntryCriteria.from(search).withProjectId(project.id).withOrder("whenCreated desc"));

      search.pager(activities);

      return ok(views.html.projects.activity.render(createTemplate(), project, activities, form));
    });
  }

  public CompletionStage<Result> activityCsvBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> ok(new ActivityCsvConverter()
        .apply(logEntryService.getAggregates(new LogEntryCriteria().withProjectId(project.id)).getList())));
  }

  public CompletionStage<Result> wordCountResetBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      if (!permissionService.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager)) {
        return redirectWithError(project.route(), "project.edit.denied", project.name);
      }

      select(project);

      projectService.resetWordCount(project.id);

      return redirectWithMessage(project.route(), "project.wordCount.reset");
    });
  }

  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_PROJECTS);
  }

  public static Call indexRoute() {
    return routes.Projects.index(DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public static Call createRoute(String username) {
    return routes.Projects.createBy(username);
  }

  public static Call doCreateRoute(String username) {
    return routes.Projects.doCreateBy(username);
  }
}
